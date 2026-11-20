#!/usr/bin/env bash
# ==============================================================================
# EduSpace - Script de Compilación Local, Empaquetado y Publicación Docker Hub
# ==============================================================================
# Uso:
#   ./build-and-push.sh [usuario_docker_hub] [tag] [--skip-compile]
#
# Ejemplos:
#   ./build-and-push.sh witmer17 latest
#   ./build-and-push.sh witmer17 latest --skip-compile
# ==============================================================================
set -e

# Colores para salida de terminal
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Procesamiento de parámetros
DOCKER_USER=""
TAG="latest"
SKIP_COMPILE=false

for arg in "$@"; do
  case $arg in
    --skip-compile)
      SKIP_COMPILE=true
      shift
      ;;
    *)
      if [ -z "$DOCKER_USER" ]; then
        DOCKER_USER="$arg"
      elif [ "$TAG" = "latest" ]; then
        TAG="$arg"
      fi
      ;;
  esac
done

DOCKER_USER="${DOCKER_USER:-${DOCKER_HUB_USER:-witmer17}}"
TAG="${TAG:-latest}"

echo -e "${CYAN}==========================================================${NC}"
echo -e "${CYAN}  EduSpace: Build and Push Docker Images${NC}"
echo -e "  Usuario Docker Hub : ${YELLOW}${DOCKER_USER}${NC}"
echo -e "  Etiqueta (Tag)     : ${YELLOW}${TAG}${NC}"
echo -e "  Omitir compilación : ${YELLOW}${SKIP_COMPILE}${NC}"
echo -e "${CYAN}==========================================================${NC}"

# Lista de servicios: NombreDeImagen | RutaDirectorio | Tipo (java|frontend)
SERVICES=(
  "eduspace-config:infra/eduspace-config:java"
  "eduspace-eureka:infra/eduspace-eureka:java"
  "eduspace-gateway:infra/eduspace-gateway:java"
  "auth-ms:services/auth-ms:java"
  "planificacion-ms:services/planificacion-ms:java"
  "alumnos-ms:services/alumnos-ms:java"
  "matricula-ms:services/matricula-ms:java"
  "asistencia-ms:services/asistencia-ms:java"
  "calificaciones-ms:services/calificaciones-ms:java"
  "pago-ms:services/pago-ms:java"
  "examenes-ms:services/examenes-ms:java"
  "tareas-ms:services/tareas-ms:java"
  "notificaciones-ms:services/notificaciones-ms:java"
  "eduspace-frontend:frontend:frontend"
)

TOTAL=${#SERVICES[@]}
ROOT_DIR="$(pwd)"

# ==============================================================================
# FASE 1: Compilación Local de Servicios
# ==============================================================================
if [ "$SKIP_COMPILE" = false ]; then
  echo -e "\n${YELLOW}>>> FASE 1: Compilación local de microservicios y frontend...${NC}"

  for item in "${SERVICES[@]}"; do
    IFS=":" read -r IMAGE_NAME SERVICE_DIR SERVICE_TYPE <<< "$item"
    echo -e "\n${CYAN}--- Compilando: ${IMAGE_NAME} (${SERVICE_DIR}) ---${NC}"

    if [ ! -d "$SERVICE_DIR" ]; then
      echo -e "${RED}Error: Directorio $SERVICE_DIR no encontrado.${NC}"
      exit 1
    fi

    cd "$ROOT_DIR/$SERVICE_DIR"

    if [ "$SERVICE_TYPE" = "java" ]; then
      if [ -f "./mvnw" ]; then
        chmod +x ./mvnw 2>/dev/null || true
        echo "Ejecutando ./mvnw clean package -DskipTests ..."
        ./mvnw clean package -DskipTests -q
      elif command -v mvn >/dev/null 2>&1; then
        echo "Ejecutando mvn clean package -DskipTests ..."
        mvn clean package -DskipTests -q
      else
        echo -e "${RED}Error: Ni ./mvnw ni mvn están disponibles en el PATH.${NC}"
        exit 1
      fi

      # Verificar que se generó el .jar
      JAR_COUNT=$(find target -maxdepth 1 -name "*.jar" ! -name "*.original" 2>/dev/null | wc -l)
      if [ "$JAR_COUNT" -eq 0 ]; then
        echo -e "${RED}Error: No se encontró el archivo .jar en $SERVICE_DIR/target/${NC}"
        exit 1
      fi
      echo -e "${GREEN}✔ Compilación Java exitosa para ${IMAGE_NAME}${NC}"

    elif [ "$SERVICE_TYPE" = "frontend" ]; then
      if command -v npm >/dev/null 2>&1; then
        echo "Ejecutando npm run build ..."
        npm run build
        echo -e "${GREEN}✔ Compilación Frontend exitosa.${NC}"
      else
        if [ -d "dist/frontend/browser" ]; then
          echo -e "${YELLOW}npm no encontrado, pero dist/frontend/browser existe. Continuando...${NC}"
        else
          echo -e "${RED}Error: npm no encontrado y dist/frontend/browser no existe.${NC}"
          exit 1
        fi
      fi
    fi

    cd "$ROOT_DIR"
  done
else
  echo -e "\n${YELLOW}>>> Compilación omitida (--skip-compile activado). Usando artefactos existentes.${NC}"
fi

# ==============================================================================
# FASE 2: Empaquetado Docker y Push a Docker Hub
# ==============================================================================
echo -e "\n${YELLOW}>>> FASE 2: Construcción y subida de imágenes Docker...${NC}"
CURRENT=0

for item in "${SERVICES[@]}"; do
  CURRENT=$((CURRENT + 1))
  IFS=":" read -r IMAGE_NAME SERVICE_DIR SERVICE_TYPE <<< "$item"
  FULL_IMAGE="${DOCKER_USER}/${IMAGE_NAME}:${TAG}"

  echo ""
  echo -e "${CYAN}----------------------------------------------------------${NC}"
  echo -e "[$CURRENT/$TOTAL] Construyendo ${YELLOW}${IMAGE_NAME}${NC} desde ${SERVICE_DIR}"
  echo -e "Target Image: ${GREEN}${FULL_IMAGE}${NC}"
  echo -e "${CYAN}----------------------------------------------------------${NC}"

  # Construir imagen Docker (contexto local ultrarrápido con JRE Alpine)
  docker build -t "$FULL_IMAGE" "$SERVICE_DIR"

  # Subir imagen a Docker Hub
  echo "Subiendo imagen a Docker Hub..."
  docker push "$FULL_IMAGE"

  echo -e "${GREEN}✔ Completado exitosamente: ${FULL_IMAGE}${NC}"
done

echo ""
echo -e "${GREEN}==========================================================${NC}"
echo -e "${GREEN}  ¡Todas las imágenes fueron compiladas, construidas y subidas!${NC}"
echo -e "  Usuario Docker Hub : ${YELLOW}${DOCKER_USER}${NC}"
echo -e "  Tag                : ${YELLOW}${TAG}${NC}"
echo -e "${GREEN}==========================================================${NC}"
