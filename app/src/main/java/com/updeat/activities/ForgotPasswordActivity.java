package com.updeat.activities;

import android.content.Intent;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.updeat.R;

import java.io.IOException;

public class ForgotPasswordActivity extends AppCompatActivity {
    private Button btnConfirm;
    TextInputEditText userEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotpassword);

        btnConfirm = (Button) findViewById(R.id.btnConfirmEmailRequest);
        userEmail = findViewById(R.id.edtxtemailrequest);

        mAuth = FirebaseAuth.getInstance();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendResetEmail();
            }
        });
    }

    public void openLogIn() {
        startActivity(new Intent(ForgotPasswordActivity.this, LogInActivity.class));
    }
    public void sendResetEmail(){
        String email = userEmail.getText().toString();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPasswordActivity.this, "Please check your email", Toast.LENGTH_SHORT).show();
                            openLogIn();
                        }
                        else {
                            Toast.makeText(ForgotPasswordActivity.this, "There was an error in sending an email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
