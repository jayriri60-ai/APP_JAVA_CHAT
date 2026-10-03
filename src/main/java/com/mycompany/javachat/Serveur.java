package com.mycompany.javachat;

import java.io.*;
import java.net.*;
import java.util.*;

public class Serveur {
    private int port;
    private static final Map<String, PrintWriter> clientsConnectes = Collections.synchronizedMap(new HashMap<>());
    private MessageDao messageDAO = new MessageDao();
    private UtilisateurDAO utilisateurDAO = new UtilisateurDAO();

    public Serveur(int port) {
        this.port = port;
    }

    public void demarrer() {
        Database.initialiserBDD();
        System.out.println("Serveur JavaChat démarré sur le port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket socketClient = serverSocket.accept();
                // Lancement d'un thread dédié pour chaque client connecté
                new Thread(new GestionnaireClient(socketClient)).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }

    // Méthode pour diffuser la liste des connectés à tout le monde
    private void diffuserListeConnectes() {
        synchronized (clientsConnectes) {
            String listePseudos = String.join(", ", clientsConnectes.keySet());
            String messageListe = "[SYSTÈME] Utilisateurs connectés : " + listePseudos;
            for (PrintWriter pw : clientsConnectes.values()) {
                pw.println(messageListe);
            }
        }
    }

    private class GestionnaireClient implements Runnable {
        private Socket socket;
        private String pseudo;
        private BufferedReader entree;
        private PrintWriter sortie;

        public GestionnaireClient(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                sortie = new PrintWriter(socket.getOutputStream(), true);

                // Étape 1 : Authentification initiale (attend "CONNECT:pseudo")
                String premiereLigne = entree.readLine();
                if (premiereLigne != null && premiereLigne.startsWith("CONNECT:")) {
                    pseudo = premiereLigne.substring(8).trim();
                    if (pseudo.isEmpty()) {
                        sortie.println("[ERREUR] Pseudo vide.");
                        socket.close();
                        return;
                    }

                    synchronized (clientsConnectes) {
                        clientsConnectes.put(pseudo, sortie);
                    }

                    // Enregistrement de l'utilisateur en BDD
                    Utilisateur u = new Utilisateur(pseudo);
                    utilisateurDAO.enregistrer(u);

                    // Message de bienvenue général
                    String msgBienvenue = "[SYSTÈME] " + pseudo + " a rejoint le chat.";
                    diffusionPublique(msgBienvenue);
                    messageDAO.enregistrer(new MessageSystem(msgBienvenue, u), "Systeme");

                    // Étape 2 : Envoi de l'historique des anciens messages publics
                    envoyerHistoriqueEtNonLus();

                    // Étape 3 : Mise à jour de la liste des connectés pour tout le monde
                    diffuserListeConnectes();
                }

                // Boucle d'écoute des messages du client
                String ligne;
                while ((ligne = entree.readLine()) != null) {
                    try {
                        traiterCommandeOuMessage(ligne);
                    } catch (CommandInvalideException e) {
                        sortie.println("[ERREUR] " + e.getMessage());
                    }
                }

            } catch (IOException e) {
                System.out.println("Connexion interrompue avec : " + pseudo);
            } finally {
                deconnexion();
            }
        }

        // Envoi des anciens messages et messages non lus à la connexion
        private void envoyerHistoriqueEtNonLus() {
    try {
        sortie.println("[SYSTÈME] --- Chargement de l'historique ---");
        
        // 1. Historique public
        List<String> historique = messageDAO.recupererDerniersMessages(30);
        for (String msg : historique) {
            sortie.println(msg);
        }

        // 2. Messages privés reçus hors-ligne
        List<String> messagesHorsLigne = messageDAO.recupererMessagesNonLus(pseudo);
        if (!messagesHorsLigne.isEmpty()) {
            sortie.println("[SYSTÈME] --- Messages reçus en votre absence ---");
            for (String msgPrive : messagesHorsLigne) {
                sortie.println(msgPrive);
            }
            messageDAO.marquerCommeLus(pseudo);
        }

        sortie.println("[SYSTÈME] --- Fin de l'historique ---");
    } catch (Exception e) {
        sortie.println("[ERREUR] Impossible de charger l'historique complet.");
    }
}

        private void traiterCommandeOuMessage(String texte) throws CommandInvalideException {
            if (texte.startsWith("/")) {
                String[] parts = texte.split(" ", 3);
                String commande = parts[0];

                switch (commande) {
                    case "/aide":
                        sortie.println("[AIDE] Commandes : /aide, /liste, /prive <pseudo> <message>, /quit");
                        break;

                    case "/liste":
                        synchronized (clientsConnectes) {
                            sortie.println("[SYSTÈME] Utilisateurs connectés : " + String.join(", ", clientsConnectes.keySet()));
                        }
                        break;

                    case "/prive":
                        if (parts.length < 3) {
                            throw new CommandInvalideException("Usage correct : /prive <pseudo> <message>");
                        }
                        String destinataire = parts[1];
                        String contenuPrive = parts[2];
                        envoyerMessagePrive(destinataire, contenuPrive);
                        break;

                    case "/quit":
                        try {
                            socket.close();
                        } catch (IOException ignored) {}
                        break;

                    default:
                        throw new CommandInvalideException("Commande inconnue : " + commande);
                }
            } else {
                // Message texte public standard
                Utilisateur exp = new Utilisateur(pseudo);
                MessageTexte msg = new MessageTexte(texte, exp);
                diffusionPublique("[" + pseudo + "] : " + texte);
                messageDAO.enregistrer(msg, "Texte");
            }
        }

        private void envoyerMessagePrive(String destinataire, String contenu) {
            PrintWriter destSortie = clientsConnectes.get(destinataire);
            Utilisateur exp = new Utilisateur(pseudo);
            Utilisateur dest = new Utilisateur(destinataire);

            if (destSortie != null) {
                // Destinataire connecté en ligne
                destSortie.println("[PRIVÉ de " + pseudo + "] : " + contenu);
                sortie.println("[PRIVÉ à " + destinataire + "] : " + contenu);
                messageDAO.enregistrer(new MessagePrive(contenu, exp, dest), "Prive");
            } else {
                // Destinataire hors-ligne : on sauvegarde quand même en BDD pour plus tard
                sortie.println("[SYSTÈME] L'utilisateur " + destinataire + " est hors-ligne. Son message lui sera remis à sa reconnexion.");
                messageDAO.enregistrer(new MessagePrive(contenu, exp, dest), "Prive");
            }
        }

        private void diffusionPublique(String message) {
            synchronized (clientsConnectes) {
                for (PrintWriter pw : clientsConnectes.values()) {
                    pw.println(message);
                }
            }
        }

        private void deconnexion() {
            if (pseudo != null) {
                synchronized (clientsConnectes) {
                    clientsConnectes.remove(pseudo);
                }
                String msgDepart = "[SYSTÈME] " + pseudo + " s'est déconnecté.";
                diffusionPublique(msgDepart);
                messageDAO.enregistrer(new MessageSystem(msgDepart, new Utilisateur(pseudo)), "Systeme");
                
                // Actualise la liste pour les autres clients restants
                diffuserListeConnectes();
            }
            try {
                socket.close();
            } catch (IOException ignored) {}
        }
    }
}