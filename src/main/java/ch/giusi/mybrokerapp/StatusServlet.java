package ch.giusi.mybrokerapp;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "StatusServlet", urlPatterns = "/status")
public class StatusServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {

            String json = service.getAccount();
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            String id = obj.get("id").getAsString();
            String status = obj.get("status").getAsString();
            String currency = obj.get("currency").getAsString();
            String cash = obj.get("cash").getAsString();
            String portfolioValue = obj.get("portfolio_value").getAsString();
            String buyingPower = obj.get("buying_power").getAsString();
            String createdAt = obj.get("created_at").getAsString();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='de'><head><meta charset='UTF-8'><title>Account Status - myBrokerApp</title>");

            // Dark-Theme Style analog Dashboard / Index
            out.println("<style>");
            out.println("body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }");
            out.println("a { color: #58a6ff; text-decoration: none; font-weight: 500; }");
            out.println("a:hover { text-decoration: underline; }");
            out.println(".container { max-width: 900px; margin: 0 auto; }");
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
            out.println("th, td { padding: 8px 10px; text-align: left; font-size: 0.95em; }");
            out.println("th { background: #161b22; color: #79c0ff; }");
            out.println("tr:nth-child(even) { background: #161b22; }");
            out.println("tr:nth-child(odd) { background: #0f141a; }");

            out.println("</style>");

            out.println("</head><body>");
            out.println("<div class='container'>");

            // Navigation
            out.println("<div class='nav-top'>");
            out.println("<a href='index.html'>Home</a>");
            out.println("<a href='dashboard'>Dashboard</a>");
            out.println("<a href='positions'>Positionen</a>");
            out.println("<a href='orders'>Neue Order</a>");
            out.println("</div>");

            // Überschrift
            out.println("<div class='title'>Account Status</div>");
            out.println("<div class='subtitle'>Alpaca Paper Trading Konto-Details</div>");

            // Karte: Zusammenfassung
            out.println("<div class='card'>");
            out.println("<h2>Zusammenfassung</h2>");
            out.println("<table>");
            out.println("<tr><th>Feld</th><th>Wert</th></tr>");
            out.println("<tr><td>Account-ID</td><td>" + id + "</td></tr>");
            out.println("<tr><td>Status</td><td>" + status + "</td></tr>");
            out.println("<tr><td>Währung</td><td>" + currency + "</td></tr>");
            out.println("<tr><td>Cash</td><td>" + cash + "</td></tr>");
            out.println("<tr><td>Portfolio-Wert</td><td>" + portfolioValue + "</td></tr>");
            out.println("<tr><td>Buying Power</td><td>" + buyingPower + "</td></tr>");
            out.println("<tr><td>Erstellt am</td><td>" + createdAt + "</td></tr>");
            out.println("</table>");
            out.println("</div>");

            // Rohes JSON optional (Debug-Bereich)
            out.println("<div class='card'>");
            out.println("<h2>Rohes JSON (Debug)</h2>");
            out.println("<pre style='white-space: pre-wrap; font-size: 0.85em; color:#c9d1d9;'>" + json + "</pre>");
            out.println("</div>");

            out.println("</div>"); // container
            out.println("</body></html>");

        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.println("Fehler beim Zugriff auf Alpaca API:");
                out.println(ex.getMessage());
            }
        }
    }
}