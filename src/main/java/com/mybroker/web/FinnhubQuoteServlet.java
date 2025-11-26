package com.mybroker.web;

import com.mybroker.service.FinnhubClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet that exposes a simple endpoint to fetch real-time quotes from Finnhub.
 * <p>
 * Example:
 * GET /BrokerApp/finnhub/quote?symbol=AAPL
 * <p>
 * Response:
 * Finnhub JSON from /quote endpoint, e.g.:
 * {
 * "c": 261.74,  // Current price
 * "h": 263.31,  // High price of the day
 * "l": 260.68,  // Low price of the day
 * "o": 261.07,  // Open price of the day
 * "pc": 259.45, // Previous close price
 * "t": 1582641000
 * }
 */
@WebServlet(name = "FinnhubQuoteServlet", urlPatterns = {"/finnhub/quote"})
public class FinnhubQuoteServlet extends HttpServlet {

    private FinnhubClient finnhubClient;

    @Override
    public void init() throws ServletException {
        super.init();

        // Option 1: from environment variable (recommended)
        String apiKey = System.getenv("FINNHUB_API_KEY");

        // Option 2: from web.xml context-param (fallback)
        if (apiKey == null || apiKey.isEmpty()) {
            apiKey = getServletContext().getInitParameter("FINNHUB_API_KEY");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new ServletException("Finnhub API key is not configured. " +
                    "Set FINNHUB_API_KEY as environment variable or context-param.");
        }

        this.finnhubClient = new FinnhubClient(apiKey);
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        String symbol = req.getParameter("symbol");

        resp.setContentType("application/json;charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");

        try (PrintWriter out = resp.getWriter()) {

            if (symbol == null || symbol.trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.write("{\"error\":\"Missing 'symbol' query parameter\"}");
                return;
            }

            try {
                String json = finnhubClient.getQuoteRaw(symbol.trim());
                resp.setStatus(HttpServletResponse.SC_OK);
                out.write(json);
            } catch (IOException e) {
                // Log server-side
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
                out.write("{\"error\":\"Failed to fetch data from Finnhub\",\"details\":\""
                        + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    /**
     * Very basic JSON string escaper to avoid breaking JSON response.
     */
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
