package com.updeat.activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.textfield.TextInputEditText;
import com.updeat.R;

public class EditEateryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button BtnSave;
    TextInputEditText time;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editeatery);

        time = findViewById(R.id.txtEatTime);
        BtnSave = findViewById(R.id.btnSave);

        BtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                if (isEmpty(time)) {
                    Toast.makeText(getApplicationContext(), "Fill in all the required fields!", Toast.LENGTH_SHORT).show();
                } else {
                    intent.putExtra("WorkTime", time.getText().toString());
                    setResult(78, intent);
                    finish();
                }
            }
        });
    }
    private boolean isEmpty(TextInputEditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}
