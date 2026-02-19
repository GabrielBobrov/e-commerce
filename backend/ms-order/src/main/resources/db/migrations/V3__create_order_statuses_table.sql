-- V3__create_order_statuses_table.sql
-- Criação da tabela order_statuses com histórico completo e trigger de status ativo

CREATE TABLE order_statuses
(
    id          UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    order_id    UUID        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    status_type VARCHAR(20) NOT NULL, -- 'ORDER' ou 'PAYMENT'
    status      VARCHAR(30) NOT NULL, -- PENDING, CONFIRMED, etc.
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    changed_by  VARCHAR(100),         -- user_id, 'SYSTEM', etc.
    reason      TEXT,
    metadata    JSONB,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW(),

    -- Garante apenas um status ativo por pedido/tipo
    CONSTRAINT unique_active_status UNIQUE (order_id, status_type, is_active) DEFERRABLE INITIALLY DEFERRED
);

-- Índice para busca por pedido
CREATE INDEX idx_order_statuses_order_id ON order_statuses (order_id);

-- Índice parcial para busca rápida de status ativos por pedido/tipo
CREATE INDEX idx_order_statuses_active ON order_statuses (order_id, status_type, is_active) WHERE is_active = true;

-- Índice para ordenação por data de criação (mais recentes primeiro)
CREATE INDEX idx_order_statuses_created_at ON order_statuses (created_at DESC);

-- Índice para busca por status
CREATE INDEX idx_order_statuses_status ON order_statuses (status);

-- Função: desativa status anterior ao inserir novo status ativo
CREATE
OR REPLACE FUNCTION deactivate_previous_status()
RETURNS TRIGGER AS $$
BEGIN
  IF
NEW.is_active THEN
UPDATE order_statuses
SET is_active = FALSE
WHERE order_id = NEW.order_id
  AND status_type = NEW.status_type
  AND is_active = TRUE;
END IF;
RETURN NEW;
END;
$$
LANGUAGE plpgsql;

-- Trigger: chama a função antes de inserir novo status ativo
CREATE TRIGGER trg_deactivate_previous_status
    BEFORE INSERT
    ON order_statuses
    FOR EACH ROW
    WHEN (NEW.is_active = TRUE)
    EXECUTE FUNCTION deactivate_previous_status();

-- View: mostra apenas os status ativos por pedido/tipo
CREATE
OR REPLACE VIEW v_order_current_status AS
SELECT *
FROM order_statuses
WHERE is_active = TRUE;