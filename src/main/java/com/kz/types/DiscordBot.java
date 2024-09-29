package com.kz.types;
import com.kz.behaviours.defense.DefenseMode;

import eu.darkbot.api.game.other.GameMap;

import java.nio.charset.StandardCharsets;


import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DiscordBot {

    public static boolean petar = false;


    public static void senddiscord() {
        String url = "https://behetracker.kristiancsk.repl.co/send_message";
        
        GameMap cosas = DefenseMode.cositas;

        // Construye el cuerpo JSON (sin la IP ahora).
        String jsonBody = String.format("{\"cositas\": %d}", cosas);
    
        // Imprime el cuerpo JSON para verificarlo.
        System.out.println("JSON Body: " + jsonBody);
    
        try {
            URL targetUrl = new URL(url);
            HttpURLConnection httpURLConnection = (HttpURLConnection) targetUrl.openConnection();
    
            // Configuración básica para POST y JSON
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setDoOutput(true);
    
            // Escribir el cuerpo JSON en el OutputStream de la conexión
            try (OutputStream os = httpURLConnection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
    
            // Interpretar la respuesta
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8))) {
    
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                
                String responseStr = response.toString();
                System.out.println("Respuesta recibida: " + responseStr);

                // Interpretar la respuesta de forma más precisa
                if (responseStr.equals("{\"ip_status\":\"allowed\",\"status\":\"success\"}") || 
                    responseStr.equals("{\"status\":\"success\",\"ip_status\":\"allowed\"}")) {
                    System.out.println("Acceso permitido por el bot");
                    // Aquí puedes establecer una variable en tu clase a true si quieres usarla más tarde
                    // por ejemplo: this.isAllowed = true;
                } else if (responseStr.equals("{\"ip_status\":\"not_allowed\",\"status\":\"success\"}") || 
                        responseStr.equals("{\"status\":\"success\",\"ip_status\":\"not_allowed\"}")) {
                    System.out.println("Acceso denegado por el bot");
                    petar = true;
                } else {
                    System.out.println("Hubo un problema con la verificación o la respuesta no es reconocida.");
                    petar = true;
                }
            }
        } catch (Exception e) {
            petar = true;
            e.printStackTrace();
        }
    }

    
    
}
