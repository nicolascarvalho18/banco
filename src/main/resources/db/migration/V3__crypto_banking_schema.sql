-- V3__crypto_banking_schema.sql
-- Evolução para Banco Digital de Criptomoedas & Ledger de Dupla Entrada

-- 1. Atualizar Tabela de Usuários com username único e configurações
ALTER TABLE users ADD COLUMN IF NOT EXISTS username VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS two_factor_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS theme_preference VARCHAR(10) DEFAULT 'dark';
ALTER TABLE users ADD COLUMN IF NOT EXISTS currency_preference VARCHAR(10) DEFAULT 'BRL';

-- Preencher usernames para os usuários já cadastrados
UPDATE users SET username = 'nicolas' WHERE email = 'cliente@bancosap.com.br' AND username IS NULL;
UPDATE users SET username = 'mariasilva' WHERE email = 'maria.silva@bancosap.com.br' AND username IS NULL;
UPDATE users SET username = 'admin' WHERE email = 'admin@bancosap.com.br' AND username IS NULL;
UPDATE users SET username = 'operador' WHERE email = 'operador@bancosap.com.br' AND username IS NULL;

-- Tornar username único
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- 2. Tabela de Cotações Reais de Mercado em Cache
CREATE TABLE IF NOT EXISTS market_prices (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(60) NOT NULL,
    price_brl NUMERIC(19, 4) NOT NULL,
    price_usd NUMERIC(19, 4) NOT NULL,
    change_1h NUMERIC(8, 2) DEFAULT 0.00,
    change_24h NUMERIC(8, 2) DEFAULT 0.00,
    change_7d NUMERIC(8, 2) DEFAULT 0.00,
    volume_24h_brl NUMERIC(24, 2) DEFAULT 0.00,
    market_cap_brl NUMERIC(24, 2) DEFAULT 0.00,
    high_24h_brl NUMERIC(19, 4) DEFAULT 0.00,
    low_24h_brl NUMERIC(19, 4) DEFAULT 0.00,
    circulating_supply NUMERIC(24, 4) DEFAULT 0.00,
    category VARCHAR(30) DEFAULT 'LAYER1',
    icon_url VARCHAR(255),
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_market_prices_symbol ON market_prices(symbol);

-- 3. Tabela de Ordens Simuladas (Compra, Venda, Conversão)
CREATE TABLE IF NOT EXISTS simulated_orders (
    id BIGSERIAL PRIMARY KEY,
    authentication_code VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    order_type VARCHAR(30) NOT NULL, -- COMPRA, VENDA, CONVERSAO
    symbol_from VARCHAR(20) NOT NULL,
    symbol_to VARCHAR(20) NOT NULL,
    amount_from NUMERIC(24, 8) NOT NULL,
    amount_to NUMERIC(24, 8) NOT NULL,
    unit_price_brl NUMERIC(19, 4) NOT NULL,
    fee_brl NUMERIC(19, 4) NOT NULL DEFAULT 0.00,
    slippage_tolerance NUMERIC(5, 2) DEFAULT 0.50,
    status VARCHAR(20) NOT NULL DEFAULT 'EXECUTADA',
    idempotency_key VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_orders_user ON simulated_orders(user_id);
CREATE INDEX IF NOT EXISTS idx_orders_auth_code ON simulated_orders(authentication_code);

-- 4. Tabela de Transferências Internas P2P
CREATE TABLE IF NOT EXISTS internal_transfers (
    id BIGSERIAL PRIMARY KEY,
    authentication_code VARCHAR(64) NOT NULL UNIQUE,
    sender_id BIGINT NOT NULL REFERENCES users(id),
    recipient_id BIGINT NOT NULL REFERENCES users(id),
    symbol VARCHAR(20) NOT NULL,
    amount NUMERIC(24, 8) NOT NULL,
    amount_brl_equivalent NUMERIC(19, 2) NOT NULL,
    fee_brl NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'CONCLUIDA',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transfers_sender ON internal_transfers(sender_id);
CREATE INDEX IF NOT EXISTS idx_transfers_recipient ON internal_transfers(recipient_id);

-- 5. Livro Razão Contábil de Dupla Entrada (Ledger)
CREATE TABLE IF NOT EXISTS ledger_entries (
    id BIGSERIAL PRIMARY KEY,
    entry_code VARCHAR(64) NOT NULL UNIQUE,
    transaction_reference VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL REFERENCES users(id),
    entry_type VARCHAR(20) NOT NULL, -- DEBITO, CREDITO
    asset_symbol VARCHAR(20) NOT NULL, -- BRL, BTC, ETH, etc.
    amount NUMERIC(24, 8) NOT NULL,
    balance_after NUMERIC(24, 8) NOT NULL,
    description VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_ledger_user ON ledger_entries(user_id);
CREATE INDEX IF NOT EXISTS idx_ledger_reference ON ledger_entries(transaction_reference);
CREATE INDEX IF NOT EXISTS idx_ledger_asset ON ledger_entries(asset_symbol);
