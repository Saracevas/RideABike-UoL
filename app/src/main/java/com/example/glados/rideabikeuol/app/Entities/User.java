package com.example.glados.rideabikeuol.app.Entities;

import java.io.Serializable;

/**
 * Created by Sylvester Saracevas on 07/03/2015.
 */
public class User implements Serializable{

    private static int id;
    private static  double balance;
    private static String name, surname, username, email;

    //main constructor for the User
    public User(int id, String name, String surname, String username, String email, double balance) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.balance = balance;
    }

    //getters
    public static int getID() {
        return id;
    }
    public static double getBalance() {
        return balance;
    }
    public static String getName() {
        return name;
    }
    public static String getSurname() {
        return surname;
    }
    public static String getUsername() {
        return username;
    }
    public static String getEmail() {
        return email;
    }

    public static void increaseBalance(double b) {
        balance = balance + b;
    }
    public static void decreaseBalace(double b) {
        balance = balance - b;
    }
}
