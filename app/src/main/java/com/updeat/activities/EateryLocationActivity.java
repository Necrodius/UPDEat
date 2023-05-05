package com.updeat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.updeat.R;
import com.updeat.models.Eatery;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EateryLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private GoogleMap mGoogleMap;
    private Marker marker;
    private Button btnSave;
    String EateryID;
    LatLng latLng;
    private double newLatitude;
    private double newLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eaterylocation);

        btnSave = findViewById(R.id.btnSave);
        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        String uid = firebaseUser.getUid();

        if (firebaseUser == null) {
            Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
        }
        DocumentReference usereateryRef = db.collection("UserEateryRelation").document(uid);
        usereateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            EateryID = documentSnapshot.getString("name");
                            if (EateryID != null) {
                                getEateryInfo(EateryID);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Invalid Eatery", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLocation();
                openEditEatery();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapEateryLocation);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMapClickListener(this);
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
    public void onMapClick(@NonNull LatLng tempLatLng) {
        mGoogleMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(tempLatLng);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tempLatLng, 15));
        mGoogleMap.addMarker(markerOptions);
        newLatitude = tempLatLng.latitude;
        newLongitude = tempLatLng.longitude;
    }
    public void getEateryInfo(String EateryID) {
        DocumentReference eateryRef = db.collection("Eateries").document(EateryID);
        eateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String EateryName = documentSnapshot.getString("name");

                            double Latitude = documentSnapshot.getDouble("latitude");
                            double Longitude = documentSnapshot.getDouble("longitude");

                            LatLng latLng = new LatLng(Latitude,Longitude);

                            if (latLng != null) {
                                mGoogleMap.clear();
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }
                            else {
                                latLng = new LatLng(14.654924,120.9886316);
                                mGoogleMap.clear();
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                            }

                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void changeLocation() {
        DocumentReference eateryRef = db.collection("Eateries").document(EateryID);
        eateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, Object> data = new HashMap<>();
                            data.put("latitude", newLatitude);
                            data.put("longitude",newLongitude);
                            db.collection("Eateries").document(EateryID)
                                    .set(data, SetOptions.merge());

                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    public void openEditEatery() {
        startActivity(new Intent(EateryLocationActivity.this, EditEateryActivity.class));
    }
}

