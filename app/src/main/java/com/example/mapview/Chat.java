package com.example.mapview;

import java.util.ArrayList;

public class Chat {
    String id;
    ArrayList<User> users;
    ArrayList<Message> messages;

    public Chat(){
        this.users = new ArrayList<>();
        this.messages = new ArrayList<>();
    }

    public Chat(ArrayList<User> users){
        this.users = users;
    }

    public Chat(String id, ArrayList<User> users, ArrayList<Message> msgs){
        this.id = id;
        this.users = users;
        this.messages = msgs;
    }
}
