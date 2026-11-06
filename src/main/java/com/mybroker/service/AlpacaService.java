package com.mybroker.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AlpacaService {

    private final String apiKey = System.getenv("ALPACA_API_KEY");
    private final String apiSecret = System.getenv("ALPACA_API_SECRET");
    private final String baseUrl = System.getenv("ALPACA_BASE_URL");
    private final String dataUrl = System.getenv("ALPACA_DATA_URL");


    private final HttpClient client = HttpClient.newHttpClient();

    public String getAccount() throws IOException, InterruptedException {
        if (apiKey == null || apiSecret == null || baseUrl == null) {
            throw new IllegalStateException(
                    "Umgebungsvariablen ALPACA_API_KEY, ALPACA_API_SECRET und ALPACA_BASE_URL müssen gesetzt sein.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/account"))
                .header("APCA-API-KEY-ID", apiKey)
                .header("APCA-API-SECRET-KEY", apiSecret)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getPositions() throws IOException, InterruptedException {
        if (apiKey == null || apiSecret == null || baseUrl == null) {
            throw new IllegalStateException(
                    "Umgebungsvariablen ALPACA_API_KEY, ALPACA_API_SECRET und ALPACA_BASE_URL müssen gesetzt sein.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/positions"))
                .header("APCA-API-KEY-ID", apiKey)
                .header("APCA-API-SECRET-KEY", apiSecret)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String createOrder(String symbol, int qty, String side, String clientOrderId)
            throws IOException, InterruptedException {

        String mode = System.getenv("ALPACA_TRADING_MODE");
        if (!("paper".equals(mode) && "https://paper-api.alpaca.markets".equals(baseUrl))
                && !("live".equals(mode) && "https://api.alpaca.markets".equals(baseUrl))) {
            throw new IllegalStateException("Set ALPACA_TRADING_MODE=paper or live with the matching Alpaca API URL");
        }
        if (qty <= 0 || symbol == null || !symbol.matches("[A-Z0-9][A-Z0-9.\\-]{0,19}")
                || !("buy".equals(side) || "sell".equals(side))
                || clientOrderId == null || !clientOrderId.matches("[a-f0-9-]{36}")) {
            throw new IllegalArgumentException("Invalid order");
        }

        if (apiKey == null || apiSecret == null || baseUrl == null) {
            throw new IllegalStateException(
                    "Umgebungsvariablen ALPACA_API_KEY, ALPACA_API_SECRET und ALPACA_BASE_URL müssen gesetzt sein.");
        }

        // Wir machen eine einfache Market-Order, DAY
        com.google.gson.JsonObject body = new com.google.gson.JsonObject();
        body.addProperty("symbol", symbol);
        body.addProperty("qty", qty);
        body.addProperty("side", side);          // "buy" oder "sell"
        body.addProperty("type", "market");
        body.addProperty("time_in_force", "day");
        body.addProperty("client_order_id", clientOrderId);

        String jsonBody = body.toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/orders"))
                .timeout(java.time.Duration.ofSeconds(20))
                .header("APCA-API-KEY-ID", apiKey)
                .header("APCA-API-SECRET-KEY", apiSecret)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Order rejected by broker (HTTP " + response.statusCode() + ")");
        }
        return response.body();
    }

    public String getOpenOrders() throws IOException, InterruptedException {
        if (apiKey == null || apiSecret == null || baseUrl == null) {
            throw new IllegalStateException(
                    "Umgebungsvariablen ALPACA_API_KEY, ALPACA_API_SECRET und ALPACA_BASE_URL müssen gesetzt sein.");
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v2/orders?status=open&direction=desc"))
                .header("APCA-API-KEY-ID", apiKey)
                .header("APCA-API-SECRET-KEY", apiSecret)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getLastFills(int limit) throws IOException, InterruptedException {
        if (apiKey == null || apiSecret == null || baseUrl == null) {
            throw new IllegalStateException(
                    "Umgebungsvariablen ALPACA_API_KEY, ALPACA_API_SECRET und ALPACA_BASE_URL müssen gesetzt sein.");
        }

        // Account-Aktivitäten vom Typ FILL
        String url = baseUrl + "/v2/account/activities"
                + "?activity_types=FILL"
                + "&page_size=" + limit
                + "&direction=desc";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APCA-API-KEY-ID", apiKey)
                .header("APCA-API-SECRET-KEY", apiSecret)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public String getLastTrade(String symbol) throws IOException, InterruptedException {
        if (apiKey == null || apiSecret == null || dataUrl == null) {
            throw new IllegalStateException(
                    "ALPACA_API_KEY, ALPACA_API_SECRET und ALPACA_DATA_URL müssen gesetzt sein.");
        }

        String url = dataUrl + "/v2/stocks/" + symbol + "/trades/latest";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("APCA-API-KEY-ID", apiKey)
                .header("APCA-API-SECRET-KEY", apiSecret)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }



}
