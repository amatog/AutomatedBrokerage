package com.mybroker.ml;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ML-Service-Client für:
 *
 *   LOKAL:  http://localhost:8000
 *   SERVER: ML_SERVICE_BASE_URL (z.B. http://ml.netdesign.ch)
 *
 * Endpoints:
 *   POST /risk
 *   GET  /trend?symbol=XYZ
 */
public class MlServiceClient {

    private final String baseUrl;

    public MlServiceClient() {
        // 1. Versuche Umgebungsvariable (für den Server)
        String env = System.getenv("ML_SERVICE_BASE_URL");

        // 2. Falls nichts gesetzt: Default für lokale Entwicklung
        if (env == null || env.isBlank()) {
            // lokal läuft dein FastAPI- oder Uvicorn-Service
            env = "http://localhost:8000";
        }

        // Trailing Slash entfernen
        if (env.endsWith("/")) {
            env = env.substring(0, env.length() - 1);
        }

        this.baseUrl = env;
    }

    // =====================
    // Risk Score
    // =====================

    /**
     * POST /risk
     * <p>
     * Der Body ist so aufgebaut, dass er sowohl
     * - mit deinem lokalen FastAPI-Service
     * - als auch mit dem WSGI-Service auf ml.netdesign.ch
     * kompatibel ist.
     */
    public RiskScoreResponseDto getRiskScore(PortfolioRequestDto portfolio) throws Exception {
        String endpoint = baseUrl + "/risk";

        JsonObject body = new JsonObject();
        body.addProperty("cash", portfolio.getCash());

        // Positions-Array
        var arr = new com.google.gson.JsonArray();
        if (portfolio.getPositions() != null) {
            for (PositionDto p : portfolio.getPositions()) {
                JsonObject pos = new JsonObject();
                pos.addProperty("symbol", p.getSymbol());
                pos.addProperty("quantity", p.getQuantity());

                // Wir senden last_price – das akzeptieren beide Services.
                double qty = p.getQuantity() <= 0 ? 1.0 : p.getQuantity();
                double lastPrice = p.getMarketValue() / qty;
                pos.addProperty("last_price", lastPrice);

                if (p.getSector() != null) {
                    pos.addProperty("sector", p.getSector());
                }

                arr.add(pos);
            }
        }
        body.add("positions", arr);

        String responseJson = postJson(endpoint, body.toString());
        JsonObject obj = JsonParser.parseString(responseJson).getAsJsonObject();

        RiskScoreResponseDto dto = new RiskScoreResponseDto();
        // lokale + WSGI-Version liefern dieselben Feldnamen:
        dto.setRiskScore(obj.get("risk_score").getAsInt());
        dto.setRiskLevel(obj.get("risk_level").getAsString());
        dto.setExplanation(obj.get("explanation").getAsString());

        return dto;
    }

    // =====================
    // Trend Score
    // =====================

    /**
     * GET /trend?symbol=XYZ
     */
    public TrendScoreResponseDto getTrendScore(String symbol) throws Exception {
        String endpoint = baseUrl + "/trend?symbol=" + symbol;

        String responseJson = getJson(endpoint);
        JsonObject obj = JsonParser.parseString(responseJson).getAsJsonObject();

        TrendScoreResponseDto dto = new TrendScoreResponseDto();
        dto.setSymbol(obj.get("symbol").getAsString());
        dto.setScore(obj.get("score").getAsDouble());
        dto.setTrend(obj.get("trend").getAsString());
        dto.setExplanation(obj.get("explanation").getAsString());

        return dto;
    }

    // =====================
    // HTTP Helper
    // =====================

    private String getJson(String endpoint) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        int status = conn.getResponseCode();
        BufferedReader reader;
        if (status >= 200 && status < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder resp = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            resp.append(line);
        }
        reader.close();
        conn.disconnect();

        return resp.toString();
    }

    private String postJson(String endpoint, String jsonBody) throws Exception {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(10000);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes());
            os.flush();
        }

        int status = conn.getResponseCode();
        BufferedReader reader;
        if (status >= 200 && status < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder resp = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            resp.append(line);
        }
        reader.close();
        conn.disconnect();

        return resp.toString();
    }
}
