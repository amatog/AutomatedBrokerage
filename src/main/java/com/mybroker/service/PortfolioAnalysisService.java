package com.mybroker.service;

import com.mybroker.model.PortfolioAnalysisResult;
import com.mybroker.model.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PortfolioAnalysisService {

    public PortfolioAnalysisResult analyse(List<Position> positions) {
        PortfolioAnalysisResult result = new PortfolioAnalysisResult();

        if (positions == null || positions.isEmpty()) {
            result.setRiskComment("Keine Positionen im Portfolio.");
            result.setVolatilityComment("-");
            result.setDiversificationComment("-");
            return result;
        }

        BigDecimal totalMv = positions.stream()
                .map(p -> p.getMarketValue() == null ? BigDecimal.ZERO : p.getMarketValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.setTotalMarketValue(totalMv);

        Map<String, BigDecimal> rawSectorValues = new HashMap<>();
        for (Position p : positions) {
            String sector = p.getSector() != null ? p.getSector() : "Unknown";
            BigDecimal mv = p.getMarketValue() != null ? p.getMarketValue() : BigDecimal.ZERO;
            rawSectorValues.merge(sector, mv, BigDecimal::add);
        }

        Map<String, BigDecimal> sectorWeights = new LinkedHashMap<>();
        BigDecimal techWeight = BigDecimal.ZERO;

        if (totalMv.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> e : rawSectorValues.entrySet()) {
                BigDecimal weight = e.getValue()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(totalMv, 2, RoundingMode.HALF_UP);
                sectorWeights.put(e.getKey(), weight);

                if ("Technology".equalsIgnoreCase(e.getKey()) || "Tech".equalsIgnoreCase(e.getKey())) {
                    techWeight = weight;
                }
            }
        }

        result.setSectorWeights(sectorWeights);
        result.setTechWeight(techWeight);

        // größte Einzelposition
        String topSymbol = null;
        BigDecimal topWeight = BigDecimal.ZERO;
        if (totalMv.compareTo(BigDecimal.ZERO) > 0) {
            for (Position p : positions) {
                BigDecimal mv = p.getMarketValue() != null ? p.getMarketValue() : BigDecimal.ZERO;
                BigDecimal weight = mv.multiply(BigDecimal.valueOf(100))
                        .divide(totalMv, 2, RoundingMode.HALF_UP);
                if (weight.compareTo(topWeight) > 0) {
                    topWeight = weight;
                    topSymbol = p.getSymbol();
                }
            }
        }

        result.setTopPositionSymbol(topSymbol);
        result.setTopPositionWeight(topWeight);

        // einfache Heuristiken für Kommentare
        result.setRiskComment(buildRiskComment(techWeight, topWeight));
        result.setVolatilityComment(buildVolatilityComment(positions));
        result.setDiversificationComment(buildDiversificationComment(sectorWeights));

        return result;
    }

    private String buildRiskComment(BigDecimal techWeight, BigDecimal topWeight) {
        StringBuilder sb = new StringBuilder();

        if (techWeight.compareTo(new BigDecimal("40")) > 0) {
            sb.append("Hohe Tech-Gewichtung (")
                    .append(techWeight).append("%). ");
        } else if (techWeight.compareTo(new BigDecimal("20")) > 0) {
            sb.append("Moderate Tech-Gewichtung (")
                    .append(techWeight).append("%). ");
        } else {
            sb.append("Tech-Anteil ist eher gering (")
                    .append(techWeight).append("%). ");
        }

        if (topWeight.compareTo(new BigDecimal("25")) > 0) {
            sb.append("Deutliche Konzentration in der größten Position (")
                    .append(topWeight).append("%).");
        } else if (topWeight.compareTo(new BigDecimal("15")) > 0) {
            sb.append("Etwas erhöhte Konzentration in der größten Position (")
                    .append(topWeight).append("%).");
        } else {
            sb.append("Die größte Position ist relativ moderat gewichtet (")
                    .append(topWeight).append("%).");
        }

        return sb.toString();
    }

    private String buildVolatilityComment(List<Position> positions) {
        // sehr einfache Heuristik: Durchschnitt der Volatilitäten
        BigDecimal sum = BigDecimal.ZERO;
        int count = 0;

        for (Position p : positions) {
            if (p.getVolatility() != null) {
                sum = sum.add(p.getVolatility());
                count++;
            }
        }

        if (count == 0) {
            return "Keine Volatilitätsdaten verfügbar.";
        }

        BigDecimal avgVol = sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        if (avgVol.compareTo(new BigDecimal("0.30")) > 0) {
            return "Durchschnittliche Volatilität ist eher hoch (" + avgVol + ").";
        } else if (avgVol.compareTo(new BigDecimal("0.15")) > 0) {
            return "Durchschnittliche Volatilität ist moderat (" + avgVol + ").";
        } else {
            return "Durchschnittliche Volatilität ist eher niedrig (" + avgVol + ").";
        }
    }

    private String buildDiversificationComment(Map<String, BigDecimal> sectorWeights) {
        if (sectorWeights == null || sectorWeights.isEmpty()) {
            return "Keine Sektorverteilung verfügbar.";
        }

        if (sectorWeights.size() <= 2) {
            return "Portfolio ist auf wenige Sektoren konzentriert.";
        }

        boolean singleDominant = sectorWeights.values().stream()
                .anyMatch(w -> w.compareTo(new BigDecimal("50")) > 0);

        if (singleDominant) {
            return "Ein Sektor dominiert das Portfolio deutlich (>50%).";
        }

        return "Sektorverteilung wirkt einigermaßen diversifiziert.";
    }
}
