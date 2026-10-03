package com.mycompany.javachat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {

    public void enregistrer(Utilisateur utilisateur) {
        String sql = "INSERT OR IGNORE INTO utilisateurs (pseudo) VALUES (?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, utilisateur.getPseudo());
            pstmt.executeUpdate();
            System.out.println("Utilisateur " + utilisateur.getPseudo() + " enregistré.");
            
        } catch (SQLException e) {
            System.err.println("Erreur DAO Utilisateur (enregistrement) : " + e.getMessage());
        }
    }

    public List<Utilisateur> listerTous() {
        List<Utilisateur> liste = new ArrayList<>();
        String sql = "SELECT pseudo FROM utilisateurs";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                // On reconstruit l'objet Java à partir de la ligne SQL
                Utilisateur u = new Utilisateur(rs.getString("pseudo"));
                liste.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Erreur DAO Utilisateur (lecture) : " + e.getMessage());
        }
        return liste;
    }
}
