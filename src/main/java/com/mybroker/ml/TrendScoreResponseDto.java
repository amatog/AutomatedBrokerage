package com.mybroker.ml;

public class TrendScoreResponseDto {

    private String symbol;
    private String trend;   // "UP", "DOWN", "NEUTRAL"
    private double score;   // z.B. 0.0 - 1.0
    private String explanation;

    public TrendScoreResponseDto() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
