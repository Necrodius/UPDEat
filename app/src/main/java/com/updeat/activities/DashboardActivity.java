package com.updeat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.updeat.R;

public class DashboardActivity extends AppCompatActivity {

    private Button btnMyEatery, btnEatery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnMyEatery = (Button) findViewById(R.id.btnMyEatery);
        btnMyEatery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewMyEatery();
            }
        });

        btnEatery = (Button) findViewById(R.id.btnEatery);
        btnEatery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewEatery();
            }
        });
    }
    public void openViewMyEatery() {
        Intent intentDash = new Intent(this, MyEateryActivity.class);
        startActivity(intentDash);
    }

    public void openViewEatery() {
        Intent intentDash = new Intent(this, EateryActivity.class);
        startActivity(intentDash);
    }
}
