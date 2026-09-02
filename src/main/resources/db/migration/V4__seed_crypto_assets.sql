-- V4__seed_crypto_assets.sql
-- Carga Inicial de Criptoativos com Cotações de Referência de Mercado

INSERT INTO market_prices (symbol, name, price_brl, price_usd, change_1h, change_24h, change_7d, volume_24h_brl, market_cap_brl, high_24h_brl, low_24h_brl, circulating_supply, category, icon_url, last_updated_at)
VALUES
('BTC', 'Bitcoin', 362450.00, 64500.00, 0.42, 2.85, 5.12, 18500000000.00, 7150000000000.00, 368200.00, 355100.00, 19750000.00, 'LAYER1', 'https://cryptologos.cc/logos/bitcoin-btc-logo.svg', CURRENT_TIMESTAMP),
('ETH', 'Ethereum', 18240.00, 3250.00, -0.15, 3.40, 7.80, 9400000000.00, 2200000000000.00, 18650.00, 17800.00, 120200000.00, 'LAYER1', 'https://cryptologos.cc/logos/ethereum-eth-logo.svg', CURRENT_TIMESTAMP),
('SOL', 'Solana', 845.50, 150.50, 1.20, 5.75, 14.20, 4200000000.00, 390000000000.00, 870.00, 810.00, 465000000.00, 'LAYER1', 'https://cryptologos.cc/logos/solana-sol-logo.svg', CURRENT_TIMESTAMP),
('BNB', 'BNB', 3250.00, 580.00, 0.05, 1.10, 3.40, 1200000000.00, 495000000000.00, 3310.00, 3190.00, 153000000.00, 'LAYER1', 'https://cryptologos.cc/logos/bnb-bnb-logo.svg', CURRENT_TIMESTAMP),
('XRP', 'XRP', 3.35, 0.60, -0.30, 1.80, 4.50, 1500000000.00, 187000000000.00, 3.48, 3.25, 56000000000.00, 'PAYMENT', 'https://cryptologos.cc/logos/xrp-xrp-logo.svg', CURRENT_TIMESTAMP),
('ADA', 'Cardano', 2.15, 0.38, 0.10, 2.10, 6.20, 480000000.00, 76000000000.00, 2.22, 2.08, 35600000000.00, 'LAYER1', 'https://cryptologos.cc/logos/cardano-ada-logo.svg', CURRENT_TIMESTAMP),
('DOGE', 'Dogecoin', 0.68, 0.12, 0.85, 4.30, -1.20, 890000000.00, 98000000000.00, 0.72, 0.64, 145000000000.00, 'MEME', 'https://cryptologos.cc/logos/dogecoin-doge-logo.svg', CURRENT_TIMESTAMP),
('LINK', 'Chainlink', 68.40, 12.20, 0.30, 3.15, 8.40, 350000000.00, 41000000000.00, 71.00, 65.50, 608000000.00, 'ORACLE', 'https://cryptologos.cc/logos/chainlink-link-logo.svg', CURRENT_TIMESTAMP),
('AVAX', 'Avalanche', 145.80, 26.00, -0.40, 4.80, 9.10, 410000000.00, 58000000000.00, 152.00, 138.00, 395000000.00, 'LAYER1', 'https://cryptologos.cc/logos/avalanche-avax-logo.svg', CURRENT_TIMESTAMP),
('MATIC', 'Polygon', 2.45, 0.44, 0.20, 1.90, 3.80, 290000000.00, 24000000000.00, 2.55, 2.38, 9800000000.00, 'LAYER2', 'https://cryptologos.cc/logos/polygon-matic-logo.svg', CURRENT_TIMESTAMP),
('LTC', 'Litecoin', 410.00, 73.00, 0.05, 1.40, 2.90, 310000000.00, 30500000000.00, 422.00, 398.00, 74800000.00, 'PAYMENT', 'https://cryptologos.cc/logos/litecoin-ltc-logo.svg', CURRENT_TIMESTAMP),
('DOT', 'Polkadot', 26.50, 4.70, 0.15, 2.30, 4.10, 210000000.00, 37000000000.00, 27.80, 25.40, 1430000000.00, 'LAYER0', 'https://cryptologos.cc/logos/polkadot-new-dot-logo.svg', CURRENT_TIMESTAMP),
('USDT', 'Tether USD', 5.62, 1.00, 0.01, 0.05, 0.10, 52000000000.00, 640000000000.00, 5.65, 5.59, 114000000000.00, 'STABLECOIN', 'https://cryptologos.cc/logos/tether-usdt-logo.svg', CURRENT_TIMESTAMP),
('USDC', 'USD Coin', 5.62, 1.00, 0.00, 0.02, 0.08, 12000000000.00, 190000000000.00, 5.64, 5.60, 34000000000.00, 'STABLECOIN', 'https://cryptologos.cc/logos/usd-coin-usdc-logo.svg', CURRENT_TIMESTAMP)
ON CONFLICT (symbol) DO UPDATE SET
    price_brl = EXCLUDED.price_brl,
    price_usd = EXCLUDED.price_usd,
    change_1h = EXCLUDED.change_1h,
    change_24h = EXCLUDED.change_24h,
    change_7d = EXCLUDED.change_7d,
    volume_24h_brl = EXCLUDED.volume_24h_brl,
    market_cap_brl = EXCLUDED.market_cap_brl,
    high_24h_brl = EXCLUDED.high_24h_brl,
    low_24h_brl = EXCLUDED.low_24h_brl,
    circulating_supply = EXCLUDED.circulating_supply,
    category = EXCLUDED.category,
    icon_url = EXCLUDED.icon_url,
    last_updated_at = CURRENT_TIMESTAMP;
