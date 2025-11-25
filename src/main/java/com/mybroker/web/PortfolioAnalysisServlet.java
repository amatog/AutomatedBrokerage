package com.mybroker.web;

import com.mybroker.ai.OpenAiClient;
import com.mybroker.model.PortfolioAnalysisResult;
import com.mybroker.model.Position;
import com.mybroker.service.PortfolioAiAdvisor;
import com.mybroker.service.PortfolioAnalysisService;
import com.mybroker.service.PortfolioDataService;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "PortfolioAnalysisServlet", urlPatterns = {"/portfolio-analysis"})
public class PortfolioAnalysisServlet extends HttpServlet {

    private PortfolioDataService dataService;
    private PortfolioAnalysisService analysisService;
    private PortfolioAiAdvisor aiAdvisor;

    @Override
    public void init() throws ServletException {
        super.init();
        this.dataService = new PortfolioDataService();
        this.analysisService = new PortfolioAnalysisService();
        this.aiAdvisor = new PortfolioAiAdvisor(new OpenAiClient());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String userId = "demoUser";

        List<Position> positions = dataService.loadCurrentPositions(userId);
        PortfolioAnalysisResult analysis = analysisService.analyse(positions);

        String aiText = aiAdvisor.buildExplanation(analysis, positions);
        analysis.setAiExplanation(aiText);

        req.setAttribute("positions", positions);
        req.setAttribute("analysis", analysis);
        req.setAttribute("aiContent", extractAssistantContent(aiText));
        req.setAttribute("aiModel", extractJsonStringField(aiText, "\"model\""));
        req.setAttribute("aiTotalTokens", extractJsonNumberField(aiText, "\"total_tokens\""));
        req.setAttribute("aiCompletionTokens", extractJsonNumberField(aiText, "\"completion_tokens\""));
        req.setAttribute("aiServiceTier", extractJsonStringField(aiText, "\"service_tier\""));

        RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/portfolio-analysis.jsp");
        dispatcher.forward(req, resp);
    }

    private String extractAssistantContent(String json) {
        if (json == null || json.isEmpty()) {
            return "";
        }
        int idx = json.indexOf("\"content\"");
        if (idx < 0) return json;
        int colon = json.indexOf(':', idx);
        if (colon < 0) return json;
        int startQuote = json.indexOf('"', colon + 1);
        if (startQuote < 0) return json;
        int endQuote = json.indexOf('"', startQuote + 1);
        if (endQuote < 0) return json;
        return json.substring(startQuote + 1, endQuote);
    }

    private String extractJsonStringField(String json, String fieldName) {
        if (json == null) return null;
        int pos = json.indexOf(fieldName);
        if (pos < 0) return null;
        int colon = json.indexOf(':', pos);
        if (colon < 0) return null;
        int firstQuote = json.indexOf('"', colon + 1);
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (firstQuote < 0 || secondQuote < 0) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }

    private String extractJsonNumberField(String json, String fieldName) {
        if (json == null) return null;
        int pos = json.indexOf(fieldName);
        if (pos < 0) return null;
        int colon = json.indexOf(':', pos);
        if (colon < 0) return null;
        int comma = json.indexOf(',', colon + 1);
        String number = comma < 0 ? json.substring(colon + 1).trim() : json.substring(colon + 1, comma).trim();
        return number.replaceAll("[^0-9]", "");
    }

}
