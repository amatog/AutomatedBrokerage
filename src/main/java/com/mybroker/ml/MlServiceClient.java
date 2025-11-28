// src/main/java/com/mybroker/ml/MlServiceClient.java
package com.mybroker.ml;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Client zum Aufruf des Python-ML-Services.
 * <p>
 * Erwartete Endpoints:
 * <p>
 * POST {ML_SERVICE_BASE_URL}/risk
 * Request-JSON:
 * {
 * "cash": 1234.56,               // optional
 * "positions": [
 * {
 * "symbol": "AAPL",
 * "quantity": 10.0,
 * "last_price": 180.0,
 * "sector": "Tech"
 * }
 * ]
 * }
 * <p>
 * Response-JSON:
 * {
 * "portfolio_id": null,
 * "total_value": 12345.67,
 * "num_positions": 5,
 * "concentration": 0.42,
 * "risk_score": 70,
 * "risk_level": "MEDIUM",
 * "explanation": "..."
 * }
 * <p>
 * GET {ML_SERVICE_BASE_URL}/trend?symbol=XYZ
 * Response-JSON:
 * {
 * "symbol": "XYZ",
 * "score": 0.65,
 * "trend": "UP",
 * "explanation": "..."
 * }
 */
public class MlServiceClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8000";
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MlServiceClient() {
        String envUrl = System.getenv("ML_SERVICE_BASE_URL");
        if (envUrl == null || envUrl.isBlank()) {
            envUrl = DEFAULT_BASE_URL;
        }
        if (envUrl.endsWith("/")) {
            envUrl = envUrl.substring(0, envUrl.length() - 1);
        }
        this.baseUrl = envUrl;
    }

    // =================== Public API ===================

    public RiskScoreResponseDto getRiskScore(PortfolioRequestDto portfolio) throws IOException {
        String url = baseUrl + "/risk";

        // Request-JSON aufbauen
        JsonNode requestJson = buildRiskRequest(portfolio);

        String responseText = postJson(url, requestJson.toString());
        JsonNode root = objectMapper.readTree(responseText);

        RiskScoreResponseDto dto = new RiskScoreResponseDto();
        dto.setRiskScore(getInt(root, "risk_score", 0));
        dto.setRiskLevel(getText(root, "risk_level", "UNKNOWN"));
        dto.setExplanation(getText(root, "explanation",
                "Modellbasierte Risiko-Einschätzung nicht verfügbar."));

        dto.setTotalValue(getDouble(root, "total_value", 0.0));
        dto.setNumPositions(getInt(root, "num_positions", 0));
        dto.setConcentration(getDouble(root, "concentration", 0.0));

        return dto;
    }

    public TrendScoreResponseDto getTrendScore(String symbol) throws IOException {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Symbol darf nicht leer sein");
        }

        String encoded = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
        String url = baseUrl + "/trend?symbol=" + encoded;

        String responseText = get(url);
        JsonNode root = objectMapper.readTree(responseText);

        TrendScoreResponseDto dto = new TrendScoreResponseDto();
        dto.setSymbol(getText(root, "symbol", symbol));
        dto.setScore(getDouble(root, "score", 0.0));
        dto.setTrend(getText(root, "trend", "NEUTRAL"));
        dto.setExplanation(getText(root, "explanation",
                "Trend nicht verfügbar (Fallback)."));

        return dto;
    }

    // =================== Request-Builder ===================

    private JsonNode buildRiskRequest(PortfolioRequestDto portfolio) {
        var root = objectMapper.createObjectNode();

        // cash optional
        root.put("cash", portfolio.getCash());

        var positionsArray = objectMapper.createArrayNode();
        if (portfolio.getPositions() != null) {
            for (PositionDto p : portfolio.getPositions()) {
                if (p == null || p.getSymbol() == null) {
                    continue;
                }
                var posNode = objectMapper.createObjectNode();
                posNode.put("symbol", p.getSymbol());
                posNode.put("quantity", p.getQuantity());

                double lastPrice = 0.0;
                if (p.getQuantity() > 0 && p.getMarketValue() > 0) {
                    lastPrice = p.getMarketValue() / p.getQuantity();
                }
                posNode.put("last_price", lastPrice);

                if (p.getSector() != null && !p.getSector().isBlank()) {
                    posNode.put("sector", p.getSector());
                }

                positionsArray.add(posNode);
            }
        }

        root.set("positions", positionsArray);
        return root;
    }

    // =================== HTTP Helper ===================

    private String postJson(String urlString, String jsonBody) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            byte[] out = jsonBody.getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(out.length);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
            }

            int status = conn.getResponseCode();
            InputStream is = status >= 200 && status < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String response = readAll(is);

            if (status < 200 || status >= 300) {
                throw new IOException("ML-Service POST " + urlString + " returned " + status + ": " + response);
            }

            return response;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String get(String urlString) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(8000);
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            InputStream is = status >= 200 && status < 300
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String response = readAll(is);

            if (status < 200 || status >= 300) {
                throw new IOException("ML-Service GET " + urlString + " returned " + status + ": " + response);
            }

            return response;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String readAll(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    // =================== JSON Helper ===================

    private String getText(JsonNode node, String field, String defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        try {
            return node.get(field).asText();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private int getInt(JsonNode node, String field, int defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        try {
            return node.get(field).asInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private double getDouble(JsonNode node, String field, double defaultValue) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return defaultValue;
        }
        try {
            return node.get(field).asDouble();
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
