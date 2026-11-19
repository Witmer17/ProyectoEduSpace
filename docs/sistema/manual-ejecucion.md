# Manual: cómo ejecutar EduSpace paso a paso

Este manual explica cómo encender el sistema en tu computadora (modo desarrollo), en el orden correcto.
Hazlo siempre en este orden: **base de datos → configuración → servicios → gateway → frontend**.

---

## 0. Lo que necesitas instalado

| Programa | Para qué |
|---|---|
| **Java 21** | Los servicios están hechos en Java. |
| **Podman** (o Docker) | Levanta las bases de datos, Kafka, MinIO y Mailpit. |
| **Node.js y npm** | Para el frontend (la página web). |
| **Python 3** | Algunos scripts de ayuda. |

---

## 1. Compilar (solo la primera vez, o después de cambiar código)

```bash
cd ~/eduspacePRACTICA/tools/e2e
./compilar.sh
```

Tarda unos minutos. Crea los archivos `.jar` de cada servicio.

---

## 2. Encender la base de datos y los servicios

Hay dos situaciones:

### A. Primera vez, o cuando quieres empezar con datos vacíos (BORRA los datos de prueba)

```bash
cd ~/eduspacePRACTICA/tools/e2e
./levantar.sh --auth-propio
```

### B. Uso diario (conserva los datos)

Si las bases de datos ya están encendidas, solo reinicia las aplicaciones:

```bash
cd ~/eduspacePRACTICA/tools/e2e
./levantar.sh --sin-reset --auth-propio
```

> **Importante:** `./levantar.sh` **sin** `--sin-reset` vuelve a crear todas las bases de datos desde cero.
> Y `./detener.sh` borra los contenedores (y con ellos los datos de prueba). Usa `--sin-reset` para el uso diario.

Espera unos **90 segundos** y comprueba que todo responde:

```bash
for p in 8090 8091 8092 8093 8094 8095 8096 8097 8098 8099 18080; do
  echo -n "puerto $p: "; curl -s localhost:$p/actuator/health | head -c 40; echo
done
```

Cada línea debe mostrar `"UP"` o `"status":"UP"`. El puerto **18080** es el gateway, la puerta de entrada de la web.

Si algo no sale en `UP`, revisa su registro: `tools/e2e/logs/<servicio>.log`
(por ejemplo `tools/e2e/logs/pago.log`, `tools/e2e/logs/gateway.log`).

---

## 3. Elegir cómo se inicia sesión

El sistema tiene dos formas de iniciar sesión. **Las dos partes deben usar la misma**
(el frontend y los servicios), o el sistema no dejará entrar.

| Modo | Frontend | Servicios | Cuándo |
|---|---|---|---|
| **Propio** (recomendado hoy) | `npm run auth:eduspace` | `--auth-propio` | Login con usuario y clave de EduSpace |
| **Keycloak** | `npm run auth:keycloak-form` | sin `--auth-propio` | Login con Keycloak |

Si cambiaste de modo, la forma más segura es el script de cambio, que reinicia todo:

```bash
cd ~/eduspacePRACTICA/tools/e2e
./modo.sh propio        # o: ./modo.sh keycloak-form
```

---

## 4. Encender el frontend (la web)

Solo la primera vez, instala las librerías:

```bash
cd ~/eduspacePRACTICA/frontend
npm install
```

Cada vez que enciendas la web (con el modo que elegiste en el paso 3):

```bash
npm run auth:eduspace   # o: npm run auth:keycloak-form
npm start
```

Abre en el navegador: **http://localhost:4200**

---

## 5. Entrar al sistema

- Usuario: `admin`
- Clave: `admin123` (clave de desarrollo que aparece en la guía del proyecto)

Si no entra, revisa el paso 3: el modo del frontend y el de los servicios deben ser el mismo.

---

## 6. Apagar

Para apagar solo la web: `Ctrl + C` en la terminal donde corre `npm start`.

Para apagar todo el backend (**borra los contenedores y los datos de prueba**):

```bash
cd ~/eduspacePRACTICA/tools/e2e
./detener.sh
```

---

## 7. Pagos con Mercado Pago (opcional)

Si quieres probar los pagos con la pasarela real, crea el archivo `services/pago-ms/.env` con tus credenciales:

```
MERCADOPAGO_ACCESS_TOKEN=<tu access token>
MERCADOPAGO_PUBLIC_KEY=<tu clave pública>
PAGO_FRONTEND_URL=http://localhost:4200
```

Este archivo **no se sube a GitHub** (está en `.gitignore`).
Sin él, el sistema usa la pasarela de demostración, que no cobra nada de verdad.

---

## 8. Los contenedores (qué son y cómo manejarlos)

Algunas piezas del sistema no son programas de Java sino **contenedores**: cajas que traen su propio programa
(una base de datos, Kafka, etc.). Se manejan con **Podman** (o Docker, si lo cambias con `CONTENEDORES=docker`).

### 8.1 Lista de contenedores

| Contenedor | Para qué sirve | Puerto en tu computadora |
|---|---|---|
| `e2e-pg-auth` | Base de datos de autenticación | 15440 |
| `e2e-pg-planificacion` | Base de planificación | 15441 |
| `e2e-pg-alumnos` | Base de alumnos | 15442 |
| `e2e-pg-matricula` | Base de matrícula | 15443 |
| `e2e-pg-asistencia` | Base de asistencia | 15444 |
| `e2e-pg-calificaciones` | Base de calificaciones | 15445 |
| `e2e-pg-pago` | Base de pagos | 15446 |
| `e2e-pg-examenes` | Base de exámenes | 15447 |
| `e2e-pg-tareas` | Base de tareas | 15448 |
| `e2e-pg-notificaciones` | Base de notificaciones | 15449 |
| `e2e-kafka` | Mensajería entre servicios | 19092 |
| `e2e-minio` | Almacén de archivos (documentos, entregas) | 19000 |
| `e2e-mailpit` | Correos de prueba (no salen a internet) | web: 18025 |
| `eduspace-keycloak` | Keycloak (login con Keycloak) | 18090 |
| `eduspace-prometheus`, `eduspace-grafana`, `eduspace-loki`, `eduspace-promtail` | Monitoreo y registros (opcional) | 19090, 13000, 13100 |

Los contenedores `e2e-*` los crea `levantar.sh`. Keycloak lo crea `infra/keycloak/levantar-dev.sh`.
El monitoreo se levanta aparte (ver `obs/README.md`).

### 8.2 Ver si están encendidos

```bash
podman ps                         # solo los que están encendidos
podman ps -a                      # todos, incluidos los apagados
podman ps --format "{{.Names}} {{.Status}}"
```

Todos deben aparecer con estado `Up`.

### 8.3 Ver los registros de un contenedor

```bash
podman logs --tail 50 e2e-pg-pago
podman logs --tail 50 eduspace-keycloak
```

### 8.4 Encender o apagar uno solo

```bash
podman stop e2e-mailpit           # apagar uno (sin borrarlo)
podman start e2e-mailpit          # volver a encenderlo
```

Apagar uno no borra sus datos. Úsalo, por ejemplo, para liberar memoria.

### 8.5 Keycloak

```bash
cd ~/eduspacePRACTICA/infra/keycloak
./levantar-dev.sh                 # lo enciende (importa el realm "eduspace")
./levantar-dev.sh --detener       # lo apaga
```

Consola de administración: http://localhost:18090 (usuario `admin`).

> Si borras el contenedor de Keycloak, se pierden las cuentas que creaste desde la aplicación.

### 8.6 Lo que NO debes hacer sin pensar

| Acción | Consecuencia |
|---|---|
| `podman rm -f e2e-pg-pago` | Borra la base de pagos con todos sus datos. |
| `./detener.sh` (en `tools/e2e`) | Borra todos los contenedores `e2e-*` y con ellos las bases. |
| `./levantar.sh` sin `--sin-reset` | Vuelve a crear todas las bases desde cero. |
| `podman rm -f eduspace-keycloak` | Se pierden las cuentas creadas en Keycloak. |

Antes de borrar algo, pregunta. Para empezar limpio de verdad, usa `./levantar.sh` (sin `--sin-reset`), que
recrea las bases, o `./levantar-dev.sh` de Keycloak.

### 8.7 Problemas con contenedores

| Problema | Qué hacer |
|---|---|
| "address already in use" o puerto ocupado | Otro programa usa ese puerto. Ejecuta `podman ps` y apaga lo que sobre con `podman stop <nombre>`. |
| Un contenedor aparece como `Exited` | Mira sus registros (`podman logs <nombre>`) y vuelve a encenderlo con `podman start <nombre>`. |
| `podman: command not found` | Instala Podman, o usa Docker con `export CONTENEDORES=docker` antes de los scripts. |
| Podman no responde | Enciende el servicio: `systemctl --user start podman.socket`. |

---

## Problemas frecuentes

| Problema | Qué hacer |
|---|---|
| No entra con `admin` | Revisa que el modo del frontend y de los servicios coincidan (paso 3). |
| Un puerto de 80xx no responde | Mira `tools/e2e/logs/<servicio>.log`. |
| Error de puerto ocupado | Ejecuta `./detener.sh` y vuelve a empezar en el paso 2. |
| Se borraron los datos | Es normal si usaste `levantar.sh` sin `--sin-reset` o `detener.sh`. |
| Cambié código y no pasa nada | Ejecuta de nuevo `./compilar.sh` y después `./levantar.sh --sin-reset --auth-propio`. |

---

## Orden resumido (uso diario)

1. Comprueba que los contenedores estén arriba: `podman ps` (sección 8).
2. `cd tools/e2e && ./levantar.sh --sin-reset --auth-propio` → espera 90 s.
3. Comprueba los puertos (paso 2).
4. `cd frontend && npm run auth:eduspace && npm start`.
5. Abre http://localhost:4200 y entra con `admin`.
