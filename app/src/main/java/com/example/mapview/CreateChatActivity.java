package com.example.mapview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;


public class CreateChatActivity extends AppCompatActivity {

    DatabaseReference db;
    UsersListAdapter adapter;
    ListView listView;
    ArrayList<UserData> users_to_chat = new ArrayList<>();
    long lastChatID;
    EditText searchUser;
    UserData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat);

        listView = findViewById(R.id.usersToChatList);

        Bundle arguments = getIntent().getExtras();
        users_to_chat = arguments.getParcelableArrayList("users_to_chat");
        data = arguments.getParcelable(UserData.class.getSimpleName());

        adapter = new UsersListAdapter(CreateChatActivity.this, users_to_chat);
        listView.setAdapter(adapter);

        db = FirebaseDatabase.getInstance().getReference();

        ValueEventListener chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastChatID = (long) dataSnapshot.child("system").child("lastChatID").getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        db.addValueEventListener(chatListener);

        searchUser = findViewById(R.id.searchUser);

        searchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = searchUser.getText().toString();
                ArrayList<UserData> searchResult = new ArrayList<>();
                for (int i = 0; i < users_to_chat.size(); i++)
                    if (users_to_chat.get(i).getName().toLowerCase().contains(query))
                        searchResult.add(users_to_chat.get(i));

                adapter = new UsersListAdapter(CreateChatActivity.this, searchResult, adapter.chatUsers);
                listView.setAdapter(adapter);
            }
        });
    }

    public void onCreateChat(View v){
        final ArrayList<UserData> chatUsers = adapter.chatUsers;
        chatUsers.add(data);
        long new_chat_id = lastChatID+1;

        /*ValueEventListener chatToUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot u: snapshot.getChildren())
                    Log.d("QUERY", String.valueOf(u.getKey()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };*/

        for (int i = 0; i < chatUsers.size(); i++) {
            Log.d("CHATUSERS", chatUsers.get(i).getId()+"");
            db.child("chats").child(String.valueOf(new_chat_id)).child("users").child(String.valueOf(i)).child("id").setValue(chatUsers.get(i).getId());
            /*Query q = db.child("users").orderByChild("name").equalTo(chatUsers.get(i));
            q.addValueEventListener(chatToUserListener);*/
        }
        db.child("system").child("lastChatID").setValue(new_chat_id);

        Intent i = new Intent(CreateChatActivity.this, ChatsActivity.class);
        i.putExtra(UserData.class.getSimpleName(), data);
        startActivity(i);
    }
}