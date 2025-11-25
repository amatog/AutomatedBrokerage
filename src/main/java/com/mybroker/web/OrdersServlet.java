package com.mybroker.web;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mybroker.service.AlpacaService;

@WebServlet(name = "OrdersServlet", urlPatterns = "/orders")
public class OrdersServlet extends HttpServlet {

    private final AlpacaService service = new AlpacaService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/orders.jsp");
        dispatcher.forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String symbol = req.getParameter("symbol");
        String qtyStr = req.getParameter("qty");
        String side = req.getParameter("side");

        try {
            int qty = Integer.parseInt(qtyStr);

            String resultJson = service.createOrder(symbol, qty, side);
            JsonObject jsonObj = JsonParser.parseString(resultJson).getAsJsonObject();

            Map<String, String> order = new HashMap<>();
            order.put("id", jsonObj.get("id").getAsString());
            order.put("status", jsonObj.get("status").getAsString());
            order.put("symbol", symbol);
            order.put("requestedQty", String.valueOf(qty));
            order.put("filledQty", jsonObj.get("filled_qty").getAsString());
            order.put("type", jsonObj.get("type").getAsString());
            order.put("tif", jsonObj.get("time_in_force").getAsString());
            order.put("createdAt", jsonObj.get("created_at").getAsString());

            req.setAttribute("order", order);
            req.setAttribute("rawJson", resultJson);
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/order-result.jsp");
            dispatcher.forward(req, resp);
        } catch (Exception ex) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            req.setAttribute("errorMessage", "Fehler beim Senden der Order: " + ex.getMessage());
            RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/error.jsp");
            dispatcher.forward(req, resp);
        }
    }
}
