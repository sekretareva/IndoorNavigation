package com.example.mapview;

public class Message {
    Long sender;
    String datetime;
    String text;

    public Message(Long sender, String datetime, String text){
        this.sender = sender;
        this.datetime = datetime;
        this.text = text;
    }
}
