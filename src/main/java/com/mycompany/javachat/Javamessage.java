/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.javachat;

/**
 *
 * @author LENOVO
 */


public class Javamessage {

    public static void main(String[] args) {
        // C'est ici qu'on appelle l'initialisation de la base de données
       Database.initialiserBDD(); // Initialise SQLite
    
       Serveur serveur = new Serveur(9999);
       serveur.demarrer(); // Lance l'écoute réseau
    }
}