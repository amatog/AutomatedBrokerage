package com.mybroker.web;

import com.google.gson.JsonObject;
import com.mybroker.ml.MlServiceClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Servlet zum Anstoßen des Trainings des Risiko-Modells im ML-Service.
 * <p>
 * URL: /ml-train
 * <p>
 * Aufrufbar per:
 * - GET  (z.B. direkt im Browser)
 * - POST (z.B. von einem Button / Formular im Dashboard)
 */
@WebServlet(name = "MlTrainServlet", urlPatterns = {"/ml-train"})
public class MlTrainServlet extends HttpServlet {

    private MlServiceClient mlClient;

    @Override
    public void init() throws ServletException {
        super.init();
        // ML-Client initialisieren (liest ML_SERVICE_BASE_URL oder nutzt localhost:8000)
        this.mlClient = new MlServiceClient();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Für einfache Tests im Browser GET erlauben
        handleTrainRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Für echte Integration z.B. ein POST vom Dashboard
        handleTrainRequest(req, resp);
    }

    /**
     * Gemeinsame Logik für GET/POST:
     * Ruft den ML-Service auf, um das Risiko-Modell neu zu trainieren,
     * und rendert ein simples HTML-Ergebnis.
     */
    private void handleTrainRequest(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("text/html; charset=UTF-8");
        PrintWriter out = resp.getWriter();

        JsonObject result = null;
        String errorMessage = null;

        try {
            // Annahme: MlServiceClient hat eine Methode trainRiskModel(),
            // die den Endpoint (z.B. POST /train-risk-model) aufruft
            result = mlClient.trainRiskModel();
        } catch (Exception e) {
            errorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            e.printStackTrace();
        }

        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"de\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\" />");
        out.println("  <title>ML-Training – Risiko-Modell</title>");
        out.println("  <style>");
        out.println("    body { font-family: Arial, sans-serif; margin: 2rem; background:#f5f5f5; }");
        out.println("    .card { background:white; padding:1.5rem 2rem; border-radius:8px; box-shadow:0 2px 6px rgba(0,0,0,0.1); }");
        out.println("    .status-ok { color: #1b7e35; font-weight:bold; }");
        out.println("    .status-error { color: #b00020; font-weight:bold; }");
        out.println("    .meta { margin-top: 1rem; font-size: 0.9rem; color:#555; }");
        out.println("    .btn { display:inline-block; margin-top:1.5rem; padding:0.5rem 1rem; border-radius:4px; border:none; background:#1976d2; color:white; text-decoration:none; }");
        out.println("    .btn:hover { background:#115293; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<div class=\"card\">");
        out.println("  <h1>ML-Training: Risiko-Modell</h1>");

        if (errorMessage != null) {
            out.println("  <p class=\"status-error\">Fehler beim Aufruf des ML-Services:</p>");
            out.println("  <pre>" + escapeHtml(errorMessage) + "</pre>");
        } else if (result == null) {
            out.println("  <p class=\"status-error\">Der ML-Service hat keine Antwort geliefert.</p>");
        } else {
            String status = result.has("status") ? result.get("status").getAsString() : "unknown";
            out.println("  <p>Status vom ML-Service: "
                    + ("ok".equalsIgnoreCase(status)
                    ? "<span class=\"status-ok\">OK</span>"
                    : "<span class=\"status-error\">" + escapeHtml(status) + "</span>")
                    + "</p>");

            if (result.has("message")) {
                out.println("  <p>Nachricht: " + escapeHtml(result.get("message").getAsString()) + "</p>");
            }
            if (result.has("trained_at")) {
                out.println("  <p>Trainiert am: " + escapeHtml(result.get("trained_at").getAsString()) + "</p>");
            }

            // Rohes JSON als Debug-Info anzeigen
            out.println("  <div class=\"meta\">");
            out.println("    <strong>Rohantwort des ML-Services:</strong>");
            out.println("    <pre>" + escapeHtml(result.toString()) + "</pre>");
            out.println("  </div>");
        }

        out.println("  <a class=\"btn\" href=\"" + req.getContextPath() + "/dashboard\">Zurück zum Dashboard</a>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Sehr einfache HTML-Escaping-Hilfsmethode.
     */
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
