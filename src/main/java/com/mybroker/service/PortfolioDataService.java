package com.mybroker.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybroker.model.Position;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Liefert die aktuellen Portfolio-Positionen für die Portfolio-Analyse.
 * Holt die Daten aus Alpaca und reichert sie mit Sektoren über Alpha Vantage an.
 */
public class PortfolioDataService {

    private final AlpacaService alpacaService = new AlpacaService();

    //Alpha Key Debug
    private void debugApiKey() {
        String Alphaenv = System.getenv("ALPHAVANTAGE_API_KEY");
        String Alphaprop = System.getProperty("ALPHAVANTAGE_API_KEY");
        System.out.println("[DEBUG] getenv  ALPHAVANTAGE_API_KEY = '" + Alphaenv + "'");
        System.out.println("[DEBUG] getprop ALPHAVANTAGE_API_KEY = '" + Alphaprop + "'");

        String Alpacaenv = System.getenv("ALPACA_API_KEY");
        String Alpacaprop = System.getProperty("ALPACA_API_KEY");
        System.out.println("[DEBUG] getenv  ALPACA_API_KEY = '" + Alpacaenv + "'");
        System.out.println("[DEBUG] getprop ALPACA_API_KEY = '" + Alpacaprop + "'");


    }

    public static void debugPrintEnv() {
        System.out.println("===== DEBUG: Environment Variables =====");

        try {
            for (var entry : System.getenv().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                // Sicherheits-Check: KEINE Secrets ausgeben!
                if (key.toLowerCase().contains("secret") ||
                        key.toLowerCase().contains("password") ||
                        key.toLowerCase().contains("token") ||
                        key.toLowerCase().contains("key")) {

                    System.out.println(key + " = ********** (hidden)");
                } else {
                    System.out.println(key + " = " + value);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error while reading environment variables: " + ex.getMessage());
        }

        System.out.println("========================================");
    }

    /**
     * Lädt die aktuellen Positionen aus Alpaca und mappt sie auf das Position-Model.
     * Anschließend werden die Sektoren pro Symbol über Alpha Vantage (OVERVIEW) nachgezogen.
     *
     * @param userId aktuell noch nicht verwendet – der Alpaca-Account ist durch API-Key/Secret definiert.
     */
    public List<Position> loadCurrentPositions(String userId) {
        List<Position> result = new ArrayList<>();

        try {
            // 1) JSON von Alpaca holen (wie im PositionsServlet)
            String json = alpacaService.getPositions();
            JsonElement root = JsonParser.parseString(json);
            JsonArray arr = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();

            // 2) Jede Position aus dem JSON in ein Position-Objekt mappen
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject pos = el.getAsJsonObject();

                String symbol = getString(pos, "symbol");
                String qtyStr = getString(pos, "qty");
                String marketValueStr = getString(pos, "market_value");
                String unrealizedPlStr = getString(pos, "unrealized_pl");

                BigDecimal quantity = toBigDecimal(qtyStr);
                BigDecimal marketValue = toBigDecimal(marketValueStr);
                BigDecimal unrealizedPnl = toBigDecimal(unrealizedPlStr);

                Position p = new Position();
                p.setSymbol(symbol);

                // Alpaca liefert im Positions-Endpoint keinen "Name" – Symbol als Fallback
                p.setName(symbol);

                // Sector zunächst "unknown" – wird (falls möglich) über Alpha Vantage gesetzt
                p.setSector("unknown");

                p.setQuantity(quantity);
                p.setMarketValue(marketValue);
                p.setUnrealizedPnl(unrealizedPnl);

                // Volatilität aktuell noch nicht vorhanden – später evtl. über ML/Marktdaten
                p.setVolatility(BigDecimal.ZERO);

                result.add(p);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // 3) Sektoren über Alpha Vantage nachziehen (wenn möglich)
        enrichSectorsFromAlphaVantage(result);

        return result;
    }

    // --------------------------------------------------------------------
    // Alpha Vantage – Sektoren
    // --------------------------------------------------------------------

    /**
     * Holt für jede Position den Sektor von Alpha Vantage und schreibt ihn in Position.sector.
     * Wenn ALPHAVANTAGE_API_KEY nicht gesetzt ist oder es Fehler gibt, bleiben die vorhandenen
     * Sektoren (z.B. "unknown") bestehen.
     */
    private void enrichSectorsFromAlphaVantage(List<Position> positions) {
        final String apiKey = System.getenv("ALPHAVANTAGE_API_KEY");
        debugApiKey();
        debugPrintEnv();
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("[PortfolioDataService] ALPHAVANTAGE_API_KEY ist nicht gesetzt : Sektor-Anreicherung wird Uebersprungen.");
            return;
        }


        for (Position p : positions) {
            String symbol = p.getSymbol();
            if (symbol == null || symbol.isBlank()) {
                continue;
            }

            try {
                String sector = fetchSectorFromAlphaVantage(symbol, apiKey);
                if (sector != null && !sector.isBlank()) {
                    System.out.println("[PortfolioDataService] AlphaVantage-Sektor für " + symbol + ": " + sector);
                    p.setSector(sector);
                } else {
                    System.out.println("[PortfolioDataService] AlphaVantage lieferte keinen Sektor für " + symbol);
                }
            } catch (Exception e) {
                System.err.println("[PortfolioDataService] AlphaVantage-Sektorabruf fehlgeschlagen für " + symbol + ": " + e.getMessage());
            }
        }
    }

    /**
     * Ruft das Company-Overview bei Alpha Vantage ab und gibt das Feld "Sector" zurück.
     * <p>
     * GET https://www.alphavantage.co/query?function=OVERVIEW&symbol=SYMBOL&apikey=KEY
     */
    private String fetchSectorFromAlphaVantage(String symbol, String apiKey) throws Exception {
        String urlStr = "https://www.alphavantage.co/query"
                + "?function=OVERVIEW"
                + "&symbol=" + symbol
                + "&apikey=" + apiKey;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new RuntimeException("AlphaVantage HTTP " + status + " für Symbol " + symbol);
            }

            try (InputStream in = conn.getInputStream();
                 InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

                JsonElement root = JsonParser.parseReader(reader);
                if (!root.isJsonObject()) {
                    return null;
                }
                JsonObject obj = root.getAsJsonObject();

                // Wenn ein Fehler zurückkommt (z.B. Rate Limit), ist meist "Note" oder "Information" gesetzt
                if (obj.has("Note") || obj.has("Information")) {
                    String note = obj.has("Note") ? obj.get("Note").getAsString() : obj.get("Information").getAsString();
                    System.err.println("[PortfolioDataService] AlphaVantage Hinweis für " + symbol + ": " + note);
                    return null;
                }

                if (obj.has("Sector") && !obj.get("Sector").isJsonNull()) {
                    return obj.get("Sector").getAsString();
                }
            }

            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // --------------------------------------------------------------------
    // Hilfsfunktionen
    // --------------------------------------------------------------------

    private String getString(JsonObject obj, String field) {
        if (obj.has(field) && !obj.get(field).isJsonNull()) {
            return obj.get(field).getAsString();
        }
        return "";
    }

    private BigDecimal toBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }
}
