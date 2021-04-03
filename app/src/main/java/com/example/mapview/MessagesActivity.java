package com.example.mapview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MessagesActivity extends AppCompatActivity {

    MessagesListAdapter adapter;
    ListView listView;
    int user_id;
    long lastMesID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        listView = findViewById(R.id.msgsList);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("chats");

        Bundle arguments = getIntent().getExtras();
        final String chat_id = arguments.get("chat_id").toString();
        user_id = (int)arguments.get("user_id");

        ValueEventListener msgListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<User> users = new ArrayList<>();
                ArrayList<Message> messages = new ArrayList<>();

                DataSnapshot chat = dataSnapshot.child(chat_id);

                for (DataSnapshot user: chat.child("users").getChildren()){
                    long uid = (long) user.child("id").getValue();
                    if (uid == user_id)
                        users.add(new User(uid));
                }

                lastMesID = 0;
                if (chat.hasChild("lastMesID"))
                    lastMesID = (long) chat.child("lastMesID").getValue();
                for (DataSnapshot msg: chat.child("messages").getChildren()){
                    messages.add(new Message((Long) msg.child("sender").getValue(), (String)msg.child("datetime").getValue(),(String)msg.child("text").getValue()));
                }

                adapter = new MessagesListAdapter(MessagesActivity.this, messages, chat_id, String.valueOf(user_id));
                listView.setAdapter(adapter);
                listView.setSelection(listView.getAdapter().getCount()-1);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        };
        myRef.addValueEventListener(msgListener);

        Button b = findViewById(R.id.send);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long id = lastMesID + 1;
                DatabaseReference msg = FirebaseDatabase.getInstance().getReference("chats").child(chat_id).child("messages").child(String.valueOf(id));
                EditText sendMsg = findViewById(R.id.sendMsg);
                String sendMsgText = sendMsg.getText().toString();

                if (!sendMsgText.isEmpty()){
                    Date curDate = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("H:m dd MMM yyyy", new Locale("ru"));

                    msg.child("sender").setValue(user_id);
                    msg.child("datetime").setValue(dateFormat.format(curDate));
                    msg.child("text").setValue(sendMsgText);

                    sendMsg.setText("");

                    FirebaseDatabase.getInstance().getReference("chats").child(chat_id).child("lastMesID").setValue(id);
                }
            }
        });
    }
}