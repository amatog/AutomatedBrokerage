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

@WebServlet(name = "DashboardServlet", urlPatterns = "/dashboard")
public class DashboardServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            String accountJson = service.getAccount();
            JsonObject account = JsonParser.parseString(accountJson).getAsJsonObject();
            req.setAttribute("cash", account.get("cash").getAsString());
            req.setAttribute("portfolioValue", account.get("portfolio_value").getAsString());

            String openOrdersJson = service.getOpenOrders();
            JsonArray openOrders = JsonParser.parseString(openOrdersJson).getAsJsonArray();
            List<Map<String, String>> openOrderViews = new ArrayList<>();
            for (JsonElement el : openOrders) {
                JsonObject ord = el.getAsJsonObject();
                Map<String, String> view = new HashMap<>();
                view.put("symbol", ord.get("symbol").getAsString());
                view.put("side", ord.get("side").getAsString());
                view.put("qty", ord.get("qty").getAsString());
                view.put("status", ord.get("status").getAsString());
                view.put("createdAt", ord.get("created_at").getAsString());
                openOrderViews.add(view);
            }
            req.setAttribute("openOrders", openOrderViews);

            String fillsJson = service.getLastFills(5);
            JsonElement fillsRoot = JsonParser.parseString(fillsJson);
            JsonArray fills = fillsRoot.isJsonArray() ? fillsRoot.getAsJsonArray() : new JsonArray();
            List<Map<String, String>> fillViews = new ArrayList<>();
            for (JsonElement el : fills) {
                JsonObject fill = el.getAsJsonObject();
                JsonObject fillData = fill.getAsJsonObject("fill_data");
                JsonObject fillSymbol = fill.getAsJsonObject("symbol");

                Map<String, String> view = new HashMap<>();
                view.put("symbol", fillSymbol.get("symbol").getAsString());
                view.put("qty", fillData.get("qty").getAsString());
                view.put("price", fillData.get("price").getAsString());
                view.put("side", fillData.get("side").getAsString());
                view.put("timestamp", fillData.get("timestamp").getAsString());
                fillViews.add(view);
            }
            req.setAttribute("fills", fillViews);

            String nasdaqJson = service.getLastTrade("QQQ");
            String dowJson = service.getLastTrade("DIA");

            JsonObject nasdaqTrade = JsonParser.parseString(nasdaqJson).getAsJsonObject().getAsJsonObject("trade");
            JsonObject dowTrade = JsonParser.parseString(dowJson).getAsJsonObject().getAsJsonObject("trade");

            Map<String, Object> markets = new HashMap<>();
            markets.put("nasdaqPrice", nasdaqTrade.get("p").getAsDouble());
            markets.put("nasdaqTime", nasdaqTrade.get("t").getAsString());
            markets.put("dowPrice", dowTrade.get("p").getAsDouble());
            markets.put("dowTime", dowTrade.get("t").getAsString());
            req.setAttribute("markets", markets);

            req.setAttribute("accountJson", accountJson);
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/dashboard.jsp");
            dispatcher.forward(req, resp);
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorMessage", "Fehler beim Laden des Dashboards: " + ex.getMessage());
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(req, resp);
        }
    }
}
