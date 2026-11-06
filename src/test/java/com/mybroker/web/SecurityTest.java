package com.mybroker.web;

import org.junit.Test;
import javax.servlet.http.*;
import java.lang.reflect.Proxy;
import java.util.*;
import static org.junit.Assert.*;

public class SecurityTest {
    private final Map<String, Object> sessionData = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private int status;
    private boolean authenticated;
    private boolean authorized;
    private final HttpSession session = (HttpSession) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class[]{HttpSession.class}, (p, m, a) -> {
                switch (m.getName()) {
                    case "getAttribute": return sessionData.get(a[0]);
                    case "setAttribute": sessionData.put((String) a[0], a[1]); return null;
                    case "removeAttribute": sessionData.remove(a[0]); return null;
                    default: return null;
                }
            });
    private HttpServletRequest request() {
        return (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class}, (p, m, a) -> {
                    switch (m.getName()) {
                        case "getUserPrincipal": return authenticated ? (java.security.Principal) () -> "operator" : null;
                        case "isUserInRole": return authorized;
                        case "getSession": return session;
                        case "getParameter": return parameters.get(a[0]);
                        default: return null;
                    }
                });
    }
    private HttpServletResponse response() {
        return (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(),
                new Class[]{HttpServletResponse.class}, (p, m, a) -> {
                    if (m.getName().equals("sendError")) status = (Integer) a[0];
                    return null;
                });
    }
    @Test public void authorizationFailsClosed() throws Exception {
        assertFalse(MutationSecurity.authorize(request(), response(), "trader"));
        assertEquals(401, status);
        authenticated = true;
        assertFalse(MutationSecurity.authorize(request(), response(), "trader"));
        assertEquals(403, status);
        authorized = true;
        assertTrue(MutationSecurity.authorize(request(), response(), "trader"));
    }
    @Test public void csrfRequiresMatchingSessionToken() throws Exception {
        assertFalse(MutationSecurity.verifyCsrf(request(), response()));
        String token = MutationSecurity.csrf(request());
        parameters.put("csrfToken", "wrong");
        assertFalse(MutationSecurity.verifyCsrf(request(), response()));
        parameters.put("csrfToken", token);
        assertTrue(MutationSecurity.verifyCsrf(request(), response()));
    }
    @Test public void invalidOrderNeverReachesBroker() throws Exception {
        authenticated = authorized = true;
        parameters.put("csrfToken", MutationSecurity.csrf(request()));
        parameters.put("qty", "-2");
        parameters.put("symbol", "AAPL");
        parameters.put("side", "buy");
        new OrdersServlet().doPost(request(), response());
        assertEquals(400, status);
    }
    @Test public void duplicateOrderIsRejectedBeforeBroker() throws Exception {
        authenticated = authorized = true;
        parameters.put("csrfToken", MutationSecurity.csrf(request()));
        parameters.put("qty", "2");
        parameters.put("symbol", "AAPL");
        parameters.put("side", "buy");
        parameters.put("clientOrderId", UUID.randomUUID().toString());
        new OrdersServlet().doPost(request(), response());
        assertEquals(409, status);
    }
    @Test public void symbolInjectionIsRejected() throws Exception {
        parameters.put("symbol", "\" ONFOCUS=ALERT(1)");
        new ValueAnalysisServlet().doGet(request(), response());
        assertEquals(400, status);
    }
}
