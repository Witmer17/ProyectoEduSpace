# Guía de Despliegue en Servidor Azure (Ubuntu x64 - 2 vCPU / 8 GB RAM)

Esta guía detalla el procedimiento para compilar, publicar las imágenes en **Docker Hub** y desplegar toda la plataforma **EduSpace** en la máquina virtual de Azure con IP **`20.197.193.121`** utilizando **Docker Compose** y **Caddy** como proxy inverso.

---

## 🏗️ 1. Compilar y Subir Imágenes a Docker Hub (Tu PC)

1. Inicia sesión en Docker Hub desde tu terminal:
   ```bash
   docker login
   ```

2. Ejecuta el script de construcción y subida (elige según tu terminal):
   - **En Linux / WSL / Git Bash:**
     ```bash
     chmod +x build-and-push.sh
     ./build-and-push.sh tu_usuario_dockerhub
     ```
   - **En Windows PowerShell:**
     ```powershell
     .\build-and-push.ps1 -DockerUser "tu_usuario_dockerhub"
     ```

El script compila localmente cada microservicio con Maven (`./mvnw clean package -DskipTests`) y el frontend (`npm run build`), empaqueta cada contenedor con la imagen ultraligera `eclipse-temurin:21-jre-alpine` (sin ejecutar Maven dentro de Docker para evitar fallos de red) y sube automáticamente las 14 imágenes a tu repositorio de Docker Hub:
- `eduspace-config`
- `eduspace-eureka`
- `eduspace-gateway`
- `auth-ms`
- `planificacion-ms`
- `alumnos-ms`
- `matricula-ms`
- `asistencia-ms`
- `calificaciones-ms`
- `pago-ms` (con pasarela Mercado Pago)
- `examenes-ms`
- `tareas-ms`
- `notificaciones-ms`
- `eduspace-frontend` (Angular en Nginx)

---

## ☁️ 2. Preparar el Servidor Azure (Ubuntu x64)

Conéctate por SSH a tu servidor:
```bash
ssh tu_usuario@20.197.193.121
```

### A. Instalar Docker y Docker Compose
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install -y ca-certificates curl gnupg lsb-release

# Clave GPG oficial de Docker
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg

# Repositorio
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo usermod -aG docker $USER
```
*(Cierra y vuelve a abrir tu sesión SSH para que tome el grupo docker).*

### B. Activar Swap de 4 GB (Imprescindible para estabilidad en 8 GB de RAM)
Dado que correrán 14 servicios Java, PostgreSQL, Kafka y Keycloak, crear un swap de 4 GB evita cualquier riesgo de OOM killer:
```bash
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```

### C. Habilitar Puertos en Azure Network Security Group (NSG)
Asegúrate de tener abiertos en el grupo de seguridad de Azure:
- **Puerto 80 (HTTP)**
- **Puerto 443 (HTTPS)**
- **Puerto 22 (SSH)**

---

## 🚀 3. Desplegar EduSpace en Azure

1. Clona tu repositorio en el servidor Azure:
   ```bash
   git clone https://github.com/Witmer17/ProyectoEduSpace.git eduspace
   cd eduspace
   ```

2. Configura las variables de entorno en el archivo `.env`:
   ```bash
   cp .env.example .env
   nano .env
   ```
   Asegúrate de colocar:
   - `DOCKER_HUB_USER=tu_usuario_dockerhub`
   - `MERCADOPAGO_ACCESS_TOKEN=tu_access_token_aqui`
   - `MERCADOPAGO_PUBLIC_KEY=tu_public_key_aqui`
   - `MERCADOPAGO_BACK_URL=http://20.197.193.121/familia/pagos`
   - `MERCADOPAGO_NOTIFICATION_URL=http://20.197.193.121/api/v1/pagos/webhook/mercadopago`

3. Inicia toda la plataforma con Docker Compose:
   ```bash
   docker compose -f docker-compose.prod.yml pull
   docker compose -f docker-compose.prod.yml up -d
   ```

4. Monitorea el arranque de los contenedores:
   ```bash
   docker compose -f docker-compose.prod.yml ps
   docker compose -f docker-compose.prod.yml logs -f eduspace-gateway
   ```

---

## 👥 4. Usuarios y Credenciales Pre-cargadas

El sistema se inicializa automáticamente con la base de datos pre-cargada con estudiantes, apoderados, cuotas con deuda y personal:

| Rol | Usuario | Contraseña | Descripción / Alumno a cargo |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `admin123` | Control total del sistema |
| **Secretaría** | `secretaria` | `secre123` | Matrículas, expedientes y horarios |
| **Cajero** | `cajero` | `cajero123` | Cobros en ventanilla |
| **Docente** | `docente` | `docente123` | Cursos, asistencia y calificaciones |
| **Apoderado** | `apoderado` | `admin123` | Carmen García (Hijos: Lucía Mendoza y Alumno 2) |
| **Apoderado** | `jorge.vargas` | `admin123` | Jorge Vargas (Hija: Sofía Vargas - con deuda para Mercado Pago) |
| **Alumno** | `alumno` | `alumno123` | Lucía Mendoza |

---

## 💳 5. Flujo de Mercado Pago en Producción

1. Ingresa a `http://20.197.193.121` en tu navegador.
2. Inicia sesión con `jorge.vargas` / `admin123` o `apoderado` / `admin123`.
3. Ve a **Familia > Pagos**.
4. Verás las cuotas pendientes del estudiante. Selecciona la cuota y haz clic en **Pagar**.
5. Pulsa **Continuar a Mercado Pago**.
6. El sistema generará la preferencia real y te redirigirá a la pasarela de Mercado Pago Checkout Pro en Perú.
7. Al completar el pago, Mercado Pago notificará al webhook público `http://20.197.193.121/api/v1/pagos/webhook/mercadopago` para marcar la cuota como pagada y generar el comprobante.
