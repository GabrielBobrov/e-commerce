-- V4__create_order_events_table.sql
-- Criação da tabela order_events para Outbox Pattern

CREATE TABLE order_events
(
    id             UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    order_id       UUID        NOT NULL REFERENCES orders (id),
    event_type     VARCHAR(50) NOT NULL, -- ORDER_CREATED, ORDER_CONFIRMED, etc.
    aggregate_type VARCHAR(50) NOT NULL DEFAULT 'Order',
    payload        JSONB       NOT NULL, -- Dados completos do evento
    published      BOOLEAN     NOT NULL DEFAULT FALSE,
    published_at   TIMESTAMP,
    sns_message_id VARCHAR(100),
    retry_count    INTEGER     NOT NULL DEFAULT 0,
    last_error     TEXT,
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),

    -- Limita o número de tentativas de publicação do evento
    CONSTRAINT chk_order_events_max_retries CHECK (retry_count <= 5)
);

-- Índice para busca por pedido
CREATE INDEX idx_order_events_order_id ON order_events (order_id);

-- Índice parcial para busca rápida de eventos não publicados e ordenação por data de criação
CREATE INDEX idx_order_events_published ON order_events (published, created_at) WHERE published = FALSE;

-- Índice para busca por tipo de evento
CREATE INDEX idx_order_events_type ON order_events (event_type);