package com.mybroker.service;

import com.mybroker.ai.OpenAiClient;
import com.mybroker.model.PortfolioAnalysisResult;
import com.mybroker.model.Position;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class PortfolioAiAdvisor {

    private final OpenAiClient openAiClient;

    public PortfolioAiAdvisor(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    /**
     * Baut einen Prompt, der die Kennzahlen erklärt und ruft deinen OpenAiClient.chat(...)
     * auf. Die Methode gibt den "reinen" KI-Text zurück (kein JSON).
     */
    public String buildExplanation(PortfolioAnalysisResult analysis, List<Position> positions) {
        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(analysis, positions);

        try {
            // dein Client liefert ROHES JSON zurück
            String jsonResponse = openAiClient.chat(systemPrompt, userPrompt);
            // wir extrahieren daraus den content-Text der Antwort
            return extractContentFromResponse(jsonResponse);
        } catch (IOException e) {
            e.printStackTrace();
            return "Fehler beim Aufruf des KI-Services: " + e.getMessage();
        }
    }

    private String buildSystemPrompt() {
        String sb = "Du bist ein Assistent, der ein Aktien-Portfolio erklärt. " +
                "Gib KEINE konkreten Kauf- oder Verkaufsempfehlungen. " +
                "Erkläre stattdessen Risiken, Diversifikation und mögliche Übergewichtungen. " +
                "Sprich den Nutzer in der Du-Form an. Verwende kurze Absätze und Aufzählungen. " +
                "Formuliere klar, aber ohne Fachjargon. ";
        return sb;
    }

    private String buildUserPrompt(PortfolioAnalysisResult analysis, List<Position> positions) {
        StringBuilder sb = new StringBuilder();

        sb.append("Hier sind die aktuellen Portfoliodaten des Nutzers.\n\n");

        sb.append("PORTFOLIO-ÜBERSICHT:\n");
        sb.append("- Gesamtwert: ").append(analysis.getTotalMarketValue()).append("\n");
        sb.append("- Tech-Gewichtung: ").append(nullSafe(analysis.getTechWeight())).append(" %\n");
        sb.append("- Größte Position: ")
                .append(analysis.getTopPositionSymbol()).append(" (")
                .append(nullSafe(analysis.getTopPositionWeight())).append(" %)\n\n");

        sb.append("SEKTORGEWICHTE (% des Portfolios):\n");
        Map<String, BigDecimal> sectorWeights = analysis.getSectorWeights();
        if (sectorWeights != null) {
            for (Map.Entry<String, BigDecimal> e : sectorWeights.entrySet()) {
                sb.append("- ").append(e.getKey()).append(": ")
                        .append(e.getValue()).append(" %\n");
            }
        }
        sb.append("\n");

        sb.append("INTERNE HEURISTIK-KOMMENTARE (nur zur Info, bitte nicht wörtlich wiederholen):\n");
        sb.append("- Risiko: ").append(analysis.getRiskComment()).append("\n");
        sb.append("- Volatilität: ").append(analysis.getVolatilityComment()).append("\n");
        sb.append("- Diversifikation: ").append(analysis.getDiversificationComment()).append("\n\n");

        sb.append("EINZELPOSITIONEN:\n");
        for (Position p : positions) {
            sb.append("- ").append(p.getSymbol())
                    .append(" (").append(p.getName()).append(") ")
                    .append("Sektor: ").append(p.getSector())
                    .append(", Marktwert: ").append(p.getMarketValue())
                    .append(", offener P/L: ").append(p.getUnrealizedPnl())
                    .append(", Volatilität: ").append(p.getVolatility())
                    .append("\n");
        }
        sb.append("\n");

        sb.append("Aufgabe für dich:\n");
        sb.append("1. Erkläre, ob das Portfolio stark auf Tech oder einzelne Titel konzentriert ist.\n");
        sb.append("2. Beschreibe das Risiko in einfachen Worten.\n");
        sb.append("3. Gib neutrale Hinweise, wo Diversifikation sinnvoll sein könnte, ohne konkrete Produkte zu nennen.\n");
        sb.append("4. Halte dich an maximal 10 Sätze und vermeide Fachchinesisch.\n");

        return sb.toString();
    }

    private String nullSafe(BigDecimal value) {
        return value == null ? "-" : value.toString();
    }

    /**
     * Sehr einfache Extraktion des ersten message.content aus der JSON-Response
     * von /v1/chat/completions.
     * <p>
     * Für produktiven Code wäre eine JSON-Library (Jackson / Gson) besser,
     * aber das hier reicht für deinen Prototyp.
     */
    private String extractContentFromResponse(String json) {
        if (json == null || json.isEmpty()) {
            return "Leere Antwort vom KI-Service.";
        }

        // Suche nach "content":"...".
        // Hinweis: funktioniert, solange das Format von OpenAI Standard bleibt.
        String marker = "\"content\":\"";
        int idx = json.indexOf(marker);
        if (idx < 0) {
            return json; // Fallback: komplettes JSON anzeigen
        }
        int start = idx + marker.length();
        int end = json.indexOf("\"", start);
        if (end < 0) {
            end = json.length();
        }

        String content = json.substring(start, end);
        // einfache Un-Escapes
        content = content.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");

        return content;
    }
}
