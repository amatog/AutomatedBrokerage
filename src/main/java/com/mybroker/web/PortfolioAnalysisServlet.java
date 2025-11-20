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
        out.println("<div class=\"card\">");
        out.println("<h2>KI-Erklärung (keine Anlageberatung)</h2>");
        out.println("<pre>" + escapeHtml(analysis.getAiExplanation()) + "</pre>");
        out.println("</div>");

        out.println("<p><a href=\"index.jsp\">Zurück zum Dashboard</a></p>");

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
}
