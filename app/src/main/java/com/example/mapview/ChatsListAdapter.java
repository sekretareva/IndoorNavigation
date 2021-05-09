package com.example.mapview;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatsListAdapter extends BaseAdapter {
    Context cntx;
    ArrayList<Chat> chats;
    UserData data;

    public ChatsListAdapter(Context cntx, ArrayList<Chat> chats, UserData data) {
        this.cntx = cntx;
        this.chats = chats;
        this.data = data;
    }

    @Override
    public int getCount() {
        return chats.size();
    }

    @Override
    public Object getItem(int position) {
        return chats.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Chat chat = chats.get(position);

        convertView = LayoutInflater.from(cntx).inflate(R.layout.chatitem, parent, false);

        final ImageView icon = convertView.findViewById(R.id.iconInChats);
        TextView username = convertView.findViewById(R.id.name);
        TextView msgTime = convertView.findViewById(R.id.lastMsgTime);
        for (int i = 0; i < chat.users.size(); i++){
            new UserData(chat.users.get(i).id).setName(chat.users.get(i).id,username);
        }

        Query q;
        if (chat.users.size()<2){
            final int[] iconRes = {R.drawable.icon1, R.drawable.icon2, R.drawable.icon3};
            q = FirebaseDatabase.getInstance().getReference("users").orderByKey().equalTo(String.valueOf(chat.users.get(0).id));
            q.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot user: snapshot.getChildren()){
                        long iconNum = (long)user.child("icon").getValue();
                        icon.setImageResource(iconRes[(int) (iconNum-1)]);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
        else
            icon.setImageResource(R.drawable.group);

        if (chat.messages.size()>0)
            msgTime.setText(chat.messages.get(chat.messages.size()-1).datetime);

        LinearLayout chatitem = convertView.findViewById(R.id.chatitem);

        chatitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(cntx, MessagesActivity.class);
                i.putExtra("chat_id", chat.id);
                i.putExtra("user_id", (int)data.getId());
                cntx.startActivity(i);
            }
        });

        return convertView;
    }
}
