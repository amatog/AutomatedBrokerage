package com.mybroker.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybroker.service.AlpacaService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "PositionsServlet", urlPatterns = "/positions")
public class PositionsServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {

            String json = service.getPositions();
            JsonElement root = JsonParser.parseString(json);
            JsonArray arr = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='de'><head><meta charset='UTF-8'><title>Offene Positionen - myBrokerApp</title>");

            // Dark-Style analog Dashboard / Index / Status
            out.println("<style>");
            out.println("body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }");
            out.println("a { color: #58a6ff; text-decoration: none; font-weight: 500; }");
            out.println("a:hover { text-decoration: underline; }");
            out.println(".container { max-width: 1000px; margin: 0 auto; }");
            out.println(".nav-top { margin-bottom: 16px; }");
            out.println(".nav-top a { margin-right: 12px; }");
            out.println(".title { font-size: 2em; color: #58a6ff; margin-bottom: 10px; }");
            out.println(".subtitle { color: #8b949e; margin-bottom: 20px; }");

            out.println(".card {");
            out.println("  background: rgba(255,255,255,0.06);");
            out.println("  backdrop-filter: blur(6px);");
            out.println("  border: 1px solid rgba(255,255,255,0.12);");
            out.println("  border-radius: 12px;");
            out.println("  padding: 18px 20px;");
            out.println("  box-shadow: 0 0 15px rgba(0,255,255,0.07);");
            out.println("  transition: transform 0.2s ease, box-shadow 0.2s ease;");
            out.println("  margin-bottom: 20px;");
            out.println("}");
            out.println(".card:hover { transform: translateY(-3px); box-shadow: 0 0 18px rgba(88,166,255,0.45); }");

            out.println("table { width: 100%; border-collapse: collapse; margin-top: 8px; }");
            out.println("th, td { padding: 8px 10px; text-align: left; font-size: 0.9em; }");
            out.println("th { background: #161b22; color: #79c0ff; }");
            out.println("tr:nth-child(even) { background: #161b22; }");
            out.println("tr:nth-child(odd) { background: #0f141a; }");
            out.println("</style>");

            out.println("</head><body>");
            out.println("<div class='container'>");

            // Nav
            out.println("<div class='nav-top'>");
            out.println("<a href='index.html'>Home</a>");
            out.println("<a href='dashboard'>Dashboard</a>");
            out.println("<a href='status'>Account Status</a>");
            out.println("<a href='orders'>Neue Order</a>");
            out.println("</div>");

            // Titel
            out.println("<div class='title'>Offene Positionen</div>");
            out.println("<div class='subtitle'>Aktuelle Positionen aus deinem Alpaca Paper-Trading-Konto</div>");

            out.println("<div class='card'>");
            if (arr.size() == 0) {
                out.println("<p>Keine offenen Positionen.</p>");
            } else {
                out.println("<table>");
                out.println("<tr>");
                out.println("<th>Symbol</th>");
                out.println("<th>Menge</th>");
                out.println("<th>Marktwert</th>");
                out.println("<th>Durchschnittskurs</th>");
                out.println("<th>Unrealisierter P&amp;L</th>");
                out.println("</tr>");

                for (JsonElement el : arr) {
                    JsonObject pos = el.getAsJsonObject();

                    String symbol = pos.get("symbol").getAsString();
                    String qty = pos.get("qty").getAsString();
                    String marketValue = pos.get("market_value").getAsString();
                    String avgEntryPrice = pos.get("avg_entry_price").getAsString();
                    String unrealizedPl = pos.get("unrealized_pl").getAsString();

                    out.println("<tr>");
                    out.println("<td>" + symbol + "</td>");
                    out.println("<td>" + qty + "</td>");
                    out.println("<td>" + marketValue + "</td>");
                    out.println("<td>" + avgEntryPrice + "</td>");
                    out.println("<td>" + unrealizedPl + "</td>");
                    out.println("</tr>");
                }

                out.println("</table>");
            }
            out.println("</div>");

            // Optional: Roh-JSON
            out.println("<div class='card'>");
            out.println("<h2>Rohes JSON (Debug)</h2>");
            out.println("<pre style='white-space: pre-wrap; font-size: 0.85em; color:#c9d1d9;'>" + json + "</pre>");
            out.println("</div>");

            out.println("</div>"); // container
            out.println("</body></html>");

        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.println("Fehler beim Laden der Positionen: " + ex.getMessage());
            }
        }
    }
}
