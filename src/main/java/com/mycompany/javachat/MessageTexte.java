package com.mycompany.javachat;

public class MessageTexte extends Message {
    public MessageTexte(String contenu, Utilisateur expediteur) {
        super(contenu, expediteur);
    }

    @Override
    public String toString() {
        return "[" + getHorodatage().toLocalTime() + "] " + getExpediteur().getPseudo() + " : " + getContenu();
    }
}