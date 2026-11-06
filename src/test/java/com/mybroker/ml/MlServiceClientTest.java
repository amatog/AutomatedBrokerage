package com.mybroker.ml;

import com.google.gson.JsonObject;
import org.junit.Test;
import java.net.*;
import java.lang.reflect.*;
import static org.junit.Assert.*;

public class MlServiceClientTest {
    @Test public void httpFailureNeverBecomesScore() throws Exception {
        HttpURLConnection connection = new HttpURLConnection(new URL("http://invalid")) {
            public int getResponseCode() { return 404; }
            public void disconnect() { }
            public boolean usingProxy() { return false; }
            public void connect() { }
        };
        Method read = MlServiceClient.class.getDeclaredMethod("readJson", HttpURLConnection.class);
        read.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class,
                () -> read.invoke(new MlServiceClient(), connection));
        assertTrue(ex.getCause() instanceof java.io.IOException);
    }
    @Test public void malformedSuccessfulResponseIsRejected() throws Exception {
        MlServiceClient client = new MlServiceClient() {
            @Override public JsonObject calculateRiskRaw(JsonObject body) { return new JsonObject(); }
            @Override public JsonObject getTrendRaw(String symbol) { return new JsonObject(); }
        };
        assertThrows(java.io.IOException.class, () -> client.getRiskScore(new PortfolioRequestDto()));
        assertThrows(java.io.IOException.class, () -> client.getTrendScore("AAPL"));
    }
}
