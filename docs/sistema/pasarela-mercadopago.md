# Pasarela real: Mercado Pago (Checkout Pro)

Pago de la **matrícula** (orden de pago de `pago-ms`). La familia es redirigida a Mercado Pago, ingresa la tarjeta allí
(este servidor nunca recibe datos de tarjeta) y Mercado Pago avisa el resultado.

## Flujo
1. «Pagar lo que falta» → `POST /api/v1/pagos/alumnos/{id}/matricula/orden` (monto calculado por el servidor).
2. `POST /api/v1/pagos/pasarela/ordenes/{codigo}/mercadopago` → crea la preferencia (una sola por orden) y devuelve la URL de pago.
3. Mercado Pago cobra y vuelve a `PAGO_FRONTEND_URL/familia/pagos?orden={codigo}`.
4. Aviso (`POST /api/v1/pagos/pasarela/mercadopago/notificacion`, sin sesión): se verifica la firma `x-signature` si hay
   `MERCADOPAGO_WEBHOOK_SECRET`, se **vuelve a consultar el pago al API** y solo entonces se confirma la orden
   (mismo `confirmarOrden` que las demás pasarelas: monto exacto, idempotente por id de pago).
5. Al volver, la pantalla llama a `POST .../mercadopago/sincronizar`, que consulta los pagos de la orden. Sirve cuando el aviso no llega.

Pagos «en proceso» no hacen nada hasta el resultado final. Rechazados no cierran la orden (se puede reintentar).

## Configuración
Las credenciales van en `services/pago-ms/.env` (ignorado por git; se importa con `spring.config.import`). Plantilla en `.env.example`.

| Variable | Uso |
|---|---|
| `MERCADOPAGO_ACCESS_TOKEN` | Access token del servidor (`APP_USR-…` con id numérico al final). Sin él, sigue la pasarela de demostración. |
| `MERCADOPAGO_PUBLIC_KEY` | Clave pública (`APP_USR-uuid`). No se usa para llamar a la API. |
| `MERCADOPAGO_WEBHOOK_SECRET` | Clave secreta del panel de Webhooks (firma `x-signature`). Opcional. |
| `PAGO_PUBLIC_BASE_URL` | URL HTTPS pública de pago-ms para los avisos (túnel en desarrollo). Sin ella no llegan avisos: usar «sincronizar». |
| `PAGO_FRONTEND_URL` | Dónde vuelve la familia (por defecto `http://localhost:4200`). |

Con el access token activo, la pantalla de demostración **no aprueba cobros** (`pagarSimulado` lo rechaza).
Gateway: `POST /api/v1/pagos/pasarela/mercadopago/notificacion` es público (`infra/eduspace-gateway`).

## Pendiente
- Pensiones en línea del apoderado y la matrícula en línea del sitio público siguen con la pasarela simulada.
- Pago duplicado sobre una orden ya aprobada: queda registrado como alerta en el log; falta el reembolso automático.
- Verificar en vivo la creación de preferencias y el aviso (requiere una cuenta de prueba y URL pública).
