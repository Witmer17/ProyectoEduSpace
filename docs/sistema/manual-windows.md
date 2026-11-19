# Manual para Windows: cómo ejecutar EduSpace

Este manual es para usar EduSpace en una computadora con **Windows 10 u 11**.
Si usas Linux o macOS, sigue el manual `manual-ejecucion.md`.

En Windows **no** se usan los scripts `.sh`. Se usan:
- `python tools\e2e\e2e_runner.py` para encender y apagar todo (Python).
- `iniciar-todo.bat` para el uso diario (doble clic).
- Docker Desktop para las bases de datos y Kafka.

---

## 0. Lo que debes instalar (una sola vez)

| Programa | Versión | Para qué | Cómo comprobar |
|---|---|---|---|
| **Java JDK** | 21 | Los servicios | `java -version` |
| **Docker Desktop** | la última | Bases de datos, Kafka, MinIO, Mailpit | Abre Docker Desktop y espera a que diga *Engine running* |
| **Python** | 3.10 o más | Los scripts de arranque | `python --version` |
| **Node.js** (LTS) | 20 o más | La página web (frontend) | `node --version` |
| **Git** (opcional) | la última | Para bajar el proyecto | `git --version` |

Notas importantes:
- Al instalar **Python**, marca la casilla **"Add python.exe to PATH"**.
- En Docker Desktop deja activado el motor **WSL 2** (es lo que trae por defecto).
- Si Windows pregunta si permites que Java o Docker accedan a la red, responde **Permitir**.

Después de instalar, **cierra y vuelve a abrir** la terminal (PowerShell o CMD) para que reconozca los programas nuevos.

---

## 1. Abrir la terminal en la carpeta del proyecto

Abre **PowerShell** y ve a la carpeta del proyecto (cámbiala por la tuya):

```powershell
cd C:\Users\TU_USUARIO\eduspacePRACTICA
```

Todos los comandos de este manual se ejecutan desde esa carpeta.

---

## 2. Compilar los servicios (solo la primera vez, o después de cambiar código)

Son 13 módulos (3 de infraestructura y 10 servicios). Copia y pega este bloque en PowerShell:

```powershell
$modulos = @(
  "infra\eduspace-config", "infra\eduspace-eureka", "infra\eduspace-gateway",
  "services\auth-ms", "services\planificacion-ms", "services\alumnos-ms",
  "services\notificaciones-ms", "services\matricula-ms", "services\asistencia-ms",
  "services\tareas-ms", "services\pago-ms", "services\examenes-ms", "services\calificaciones-ms"
)
foreach ($m in $modulos) {
  Write-Host "Compilando $m ..."
  Push-Location $m
  .\mvnw.cmd -q clean package -DskipTests
  Pop-Location
}
Write-Host "Listo: todos los módulos compilados."
```

La primera vez tarda bastante, porque descarga librerías de internet. Si algún módulo da error,
vuelve a ejecutar el bloque: los que ya compilaron se vuelven a compilar sin problema.

---

## 3. Primera vez, o cuando quieres empezar con datos vacíos

```powershell
python tools\e2e\e2e_runner.py start
```

Este comando:
1. Crea las bases de datos, Kafka, MinIO y Mailpit (en Docker).
2. Enciende los servicios Java en orden: configuración, Eureka, auth y los demás.

> **Atención:** `start` **borra** los contenedores de prueba anteriores (`e2e-*`) y con ellos los datos.
> Úsalo solo la primera vez o cuando quieras empezar de cero.

Espera unos **2 minutos** y comprueba (sección 5).

---

## 4. Uso diario (conserva los datos)

Cuando ya usaste `start` al menos una vez, el uso diario es sencillo:

1. Abre **Docker Desktop** y espera a que esté en ejecución.
2. Haz **doble clic** en `iniciar-todo.bat` (en la carpeta raíz del proyecto).

Ese archivo:
1. Enciende los contenedores que ya existen (sin borrar datos).
2. Reinicia los 13 servicios Java.
3. Abre una ventana con el frontend en http://localhost:4200.

> Antes de la primera vez con `iniciar-todo.bat`, corre una vez el modo de login (sección 4.1).

### 4.1 Elegir el modo de login (una vez)

El frontend y los servicios deben usar el mismo modo. Para el modo propio (recomendado hoy), ejecuta en PowerShell:

```powershell
npm.cmd --prefix frontend run auth:eduspace
```

Si Windows bloquea `npm` con un error de *scripts deshabilitados*, usa `npm.cmd` como arriba (es lo que evita el bloqueo).

---

## 5. Comprobar que todo funciona

```powershell
foreach ($p in 8090,8091,8092,8093,8094,8095,8096,8097,8098,8099,18080) {
  Write-Host -NoNewline "puerto $p : "
  curl.exe -s "localhost:$p/actuator/health"
  Write-Host ""
}
```

Cada línea debe mostrar `"UP"` o `"status":"UP"`. El puerto **18080** es el gateway.

Si un puerto no responde, mira su registro en `tools\e2e\logs\` (por ejemplo `pago.log` o `gateway.log`).

Después, abre **http://localhost:4200** en el navegador y entra con:
- Usuario: `admin`
- Clave: `admin123` (clave de desarrollo que documenta el proyecto)

---

## 6. Apagar

Para apagar **solo la web**: cierra la ventana de la terminal que dice `npm start`.

Para apagar **todo** (esto **borra los contenedores y los datos de prueba**):

```powershell
python tools\e2e\e2e_runner.py stop
```

Si solo quieres detener las bases de datos sin borrarlas, usa la sección 7.

---

## 7. Contenedores de Docker

Los contenedores son las bases de datos, Kafka, MinIO, Mailpit y Keycloak. Se manejan con Docker Desktop o con PowerShell.

### 7.1 Lista

| Contenedor | Para qué sirve | Puerto |
|---|---|---|
| `e2e-pg-auth` | Base de autenticación | 15440 |
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
| `e2e-minio` | Archivos (documentos, entregas) | 19000 |
| `e2e-mailpit` | Correos de prueba | web: 18025 |
| `eduspace-keycloak` | Keycloak (opcional) | 18090 |

### 7.2 Comandos en PowerShell

```powershell
docker ps                              # los que están encendidos
docker ps -a                           # todos, incluidos los apagados
docker logs --tail 50 e2e-pg-pago      # ver registros
docker stop e2e-mailpit                # apagar uno (no borra datos)
docker start e2e-mailpit              # encender uno
```

### 7.3 Lo que borra datos (cuidado)

| Acción | Consecuencia |
|---|---|
| `docker rm -f e2e-pg-pago` | Borra la base de pagos. |
| `python tools\e2e\e2e_runner.py start` | Borra todos los contenedores `e2e-*` y los vuelve a crear vacíos. |
| `python tools\e2e\e2e_runner.py stop` | Borra todos los contenedores `e2e-*`. |
| `docker rm -f eduspace-keycloak` | Se pierden las cuentas creadas en Keycloak. |

### 7.4 Keycloak (opcional)

Keycloak no es necesario si usas el modo propio (sección 4.1). Si lo necesitas, el script es para Linux:
abre **Git Bash** en la carpeta del proyecto y ejecuta:

```bash
CONTENEDORES=docker bash infra/keycloak/levantar-dev.sh
```

Consola: http://localhost:18090 (usuario `admin`).

### 7.5 Monitoreo (opcional)

```powershell
cd obs
docker compose -f compose-dev.yml up -d      # encender
docker compose -f compose-dev.yml down       # apagar (conserva los datos)
cd ..
```

---

## 8. Pagos con Mercado Pago (opcional)

Crea el archivo `services\pago-ms\.env` (en esa carpeta exacta) con tus credenciales:

```
MERCADOPAGO_ACCESS_TOKEN=<tu access token>
MERCADOPAGO_PUBLIC_KEY=<tu clave pública>
PAGO_FRONTEND_URL=http://localhost:4200
```

Este archivo **no se sube a GitHub**. Sin él, el sistema usa la pasarela de demostración, que no cobra nada de verdad.

---

## 9. Problemas frecuentes en Windows

| Problema | Qué hacer |
|---|---|
| `python` no se reconoce | Reinstala Python marcando **Add python.exe to PATH**, y reabre la terminal. |
| `docker` no se reconoce o "cannot connect to the Docker daemon" | Abre **Docker Desktop** y espera a *Engine running*. |
| `java` no se reconoce | Instala JDK 21 y configura `JAVA_HOME` y el `PATH`. Reabre la terminal. |
| `No se encontró jar en ...target` | No compilaste. Ejecuta el bloque de la sección 2. |
| Un puerto está ocupado | Mira qué lo usa: `netstat -ano \| findstr :8090`. Ciérralo en el *Administrador de tareas* o usa `python tools\e2e\e2e_runner.py stop` y vuelve a empezar. |
| `npm` bloqueado por "scripts deshabilitados" | Usa `npm.cmd` en lugar de `npm`, o ejecuta: `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`. |
| No entra con `admin` | El modo del frontend y de los servicios no coinciden. Ejecuta `npm.cmd --prefix frontend run auth:eduspace` y reinicia con `python tools\e2e\e2e_runner.py restart-services`. |
| Windows Defender o el firewall bloquea Java | Permite el acceso en redes privadas. |
| `iniciar-todo.bat` dice que no hay contenedores | Es la primera vez: ejecuta primero la sección 3 (`start`). |

---

## Resumen

**Primera vez:** sección 2 (compilar) → sección 3 (`start`) → sección 4.1 (modo) → sección 5 (comprobar).

**Uso diario:** Docker Desktop encendido → doble clic en `iniciar-todo.bat` → http://localhost:4200.

**Para apagar:** `python tools\e2e\e2e_runner.py stop` (borra datos de prueba).
