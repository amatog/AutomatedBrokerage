package com.mybroker.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "PositionsServlet", urlPatterns = "/positions")
public class PositionsServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String json = service.getPositions();
            JsonElement root = JsonParser.parseString(json);
            JsonArray arr = root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();

            List<Map<String, String>> positions = new ArrayList<>();
            for (JsonElement el : arr) {
                JsonObject pos = el.getAsJsonObject();
                Map<String, String> view = new HashMap<>();
                view.put("symbol", pos.get("symbol").getAsString());
                view.put("qty", pos.get("qty").getAsString());
                view.put("marketValue", pos.get("market_value").getAsString());
                view.put("avgEntryPrice", pos.get("avg_entry_price").getAsString());
                view.put("unrealizedPl", pos.get("unrealized_pl").getAsString());
                positions.add(view);
            }

            req.setAttribute("positions", positions);
            req.setAttribute("rawJson", json);

            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/positions.jsp");
            dispatcher.forward(req, resp);
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorMessage", "Fehler beim Laden der Positionen: " + ex.getMessage());
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(req, resp);
        }
    }
}
