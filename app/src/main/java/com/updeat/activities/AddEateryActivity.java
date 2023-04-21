package com.updeat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.updeat.R;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddEateryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnSave;
    TextInputEditText textEateryName;
    TextInputEditText textTimeRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addeatery);

        btnSave = (Button) findViewById(R.id.btnSave);
        textEateryName = findViewById(R.id.edittxtEateryName);
        textTimeRange = findViewById(R.id.edittxtEatTime);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEatery();
            }
        });
    }

    public void createEatery() {
        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String EateryName = textEateryName.getText().toString();
        String TimeRange = textTimeRange.getText().toString();

        if (!isOnline()) {
            Toast.makeText(getApplicationContext(),"No Internet Connection!",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(EateryName)){
            textEateryName.setError("Eatery Name cannot be empty!");
            textEateryName.requestFocus();
        }
        else {
            Map<String,Object> data = new HashMap<>();
            data.put("name",EateryName);

            db.collection("UserEateryRelation").document(uid)
                    .set(data)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddEateryActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    });

            data.put("timerange",TimeRange);

            db.collection("Eateries").document(EateryName)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddEateryActivity.this, "Eatery Added Successfully", Toast.LENGTH_SHORT).show();
                            openViewDashboard();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddEateryActivity.this, "An Error Occurred", Toast.LENGTH_SHORT).show();
                        }
                    });


        }

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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }

    public void openViewDashboard() {
        startActivity(new Intent(AddEateryActivity.this, DashboardActivity.class));
    }
}