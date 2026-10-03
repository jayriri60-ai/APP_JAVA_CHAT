package com.mycompany.javachat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    // Fichier local SQLite (pas besoin de serveur)
    private static final String URL = "jdbc:sqlite:javachat1.db";

    public static Connection getConnection() throws SQLException {
    try {
        // Force le chargement du pilote SQLite
        Class.forName("org.sqlite.JDBC");
    } catch (ClassNotFoundException e) {
        System.err.println("Le fichier JAR SQLite est introuvable dans le classpath !");
    }
    
    // Remplace par ton URL exacte
    return DriverManager.getConnection("jdbc:sqlite:javachat1.db"); 
}
    public static void initialiserBDD() {
        String sqlUtilisateurs = "CREATE TABLE IF NOT EXISTS utilisateurs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "pseudo TEXT UNIQUE NOT NULL,"
                + "date_creation DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String sqlMessages = "CREATE TABLE IF NOT EXISTS messages (\n" +
"    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
"    expediteur TEXT,\n" +
"    destinataire TEXT,\n" +
"    contenu TEXT,\n" +
"    date_envoi TEXT,\n" +
"    type TEXT,\n" +
"    lu INTEGER DEFAULT 0\n" +
");";
     
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlUtilisateurs);
            stmt.execute(sqlMessages);
            System.out.println("Base de données initialisée avec succès.");
        } catch (SQLException e) {
            System.err.println("Erreur d'initialisation BDD : " + e.getMessage());
        }
    }
}