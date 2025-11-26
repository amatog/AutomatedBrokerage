// src/main/java/com/mybroker/ml/MlServiceClient.java
package com.mybroker.ml;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class MlServiceClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8000"; // fallback
    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MlServiceClient() {
        String envUrl = System.getenv("ML_SERVICE_URL");
        this.baseUrl = (envUrl != null && !envUrl.isBlank()) ? envUrl : DEFAULT_BASE_URL;
    }

    // ===== Risk-Score =====

    public RiskScoreResponseDto getRiskScore(PortfolioRequestDto portfolio) throws IOException {
        String endpoint = baseUrl + "/ml/risk-score";
        String jsonRequest = objectMapper.writeValueAsString(portfolio);
        String responseBody = doPost(endpoint, jsonRequest);
        return objectMapper.readValue(responseBody, RiskScoreResponseDto.class);
    }

    // ===== Trend-Score =====

    public TrendScoreResponseDto getTrendScore(String symbol) throws IOException {
        String endpoint = baseUrl + "/ml/trend-score";

        TrendRequestDto req = new TrendRequestDto(symbol);
        String jsonRequest = objectMapper.writeValueAsString(req);

        String responseBody = doPost(endpoint, jsonRequest);
        return objectMapper.readValue(responseBody, TrendScoreResponseDto.class);
    }

    private String doPost(String endpoint, String jsonRequest) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonRequest.getBytes());
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            if (status >= 200 && status < 300) {
                return sb.toString();
            } else {
                throw new IOException("ML service error: HTTP " + status + " body: " + sb);
            }
        } finally {
            conn.disconnect();
        }
    }

    // ===== Hilfs-Methode für POST-Request =====

    /**
     * Anfrage-Objekt für den Trend-Endpunkt.
     * Muss zur FastAPI/Python-Definition passen (fields: symbol).
     */
    public static class TrendRequestDto {
        private String symbol;

        public TrendRequestDto() {
        }

        public TrendRequestDto(String symbol) {
            this.symbol = symbol;
        }

        public String getSymbol() {
            return symbol;
        }

        public void setSymbol(String symbol) {
            this.symbol = symbol;
        }
    }
}
