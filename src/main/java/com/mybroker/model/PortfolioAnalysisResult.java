package com.mybroker.model;

import java.math.BigDecimal;
import java.util.Map;

public class PortfolioAnalysisResult {

    private BigDecimal totalMarketValue;
    private Map<String, BigDecimal> sectorWeights; // Sector -> Anteil in %
    private BigDecimal techWeight;                 // Anteil Tech in %
    private BigDecimal topPositionWeight;          // größte Einzelposition in %
    private String topPositionSymbol;

    private String riskComment;            // Kurzkommentar (z.B. Konzentrationsrisiko)
    private String volatilityComment;      // Kommentar zur Volatilität
    private String diversificationComment; // Kommentar zur Diversifikation

    private String aiExplanation;          // Volltext von der KI

    public BigDecimal getTotalMarketValue() {
        return totalMarketValue;
    }

    public void setTotalMarketValue(BigDecimal totalMarketValue) {
        this.totalMarketValue = totalMarketValue;
    }

    public Map<String, BigDecimal> getSectorWeights() {
        return sectorWeights;
    }

    public void setSectorWeights(Map<String, BigDecimal> sectorWeights) {
        this.sectorWeights = sectorWeights;
    }

    public BigDecimal getTechWeight() {
        return techWeight;
    }

    public void setTechWeight(BigDecimal techWeight) {
        this.techWeight = techWeight;
    }

    public BigDecimal getTopPositionWeight() {
        return topPositionWeight;
    }

    public void setTopPositionWeight(BigDecimal topPositionWeight) {
        this.topPositionWeight = topPositionWeight;
    }

    public String getTopPositionSymbol() {
        return topPositionSymbol;
    }

    public void setTopPositionSymbol(String topPositionSymbol) {
        this.topPositionSymbol = topPositionSymbol;
    }

    public String getRiskComment() {
        return riskComment;
    }

    public void setRiskComment(String riskComment) {
        this.riskComment = riskComment;
    }

    public String getVolatilityComment() {
        return volatilityComment;
    }

    public void setVolatilityComment(String volatilityComment) {
        this.volatilityComment = volatilityComment;
    }

    public String getDiversificationComment() {
        return diversificationComment;
    }

    public void setDiversificationComment(String diversificationComment) {
        this.diversificationComment = diversificationComment;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }
}
