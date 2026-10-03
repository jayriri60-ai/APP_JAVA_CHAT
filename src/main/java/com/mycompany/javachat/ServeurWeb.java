/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javachat;

/**
 *
 * @author LENOVO
 */




import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public class ServeurWeb {
    public static void demarrer(int port) {
        try {
            HttpServer serveur = HttpServer.create(new InetSocketAddress(port), 0);
            
            serveur.createContext("/", (HttpExchange echange) -> {
                File fichierHtml = new File("index.html");
                byte[] reponseBytes;
                
                if (fichierHtml.exists()) {
                    reponseBytes = Files.readAllBytes(fichierHtml.toPath());
                } else {
                    String erreur = "<h1>Erreur : index.html introuvable à la racine du projet !</h1>";
                    reponseBytes = erreur.getBytes();
                }

                // En-tête HTTP et taille de la réponse
                echange.sendResponseHeaders(200, reponseBytes.length);
                
                // Utilisation d'un try-with-resources pour fermer automatiquement le flux os
                try (OutputStream os = echange.getResponseBody()) {
                    os.write(reponseBytes);
                }
            });

            serveur.setExecutor(null);
            serveur.start();
            System.out.println("Serveur Web démarré ! Ouvrez votre navigateur sur : http://localhost:" + port);

        } catch (IOException e) {
            System.err.println("Erreur du serveur web : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        Database.initialiserBDD();
        demarrer(8080);
    }
}