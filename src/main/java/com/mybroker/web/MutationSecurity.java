package com.mybroker.web;

import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;

/** Uses the servlet container's authenticated principal and configured roles. */
public final class MutationSecurity {
    private MutationSecurity() { }
    public static boolean authorize(HttpServletRequest req, HttpServletResponse resp, String role)
            throws IOException {
        if (req.getUserPrincipal() == null) {
            resp.sendError(401, "Container authentication is required");
            return false;
        }
        if (!req.isUserInRole(role)) { resp.sendError(403); return false; }
        resp.setHeader("Cache-Control", "no-store");
        return true;
    }
    public static String csrf(HttpServletRequest req) {
        HttpSession session = req.getSession();
        synchronized (session) {
            String token = (String) session.getAttribute("csrfToken");
            if (token == null) {
                token = UUID.randomUUID().toString();
                session.setAttribute("csrfToken", token);
            }
            return token;
        }
    }
    public static boolean verifyCsrf(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        String expected = session == null ? null : (String) session.getAttribute("csrfToken");
        String supplied = req.getParameter("csrfToken");
        if (expected == null || supplied == null || !MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8))) {
            resp.sendError(403, "Invalid CSRF token");
            return false;
        }
        return true;
    }
}
