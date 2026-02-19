-- Script de inicialização do PostgreSQL
-- Este arquivo é executado automaticamente quando o container PostgreSQL inicia

-- Cria o banco de dados order_db
CREATE DATABASE order_db
    ENCODING = 'UTF8';

-- Garante que o usuário paymentuser tem todas as permissões no banco
GRANT ALL PRIVILEGES ON DATABASE order_db TO paymentuser;
