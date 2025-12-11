package com.mybroker.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ValueServiceClient {

    private final String baseUrl; // z.B. https://value.netdesign.ch/api

    public ValueServiceClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public JsonObject getScore(String symbol) throws IOException {
        return get("/score", symbol);
    }

    private JsonObject get(String path, String symbol) throws IOException {
        String urlStr = baseUrl + path + "?symbol=" + encode(symbol);
        URL url = new URL(urlStr);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5_000);
        conn.setReadTimeout(10_000);

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        String body;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            body = sb.toString();
        } finally {
            conn.disconnect();
        }

        if (status < 200 || status >= 300) {
            throw new IOException("ValueService returned HTTP " + status + " for " + urlStr
                    + " body=" + body);
        }

        JsonElement root = JsonParser.parseString(body);
        if (!root.isJsonObject()) {
            throw new IOException("Unexpected JSON from ValueService: " + body);
        }
        return root.getAsJsonObject();
    }

    private String encode(String s) {
        return s.replace(" ", "%20");
    }
}
