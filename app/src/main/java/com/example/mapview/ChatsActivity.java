package com.example.mapview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ChatsActivity extends AppCompatActivity {

    ChatsListAdapter adapter;
    ListView listView;
    ArrayList<Chat> chats = new ArrayList<>();

    ArrayList<User> chatUsers;
    ArrayList<UserData> users = new ArrayList<>();
    ArrayList<Message> messages;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference chatsRef = database.getReference("chats");
    DatabaseReference usersRef = database.getReference("users");

    UserData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chats);

        listView = findViewById(R.id.chatsList);

        Bundle arguments = getIntent().getExtras();
        data = arguments.getParcelable(UserData.class.getSimpleName());

        ValueEventListener chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot chat: dataSnapshot.getChildren()){

                    chatUsers = new ArrayList<>();
                    messages = new ArrayList<>();

                    for (DataSnapshot user: chat.child("users").getChildren()){
                        User sender = new User((long) user.child("id").getValue());
                        chatUsers.add(sender);
                    }

                    boolean isUsersChat = false;

                    for (User user: chatUsers){
                        if (user.id == data.getId()) {
                            isUsersChat = true;
                            chatUsers.remove(user);
                            break;
                        }
                    }

                    if (isUsersChat){
                        long lastMesID = 0;
                        if (chat.hasChild("lastMesID"))
                             lastMesID = (long) chat.child("lastMesID").getValue();
                        DataSnapshot lm = chat.child("messages")
                                .child(String.valueOf(lastMesID));
                        Message lastMsg = new Message(
                                (Long)lm.child("sender").getValue(),
                                (String)lm.child("datetime").getValue(),
                                (String)lm.child("text").getValue());
                        messages.add(lastMsg);
                        Chat ch = new Chat(chat.getKey(), chatUsers, messages);
                        chats.add(ch);
                    }
                }

                Collections.reverse(chats);
                adapter = new ChatsListAdapter(ChatsActivity.this, chats, data);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        chatsRef.addValueEventListener(chatListener);

        ValueEventListener userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot user: dataSnapshot.getChildren()){
                    int id = Integer.parseInt(user.getKey());
                    String name = (String)user.child("name").getValue();
                    int icon = Integer.parseInt(String.valueOf(user.child("icon").getValue()));
                    if (!name.equals(data.getName())){
                        users.add(new UserData(id, name, icon));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        usersRef.addValueEventListener(userListener);
    }

    public void onChooseUsersForChat(View v){
        Intent i = new Intent(ChatsActivity.this, CreateChatActivity.class);
        i.putParcelableArrayListExtra("users_to_chat", users);
        i.putExtra(UserData.class.getSimpleName(), data);
        startActivity(i);
    }
}