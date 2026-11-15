-- Permite registrar pagos procesados mediante pasarela Mercado Pago
ALTER TABLE pagos DROP CONSTRAINT IF EXISTS ck_pago_medio;
ALTER TABLE pagos ADD CONSTRAINT ck_pago_medio CHECK (medio IN ('EFECTIVO','TARJETA','YAPE_PLIN','TRANSFERENCIA','ONLINE_SIMULADO','MERCADO_PAGO'));
