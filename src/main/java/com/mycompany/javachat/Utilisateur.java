/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.javachat;

/**
 *
 * @author LENOVO
 */
public class Utilisateur {
private int id;
    private String motDePasse;
    private String pseudo;
    private boolean statut;

    public Utilisateur(String pseudo) {
        this.pseudo = pseudo;
        this.statut = true; // Par défaut en ligne à la création
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public boolean isStatut() {
        return statut;
    }
    
    public boolean getStatut() {
        return statut;
    }

    public void setStatut(boolean statut) {
        this.statut = statut;
    }

    public void info() {
        System.out.println("Nom d'utilisateur : " + pseudo + " | Statut : " + (statut ? "En ligne" : "Hors ligne"));
    }
}