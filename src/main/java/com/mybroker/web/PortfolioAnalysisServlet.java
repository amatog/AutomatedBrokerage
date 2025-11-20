package com.mybroker.web;

import com.mybroker.ai.OpenAiClient;
import com.mybroker.model.PortfolioAnalysisResult;
import com.mybroker.model.Position;
import com.mybroker.service.PortfolioAiAdvisor;
import com.mybroker.service.PortfolioAnalysisService;
import com.mybroker.service.PortfolioDataService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PortfolioAnalysisServlet", urlPatterns = {"/portfolio-analysis"})
public class PortfolioAnalysisServlet extends HttpServlet {

    private PortfolioDataService dataService;
    private PortfolioAnalysisService analysisService;
    private PortfolioAiAdvisor aiAdvisor;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dataService = new PortfolioDataService();
        this.analysisService = new PortfolioAnalysisService();
        this.aiAdvisor = new PortfolioAiAdvisor(new OpenAiClient());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // In einem echten System: User aus Session holen
        String userId = "demoUser";

        List<Position> positions = dataService.loadCurrentPositions(userId);
        PortfolioAnalysisResult analysis = analysisService.analyse(positions);

        String aiText = aiAdvisor.buildExplanation(analysis, positions);
        analysis.setAiExplanation(aiText);

        renderHtml(resp, positions, analysis);
    }

    private void renderHtml(HttpServletResponse resp,
                            List<Position> positions,
                            PortfolioAnalysisResult analysis) throws IOException {

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"de\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\"/>");
        out.println("  <title>Portfolio-Analyse mit KI</title>");
        out.println("  <style>");
        out.println("    body { font-family: Arial, sans-serif; background: #0f172a; color: #e5e7eb; margin: 0; padding: 0; }");
        out.println("    .container { max-width: 1200px; margin: 0 auto; padding: 20px; }");
        out.println("    h1 { color: #f97316; }");
        out.println("    .card { background: #020617; border-radius: 16px; padding: 20px; margin-bottom: 20px; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }");
        out.println("    .grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); grid-gap: 20px; }");
        out.println("    table { width: 100%; border-collapse: collapse; margin-top: 10px; }");
        out.println("    th, td { padding: 8px 6px; text-align: left; font-size: 14px; }");
        out.println("    th { border-bottom: 1px solid #1f2937; color: #9ca3af; }");
        out.println("    tr:nth-child(even) { background-color: #020617; }");
        out.println("    tr:nth-child(odd) { background-color: #020617; }");
        out.println("    .badge { display: inline-block; padding: 4px 8px; border-radius: 999px; font-size: 12px; background: #1f2937; color: #e5e7eb; }");
        out.println("    .badge-warn { background: #b91c1c; color: #fee2e2; }");
        out.println("    .badge-ok { background: #166534; color: #bbf7d0; }");
        out.println("    pre { white-space: pre-wrap; background: #020617; padding: 12px; border-radius: 12px; font-size: 14px; }");
        out.println("    a { color: #38bdf8; text-decoration: none; }");
        out.println("    a:hover { text-decoration: underline; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"container\">");

        out.println("<h1>Portfolio-Analyse mit KI</h1>");
        out.println("<p>Die KI erklärt dir dein aktuelles Portfolio, ohne konkrete Anlageempfehlungen zu geben.</p>");

        // Top-Kennzahlen
        out.println("<div class=\"grid\">");

        out.println("  <div class=\"card\">");
        out.println("    <h2>Gesamtübersicht</h2>");
        out.println("    <p><strong>Gesamtwert:</strong> " + safe(analysis.getTotalMarketValue()) + "</p>");
        out.println("    <p><strong>Tech-Gewichtung:</strong> " + safe(analysis.getTechWeight()) + " %</p>");
        out.println("    <p><strong>Größte Position:</strong> " + safe(analysis.getTopPositionSymbol()) +
                " (" + safe(analysis.getTopPositionWeight()) + " %)</p>");
        out.println("  </div>");

        out.println("  <div class=\"card\">");
        out.println("    <h2>Risiko & Diversifikation</h2>");
        out.println("    <p><strong>Risiko-Einschätzung:</strong><br/>" + safe(analysis.getRiskComment()) + "</p>");
        out.println("    <p><strong>Volatilität:</strong><br/>" + safe(analysis.getVolatilityComment()) + "</p>");
        out.println("    <p><strong>Diversifikation:</strong><br/>" + safe(analysis.getDiversificationComment()) + "</p>");
        out.println("  </div>");

        out.println("</div>"); // grid

        // Sektor-Gewichte
        out.println("<div class=\"card\">");
        out.println("<h2>Sektorverteilung</h2>");
        out.println("<table>");
        out.println("<thead><tr><th>Sektor</th><th>Gewichtung</th></tr></thead>");
        out.println("<tbody>");
        Map<String, BigDecimal> sectorWeights = analysis.getSectorWeights();
        if (sectorWeights != null) {
            for (Map.Entry<String, BigDecimal> e : sectorWeights.entrySet()) {
                out.println("<tr>");
                out.println("<td>" + e.getKey() + "</td>");
                out.println("<td>" + e.getValue() + " %</td>");
                out.println("</tr>");
            }
        }
        out.println("</tbody>");
        out.println("</table>");
        out.println("</div>");

        // Einzelpositionen
        out.println("<div class=\"card\">");
        out.println("<h2>Einzelpositionen</h2>");
        out.println("<table>");
        out.println("<thead><tr>");
        out.println("<th>Symbol</th><th>Name</th><th>Sektor</th><th>Marktwert</th><th>Offener P/L</th><th>Volatilität</th>");
        out.println("</tr></thead>");
        out.println("<tbody>");
        for (Position p : positions) {
            out.println("<tr>");
            out.println("<td>" + safe(p.getSymbol()) + "</td>");
            out.println("<td>" + safe(p.getName()) + "</td>");
            out.println("<td>" + safe(p.getSector()) + "</td>");
            out.println("<td>" + safe(p.getMarketValue()) + "</td>");
            out.println("<td>" + safe(p.getUnrealizedPnl()) + "</td>");
            out.println("<td>" + safe(p.getVolatility()) + "</td>");
            out.println("</tr>");
        }
        out.println("</tbody>");
        out.println("</table>");
        out.println("</div>");

        // KI-Erklärung
        // KI-Erklärung
        out.println("<div class=\"card\">");
        out.println("<h2>KI-Erklärung (keine Anlageberatung)</h2>");

        String aiRaw = analysis.getAiExplanation();

        if (aiRaw != null && aiRaw.trim().startsWith("{")) {
            // Sieht nach JSON aus → wir parsen die wichtigsten Teile
            String content = extractAssistantContent(aiRaw);
            String model = extractJsonStringField(aiRaw, "\"model\"");
            String totalTokens = extractJsonNumberField(aiRaw, "\"total_tokens\"");
            String completionTokens = extractJsonNumberField(aiRaw, "\"completion_tokens\"");
            String serviceTier = extractJsonStringField(aiRaw, "\"service_tier\"");

            // Haupttext der KI im aktuellen Layout (Absätze, kein JSON)
            out.println("<p>" + escapeHtml(content).replace("\n", "<br/>") + "</p>");

            // Dünne Linie und Metadaten in kleinerer Schrift
            out.println("<hr style=\"border: 0; border-top: 1px solid #1f2937; margin: 12px 0;\"/>");
            out.println("<p style=\"font-size: 0.85em; color: #8b949e;\">");
            out.println("<strong>Modell:</strong> " + escapeHtml(defaultIfEmpty(model, "-")) + "<br/>");
            out.println("<strong>Tokens gesamt:</strong> " + escapeHtml(defaultIfEmpty(totalTokens, "-")) + "<br/>");
            out.println("<strong>Tokens Antwort:</strong> " + escapeHtml(defaultIfEmpty(completionTokens, "-")) + "<br/>");
            out.println("<strong>Service-Tier:</strong> " + escapeHtml(defaultIfEmpty(serviceTier, "-")));
            out.println("</p>");
        } else {
            // Falls doch schon Plain-Text (z.B. nach späteren Änderungen)
            out.println("<pre>" + escapeHtml(aiRaw) + "</pre>");
        }

        out.println("</div>");


        out.println("<p><a href=\"dashboard\">Zurück zum Dashboard</a></p>");

        out.println("</div>"); // container
        out.println("</body>");
        out.println("</html>");
    }

    private String safe(Object o) {
        return o == null ? "-" : String.valueOf(o);
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String formatJsonIfNeeded(String text) {
        if (text == null) return "";
        String trimmed = text.trim();

        // nur formatieren, wenn es nach JSON aussieht
        if (!(trimmed.startsWith("{") || trimmed.startsWith("["))) {
            return text;
        }

        return prettyPrintJson(trimmed);
    }

    private String prettyPrintJson(String json) {
        StringBuilder sb = new StringBuilder();
        int indent = 0;
        boolean inQuotes = false;
        boolean escape = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (escape) {
                sb.append(c);
                escape = false;
                continue;
            }

            if (c == '\\') {
                sb.append(c);
                escape = true;
                continue;
            }

            if (c == '"') {
                sb.append(c);
                inQuotes = !inQuotes;
                continue;
            }

            if (inQuotes) {
                sb.append(c);
                continue;
            }

            switch (c) {
                case '{':
                case '[':
                    sb.append(c);
                    sb.append('\n');
                    indent++;
                    appendIndent(sb, indent);
                    break;
                case '}':
                case ']':
                    sb.append('\n');
                    indent--;
                    appendIndent(sb, indent);
                    sb.append(c);
                    break;
                case ',':
                    sb.append(c);
                    sb.append('\n');
                    appendIndent(sb, indent);
                    break;
                case ':':
                    sb.append(c).append(' ');
                    break;
                default:
                    if (!Character.isWhitespace(c)) {
                        sb.append(c);
                    }
                    break;
            }
        }

        return sb.toString();
    }

    private void appendIndent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append("  "); // 2 Spaces pro Ebene
        }
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }

    /**
     * Holt den Text aus choices[0].message.content aus dem JSON der Chat-Completion.
     */
    private String extractAssistantContent(String json) {
        if (json == null) return "";
        String marker = "\"content\":";
        int idx = json.indexOf(marker);
        if (idx < 0) {
            return json; // Fallback: JSON komplett anzeigen
        }

        // wir erwarten "content": "...."
        int startQuote = json.indexOf('"', idx + marker.length());
        if (startQuote < 0) return json;
        int endQuote = findStringEnd(json, startQuote);

        String raw = json.substring(startQuote + 1, endQuote);
        // Unescape der wichtigsten Sequenzen
        raw = raw.replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
        return raw;
    }

    /**
     * Findet das Ende eines JSON-Strings, berücksichtigt einfache Escapes.
     */
    private int findStringEnd(String json, int startQuoteIndex) {
        boolean escape = false;
        for (int i = startQuoteIndex + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\') {
                escape = true;
                continue;
            }
            if (c == '"') {
                return i;
            }
        }
        return json.length() - 1;
    }

    /**
     * Einfaches herausziehen eines String-Feldes wie "model": "gpt-4.1-mini-2025-04-14"
     */
    private String extractJsonStringField(String json, String fieldNameWithQuotes) {
        if (json == null) return null;
        int idx = json.indexOf(fieldNameWithQuotes);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;

        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;
        int endQuote = findStringEnd(json, firstQuote);

        String raw = json.substring(firstQuote + 1, endQuote);
        raw = raw.replace("\\\"", "\"").replace("\\\\", "\\");
        return raw;
    }

    /**
     * Einfaches herausziehen einer Zahl wie "total_tokens": 691
     */
    private String extractJsonNumberField(String json, String fieldNameWithQuotes) {
        if (json == null) return null;
        int idx = json.indexOf(fieldNameWithQuotes);
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return null;

        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) {
            end++;
        }

        if (start >= end) return null;
        return json.substring(start, end);
    }


}
