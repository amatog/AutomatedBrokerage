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

@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {

            // --- Daten holen ---

            // 1) Account (Cash / Portfolio-Wert)
            String accountJson = service.getAccount();
            JsonObject account = JsonParser.parseString(accountJson).getAsJsonObject();
            String cash = account.get("cash").getAsString();
            String portfolioValue = account.get("portfolio_value").getAsString();

            // 2) Offene Orders
            String openOrdersJson = service.getOpenOrders();
            JsonArray openOrders = JsonParser.parseString(openOrdersJson).getAsJsonArray();

            // 3) Letzte 5 Fills (Transaktionen)
            String fillsJson = service.getLastFills(5);
            JsonElement fillsRoot = JsonParser.parseString(fillsJson);
            JsonArray fills = fillsRoot.isJsonArray() ? fillsRoot.getAsJsonArray() : new JsonArray();

            // 4) NASDAQ (QQQ) & Dow (DIA) – letzte Trades
            String nasdaqJson = service.getLastTrade("QQQ");
            String dowJson = service.getLastTrade("DIA");

            JsonObject nasdaqObj = JsonParser.parseString(nasdaqJson).getAsJsonObject();
            JsonObject dowObj = JsonParser.parseString(dowJson).getAsJsonObject();

            JsonObject nasdaqTrade = nasdaqObj.getAsJsonObject("trade");
            JsonObject dowTrade = dowObj.getAsJsonObject("trade");

            double nasdaqPrice = nasdaqTrade.get("p").getAsDouble();
            String nasdaqTime = nasdaqTrade.get("t").getAsString();

            double dowPrice = dowTrade.get("p").getAsDouble();
            String dowTime = dowTrade.get("t").getAsString();

            // --- HTML AUSGABE ---

            out.println("<!DOCTYPE html>");
            out.println("<html><head><meta charset='UTF-8'><title>myBrokerApp Dashboard</title>");

            // Moderner Dark-Mode Style
            out.println("<style>");
            out.println("body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }");
            out.println("h1, h2 { color: #58a6ff; }");
            out.println("a { color: #58a6ff; text-decoration: none; font-weight: 500; }");
            out.println("a:hover { text-decoration: underline; }");

            out.println(".header { display:flex; align-items:center; justify-content:space-between; margin-bottom:20px; }");
            out.println(".header-title { font-size: 1.8em; }");
            out.println(".header-links a { margin-left: 12px; }");

            out.println(".grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 24px; }");

            out.println(".card {");
            out.println("  background: rgba(255,255,255,0.06);");
            out.println("  backdrop-filter: blur(6px);");
            out.println("  border: 1px solid rgba(255,255,255,0.12);");
            out.println("  border-radius: 12px;");
            out.println("  padding: 18px 20px;");
            out.println("  box-shadow: 0 0 15px rgba(0,255,255,0.07);");
            out.println("  transition: transform 0.2s ease, box-shadow 0.2s ease;");
            out.println("}");
            out.println(".card:hover { transform: translateY(-3px); box-shadow: 0 0 18px rgba(88,166,255,0.45); }");

            out.println("table { width: 100%; border-collapse: collapse; margin-top: 8px; }");
            out.println("th, td { padding: 8px 10px; text-align: left; font-size: 0.9em; }");
            out.println("th { background: #161b22; color: #79c0ff; }");
            out.println("tr:nth-child(even) { background: #161b22; }");
            out.println("tr:nth-child(odd) { background: #0f141a; }");

            out.println(".tag { padding: 4px 8px; border-radius: 6px; font-size: 0.8em; text-transform: uppercase; font-weight: 600; }");
            out.println(".tag-buy { background:#238636; color:#ffffff; }");
            out.println(".tag-sell { background:#da3633; color:#ffffff; }");

            out.println("</style>");

            out.println("</head><body>");

            // Header
            out.println("<div class='header'>");
            out.println("<div class='header-title'>myBrokerApp Dashboard</div>");
            out.println("<div class='header-links'>");
            out.println("<a href='index.html'>Home</a>");
            out.println("<a href='orders'>Neue Order</a>");
            out.println("<a href='positions'>Positionen</a>");
            out.println("</div>");
            out.println("</div>");

            out.println("<div class='grid'>");

            // Karte: Konto
            out.println("<div class='card'>");
            out.println("<h2>Konto</h2>");
            out.println("<p><strong>Cash:</strong> " + cash + "</p>");
            out.println("<p><strong>Portfolio-Wert:</strong> " + portfolioValue + "</p>");
            out.println("</div>");

            // Karte: Märkte (NASDAQ / Dow)
            out.println("<div class='card'>");
            out.println("<h2>Marktindikatoren</h2>");
            out.println("<p><strong>NASDAQ (QQQ):</strong> " + nasdaqPrice + " <small>(Letzter Trade: " + nasdaqTime + ")</small></p>");
            out.println("<p><strong>Dow Jones (DIA):</strong> " + dowPrice + " <small>(Letzter Trade: " + dowTime + ")</small></p>");
            out.println("</div>");

            // Karte: Offene Orders
            out.println("<div class='card'>");
            out.println("<h2>Offene Orders</h2>");
            if (openOrders.size() == 0) {
                out.println("<p>Keine offenen Orders.</p>");
            } else {
                out.println("<table>");
                out.println("<tr><th>Symbol</th><th>Side</th><th>Menge</th><th>Status</th><th>Erstellt</th></tr>");
                for (JsonElement el : openOrders) {
                    JsonObject ord = el.getAsJsonObject();
                    String symbol = ord.get("symbol").getAsString();
                    String side = ord.get("side").getAsString();
                    String qty = ord.get("qty").getAsString();
                    String status = ord.get("status").getAsString();
                    String createdAt = ord.get("created_at").getAsString();

                    String sideTag = "<span class='tag " +
                            ("buy".equalsIgnoreCase(side) ? "tag-buy" : "tag-sell") +
                            "'>" + side + "</span>";

                    out.println("<tr>");
                    out.println("<td>" + symbol + "</td>");
                    out.println("<td>" + sideTag + "</td>");
                    out.println("<td>" + qty + "</td>");
                    out.println("<td>" + status + "</td>");
                    out.println("<td>" + createdAt + "</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
            out.println("</div>");

            // Karte: Letzte 5 Transaktionen
            out.println("<div class='card'>");
            out.println("<h2>Letzte 5 Transaktionen (Fills)</h2>");
            if (fills.size() == 0) {
                out.println("<p>Keine Fills gefunden.</p>");
            } else {
                out.println("<table>");
                out.println("<tr><th>Symbol</th><th>Side</th><th>Menge</th><th>Preis</th><th>Zeit</th></tr>");
                for (JsonElement el : fills) {
                    JsonObject f = el.getAsJsonObject();
                    String symbol = f.get("symbol").getAsString();
                    String side = f.get("side").getAsString();
                    String qty = f.get("qty").getAsString();
                    String price = f.get("price").getAsString();
                    String time = f.get("transaction_time").getAsString();

                    String sideTag = "<span class='tag " +
                            ("buy".equalsIgnoreCase(side) ? "tag-buy" : "tag-sell") +
                            "'>" + side + "</span>";

                    out.println("<tr>");
                    out.println("<td>" + symbol + "</td>");
                    out.println("<td>" + sideTag + "</td>");
                    out.println("<td>" + qty + "</td>");
                    out.println("<td>" + price + "</td>");
                    out.println("<td>" + time + "</td>");
                    out.println("</tr>");
                }
                out.println("</table>");
            }
            out.println("</div>");

            out.println("</div>"); // .grid

            out.println("</body></html>");

        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.println("Fehler im Dashboard: " + ex.getMessage());
            }
        }
    }
}
