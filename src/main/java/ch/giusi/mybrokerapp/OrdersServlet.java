package ch.giusi.mybrokerapp;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@WebServlet(name = "OrdersServlet", urlPatterns = "/orders")
public class OrdersServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        try (PrintWriter out = resp.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<html lang='de'><head><meta charset='UTF-8'><title>Neue Order - myBrokerApp</title>");

            // Dark-Style
            out.println("<style>");
            out.println("body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }");
            out.println("a { color: #58a6ff; text-decoration: none; font-weight: 500; }");
            out.println("a:hover { text-decoration: underline; }");
            out.println(".container { max-width: 700px; margin: 0 auto; }");
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

            out.println("label { display:block; margin-top: 10px; margin-bottom: 4px; }");
            out.println("input[type=text], input[type=number] {");
            out.println("  width: 100%; padding: 8px 10px; border-radius: 8px;");
            out.println("  border: 1px solid #30363d; background:#0d1117; color:#e6edf3;");
            out.println("}");
            out.println(".radio-group { margin-top: 10px; }");
            out.println(".btn { margin-top: 14px; padding: 8px 14px; border-radius: 8px; border: 1px solid #58a6ff; background:#58a6ff; color:#0d1117; font-weight: 500; cursor:pointer; }");
            out.println(".btn:hover { background:#1f6feb; border-color:#1f6feb; }");
            out.println("</style>");

            out.println("</head><body>");
            out.println("<div class='container'>");

            out.println("<div class='nav-top'>");
            out.println("<a href='index.html'>Home</a>");
            out.println("<a href='dashboard'>Dashboard</a>");
            out.println("<a href='status'>Account Status</a>");
            out.println("<a href='positions'>Positionen</a>");
            out.println("</div>");

            out.println("<div class='title'>Neue Order</div>");
            out.println("<div class='subtitle'>Erfasse eine neue Market-Order im Alpaca Paper-Trading Konto.</div>");

            out.println("<div class='card'>");
            out.println("<form method='post' action='orders'>");
            out.println("<label for='symbol'>Symbol</label>");
            out.println("<input type='text' id='symbol' name='symbol' value='AAPL' required>");

            out.println("<label for='qty'>Menge</label>");
            out.println("<input type='number' id='qty' name='qty' value='1' min='1' required>");

            out.println("<div class='radio-group'>");
            out.println("Side: ");
            out.println("<label><input type='radio' name='side' value='buy' checked> Buy</label>");
            out.println("<label><input type='radio' name='side' value='sell'> Sell</label>");
            out.println("</div>");

            out.println("<button type='submit' class='btn'>Order senden</button>");
            out.println("</form>");
            out.println("</div>");

            out.println("</div>"); // container
            out.println("</body></html>");

        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html;charset=UTF-8");

        String symbol = req.getParameter("symbol");
        String qtyStr = req.getParameter("qty");
        String side = req.getParameter("side");

        try (PrintWriter out = resp.getWriter()) {
            int qty = Integer.parseInt(qtyStr);

            String resultJson = service.createOrder(symbol, qty, side);

            JsonObject jsonObj = JsonParser.parseString(resultJson).getAsJsonObject();

            String orderId   = jsonObj.get("id").getAsString();
            String status    = jsonObj.get("status").getAsString();
            String createdAt = jsonObj.get("created_at").getAsString();
            String filledQty = jsonObj.get("filled_qty").getAsString();
            String type      = jsonObj.get("type").getAsString();
            String tif       = jsonObj.get("time_in_force").getAsString();

            out.println("<!DOCTYPE html>");
            out.println("<html lang='de'><head><meta charset='UTF-8'><title>Order Ergebnis - myBrokerApp</title>");

            // Dark-Style
            out.println("<style>");
            out.println("body { font-family: 'Segoe UI', Roboto, sans-serif; background: #0d1117; color: #e6edf3; margin: 0; padding: 20px; }");
            out.println("a { color: #58a6ff; text-decoration: none; font-weight: 500; }");
            out.println("a:hover { text-decoration: underline; }");
            out.println(".container { max-width: 800px; margin: 0 auto; }");
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

            out.println("<div class='nav-top'>");
            out.println("<a href='index.html'>Home</a>");
            out.println("<a href='dashboard'>Dashboard</a>");
            out.println("<a href='status'>Account Status</a>");
            out.println("<a href='positions'>Positionen</a>");
            out.println("</div>");

            out.println("<div class='title'>Order Ergebnis</div>");
            out.println("<div class='subtitle'>Zusammenfassung der gesendeten Order</div>");

            out.println("<div class='card'>");
            out.println("<h2>Order Details</h2>");
            out.println("<table>");
            out.println("<tr><th>Order-ID</th><td>" + orderId + "</td></tr>");
            out.println("<tr><th>Status</th><td>" + status + "</td></tr>");
            out.println("<tr><th>Symbol</th><td>" + symbol + "</td></tr>");
            out.println("<tr><th>Angeforderte Menge</th><td>" + qty + "</td></tr>");
            out.println("<tr><th>Gefüllte Menge</th><td>" + filledQty + "</td></tr>");
            out.println("<tr><th>Order-Typ</th><td>" + type + "</td></tr>");
            out.println("<tr><th>Time-in-Force</th><td>" + tif + "</td></tr>");
            out.println("<tr><th>Erstellt</th><td>" + createdAt + "</td></tr>");
            out.println("</table>");
            out.println("</div>");

            out.println("<div class='card'>");
            out.println("<h2>Rohes JSON (Debug)</h2>");
            out.println("<pre style='white-space: pre-wrap; font-size: 0.85em; color:#c9d1d9;'>" + resultJson + "</pre>");
            out.println("</div>");

            out.println("<div>");
            out.println("<a href='orders'>Weitere Order erstellen</a><br>");
            out.println("<a href='positions'>Positionen anzeigen</a><br>");
            out.println("<a href='dashboard'>Zum Dashboard</a>");
            out.println("</div>");

            out.println("</div>"); // container
            out.println("</body></html>");

        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = resp.getWriter()) {
                out.println("Fehler beim Senden der Order: " + ex.getMessage());
            }
        }
    }
}
