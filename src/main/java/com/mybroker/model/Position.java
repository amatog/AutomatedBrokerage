package com.mybroker.model;

import java.math.BigDecimal;

public class Position {

    private String symbol;
    private String name;
    private String sector;
    private BigDecimal quantity;
    private BigDecimal marketValue;   // aktueller Marktwert in Konto-Währung
    private BigDecimal unrealizedPnl; // offener Gewinn/Verlust
    private BigDecimal volatility;    // z.B. historische Volatilität (0.25 = 25%)

    public Position() {
    }

    public Position(String symbol,
                    String name,
                    String sector,
                    BigDecimal quantity,
                    BigDecimal marketValue,
                    BigDecimal unrealizedPnl,
                    BigDecimal volatility) {
        this.symbol = symbol;
        this.name = name;
        this.sector = sector;
        this.quantity = quantity;
        this.marketValue = marketValue;
        this.unrealizedPnl = unrealizedPnl;
        this.volatility = volatility;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public BigDecimal getUnrealizedPnl() {
        return unrealizedPnl;
    }

    public void setUnrealizedPnl(BigDecimal unrealizedPnl) {
        this.unrealizedPnl = unrealizedPnl;
    }

    public BigDecimal getVolatility() {
        return volatility;
    }

    public void setVolatility(BigDecimal volatility) {
        this.volatility = volatility;
    }
}

