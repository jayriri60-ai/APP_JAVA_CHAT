package com.mycompany.javachat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class MessageDao {

    public void enregistrer(Message message, String type) {
    String sql = "INSERT INTO messages (expediteur, destinataire, contenu, date_envoi, type, lu) VALUES (?, ?, ?, ?, ?, ?)";
    
    try (Connection conn = Database.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setString(1, message.getExpediteur().getPseudo());
        
        int statutLu = 1; // Par défaut lu pour le public/système
        if (message instanceof MessagePrive) {
            MessagePrive messagePrive = (MessagePrive) message;
            pstmt.setString(2, messagePrive.getDestinataire().getPseudo());
            statutLu = 0; // Non lu tant que le destinataire ne s'est pas connecté
        } else {
            pstmt.setString(2, null);
        }
        
        pstmt.setString(3, message.getContenu());
        pstmt.setString(4, message.getHorodatage().toString());
        pstmt.setString(5, type);
        pstmt.setInt(6, statutLu);
        
        pstmt.executeUpdate();
        
    } catch (SQLException e) {
        System.err.println("Erreur lors de l'enregistrement du message : " + e.getMessage());
    }
}
    // AJOUT DEMANDÉ PAR LE SUJET
    public List<Message> listerTous() {
        List<Message> historiques = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY id ASC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String type = rs.getString("type");
                String contenu = rs.getString("contenu");
                Utilisateur exp = new Utilisateur(rs.getString("expediteur"));
                
                // Reconstruction polymorphe selon le type en base
                if ("Texte".equals(type)) {
                    historiques.add(new MessageTexte(contenu, exp));
                } else if ("Systeme".equals(type)) {
                    historiques.add(new MessageSystem(contenu, exp));
                } else if ("Prive".equals(type)) {
                    Utilisateur dest = new Utilisateur(rs.getString("destinataire"));
                    historiques.add(new MessagePrive(contenu, exp, dest));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur DAO Message (lecture) : " + e.getMessage());
        }
        return historiques;
    }
    // Récupère les derniers messages pour l'historique public
// Récupère les derniers messages pour l'historique public
    public List<String> recupererDerniersMessages(int limite) {
        List<String> liste = new ArrayList<>();
        String sql = "SELECT * FROM messages ORDER BY id DESC LIMIT ?";
    
    // CORRECTION : Utilisation de getConnection() au lieu de connecter()
        try (Connection conn = Database.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String contenu = rs.getString("contenu");
                String expediteur = rs.getString("expediteur");
                String type = rs.getString("type");
                String destinataire = rs.getString("destinataire");
            
                if ("Prive".equals(type)) {
                    liste.add("[PRIVÉ entre " + expediteur + " et " + destinataire + "] : " + contenu);
                } else if ("Systeme".equals(type)) {
                    liste.add(contenu);
                } else {
                    liste.add("[" + expediteur + "] : " + contenu);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur historique : " + e.getMessage());
        }
        // Pour inverser et avoir du plus ancien au plus récent
        Collections.reverse(liste);
        return liste;
    }
    // Récupère les messages privés non lus pour un utilisateur spécifique
    public List<String> recupererMessagesNonLus(String pseudoDestinataire) {
        List<String> liste = new ArrayList<>();
        // On sélectionne les messages destinés à l'utilisateur où "lu = 0"
        String sql = "SELECT * FROM messages WHERE destinataire = ? AND lu = 0 ORDER BY id ASC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pseudoDestinataire);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String expediteur = rs.getString("expediteur");
                String contenu = rs.getString("contenu");
                liste.add("[PRIVÉ de " + expediteur + "] : " + contenu);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération messages non lus : " + e.getMessage());
        }
        return liste;
    }

    // Marque tous les messages d'un utilisateur comme lus une fois qu'il s'est connecté
    public void marquerCommeLus(String pseudoDestinataire) {
        // On passe la colonne "lu" à 1 pour tous ses messages
        String sql = "UPDATE messages SET lu = 1 WHERE destinataire = ? AND lu = 0";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pseudoDestinataire);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Erreur mise à jour statut lu : " + e.getMessage());
        }
    }
}