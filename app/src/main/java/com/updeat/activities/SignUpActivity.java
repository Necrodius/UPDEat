package com.updeat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.updeat.R;

import java.io.IOException;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private Button btnConfirm;
    TextInputEditText userEmail, userPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        userEmail = findViewById(R.id.edtxtUsername);
        userPassword = findViewById(R.id.edtxtPassword);

        mAuth = FirebaseAuth.getInstance();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               createUser();
            }
        });
    }
    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
    public static boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();
    }
    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
    public void createUser() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if (!isOnline()){
            Toast.makeText(getApplicationContext(),"No Internet Connection!",Toast.LENGTH_SHORT).show();
        } else if (!isValidEmail(email)){
            if (TextUtils.isEmpty(email)){
                userEmail.setError("Email cannot be empty!");
                userEmail.requestFocus();
            } else {
                userEmail.setError("Invalid Email!");
                userEmail.requestFocus();
            }
        }
        else if (password.length()<8 &&!isValidPassword(password)){
            if (TextUtils.isEmpty(password)) {
                userPassword.setError("Password cannot be empty!");
                userPassword.requestFocus();
            } else {
                userPassword.setError("Invalid Password!");
                userPassword.requestFocus();
            }
        }
        else{
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(SignUpActivity.this, "User registered succesfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignUpActivity.this, LogInActivity.class));
                    }
                    else {
                        task.getException();
                        Toast.makeText( SignUpActivity.this, "Registration Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}