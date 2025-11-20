package com.mybroker.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybroker.ai.OpenAiClient;
import com.mybroker.service.AlpacaService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Smart Trading Assistant – Chat-Endpoint.
 * <p>
 * POST /ai-chat
 * - Parameter: message (Frage des Users in natürlicher Sprache)
 * - Antwort: JSON { "reply": "..." } oder { "error": "..." }
 */
@WebServlet(name = "AiChatServlet", urlPatterns = {"/ai-chat"})
public class AiChatServlet extends HttpServlet {

    private OpenAiClient openAiClient;
    private AlpacaService alpacaService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.openAiClient = new OpenAiClient();
        this.alpacaService = new AlpacaService(); // ggf. an DI / Konstruktor anpassen
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String userMessage = req.getParameter("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"message parameter is required\"}");
            }
            return;
        }

        // 1) Kontext aus deinen Datenquellen holen
        String tradingContext = buildTradingContext();

        // 2) System-Prompt (Rolle "system") – Regeln und Rolle des Assistenten
        String systemPrompt =
                "Du bist ein Smart Trading Assistant für die Web-App 'MyBrokerApp'. "
                        + "Du beantwortest Fragen des Nutzers in natürlicher Sprache, vorzugsweise auf Deutsch.\n\n"
                        + "Deine Aufgaben:\n"
                        + "- Erkläre und kommentiere offene Orders, Positionen, Tagesergebnis und Marktbewegungen "
                        + "  (z. B. Nasdaq, Dow Jones) auf Basis der dir gegebenen Daten.\n"
                        + "- Unterstütze den Nutzer beim Verständnis von Risiko, Volatilität und Exponierung.\n\n"
                        + "WICHTIG:\n"
                        + "- Gib KEINE konkreten Kauf- oder Verkaufsempfehlungen.\n"
                        + "- Triff keine harten Prognosen über die Zukunft.\n"
                        + "- Wenn eine Information nicht in den Daten enthalten ist, sag ehrlich, dass du sie nicht weißt.\n"
                        + "- Sei klar, sachlich, aber freundlich und hilfsbereit.\n";

        // 3) User-Prompt (Rolle "user") – Kontext + konkrete Frage
        String userPrompt =
                "Hier sind aktuelle Daten zu Portfolio, offenen Orders und Märkten:\n\n"
                        + tradingContext
                        + "\n\nDie Frage des Nutzers lautet:\n"
                        + userMessage;

        String rawJson;
        String answerText;

        try {
            // 4) OpenAI anrufen (liefert Roh-JSON)
            rawJson = openAiClient.chat(systemPrompt, userPrompt);

            // 5) Inhalt aus dem JSON herausziehen
            answerText = extractFirstMessageContent(rawJson);
            if (answerText == null || answerText.isEmpty()) {
                answerText = "Ich konnte keine verständliche Antwort von der AI lesen.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.write("{\"error\":\"AI request failed: " + escapeForJson(e.getMessage()) + "\"}");
            }
            return;
        }

        // 6) Antwort an das Frontend
        try (PrintWriter out = resp.getWriter()) {
            out.write("{\"reply\":\"" + escapeForJson(answerText) + "\"}");
        }
        rawJson = openAiClient.chat(systemPrompt, userPrompt);
        System.out.println("OpenAI RAW RESPONSE: " + rawJson);
        answerText = extractFirstMessageContent(rawJson);
    }

    /**
     * Baut einen einfachen Text-Kontext aus deinen Trading-Daten.
     * Hier kannst du später echte Aufrufe an AlpacaService einbauen.
     */
    private String buildTradingContext() {

        // TODO: Echte Daten holen, z. B.:
        // var positions = alpacaService.getOpenPositions();
        // var orders = alpacaService.getOpenOrders();
        // var dailyPnl = alpacaService.getTodayPnl();
        // var nasdaq = alpacaService.getIndexSnapshot("NDX");
        // var dow = alpacaService.getIndexSnapshot("DJI");
        //
        // Dann schön formatieren, z. B. als Liste.

        String sb = "Beispiel-Daten (Platzhalter – später durch echte Werte ersetzen):\n" +
                "- Offene Positionen: 3 (z.B. TSLA, AAPL, MSFT)\n" +
                "- Offene Orders: 1 Limit-Buy auf TSLA bei 200 USD\n" +
                "- Heutiger PnL: +120.50 USD\n" +
                "- Nasdaq Entwicklung heute: +1.2 %\n" +
                "- Dow Jones Entwicklung heute: -0.3 %\n";

        return sb;
    }

    /**
     * Sehr einfache Extraktion des ersten "content"-Feldes aus der OpenAI-Response.
     * Für Produktion wäre eine JSON-Library wie Jackson oder Gson vorzuziehen.
     */
    private String extractFirstMessageContent(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            // choices[0]
            JsonArray choices = root.getAsJsonArray("choices");
            if (choices == null || choices.size() == 0) {
                return null;
            }

            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            if (message == null) {
                return null;
            }

            JsonElement contentEl = message.get("content");
            if (contentEl == null || contentEl.isJsonNull()) {
                return null;
            }

            // Fall 1: klassisch – content ist ein String
            if (contentEl.isJsonPrimitive()) {
                return contentEl.getAsString().trim();
            }

            // Fall 2: neues Format – content ist ein Array von Content-Parts
            if (contentEl.isJsonArray()) {
                JsonArray parts = contentEl.getAsJsonArray();
                StringBuilder sb = new StringBuilder();

                for (JsonElement partEl : parts) {
                    if (!partEl.isJsonObject()) continue;
                    JsonObject part = partEl.getAsJsonObject();

                    // z.B. { "type": "text", "text": "..." }
                    JsonElement typeEl = part.get("type");
                    if (typeEl != null && "text".equals(typeEl.getAsString())) {
                        JsonElement textEl = part.get("text");
                        if (textEl != null && !textEl.isJsonNull()) {

                            if (textEl.isJsonPrimitive()) {
                                sb.append(textEl.getAsString());
                            } else if (textEl.isJsonObject()) {
                                // Falls strukturierter: { "value": "..." } etc.
                                JsonObject textObj = textEl.getAsJsonObject();
                                JsonElement valueEl = textObj.get("value");
                                if (valueEl != null && valueEl.isJsonPrimitive()) {
                                    sb.append(valueEl.getAsString());
                                } else {
                                    sb.append(textObj);
                                }
                            }
                            sb.append("\n");
                        }
                    }
                }

                String result = sb.toString().trim();
                return result.isEmpty() ? null : result;
            }

            // Fallback: unerwartiger Typ
            return contentEl.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Escaping für das JSON im Servlet-Response.
     */
    private String escapeForJson(String s) {
        if (s == null) return "";
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}

