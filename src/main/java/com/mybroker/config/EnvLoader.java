package com.mybroker.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class EnvLoader {

    public static void loadEnv() {

        File file = resolveEnvFile();

        System.out.println("[EnvLoader] Suche .env unter: " + file.getAbsolutePath());

        if (!file.exists()) {
            System.out.println("[EnvLoader] Keine .env Datei gefunden. Überspringe.");
            return;
        }

        System.out.println("[EnvLoader] Lade lokale .env Datei ...");

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;
            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#")) continue;

                int idx = line.indexOf('=');
                if (idx == -1) continue;

                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();

                if (System.getProperty(key) == null) {
                    System.setProperty(key, value);
                    System.out.println("[EnvLoader] Setze System-Property: " + key);
                }
            }

        } catch (IOException e) {
            System.err.println("[EnvLoader] Fehler beim Laden der .env Datei:");
            e.printStackTrace();
        }
    }

    private static File resolveEnvFile() {

        // 1️⃣ Projekt-Root (user.dir)
        String projectDir = System.getProperty("user.dir");
        if (projectDir != null) {
            File f = new File(projectDir, ".env");
            if (f.exists()) return f;
        }

        // 2️⃣ WEB-INF/classes/.env
        File classpathVariant = new File("classes/.env");
        if (classpathVariant.exists()) return classpathVariant;

        // 3️⃣ catalina.base
        String catalinaBase = System.getProperty("catalina.base");
        if (catalinaBase != null) {
            File f = new File(catalinaBase, ".env");
            if (f.exists()) return f;
        }

        // 4️⃣ Fallback (working directory)
        return new File(".env");
    }
}
