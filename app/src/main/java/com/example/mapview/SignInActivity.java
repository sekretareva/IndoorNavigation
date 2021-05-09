package com.example.mapview;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText email, password;
    Intent i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        mAuth = FirebaseAuth.getInstance();
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        i = new Intent(SignInActivity.this, MapActivity.class);
    }

    public void onSignIn(View v){
        if (!validateForm(email.getText().toString().trim(), password.getText().toString().trim())) {
            return;
        }

        mAuth.signInWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Вход выполнен успешно.",Toast.LENGTH_SHORT).show();
                            startActivity(i);
                        } else {
                            Toast.makeText(SignInActivity.this, "Неверный пароль или email.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void onNotHaveAccount(View v){
        Intent i = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(i);
    }

    private boolean validateForm(String e, String p) {
        boolean valid = true;

        if (TextUtils.isEmpty(e)) {
            valid = false;
            Toast.makeText(SignInActivity.this, "Введите почту.", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(p)) {
            valid = false;
            Toast.makeText(SignInActivity.this, "Введите пароль.", Toast.LENGTH_SHORT).show();
        }

        return valid;
    }
}