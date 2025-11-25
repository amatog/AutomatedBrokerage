package com.mybroker.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybroker.service.AlpacaService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "StatusServlet", urlPatterns = "/status")
public class StatusServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String json = service.getAccount();
            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();

            Map<String, String> account = new HashMap<>();
            account.put("id", obj.get("id").getAsString());
            account.put("status", obj.get("status").getAsString());
            account.put("currency", obj.get("currency").getAsString());
            account.put("cash", obj.get("cash").getAsString());
            account.put("portfolioValue", obj.get("portfolio_value").getAsString());
            account.put("buyingPower", obj.get("buying_power").getAsString());
            account.put("createdAt", obj.get("created_at").getAsString());

            req.setAttribute("account", account);
            req.setAttribute("rawJson", json);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/status.jsp");
            dispatcher.forward(req, resp);
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorMessage", "Fehler beim Zugriff auf Alpaca API: " + ex.getMessage());
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(req, resp);
        }
    }
}
