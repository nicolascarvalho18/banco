-- V2__seed_demo_data.sql
-- Seed de Dados Iniciais Demonstrativos do Banco SAP

-- Inserção de Usuários Base (Senhas em BCrypt com salt 10 para desenvolvimento)
-- Hash $2a$10$7Z/H.n52jDqB39/Vf1l1EeejS15n4s8l4P84g67z2lM5s0m4p4x0e -> Admin@2026, Cliente@2026, Operador@2026
INSERT INTO users (id, full_name, cpf, birth_date, phone, email, password_hash, role, status, address)
VALUES 
(1, 'Administrador do Sistema', '000.000.000-00', '1988-05-15', '(11) 98888-0000', 'admin@bancosap.com.br', '$2a$10$vI8aWB0z3G4wE5Z37hC6ee6X4Fv1v4.YdM5O382sB2vH98a8iN5mK', 'ROLE_ADMIN', 'ATIVO', 'Av. Paulista, 1000 - São Paulo/SP'),
(2, 'Atendente Operacional SAP', '111.111.111-11', '1995-10-20', '(11) 97777-1111', 'operador@bancosap.com.br', '$2a$10$vI8aWB0z3G4wE5Z37hC6ee6X4Fv1v4.YdM5O382sB2vH98a8iN5mK', 'ROLE_ATENDENTE', 'ATIVO', 'Av. Faria Lima, 2000 - São Paulo/SP'),
(3, 'Nicolas Carvalho Ferreira', '123.456.789-00', '1998-03-25', '(11) 99999-8888', 'cliente@bancosap.com.br', '$2a$10$vI8aWB0z3G4wE5Z37hC6ee6X4Fv1v4.YdM5O382sB2vH98a8iN5mK', 'ROLE_CLIENTE', 'ATIVO', 'Rua das Flores, 123 - Jardins, São Paulo/SP'),
(4, 'Maria Helena Silva', '987.654.321-11', '1992-07-14', '(21) 98888-7777', 'maria.silva@bancosap.com.br', '$2a$10$vI8aWB0z3G4wE5Z37hC6ee6X4Fv1v4.YdM5O382sB2vH98a8iN5mK', 'ROLE_CLIENTE', 'ATIVO', 'Av. Atlântica, 450 - Copacabana, Rio de Janeiro/RJ');

-- Contas Bancárias
INSERT INTO accounts (id, user_id, agency_number, account_number, account_type, balance, savings_balance, credit_limit, daily_pix_limit, nightly_pix_limit, status)
VALUES
(1, 1, '0001', '10001-9', 'CORRENTE', 500000.00, 0.00, 100000.00, 100000.00, 20000.00, 'ATIVO'),
(2, 2, '0001', '20002-8', 'CORRENTE', 25000.00, 5000.00, 15000.00, 15000.00, 2000.00, 'ATIVO'),
(3, 3, '0001', '33458-1', 'CORRENTE', 14850.75, 4200.00, 8000.00, 10000.00, 1000.00, 'ATIVO'),
(4, 4, '0001', '44892-3', 'CORRENTE', 8320.50, 1500.00, 6000.00, 10000.00, 1000.00, 'ATIVO');

-- Chaves PIX
INSERT INTO pix_keys (id, account_id, key_type, key_value)
VALUES
(1, 3, 'CPF', '123.456.789-00'),
(2, 3, 'EMAIL', 'cliente@bancosap.com.br'),
(3, 4, 'CPF', '987.654.321-11'),
(4, 4, 'EMAIL', 'maria.silva@bancosap.com.br');

-- Carteiras Cripto
INSERT INTO crypto_wallets (id, user_id, wallet_address)
VALUES
(1, 3, '0xSAP77a9b8C41Fe23Dd091F8301B6d4f9A02e5C81'),
(2, 4, '0xSAP11f23E90bB54Aa329C011F492E7d1B55a7F99');

-- Ativos Cripto
INSERT INTO crypto_assets (wallet_id, symbol, name, balance)
VALUES
(1, 'BTC', 'Bitcoin', 0.12500000),
(1, 'ETH', 'Ethereum', 1.85000000),
(1, 'SOL', 'Solana', 12.40000000),
(1, 'USDT', 'Tether USD', 850.00000000),
(1, 'ADA', 'Cardano', 450.00000000),
(2, 'BTC', 'Bitcoin', 0.05000000),
(2, 'ETH', 'Ethereum', 0.50000000),
(2, 'USDT', 'Tether USD', 300.00000000);

-- Cartões Virtuais e Físicos
INSERT INTO virtual_cards (id, account_id, card_number_masked, card_number_token, holder_name, expiration_date, cvv_simulated, card_type, status, spending_limit, used_limit)
VALUES
(1, 3, '•••• •••• •••• 8842', '5544882211998842', 'NICOLAS C FERREIRA', '09/29', '784', 'FISICO', 'ATIVO', 8000.00, 1420.50),
(2, 3, '•••• •••• •••• 1209', '4411223399001209', 'NICOLAS C FERREIRA', '12/28', '331', 'VIRTUAL', 'ATIVO', 3000.00, 349.90);
