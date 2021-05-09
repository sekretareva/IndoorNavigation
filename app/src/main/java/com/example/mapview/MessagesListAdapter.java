package com.example.mapview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MessagesListAdapter extends BaseAdapter {
    Context cntx;
    ArrayList<Message> messages;
    String chat_id;
    long usersAmount;
    String user_id;

    public MessagesListAdapter(Context cntx, ArrayList<Message> messages, String chat_id, String user_id) {
        this.cntx = cntx;
        this.messages = messages;
        this.chat_id = chat_id;
        this.user_id = user_id;

        DatabaseReference db = FirebaseDatabase.getInstance().getReference("chats").child(chat_id).child("users");
        ValueEventListener dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               usersAmount = dataSnapshot.getChildrenCount();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        db.addValueEventListener(dbListener);
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message msg = messages.get(position);

        if (String.valueOf(msg.sender).equals(user_id))
            convertView = LayoutInflater.from(cntx).inflate(R.layout.my_msgitem, parent, false);
        else{
            convertView = LayoutInflater.from(cntx).inflate(R.layout.msgitem, parent, false);
            final ImageView senderIcon = convertView.findViewById(R.id.senderIcon);
            UserData data = new UserData(msg.sender);
            data.setIcon(msg.sender, senderIcon);
            TextView senderName = convertView.findViewById(R.id.senderName);
            data.setName(msg.sender, senderName);
        }

        TextView msgTime = convertView.findViewById(R.id.msgTime);
        TextView msgText = convertView.findViewById(R.id.msgText);

        msgTime.setText(msg.datetime);
        msgText.setText(msg.text);

        return convertView;
    }
}

