package com.example.mapview;

public class User {
    String name;
    String email;
    long id;

    public User(String name){
        this.name = name;
    }
    public User(String name, String email){
        this.name = name;
        this.email = email;
    }
    public User(long id){
        this.id  = id;
    }
}
