-- =====================================================================
-- Pasarela real (Mercado Pago, Checkout Pro) para las órdenes de pago de la matrícula.
-- pasarela: quién cobra la orden (SIMULADA o MERCADOPAGO).
-- preferencia_id: la preferencia creada en Mercado Pago (se reutiliza si la familia vuelve a pulsar «Pagar»).
-- =====================================================================
ALTER TABLE ordenes_pago ADD COLUMN pasarela VARCHAR(12) NOT NULL DEFAULT 'SIMULADA';
ALTER TABLE ordenes_pago ADD CONSTRAINT ck_orden_pasarela CHECK (pasarela IN ('SIMULADA','MERCADOPAGO'));
ALTER TABLE ordenes_pago ADD COLUMN preferencia_id VARCHAR(80);
ALTER TABLE ordenes_pago ADD COLUMN preferencia_url VARCHAR(500);   -- init_point: a donde se manda a la familia
-- (El índice único de transaccion_id ya existe desde V7: un mismo pago no confirma dos veces.)
