package com.mybroker.ml;

public class RiskScoreResponseDto {

    private int riskScore;
    private String riskLevel;
    private String explanation;

    private double totalValue;
    private int numPositions;
    private double concentration;

    public RiskScoreResponseDto() {
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public double getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }

    public int getNumPositions() {
        return numPositions;
    }

    public void setNumPositions(int numPositions) {
        this.numPositions = numPositions;
    }

    public double getConcentration() {
        return concentration;
    }

    public void setConcentration(double concentration) {
        this.concentration = concentration;
    }
}
