package com.updeat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.updeat.R;

public class LogInActivity extends AppCompatActivity {
    private Button btnConfirm;
    TextInputEditText userEmail, userPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        userEmail = findViewById(R.id.edtxtUsername);
        userPassword = findViewById(R.id.edtxtPassword);

        mAuth = FirebaseAuth.getInstance();
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }
    public void openViewDashboard() {
        startActivity(new Intent(LogInActivity.this, DashboardActivity.class));
    }

    public void loginUser(){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if (TextUtils.isEmpty(email)){
            userEmail.setError("Email cannot be empty!");
            userEmail.requestFocus();
        }
        else if (TextUtils.isEmpty(password)){
            userPassword.setError("Password cannot be empty!");
            userPassword.requestFocus();
        }
        else{
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(LogInActivity.this, "User logged in succesfully", Toast.LENGTH_SHORT).show();
                        openViewDashboard();
                    }
                    else {
                        Toast.makeText( LogInActivity.this, "Log in Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}