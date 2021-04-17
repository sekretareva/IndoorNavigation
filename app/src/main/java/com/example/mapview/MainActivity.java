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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity{

    private FirebaseAuth auth;
    DatabaseReference db;
    EditText email, password, username;
    long lastUserID = 0;
    boolean uniqUsername;
    Intent i;
    ArrayList<String> usernames;
    ImageView icon1, icon2, icon3;
    int icon = 1;
    UserData data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username);
        db = FirebaseDatabase.getInstance().getReference();
        i = new Intent(MainActivity.this, MapActivity.class);
        usernames = new ArrayList<>();
        icon1 = findViewById(R.id.icon1); icon2 = findViewById(R.id.icon2); icon3 = findViewById(R.id.icon3);
        icon1.setImageResource(R.drawable.icon1); icon2.setImageResource(R.drawable.icon2); icon3.setImageResource(R.drawable.icon3);
        icon1.setTag(1); icon2.setTag(2); icon3.setTag(3);

        if(auth.getCurrentUser() != null){
            startActivity(i);
        }

        ValueEventListener dbListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lastUserID = (long) dataSnapshot.child("system").child("lastUserID").getValue();
                for (DataSnapshot user: dataSnapshot.child("users").getChildren()){
                    usernames.add(user.getValue().toString());
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        db.addValueEventListener(dbListener);
    }

    public void onSignUp(View v){
        uniqUsername = true;
        createAccount(email.getText().toString(), password.getText().toString(), username.getText().toString());
    }

    public void onHaveAccount(View v){
        Intent i = new Intent(MainActivity.this, SignInActivity.class);
        startActivity(i);
    }

    private void createAccount(String email, String password, final String name) {
        Log.d("CREATE", email);
        if (!validateForm(email, password, name)) {
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("CREATED", "success");
                            Toast.makeText(MainActivity.this, "Успешная авторизация!",Toast.LENGTH_SHORT).show();

                            final FirebaseUser user = auth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                            if (user != null){
                                user.updateProfile(profileUpdates);
                            }

                            String user_id = String.valueOf(lastUserID+1);

                            db.child("users").child(user_id).child("name").setValue(name);
                            db.child("users").child(user_id).child("icon").setValue(icon);
                            db.child("system").child("lastUserID").setValue(lastUserID+1);

                            data = new UserData((int) (lastUserID+1), name, icon);
                            i.putExtra(UserData.class.getSimpleName(), data);
                            startActivity(i);
                        } else {
                            Log.w("TAG", "failure", task.getException());
                            Toast.makeText(MainActivity.this, task.getException().getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean validateForm(String email, String password, String username) {
        boolean valid = true;

        for (String name: usernames)
            if (name.equals(username)){
                uniqUsername = false;
                break;
            }

        if(!uniqUsername){
            valid = false;
            Toast.makeText(MainActivity.this, "Неуникальное имя пользователя!", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(email)) {
            valid = false;
            Toast.makeText(MainActivity.this, "Введите почту.", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {
            valid = false;
            Toast.makeText(MainActivity.this, "Введите пароль.", Toast.LENGTH_SHORT).show();
        }
        else{
            if (password.length()<6){
                valid = false;
                Toast.makeText(MainActivity.this, "Пароль должен состоять минимум из 6 символов.", Toast.LENGTH_SHORT).show();
            }
        }

        return valid;
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
            icon = 1;
        }
    }
}