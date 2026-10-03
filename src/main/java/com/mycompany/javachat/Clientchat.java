package com.mycompany.javachat;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;

public class Clientchat extends JFrame {
    private JTextPane zoneAffichageMessages;
    private DefaultListModel<String> modeleListeConnectes;
    private JList<String> listeConnectes;
    private JTextField champSaisieMessage;
    private JButton boutonEnvoyer;
    private JButton boutonDeconnexion;

    private Socket socket;
    private PrintWriter sortie;
    private BufferedReader entree;
    private String pseudo;
    private int portServeur;
    private String ipServeur;

    public Clientchat(String pseudo, String ipServeur, int port) {
        this.pseudo = pseudo;
        this.ipServeur = ipServeur;
        this.portServeur = port;

        setTitle("JavaChat - Connecté en tant que : " + pseudo);
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 1. Zone Centrale : Affichage des messages
        zoneAffichageMessages = new JTextPane();
        zoneAffichageMessages.setEditable(false);
        JScrollPane scrollMessages = new JScrollPane(zoneAffichageMessages);

        // 2. Zone Droite : Liste des utilisateurs connectés
        modeleListeConnectes = new DefaultListModel<>();
        listeConnectes = new JList<>(modeleListeConnectes);
        
        // Clic sur un utilisateur pour préparer un message privé
        listeConnectes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String selectedUser = listeConnectes.getSelectedValue();
                if (selectedUser != null && !selectedUser.equals(pseudo)) {
                    champSaisieMessage.setText("/prive " + selectedUser + " ");
                    champSaisieMessage.requestFocus();
                }
            }
        });

        JScrollPane scrollUsers = new JScrollPane(listeConnectes);
        scrollUsers.setPreferredSize(new Dimension(160, 0));
        scrollUsers.setBorder(BorderFactory.createTitledBorder("Connectés (Clic=Mp)"));

        // 3. Zone Basse
        JPanel panelBas = new JPanel(new BorderLayout(5, 5));
        panelBas.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        champSaisieMessage = new JTextField();
        boutonEnvoyer = new JButton("Envoyer");
        boutonDeconnexion = new JButton("Changer d'utilisateur");
        boutonDeconnexion.setForeground(new Color(180, 0, 0));

        JPanel panelBoutonsEst = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBoutonsEst.add(boutonEnvoyer);
        panelBoutonsEst.add(boutonDeconnexion);

        panelBas.add(champSaisieMessage, BorderLayout.CENTER);
        panelBas.add(panelBoutonsEst, BorderLayout.EAST);

        add(scrollMessages, BorderLayout.CENTER);
        add(scrollUsers, BorderLayout.EAST);
        add(panelBas, BorderLayout.SOUTH);

        boutonEnvoyer.addActionListener(e -> envoyerMessage());
        champSaisieMessage.addActionListener(e -> envoyerMessage());
        boutonDeconnexion.addActionListener(e -> retournerALaConnexion());

        connecterAuServeur(ipServeur, port);
    }

    private void connecterAuServeur(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            sortie = new PrintWriter(socket.getOutputStream(), true);
            entree = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sortie.println("CONNECT:" + pseudo);

            new Thread(() -> {
                try {
                    String messageRecu;
                    while ((messageRecu = entree.readLine()) != null) {
                        final String msg = messageRecu;
                        SwingUtilities.invokeLater(() -> traiterMessageRecu(msg));
                    }
                } catch (IOException e) {
                    // Déconnexion
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Impossible de se connecter au serveur : " + e.getMessage(), "Erreur réseau", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void traiterMessageRecu(String message) {
        // Détection élargie de la liste des connectés envoyée par le serveur
        if (message.contains("Utilisateurs connectés :")) {
            modeleListeConnectes.clear();
            // Extrait tout ce qui se trouve après les deux-points
            try {
                String[] parties = message.split(":");
                if (parties.length > 1) {
                    String[] pseudos = parties[parties.length - 1].trim().split(",");
                    for (String p : pseudos) {
                        String userPropre = p.trim();
                        if (!userPropre.isEmpty()) {
                            modeleListeConnectes.addElement(userPropre);
                        }
                    }
                }
            } catch (Exception e) {
                // En cas de format inattendu
            }
        }

        // Coloration des messages
        if (message.startsWith("[SYSTÈME]") || message.startsWith("[AIDE]") || message.startsWith("[ERREUR]")) {
            Color couleur = message.startsWith("[ERREUR]") ? Color.RED : Color.BLUE;
            ajouterTexteColore(message + "\n", couleur);
        } else if (message.startsWith("[PRIVÉ")) {
            ajouterTexteColore(message + "\n", new Color(128, 0, 128));
        } else {
            ajouterTexteColore(message + "\n", Color.BLACK);
        }
    }

    private void ajouterTexteColore(String texte, Color couleur) {
        StyledDocument doc = zoneAffichageMessages.getStyledDocument();
        SimpleAttributeSet style = new SimpleAttributeSet();
        StyleConstants.setForeground(style, couleur);
        try {
            doc.insertString(doc.getLength(), texte, style);
        } catch (BadLocationException ignored) {}
    }

    private void envoyerMessage() {
        String texte = champSaisieMessage.getText().trim();
        if (!texte.isEmpty() && sortie != null) {
            sortie.println(texte);
            champSaisieMessage.setText("");
        }
    }

    private void retournerALaConnexion() {
        try {
            if (sortie != null) sortie.println("/quit");
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}

        this.dispose();

        SwingUtilities.invokeLater(() -> {
            new FenetreConnexion().setVisible(true);
        });
    }
}