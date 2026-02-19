-- V1__create_orders_table.sql
-- Criação da tabela orders para o Order Service

-- Habilita extensão para geração de UUIDs, se ainda não existir
CREATE
EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE orders
(
    id               UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    order_number     VARCHAR(20)    NOT NULL UNIQUE,    -- Código amigável do pedido
    customer_id      UUID           NOT NULL,
    subtotal         DECIMAL(10, 2) NOT NULL,
    tax              DECIMAL(10, 2) NOT NULL DEFAULT 0,
    shipping_fee     DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount         DECIMAL(10, 2) NOT NULL DEFAULT 0,
    total_amount     DECIMAL(10, 2) NOT NULL,
    shipping_address JSONB          NOT NULL,           -- Endereço completo em JSON
    payment_id       VARCHAR(100),
    tracking_code    VARCHAR(100),
    notes            TEXT,
    metadata         JSONB,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP      NOT NULL DEFAULT NOW(),
    version          INTEGER        NOT NULL DEFAULT 0, -- Controle de concorrência otimista

    -- Garante que o total não seja negativo
    CONSTRAINT chk_orders_valid_total CHECK (total_amount >= 0),
    -- Garante que o subtotal não seja negativo
    CONSTRAINT chk_orders_valid_subtotal CHECK (subtotal >= 0)
);

-- Índice para busca por cliente
CREATE INDEX idx_orders_customer_id ON orders (customer_id);

-- Índice para ordenação por data de criação (mais recentes primeiro)
CREATE INDEX idx_orders_created_at ON orders (created_at DESC);

-- Índice para busca rápida por order_number
CREATE INDEX idx_orders_order_number ON orders (order_number);

-- Índice GIN para consultas em shipping_address (JSONB)
CREATE INDEX idx_orders_shipping_address ON orders USING GIN (shipping_address);

-- Índice GIN para consultas em metadata (JSONB)
CREATE INDEX idx_orders_metadata ON orders USING GIN (metadata);