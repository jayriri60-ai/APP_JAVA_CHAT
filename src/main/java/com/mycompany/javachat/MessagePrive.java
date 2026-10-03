package com.mycompany.javachat;


public class MessagePrive extends Message {
    private final Utilisateur destinataire; // Bien joué, c'était déjà private ici !

    public MessagePrive(String contenu, Utilisateur expediteur, Utilisateur destinataire) {
        super(contenu, expediteur);
        this.destinataire = destinataire;
    }

    public Utilisateur getDestinataire() {
        return destinataire;
    }

    @Override
    public String toString() {
        return "[PRIVÉ][" + getHorodatage().toLocalTime() + "] " + getExpediteur().getPseudo() + " -> " + destinataire.getPseudo() + " : " + getContenu();
    }
}