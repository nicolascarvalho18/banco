package com.bancosap.enums;

public enum CryptoSymbol {
    BTC("Bitcoin"),
    ETH("Ethereum"),
    SOL("Solana"),
    USDT("Tether USD"),
    ADA("Cardano");

    private final String fullName;

    CryptoSymbol(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        return fullName;
    }
}
