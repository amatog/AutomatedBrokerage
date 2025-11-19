package com.mybroker.config;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("[AppStartup] Initialisiere Umgebungsvariablen...");
        EnvLoader.loadEnv();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nichts zu tun
    }
}

