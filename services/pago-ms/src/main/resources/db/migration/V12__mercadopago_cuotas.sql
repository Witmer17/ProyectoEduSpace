-- =====================================================================
-- Pagos de pensiones con Mercado Pago (apoderado) y del medio MERCADOPAGO.
-- El pago queda PENDIENTE con medio MERCADOPAGO hasta que el aviso verificado de la pasarela lo confirma o lo rechaza.
-- Caja no lo aprueba a mano: lo confirma Mercado Pago.
-- =====================================================================
ALTER TABLE pagos DROP CONSTRAINT ck_pago_medio;
ALTER TABLE pagos ADD CONSTRAINT ck_pago_medio CHECK (medio IN ('EFECTIVO','TARJETA','YAPE_PLIN','TRANSFERENCIA','ONLINE_SIMULADO','CREDITO','MERCADOPAGO'));
ALTER TABLE pagos ADD COLUMN preferencia_id  VARCHAR(80);
ALTER TABLE pagos ADD COLUMN preferencia_url VARCHAR(500);   -- a donde se manda al apoderado para pagar
