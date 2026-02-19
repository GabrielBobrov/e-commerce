-- V2__create_order_items_table.sql
-- Criação da tabela order_items vinculada a orders

CREATE TABLE order_items
(
    id            UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    order_id      UUID           NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id    UUID           NOT NULL,
    product_name  VARCHAR(255)   NOT NULL, -- Snapshot do nome do produto
    product_sku   VARCHAR(100)   NOT NULL,
    product_image VARCHAR(500),
    quantity      INTEGER        NOT NULL,
    unit_price    DECIMAL(10, 2) NOT NULL,
    discount      DECIMAL(10, 2) NOT NULL DEFAULT 0,
    subtotal      DECIMAL(10, 2) NOT NULL,
    attributes    JSONB,
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW(),

    -- Garante que a quantidade seja positiva
    CONSTRAINT chk_order_items_valid_quantity CHECK (quantity > 0),
    -- Garante que o preço unitário não seja negativo
    CONSTRAINT chk_order_items_valid_unit_price CHECK (unit_price >= 0),
    -- Garante que o subtotal não seja negativo
    CONSTRAINT chk_order_items_valid_subtotal CHECK (subtotal >= 0)
);

-- Índice para busca por pedido
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

-- Índice para busca por produto
CREATE INDEX idx_order_items_product_id ON order_items (product_id);