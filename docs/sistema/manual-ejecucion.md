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

1. `cd tools/e2e && ./levantar.sh --sin-reset --auth-propio` → espera 90 s.
2. Comprueba los puertos (paso 2).
3. `cd frontend && npm run auth:eduspace && npm start`.
4. Abre http://localhost:4200 y entra con `admin`.
