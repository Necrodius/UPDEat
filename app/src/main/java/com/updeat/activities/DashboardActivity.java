package com.updeat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.updeat.R;

public class DashboardActivity extends AppCompatActivity {

    private Button btnMyEatery, btnEatery;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnMyEatery = (Button) findViewById(R.id.btnMyEatery);
        btnEatery = (Button) findViewById(R.id.btnEatery);

        mAuth = FirebaseAuth.getInstance();
        btnMyEatery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewMyEatery();
            }
        });

        btnEatery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewEatery();
            }
        });
    }

    @Override
    public void onBackPressed(){
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void openViewMyEatery() {
        startActivity(new Intent(this, MyEateryActivity.class));

    }

    public void openViewEatery() {
        startActivity(new Intent(this, EateryActivity.class));
    }
}
