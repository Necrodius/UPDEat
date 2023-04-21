package com.updeat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.updeat.R;

public class DashboardActivity extends AppCompatActivity {

    private Button btnMyEatery, btnEatery;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private boolean userHasEatery = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        btnMyEatery = (Button) findViewById(R.id.btnMyEatery);
        btnEatery = (Button) findViewById(R.id.btnEatery);

        mAuth = FirebaseAuth.getInstance();

        checkIfHasEatery();

    }

    @Override
    public void onBackPressed(){
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
    }

    public void openViewMyEatery() {
        startActivity(new Intent(this, MyEateryActivity.class));

    }

    public void openViewAddEatery() {
        startActivity(new Intent(this, AddEateryActivity.class));

    }

    // checks if the user already has an eatery linked to their account - if they don't the my eatery button becomes an add eatery button
    public void checkIfHasEatery() {
        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        String uid = firebaseUser.getUid();
        if (firebaseUser == null) {
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }

        DocumentReference UserEateryRef = db.collection("UserEateryRelation").document(uid);
        UserEateryRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            btnMyEatery.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openViewMyEatery();
                                }
                            });
                        }
                        else {
                            btnMyEatery.setText("Add Eatery");
                            btnMyEatery.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    openViewAddEatery();
                                }
                            });
                        }
                    }

    });
    }
}
