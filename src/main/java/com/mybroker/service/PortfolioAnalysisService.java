package com.mybroker.service;

import com.mybroker.model.PortfolioAnalysisResult;
import com.mybroker.model.Position;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class PortfolioAnalysisService {

    public PortfolioAnalysisResult analyse(List<Position> positions) {
        PortfolioAnalysisResult result = new PortfolioAnalysisResult();

        // Kein Portfolio -> nur Basisinfos setzen
        if (positions == null || positions.isEmpty()) {
            result.setTotalMarketValue(BigDecimal.ZERO);
            result.setRiskComment("Keine Positionen im Portfolio.");
            result.setVolatilityComment("-");
            result.setDiversificationComment("-");
            return result;
        }

        // Gesamtmarktwert berechnen
        BigDecimal totalMv = positions.stream()
                .map(p -> p.getMarketValue() == null ? BigDecimal.ZERO : p.getMarketValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        result.setTotalMarketValue(totalMv);

        if (totalMv.compareTo(BigDecimal.ZERO) <= 0) {
            result.setRiskComment("Gesamtmarktwert ist 0 oder negativ.");
            result.setVolatilityComment("-");
            result.setDiversificationComment("-");
            return result;
        }

        // --- Sektor-Gewichte & Tech-Anteil ---
        Map<String, BigDecimal> sectorWeights = buildSectorWeights(positions, totalMv);
        result.setSectorWeights(sectorWeights);

        BigDecimal techWeight = computeTechWeight(positions, totalMv);
        result.setTechWeight(techWeight);

        // --- Größte Einzelposition ---
        computeTopPosition(positions, totalMv, result);

        // --- Kommentare ---
        result.setRiskComment(buildRiskComment(techWeight, result.getTopPositionWeight()));
        result.setVolatilityComment(buildVolatilityComment(positions));
        result.setDiversificationComment(buildDiversificationComment(sectorWeights));

        return result;
    }

    /**
     * Aggregiert Marktwerte je Sektor und wandelt sie in Prozent-Gewichte um.
     * Rückgabe ist nach Gewicht (absteigend) sortiert.
     */
    private Map<String, BigDecimal> buildSectorWeights(List<Position> positions, BigDecimal totalMv) {
        Map<String, BigDecimal> sectorMv = new HashMap<>();

        for (Position p : positions) {
            BigDecimal mv = p.getMarketValue() == null ? BigDecimal.ZERO : p.getMarketValue();
            if (mv.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            String sector = p.getSector();
            if (sector == null || sector.isBlank()) {
                sector = "Unknown";
            }
            sectorMv.merge(sector, mv, BigDecimal::add);
        }

        Map<String, BigDecimal> sectorWeights = new LinkedHashMap<>();
        sectorMv.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(e -> {
                    BigDecimal weight = e.getValue()
                            .multiply(BigDecimal.valueOf(100))
                            .divide(totalMv, 2, RoundingMode.HALF_UP);
                    sectorWeights.put(e.getKey(), weight);
                });

        return sectorWeights;
    }

    /**
     * Tech-Anteil: alle Positionen, deren Sektor "tech" (case-insensitive) enthält.
     * Damit greifen auch Bezeichnungen wie "Information Technology".
     */
    private BigDecimal computeTechWeight(List<Position> positions, BigDecimal totalMv) {
        BigDecimal techMv = BigDecimal.ZERO;

        for (Position p : positions) {
            BigDecimal mv = p.getMarketValue() == null ? BigDecimal.ZERO : p.getMarketValue();
            if (mv.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            String sector = p.getSector();
            if (sector != null && sector.toLowerCase().contains("tech")) {
                techMv = techMv.add(mv);
            }
        }

        if (techMv.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return techMv.multiply(BigDecimal.valueOf(100))
                .divide(totalMv, 2, RoundingMode.HALF_UP);
    }

    private void computeTopPosition(List<Position> positions,
                                    BigDecimal totalMv,
                                    PortfolioAnalysisResult result) {

        Position top = positions.stream()
                .filter(p -> p.getMarketValue() != null)
                .max(Comparator.comparing(Position::getMarketValue))
                .orElse(null);

        if (top == null || top.getMarketValue() == null
                || top.getMarketValue().compareTo(BigDecimal.ZERO) <= 0) {
            result.setTopPositionSymbol(null);
            result.setTopPositionWeight(BigDecimal.ZERO);
            return;
        }

        BigDecimal topWeight = top.getMarketValue()
                .multiply(BigDecimal.valueOf(100))
                .divide(totalMv, 2, RoundingMode.HALF_UP);

        result.setTopPositionSymbol(top.getSymbol());
        result.setTopPositionWeight(topWeight);
    }

    private String buildRiskComment(BigDecimal techWeight, BigDecimal topWeight) {
        StringBuilder sb = new StringBuilder();

        if (techWeight != null && techWeight.compareTo(new BigDecimal("40")) > 0) {
            sb.append("Hohe Tech-Gewichtung (")
                    .append(techWeight).append("%). ");
        } else if (techWeight != null && techWeight.compareTo(new BigDecimal("20")) > 0) {
            sb.append("Moderate Tech-Gewichtung (")
                    .append(techWeight).append("%). ");
        }

        if (topWeight != null && topWeight.compareTo(new BigDecimal("35")) > 0) {
            sb.append("Sehr hohe Konzentration auf eine Einzelposition (")
                    .append(topWeight).append("%).");
        } else if (topWeight != null && topWeight.compareTo(new BigDecimal("20")) > 0) {
            sb.append("Erhöhte Konzentration auf eine Einzelposition (")
                    .append(topWeight).append("%).");
        }

        if (sb.length() == 0) {
            return "Risikoprofil wirkt ausgewogen (keine starke Konzentration sichtbar).";
        }
        return sb.toString();
    }

    private String buildVolatilityComment(List<Position> positions) {
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
            return "Keine Sektorinformationen verfügbar.";
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
