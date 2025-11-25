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

    private static final String DEFAULT_PLACEHOLDER = "-";

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            JsonObject account = parseJsonObject(service.getAccount());
            req.setAttribute("cash", readString(account, "cash", DEFAULT_PLACEHOLDER));
            req.setAttribute("portfolioValue", readString(account, "portfolio_value", DEFAULT_PLACEHOLDER));

            JsonArray openOrders = parseJsonArray(service.getOpenOrders());
            List<Map<String, String>> openOrderViews = new ArrayList<>();
            for (JsonElement element : openOrders) {
                JsonObject order = asObject(element);
                if (order != null) {
                    openOrderViews.add(buildOpenOrderView(order));
                }
            }
            req.setAttribute("openOrders", openOrderViews);

            JsonArray fills = parseFills(service.getLastFills(5));
            List<Map<String, String>> fillViews = new ArrayList<>();
            for (JsonElement element : fills) {
                fillViews.add(buildFillView(element));
            }
            req.setAttribute("fills", fillViews);

            JsonObject nasdaqTrade = readTrade(service.getLastTrade("QQQ"));
            JsonObject dowTrade = readTrade(service.getLastTrade("DIA"));

            Map<String, Object> markets = new HashMap<>();
            markets.put("nasdaqPrice", readDouble(nasdaqTrade, "p", 0.0));
            markets.put("nasdaqTime", readString(nasdaqTrade, "t", DEFAULT_PLACEHOLDER));
            markets.put("dowPrice", readDouble(dowTrade, "p", 0.0));
            markets.put("dowTime", readString(dowTrade, "t", DEFAULT_PLACEHOLDER));
            req.setAttribute("markets", markets);

            req.setAttribute("accountJson", account.toString());
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/dashboard.jsp");
            dispatcher.forward(req, resp);
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorMessage", "Fehler beim Laden des Dashboards: " + ex.getMessage());
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(req, resp);
        }
    }

    private JsonObject parseJsonObject(String json) {
        try {
            JsonElement parsed = JsonParser.parseString(json == null ? "{}" : json);
            return parsed.isJsonObject() ? parsed.getAsJsonObject() : new JsonObject();
        } catch (Exception ex) {
            return new JsonObject();
        }
    }

    private JsonArray parseJsonArray(String json) {
        JsonElement parsed = JsonParser.parseString(json == null ? "[]" : json);
        return parsed.isJsonArray() ? parsed.getAsJsonArray() : new JsonArray();
    }

    private JsonObject asObject(JsonElement element) {
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private JsonArray parseFills(String fillsJson) {
        JsonElement parsed = JsonParser.parseString(fillsJson == null ? "[]" : fillsJson);
        if (parsed.isJsonArray()) {
            return parsed.getAsJsonArray();
        }
        if (parsed.isJsonObject() && parsed.getAsJsonObject().has("fills")) {
            JsonElement fillsElement = parsed.getAsJsonObject().get("fills");
            if (fillsElement != null && fillsElement.isJsonArray()) {
                return fillsElement.getAsJsonArray();
            }
        }
        return new JsonArray();
    }

    private Map<String, String> buildOpenOrderView(JsonObject order) {
        Map<String, String> view = new HashMap<>();
        view.put("symbol", readString(order, "symbol", DEFAULT_PLACEHOLDER));
        view.put("side", readString(order, "side", DEFAULT_PLACEHOLDER));
        view.put("qty", readString(order, "qty", DEFAULT_PLACEHOLDER));
        view.put("status", readString(order, "status", DEFAULT_PLACEHOLDER));
        view.put("createdAt", readString(order, "created_at", DEFAULT_PLACEHOLDER));
        return view;
    }

    private Map<String, String> buildFillView(JsonElement fillElement) {
        JsonObject fill = asObject(fillElement);
        Map<String, String> view = new HashMap<>();
        if (fill == null) {
            view.put("symbol", DEFAULT_PLACEHOLDER);
            view.put("qty", DEFAULT_PLACEHOLDER);
            view.put("price", DEFAULT_PLACEHOLDER);
            view.put("side", DEFAULT_PLACEHOLDER);
            view.put("timestamp", DEFAULT_PLACEHOLDER);
            return view;
        }

        JsonObject fillData = fill.has("fill_data") && fill.get("fill_data").isJsonObject()
                ? fill.getAsJsonObject("fill_data")
                : null;
        JsonObject symbolObj = fill.has("symbol") && fill.get("symbol").isJsonObject()
                ? fill.getAsJsonObject("symbol")
                : null;

        view.put("symbol", readString(symbolObj, "symbol", readString(fill, "symbol", DEFAULT_PLACEHOLDER)));
        view.put("qty", readString(fillData, "qty", readString(fill, "qty", DEFAULT_PLACEHOLDER)));
        view.put("price", readString(fillData, "price", readString(fill, "price", DEFAULT_PLACEHOLDER)));
        view.put("side", readString(fillData, "side", readString(fill, "side", DEFAULT_PLACEHOLDER)));
        view.put("timestamp", readString(fillData, "timestamp",
                readString(fill, "transaction_time", readString(fill, "timestamp", DEFAULT_PLACEHOLDER))));
        return view;
    }

    private JsonObject readTrade(String json) {
        JsonObject parsed = parseJsonObject(json);
        JsonElement tradeElement = parsed.get("trade");
        if (tradeElement != null && tradeElement.isJsonObject()) {
            return tradeElement.getAsJsonObject();
        }
        if (parsed.has("p") || parsed.has("t")) {
            return parsed;
        }
        return new JsonObject();
    }

    private String readString(JsonObject obj, String memberName, String defaultValue) {
        if (obj == null || memberName == null) {
            return defaultValue;
        }
        if (!obj.has(memberName)) {
            return defaultValue;
        }

        JsonElement element = obj.get(memberName);
        if (element != null && !element.isJsonNull()) {
            if (element.isJsonPrimitive()) {
                return element.getAsString();
            }
            if (element.isJsonObject() && element.getAsJsonObject().has("value")) {
                JsonElement valueElement = element.getAsJsonObject().get("value");
                if (valueElement != null && valueElement.isJsonPrimitive()) {
                    return valueElement.getAsString();
                }
            }
        }
        return defaultValue;
    }

    private double readDouble(JsonObject obj, String memberName, double defaultValue) {
        if (obj == null || memberName == null) {
            return defaultValue;
        }
        if (!obj.has(memberName)) {
            return defaultValue;
        }

        JsonElement element = obj.get(memberName);
        if (element != null && element.isJsonPrimitive()) {
            try {
                return element.getAsDouble();
            } catch (Exception ignored) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
