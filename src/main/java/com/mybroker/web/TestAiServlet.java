package com.mybroker.web;

import com.mybroker.ai.OpenAiClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "TestAiServlet", urlPatterns = {"/test-ai"})
public class TestAiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/plain; charset=UTF-8");

        try (PrintWriter out = response.getWriter()) {

            out.println("=== OpenAI Test ===");
            out.println();

            // 1. Zeige, was an Properties/Einstellungen da ist
            String keyFromProps = System.getProperty("OPENAI_API_KEY");
            String keyFromEnv = System.getenv("OPENAI_API_KEY");
            String modelFromProps = System.getProperty("OPENAI_MODEL");
            String modelFromEnv = System.getenv("OPENAI_MODEL");

            out.println("System.getProperty(OPENAI_API_KEY): " +
                    (keyFromProps != null ? "[gesetzt]" : "null"));
            out.println("System.getenv(OPENAI_API_KEY):      " +
                    (keyFromEnv != null ? "[gesetzt]" : "null"));
            out.println("System.getProperty(OPENAI_MODEL):   " +
                    (modelFromProps != null ? modelFromProps : "null"));
            out.println("System.getenv(OPENAI_MODEL):        " +
                    (modelFromEnv != null ? modelFromEnv : "null"));
            out.println();

            // 2. Versuche einen OpenAiClient zu bauen
            OpenAiClient client;
            try {
                client = new OpenAiClient();
            } catch (IllegalStateException e) {
                out.println("❌ Fehler beim Erzeugen von OpenAiClient:");
                out.println(e.getMessage());
                return;
            }

            out.println("✅ OpenAiClient wurde erfolgreich erzeugt.");
            out.println();

            // 3. Test-Request an OpenAI senden
            try {
                String systemPrompt = "Du bist ein einfacher Test-Assistent. Antworte sehr kurz.";
                String userPrompt = "Antworte nur mit 'OK'.";

                out.println("Sende Test-Request an OpenAI...");
                String jsonResponse = client.chat(systemPrompt, userPrompt);

                out.println("✅ Antwort von OpenAI empfangen.");
                out.println();
                out.println("Rohes JSON (erste 1000 Zeichen):");
                out.println("--------------------------------");
                if (jsonResponse.length() > 1000) {
                    out.println(jsonResponse.substring(0, 1000) + "...");
                } else {
                    out.println(jsonResponse);
                }

            } catch (IOException e) {
                out.println("❌ IO-Fehler beim Aufruf der OpenAI API:");
                e.printStackTrace(out);
            } catch (Exception e) {
                out.println("❌ Unerwarteter Fehler beim Aufruf der OpenAI API:");
                e.printStackTrace(out);
            }
        }
    }
}
