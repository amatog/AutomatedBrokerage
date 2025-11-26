package com.mybroker.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class FinnhubClient {

    // Base URL for Finnhub API
    private static final String BASE_URL = "https://finnhub.io/api/v1";
    // You can also inject this via constructor or use environment variables.
    private final String apiKey;

    public FinnhubClient(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Fetch latest quote for a symbol using Finnhub "quote" endpoint.
     * Example: GET /quote?symbol=AAPL&token=YOUR_API_KEY
     */
    public String getQuoteRaw(String symbol) throws IOException {
        String encodedSymbol = URLEncoder.encode(symbol, StandardCharsets.UTF_8);
        String urlStr = BASE_URL + "/quote?symbol=" + encodedSymbol + "&token=" + apiKey;

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int status = conn.getResponseCode();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        status >= 200 && status < 300
                                ? conn.getInputStream()
                                : conn.getErrorStream(),
                        StandardCharsets.UTF_8))) {

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            if (status < 200 || status >= 300) {
                throw new IOException("Finnhub API error. HTTP status: " + status +
                        ", body: " + response);
            }

            return response.toString();
        } finally {
            conn.disconnect();
        }
    }
}

