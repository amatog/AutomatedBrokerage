package com.mybroker.ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Client für Aufrufe der OpenAI Chat Completions API.
 *
 * API Key und Modell werden wie folgt gesucht:
 * 1. System Property (z.B. aus .env via EnvLoader): OPENAI_API_KEY, OPENAI_MODEL
 * 2. Environment Variable: OPENAI_API_KEY, OPENAI_MODEL
 */
public class OpenAiClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final String apiKey;
    private final String model;

    public OpenAiClient() {
        // 1. Versuche System Properties (gesetzt durch EnvLoader)
        String keyFromProps = System.getProperty("OPENAI_API_KEY");
        // 2. Fallback: Environment Variable
        String keyFromEnv = System.getenv("OPENAI_API_KEY");

        this.apiKey = (keyFromProps != null && !keyFromProps.isEmpty())
                ? keyFromProps
                : keyFromEnv;

        if (this.apiKey == null || this.apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY ist weder als System Property noch als Environment Variable gesetzt.");
        }

        // Modell
        String modelFromProps = System.getProperty("OPENAI_MODEL");
        String modelFromEnv = System.getenv("OPENAI_MODEL");

        String m = null;
        if (modelFromProps != null && !modelFromProps.isEmpty()) {
            m = modelFromProps;
        } else if (modelFromEnv != null && !modelFromEnv.isEmpty()) {
            m = modelFromEnv;
        }

        this.model = (m != null && !m.isEmpty()) ? m : "gpt-4.1-mini";
    }

    /**
     * Führt einen Chat-Completion Request aus.
     *
     * @param systemPrompt Anweisung an das Modell (Rolle "system")
     * @param userPrompt   Inhalt der Nutzereingabe + Kontext (Rolle "user")
     * @return Rohes JSON der OpenAI-Antwort
     * @throws IOException bei Kommunikationsfehlern
     */
    public String chat(String systemPrompt, String userPrompt) throws IOException {
        String jsonBody = buildRequestBody(systemPrompt, userPrompt);

        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);

        // Falls du ein bestimmtes Projekt in OpenAI konfigurierst:
        // String projectId = System.getProperty("OPENAI_PROJECT_ID", System.getenv("OPENAI_PROJECT_ID"));
        // if (projectId != null && !projectId.isEmpty()) {
        //     conn.setRequestProperty("OpenAI-Project", projectId);
        // }

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes("UTF-8"));
        }

        int status = conn.getResponseCode();
        InputStream is = (status >= 200 && status < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        if (status < 200 || status >= 300) {
            throw new IOException("OpenAI API Fehler (" + status + "): " + response);
        }

        return response.toString();
    }

    private String buildRequestBody(String systemPrompt, String userPrompt) {
        String sys = escapeJson(systemPrompt);
        String usr = escapeJson(userPrompt);
        return "{"
                + "\"model\":\"" + model + "\","
                + "\"messages\":["
                + "{\"role\":\"system\",\"content\":\"" + sys + "\"},"
                + "{\"role\":\"user\",\"content\":\"" + usr + "\"}"
                + "]"
                + "}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}

