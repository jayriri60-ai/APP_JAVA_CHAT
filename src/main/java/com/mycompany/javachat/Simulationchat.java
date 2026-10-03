
package com.mycompany.javachat;

import java.util.ArrayList;
import java.util.List;

public class Simulationchat {

    public static void main(String[] args) {
        // STEP 1 : Initialisation de la base de données et des DAO
        System.out.println("=== 1. INITIALISATION BDD ===");
        Database.initialiserBDD();

        UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
        MessageDao messageDAO = new MessageDao();

        // STEP 2 : Création des 3 utilisateurs (Phase 1)
        Utilisateur admin = new Utilisateur("Admin");
        Utilisateur alice = new Utilisateur("Alice");
        Utilisateur bob = new Utilisateur("Bob");

        // Sauvegarde des utilisateurs en BDD (Phase 2)
        utilisateurDAO.enregistrer(admin);
        utilisateurDAO.enregistrer(alice);
        utilisateurDAO.enregistrer(bob);

        // STEP 3 : Instanciation de 8 messages mélangeant les 3 sous-types (Phase 1)
        List<Message> conversation = new ArrayList<>();

        Message m1 = new MessageSystem("Le serveur JavaChat est en ligne.", admin);
        Message m2 = new MessageSystem("Alice a rejoint la discussion.", admin);
        Message m3 = new MessageTexte("Salut tout le monde !", alice);
        Message m4 = new MessageSystem("Bob a rejoint la discussion.", admin);
        Message m5 = new MessageTexte("Hello Alice, tu as avancé sur la phase 2 ?", bob);
        Message m6 = new MessageTexte("Oui, la connexion SQLite fonctionne parfaitement !", alice);
        Message m7 = new MessagePrive("Tu peux me montrer ton code DAO ?", bob, alice);
        Message m8 = new MessagePrive("Pas de problème, regarde la simulation !", alice, bob);

        conversation.add(m1);
        conversation.add(m2);
        conversation.add(m3);
        conversation.add(m4);
        conversation.add(m5);
        conversation.add(m6);
        conversation.add(m7);
        conversation.add(m8);

        // STEP 4 : Démonstration du polymorphisme (Affichage local)
        System.out.println("\n=== 2. CONVERSATION EN MÉMOIRE (POLYMORPHISME) ===");
        for (Message msg : conversation) {
            System.out.println(msg.toString());
        }

        // STEP 5 : Persistance - Sauvegarde des messages en BDD (Phase 2)
        System.out.println("\n=== 3. SAUVEGARDE DANS SQLITE ===");
        messageDAO.enregistrer(m1, "Systeme");
        messageDAO.enregistrer(m2, "Systeme");
        messageDAO.enregistrer(m3, "Texte");
        messageDAO.enregistrer(m4, "Systeme");
        messageDAO.enregistrer(m5, "Texte");
        messageDAO.enregistrer(m6, "Texte");
        messageDAO.enregistrer(m7, "Prive");
        messageDAO.enregistrer(m8, "Prive");

        // STEP 6 : Vérification de la persistance (Relecture depuis SQLite)
        System.out.println("\n=== 4. RELECTURE DEPUIS LA BDD (LISTER TOUS) ===");
        List<Message> messagesEnBDD = messageDAO.listerTous();
        for (Message msg : messagesEnBDD) {
            System.out.println("[BDD] " + msg.toString());
        }
    }
}
