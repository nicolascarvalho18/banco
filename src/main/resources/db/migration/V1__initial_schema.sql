-- V1__initial_schema.sql
-- Banco SAP - Esquema Inicial de Banco de Dados

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE,
    birth_date DATE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'ROLE_CLIENTE',
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMP,
    two_factor_code VARCHAR(6),
    two_factor_expiry TIMESTAMP,
    profile_photo_url VARCHAR(255),
    address VARCHAR(255),
    transaction_pin_hash VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    agency_number VARCHAR(10) NOT NULL DEFAULT '0001',
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_type VARCHAR(30) NOT NULL DEFAULT 'CORRENTE',
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    savings_balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    credit_limit NUMERIC(19, 2) NOT NULL DEFAULT 5000.00,
    daily_pix_limit NUMERIC(19, 2) NOT NULL DEFAULT 10000.00,
    nightly_pix_limit NUMERIC(19, 2) NOT NULL DEFAULT 1000.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE pix_keys (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    key_type VARCHAR(20) NOT NULL,
    key_value VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    authentication_code VARCHAR(64) NOT NULL UNIQUE,
    source_account_id BIGINT REFERENCES accounts(id),
    destination_account_id BIGINT REFERENCES accounts(id),
    destination_name VARCHAR(120),
    destination_document VARCHAR(20),
    destination_bank VARCHAR(60),
    transaction_type VARCHAR(40) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    fee NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    category VARCHAR(50) NOT NULL DEFAULT 'OUTROS',
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'CONCLUIDA',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE virtual_cards (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    card_number_masked VARCHAR(20) NOT NULL,
    card_number_token VARCHAR(64) NOT NULL,
    holder_name VARCHAR(120) NOT NULL,
    expiration_date VARCHAR(7) NOT NULL,
    cvv_simulated VARCHAR(4) NOT NULL,
    card_type VARCHAR(20) NOT NULL DEFAULT 'VIRTUAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    spending_limit NUMERIC(19, 2) NOT NULL DEFAULT 2000.00,
    used_limit NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    is_temporary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE bill_payments (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    barcode VARCHAR(60) NOT NULL,
    recipient_name VARCHAR(120) NOT NULL,
    due_date DATE NOT NULL,
    payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    amount NUMERIC(19, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PAGO',
    authentication_code VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE crypto_wallets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    wallet_address VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE crypto_assets (
    id BIGSERIAL PRIMARY KEY,
    wallet_id BIGINT NOT NULL REFERENCES crypto_wallets(id) ON DELETE CASCADE,
    symbol VARCHAR(10) NOT NULL,
    name VARCHAR(50) NOT NULL,
    balance NUMERIC(28, 8) NOT NULL DEFAULT 0.00000000,
    CONSTRAINT uk_wallet_symbol UNIQUE (wallet_id, symbol)
);

CREATE TABLE crypto_transactions (
    id BIGSERIAL PRIMARY KEY,
    tx_hash VARCHAR(66) NOT NULL UNIQUE,
    source_wallet_id BIGINT REFERENCES crypto_wallets(id),
    destination_wallet_id BIGINT REFERENCES crypto_wallets(id),
    symbol VARCHAR(10) NOT NULL,
    quantity NUMERIC(28, 8) NOT NULL,
    unit_price_brl NUMERIC(19, 2) NOT NULL,
    total_brl NUMERIC(19, 2) NOT NULL,
    operation_type VARCHAR(30) NOT NULL, -- COMPRA, VENDA, TRANSFERENCIA_P2P
    status VARCHAR(20) NOT NULL DEFAULT 'CONCLUIDA',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    user_email VARCHAR(120),
    action VARCHAR(50) NOT NULL,
    resource VARCHAR(80),
    ip_address VARCHAR(50),
    user_agent VARCHAR(255),
    details TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(30) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE support_tickets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    protocol VARCHAR(30) NOT NULL UNIQUE,
    subject VARCHAR(150) NOT NULL,
    category VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTO', -- ABERTO, EM_ANDAMENTO, RESOLVIDO, FECHADO
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE support_messages (
    id BIGSERIAL PRIMARY KEY,
    ticket_id BIGINT NOT NULL REFERENCES support_tickets(id) ON DELETE CASCADE,
    sender_type VARCHAR(20) NOT NULL, -- USER, OPERATOR, BOT
    sender_name VARCHAR(100) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Índices de Performance e Integridade
CREATE INDEX idx_transactions_source ON transactions(source_account_id);
CREATE INDEX idx_transactions_dest ON transactions(destination_account_id);
CREATE INDEX idx_transactions_auth ON transactions(authentication_code);
CREATE INDEX idx_audit_created ON audit_logs(created_at);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, is_read);
