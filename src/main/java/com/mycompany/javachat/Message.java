package com.mycompany.javachat;

import java.time.LocalDateTime;

public abstract class Message {
    private int id_message; // private
    private String contenu; // private
    private Utilisateur expediteur; // private
    private LocalDateTime horodatage; // private

    public Message(String contenu, Utilisateur expediteur) {
        this.contenu = contenu;
        this.expediteur = expediteur;
        this.horodatage = LocalDateTime.now(); 
    }

    // Ajout des getters pour l'encapsulation
    public String getContenu() {
        return contenu;
    }

    public Utilisateur getExpediteur() {
        return expediteur;
    }

    public LocalDateTime getHorodatage() {
        return horodatage;
    }

    @Override
    public abstract String toString();
}