package com.mycompany.javachat;

import javax.swing.*;
import java.awt.*;

public class FenetreConnexion extends JFrame {
    private JTextField champPseudo;
    private JPasswordField champMotDePasse;
    private JButton boutonConnexion;

    public FenetreConnexion() {
        setTitle("JavaChat - Connexion");
        setSize(350, 220);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Utilisation d'un agencement simple en grille
        setLayout(new GridLayout(4, 1, 5, 5));

        // Titre en haut
        JLabel titre = new JLabel("Connexion à JavaChat", JLabel.CENTER);
        titre.setFont(new Font("Arial", Font.BOLD, 16));
        add(titre);

        // Ligne du Pseudo
        JPanel panelPseudo = new JPanel(new FlowLayout());
        panelPseudo.add(new JLabel("Pseudo :"));
        champPseudo = new JTextField(15);
        panelPseudo.add(champPseudo);
        add(panelPseudo);

        // Ligne du Mot de passe
        JPanel panelMdp = new JPanel(new FlowLayout());
        panelMdp.add(new JLabel("Mot de passe :"));
        champMotDePasse = new JPasswordField(15);
        panelMdp.add(champMotDePasse);
        add(panelMdp);

        // Bouton de connexion en bas
        JPanel panelBouton = new JPanel(new FlowLayout());
        boutonConnexion = new JButton("Se connecter");
        panelBouton.add(boutonConnexion);
        add(panelBouton);

        // Événements (Clic sur le bouton ou touche Entrée)
        boutonConnexion.addActionListener(e -> tenterConnexion());
        champMotDePasse.addActionListener(e -> tenterConnexion());
    }

    private void tenterConnexion() {
        String pseudo = champPseudo.getText().trim();
        String motDePasse = new String(champMotDePasse.getPassword()).trim();

        // Vérification basique
        if (pseudo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le pseudo ne peut pas être vide !", "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Optionnel : Tu pourras ici vérifier le mot de passe avec ta BDD/DAO si besoin.
        // Pour l'instant, on autorise la connexion dès qu'un pseudo est entré :
        
        // Fermeture de la fenêtre de connexion
        this.dispose();

        // Ouverture de la fenêtre principale de chat avec le pseudo saisi
        SwingUtilities.invokeLater(() -> {
            new Clientchat(pseudo, "127.0.0.1", 9999).setVisible(true);
        });
    }

    public static void main(String[] args) {
        // Point d'entrée principal de l'application : on lance d'abord la connexion
        SwingUtilities.invokeLater(() -> {
            new FenetreConnexion().setVisible(true);
        });
    }
}