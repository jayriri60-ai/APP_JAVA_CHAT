package com.mycompany.javachat;

public class CommandInvalideException extends Exception {
    public CommandInvalideException(String message) {
        super(message);
    }
}