package com.example.mapview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity{

    private FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference db;
    EditText oldPassword, newPassword, username;
    boolean uniqUsername;
    Intent i;
    ArrayList<String> usernames;
    ImageView icon1, icon2, icon3;
    int icon = 0;
    UserData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        oldPassword = findViewById(R.id.oldPassword);
        newPassword = findViewById(R.id.newPassword);
        username = findViewById(R.id.username);
        username.setText(user.getDisplayName());
        db = FirebaseDatabase.getInstance().getReference();
        i = new Intent(SettingsActivity.this, MapActivity.class);
        usernames = new ArrayList<>();
        icon1 = findViewById(R.id.icon1); icon2 = findViewById(R.id.icon2); icon3 = findViewById(R.id.icon3);
        icon1.setImageResource(R.drawable.icon1); icon2.setImageResource(R.drawable.icon2); icon3.setImageResource(R.drawable.icon3);
        icon1.setTag(1); icon2.setTag(2); icon3.setTag(3);
        Bundle arguments = getIntent().getExtras();
        data = arguments.getParcelable(UserData.class.getSimpleName());

        ValueEventListener dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot user: dataSnapshot.child("users").getChildren()){
                    usernames.add(user.child("name").getValue().toString());
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        db.addValueEventListener(dbListener);
    }

    public void onSaved(View v){
        uniqUsername = true;
        updateAccount(oldPassword.getText().toString(), newPassword.getText().toString(), username.getText().toString());
    }

    public void onCanceled(View v){
        Intent i = new Intent(SettingsActivity.this, MapActivity.class);
        startActivity(i);
    }

    private void updateAccount(String oldPassword, String newPassword, final String name) {
        if (!validateForm(oldPassword, newPassword, name)) {
            return;
        }
        i.putExtra(UserData.class.getSimpleName(), data);
        startActivity(i);
    }

    private boolean validateForm(String oldPassword, final String newPassword, String username) {
        boolean valid = true;

        for (String name: usernames) {
            if (name.equals(username)) {
                uniqUsername = false;
                break;
            }
        }

        if(!username.isEmpty() && !username.equals(user.getDisplayName())){
            if(!uniqUsername){
                Toast.makeText(SettingsActivity.this, "Неуникальное имя пользователя!", Toast.LENGTH_SHORT).show();
                valid = false;
                Log.d("UPDATE", "not update username");
            }
            else{
                Log.d("UPDATE", "update username");
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(username).build();
                if (user != null){
                    user.updateProfile(profileUpdates);
                    data.setName(username);
                }

                Query q = db.child("users").orderByChild("name").equalTo(user.getDisplayName());
                q.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot s: snapshot.getChildren())
                            updateUserName(s.getKey());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        }

        if (icon!=0){
            data.updateIcon(icon);
        }

        if (valid && !newPassword.isEmpty() ){
            valid = false;
            if(oldPassword.isEmpty()){
                Toast.makeText(SettingsActivity.this, "Введите старый пароль!", Toast.LENGTH_SHORT).show();
            }
            else{
                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (newPassword.length()<6){
                                        Toast.makeText(SettingsActivity.this, "Пароль должен состоять минимум из 6 символов.", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        user.updatePassword(newPassword);
                                        Log.d("UPDATE", "update password");
                                        i.putExtra(UserData.class.getSimpleName(), data);
                                        startActivity(i);
                                    }
                                }
                                else{
                                    Toast.makeText(SettingsActivity.this, "Неверный старый пароль!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }

        return valid;
    }

    private void updateUserName(String key) {
        db.child("users").child(key).child("name").setValue(username.getText().toString());
    }

    public void onIconClick(View v){
        int icon_id = (int)v.getTag();

        if ((int)v.getTag() < 4){
            v.setBackgroundColor(Color.YELLOW);
            v.setTag((int)v.getTag()*2);
            icon = icon_id;
        }
        else{
            v.setBackgroundColor(Color.TRANSPARENT);
            v.setTag((int)v.getTag()/2);
            icon = data.getIcon();
        }
    }
}
