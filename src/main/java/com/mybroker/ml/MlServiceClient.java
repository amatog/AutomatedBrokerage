package com.mybroker.ml;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MlServiceClient {

    private final String baseUrl;

    public MlServiceClient() {
        String env = System.getenv("ML_SERVICE_BASE_URL");
        if (env == null || env.isBlank()) {
            env = "http://localhost:8000";
        }
        this.baseUrl = env.replaceAll("/$", "");
    }

    // ---------------------------------------------------------
    // Low-Level HTTP
    // ---------------------------------------------------------

    private JsonObject doPost(String endpoint, JsonObject body) throws Exception {
        URL url = new URL(baseUrl + endpoint);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setConnectTimeout(6000);
        con.setReadTimeout(6000);
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        if (body != null) {
            try (OutputStream os = con.getOutputStream()) {
                os.write(body.toString().getBytes());
            }
        }

        return readJson(con);
    }

    private JsonObject readJson(HttpURLConnection con) throws Exception {
        int code = con.getResponseCode();

        BufferedReader br = new BufferedReader(
                new InputStreamReader(
                        code < 400 ? con.getInputStream() : con.getErrorStream()
                )
        );

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        br.close();

        if (sb.length() == 0) {
            JsonObject empty = new JsonObject();
            empty.addProperty("status", "empty-response");
            return empty;
        }

        return JsonParser.parseString(sb.toString()).getAsJsonObject();
    }

    // ---------------------------------------------------------
    // Endpoints
    // ---------------------------------------------------------

    public JsonObject trainRiskModel() throws Exception {
        return doPost("/train-risk-model", null);
    }

    public JsonObject calculateRiskRaw(JsonObject body) throws Exception {
        return doPost("/risk", body);
    }

    public JsonObject getTrendRaw(String symbol) throws Exception {
        URL url = new URL(baseUrl + "/trend?symbol=" + symbol);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);
        return readJson(con);
    }

    // ---------------------------------------------------------
    // High-Level Wrapper: Risiko
    // ---------------------------------------------------------

    public RiskScoreResponseDto getRiskScore(PortfolioRequestDto p) throws Exception {

        JsonObject body = new JsonObject();

        // 1) cash
        body.addProperty("cash", p.getCash());

        // 2) positions: FastAPI erwartet symbol, quantity, last_price
        JsonArray arr = new JsonArray();
        if (p.getPositions() != null) {
            for (PositionDto pos : p.getPositions()) {
                JsonObject j = new JsonObject();

                if (pos.getSymbol() != null) {
                    j.addProperty("symbol", pos.getSymbol());
                }

                double quantity = pos.getQuantity();
                double marketValue = pos.getMarketValue();
                double lastPrice = 0.0;
                if (quantity != 0.0) {
                    lastPrice = marketValue / quantity;
                }

                j.addProperty("quantity", quantity);
                j.addProperty("last_price", lastPrice);   // 🔴 wichtig für FastAPI
                j.addProperty("marketValue", marketValue); // optional, wird ignoriert oder für Debug

                if (pos.getSector() != null) {
                    j.addProperty("sector", pos.getSector());
                }

                arr.add(j);
            }
        }
        body.add("positions", arr);

        // DEBUG: Request loggen
        System.out.println("=== ML DEBUG /risk Request ===");
        System.out.println(body);

        JsonObject obj = calculateRiskRaw(body);

        // DEBUG: Response loggen
        System.out.println("=== ML DEBUG /risk Response ===");
        System.out.println(obj.toString());

        // FastAPI-Fehler (Validation etc.)
        if (obj.has("detail")) {
            RiskScoreResponseDto errorDto = new RiskScoreResponseDto();
            errorDto.setRiskScore(0);
            errorDto.setRiskLevel("UNKNOWN");
            errorDto.setExplanation("ML-Validierungsfehler: " + obj.get("detail").toString());
            return errorDto;
        }

        RiskScoreResponseDto dto = new RiskScoreResponseDto();

        dto.setRiskScore(obj.has("risk_score") && !obj.get("risk_score").isJsonNull()
                ? obj.get("risk_score").getAsInt()
                : 0);

        dto.setRiskLevel(obj.has("risk_level") && !obj.get("risk_level").isJsonNull()
                ? obj.get("risk_level").getAsString()
                : "UNKNOWN");

        if (obj.has("explanation") && !obj.get("explanation").isJsonNull()) {
            dto.setExplanation(obj.get("explanation").getAsString());
        } else if (obj.has("message") && !obj.get("message").isJsonNull()) {
            dto.setExplanation(obj.get("message").getAsString());
        } else {
            dto.setExplanation("");
        }

        dto.setTotalValue(obj.has("total_value") && !obj.get("total_value").isJsonNull()
                ? obj.get("total_value").getAsDouble()
                : 0.0);

        dto.setNumPositions(obj.has("num_positions") && !obj.get("num_positions").isJsonNull()
                ? obj.get("num_positions").getAsInt()
                : 0);

        dto.setConcentration(obj.has("concentration") && !obj.get("concentration").isJsonNull()
                ? obj.get("concentration").getAsDouble()
                : 0.0);

        return dto;
    }

    // ---------------------------------------------------------
    // High-Level Wrapper: Trend
    // ---------------------------------------------------------

    public TrendScoreResponseDto getTrendScore(String symbol) throws Exception {
        JsonObject obj = getTrendRaw(symbol);

        TrendScoreResponseDto dto = new TrendScoreResponseDto();
        dto.setSymbol(symbol);

        if (obj.has("trend_score")) {
            dto.setScore(obj.get("trend_score").getAsDouble());
        } else if (obj.has("score")) {
            dto.setScore(obj.get("score").getAsDouble());
        } else {
            dto.setScore(0.0);
        }

        if (obj.has("direction")) {
            dto.setTrend(obj.get("direction").getAsString());
        } else if (obj.has("trend")) {
            dto.setTrend(obj.get("trend").getAsString());
        } else {
            dto.setTrend("FLAT");
        }

        if (obj.has("explanation")) {
            dto.setExplanation(obj.get("explanation").getAsString());
        } else if (obj.has("message")) {
            dto.setExplanation(obj.get("message").getAsString());
        } else {
            dto.setExplanation("");
        }

        return dto;
    }

    // ---------------------------------------------------------
    // Healthcheck
    // ---------------------------------------------------------

    public boolean isServiceHealthy() {
        try {
            URL url = new URL(baseUrl + "/health");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);
            return con.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
