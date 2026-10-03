package com.mycompany.javachat;

public class MessageSystem extends Message {
    public MessageSystem(String contenu, Utilisateur expediteur) {
        super(contenu, expediteur);
    }

    @Override
    public String toString() {
        return "[SYSTÈME][" + getHorodatage().toLocalTime() + "] " + getContenu();
    }
}