package com.example.mapview;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class UsersListAdapter extends BaseAdapter {
    ArrayList<UserData> users;
    Context cntx;
    ArrayList<UserData> chatUsers = new ArrayList<>();

    public UsersListAdapter(Context cntx, ArrayList<UserData> users) {
        this.cntx = cntx;
        this.users = users;
    }

    public UsersListAdapter(Context cntx, ArrayList<UserData> users, ArrayList<UserData> chatUsers) {
        this.cntx = cntx;
        this.users = users;
        this.chatUsers = chatUsers;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final UserData user = users.get(position);

        convertView = LayoutInflater.from(cntx).inflate(R.layout.useritem, parent, false);

        TextView username = convertView.findViewById(R.id.user);
        username.setText(user.getName());

        final CheckBox user_checkbox = convertView.findViewById(R.id.user_checkbox);
        for (int i = 0; i < chatUsers.size(); i++) {
            if (user.equals(chatUsers.get(i)))
                user_checkbox.setChecked(true);
        }

        user_checkbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (user_checkbox.isChecked())
                    chatUsers.add(user);
                else
                    chatUsers.remove(user);

                for (int i = 0; i<chatUsers.size();i++)
                    Log.d("NEWCHAT", chatUsers.get(i).getId()+"");
            }
        });

        return convertView;
    }
}
