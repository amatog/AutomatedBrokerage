package com.mybroker.web;

import com.google.gson.JsonObject;
import com.mybroker.service.ValueServiceClient;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "ValueAnalysisServlet", urlPatterns = "/value-analysis")
public class ValueAnalysisServlet extends HttpServlet {

    private ValueServiceClient valueClient;

    @Override
    public void init() throws ServletException {
        String baseUrl = getServletContext().getInitParameter("valueServiceBaseUrl");
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "https://value.netdesign.ch/api";
        }
        this.valueClient = new ValueServiceClient(baseUrl);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String format = req.getParameter("format");
        String symbol = req.getParameter("symbol");

        if ("json".equalsIgnoreCase(format)) {
            handleJson(symbol, resp);
            return;
        }

        req.setAttribute("initialSymbol", symbol != null ? symbol.toUpperCase().trim() : "");
        RequestDispatcher rd = req.getRequestDispatcher("/jsp/value-analysis.jsp");
        rd.forward(req, resp);
    }

    private void handleJson(String symbol, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");

        if (symbol == null || symbol.isBlank()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Parameter 'symbol' ist erforderlich.\"}");
            return;
        }

        symbol = symbol.trim().toUpperCase();

        try {
            JsonObject json = valueClient.getScore(symbol);
            json.addProperty("symbol", symbol);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write(json.toString());
        } catch (IOException ex) {
            resp.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            String msg = ex.getMessage().replace("\"", "\\\"");
            resp.getWriter().write("{\"error\":\"Fehler beim Value-Service: " + msg + "\"}");
        }
    }
}
