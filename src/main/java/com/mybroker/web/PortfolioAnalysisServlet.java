package com.mybroker.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybroker.ai.OpenAiClient;
import com.mybroker.model.PortfolioAnalysisResult;
import com.mybroker.model.Position;
import com.mybroker.service.PortfolioAiAdvisor;
import com.mybroker.service.PortfolioAnalysisService;
import com.mybroker.service.PortfolioDataService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "PortfolioAnalysisServlet", urlPatterns = {"/portfolio-analysis"})
public class PortfolioAnalysisServlet extends HttpServlet {

    private PortfolioDataService dataService;
    private PortfolioAnalysisService analysisService;
    private PortfolioAiAdvisor aiAdvisor;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = "demoUser";

        // 1) Portfolio laden und analysieren
        List<Position> positions = dataService.loadCurrentPositions(userId);
        PortfolioAnalysisResult analysis = analysisService.analyse(positions);

        // 2) KI-Text erzeugen
        String aiText = aiAdvisor.buildExplanation(analysis, positions);
        analysis.setAiExplanation(aiText);

        // 3) Alpaca-Portfolio-Historie laden (für Kachel "Portfolio-Entwicklung")
        List<String> performanceLabels = new ArrayList<>();
        List<BigDecimal> performanceValues = new ArrayList<>();

        try {
            List<PortfolioPoint> history = loadPortfolioHistoryFromAlpaca("1M", "1D");
            for (PortfolioPoint p : history) {
                performanceLabels.add(p.label());
                performanceValues.add(p.equity());
            }
        } catch (Exception e) {
            // Wenn hier etwas schief geht, lassen wir die Listen einfach leer
            e.printStackTrace();
        }

        // 4) Attribute für JSP setzen
        req.setAttribute("positions", positions);
        req.setAttribute("analysis", analysis);

        // KI-Infos
        req.setAttribute("aiContent", extractAssistantContent(aiText));
        req.setAttribute("aiModel", extractJsonStringField(aiText, "\"model\""));
        req.setAttribute("aiTotalTokens", extractJsonNumberField(aiText, "\"total_tokens\""));
        req.setAttribute("aiCompletionTokens", extractJsonNumberField(aiText, "\"completion_tokens\""));
        req.setAttribute("aiServiceTier", extractJsonStringField(aiText, "\"service_tier\""));

        // Performance-Daten für die Line-Chart (Portfolio-Entwicklung)
        req.setAttribute("performanceLabels", performanceLabels);
        req.setAttribute("performanceValues", performanceValues);

        RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/portfolio-analysis.jsp");
        dispatcher.forward(req, resp);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        this.dataService = new PortfolioDataService();
        this.analysisService = new PortfolioAnalysisService();
        this.aiAdvisor = new PortfolioAiAdvisor(new OpenAiClient());
    }

    /**
     * Lädt die Portfolio-Historie (Equity) von Alpaca und gibt sie als Liste von Punkten zurück.
     * period: z.B. "1M", "3M", "6M"
     * timeframe: z.B. "1D"
     */
    private List<PortfolioPoint> loadPortfolioHistoryFromAlpaca(String period, String timeframe) throws IOException {
        List<PortfolioPoint> result = new ArrayList<>();

        String apiKey = System.getenv("ALPACA_API_KEY");
        String apiSecret = System.getenv("ALPACA_API_SECRET");
        String baseUrl = System.getenv("ALPACA_BASE_URL");

        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://paper-api.alpaca.markets";
        }

        if (apiKey == null || apiKey.isBlank() || apiSecret == null || apiSecret.isBlank()) {
            // Ohne Credentials können wir nichts laden – leere Liste zurück
            return result;
        }

        String urlStr = baseUrl + "/v2/account/portfolio/history"
                + "?period=" + period
                + "&timeframe=" + timeframe
                + "&intraday_reporting=extended_hours";

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("APCA-API-KEY-ID", apiKey);
            conn.setRequestProperty("APCA-API-SECRET-KEY", apiSecret);

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new IOException("Alpaca portfolio history error: HTTP " + status);
            }

            try (InputStream in = conn.getInputStream()) {
                JsonNode root = mapper.readTree(in);
                JsonNode equityArr = root.get("equity");
                JsonNode timestampsArr = root.get("timestamp");

                if (equityArr != null && timestampsArr != null &&
                        equityArr.isArray() && timestampsArr.isArray()) {

                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                            .withZone(ZoneId.systemDefault());

                    int n = Math.min(equityArr.size(), timestampsArr.size());
                    for (int i = 0; i < n; i++) {
                        double eq = equityArr.get(i).asDouble();
                        long ts = timestampsArr.get(i).asLong(); // Sekunden seit Epoch

                        String label = fmt.format(Instant.ofEpochSecond(ts));
                        result.add(new PortfolioPoint(label, BigDecimal.valueOf(eq)));
                    }
                }
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return result;
    }

    private String extractJsonNumberField(String json, String fieldName) {
        if (json == null) return null;
        int pos = json.indexOf(fieldName);
        if (pos < 0) return null;
        int colon = json.indexOf(':', pos);
        if (colon < 0) return null;
        int comma = json.indexOf(',', colon + 1);
        String number = comma < 0
                ? json.substring(colon + 1).trim()
                : json.substring(colon + 1, comma).trim();
        return number.replaceAll("[^0-9]", "");
    }

    private String extractAssistantContent(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        int idx = json.indexOf("\"content\"");
        if (idx < 0) return json;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return json;
        int startQuote = json.indexOf('"', colon + 1);
        if (startQuote < 0) return json;
        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote < 0) return json;
        return json.substring(startQuote + 1, endQuote);
    }

    private String extractJsonStringField(String json, String fieldName) {
        if (json == null) return null;
        int pos = json.indexOf(fieldName);
        if (pos < 0) return null;
        int colon = json.indexOf(':', pos);
        if (colon < 0) return null;
        int firstQuote = json.indexOf('"', colon + 1);
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }

    /**
     * Einfaches DTO für einen Punkt der Portfolio-Historie.
     *
     * @param label  z.B. "2025-02-01"
     * @param equity Portfoliowert
     */
        private record PortfolioPoint(String label, BigDecimal equity) {
    }

}
