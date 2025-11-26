package com.mybroker.ml;

import java.util.List;

public class PortfolioRequestDto {

    private double cash;
    private List<PositionDto> positions;

    public PortfolioRequestDto() {
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public List<PositionDto> getPositions() {
        return positions;
    }

    public void setPositions(List<PositionDto> positions) {
        this.positions = positions;
    }
}
