package com.updeat.activities;

import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.updeat.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditEateryActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button BtnSave, BtnMenu, BtnChangeLocation;
    TextInputEditText textTimeRange;
    TextInputEditText textEateryName;
    private ListView lstMenu;
    String EateryID;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    GoogleMap mGoogleMap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editeatery);

        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        String uid = firebaseUser.getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        textEateryName = findViewById(R.id.edittxtEateryName);
        textTimeRange = findViewById(R.id.edittxtEatTime);
        BtnSave = findViewById(R.id.btnSave);
        BtnMenu = findViewById(R.id.btnEditMenu);
        BtnChangeLocation = findViewById(R.id.btnEditLocation);


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
        BtnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOnline()) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                }
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditEateryActivity.this);
                    builder.setCancelable(true);
                    builder.setMessage("Save your Changes?");
                    builder.setPositiveButton("Confirm",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (isEmpty(textEateryName)) {
                                        Toast.makeText(getApplicationContext(), "Your Eatery needs a name!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("name", textEateryName.getText().toString());
                                        data.put("timerange", textTimeRange.getText().toString());

                                        db.collection("Eateries").document(EateryID)
                                                .set(data, SetOptions.merge());
                                        openViewDashboard();
                                    }
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

            }
        });

        BtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuEdit();
            }
        });

        BtnChangeLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChangeLocation();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapEateryLocation);
        mapFragment.getMapAsync(this);
}

    private boolean isEmpty(TextInputEditText etText) {
        return etText.getText().toString().trim().length() == 0;
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;
    }

    public void openMenuEdit() {
        startActivity(new Intent(EditEateryActivity.this, EditMenuActivity.class));
    }

    public void getEateryInfo(String EateryID) {
        DocumentReference eateryRef = db.collection("Eateries").document(EateryID);
        eateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String EateryName = documentSnapshot.getString("name");
                            textEateryName.setText(EateryName);
                            String TimeRange = documentSnapshot.getString("timerange");

                            double Latitude = documentSnapshot.getDouble("latitude");
                            double Longitude = documentSnapshot.getDouble("longitude");
                            LatLng latLng = new LatLng(Latitude,Longitude);

                            if (latLng != null) {
                                mGoogleMap.clear();
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(latLng);
                                markerOptions.title(EateryName);
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                mGoogleMap.addMarker(markerOptions);
                            }
                            if (TimeRange != null) {
                                textTimeRange.setText(TimeRange);
                            }
                            Map<String, List<String>> menu = (HashMap<String,List<String>>) documentSnapshot.getData().get("Menu");
                            if (menu !=null) {
                                displayMenuItems(menu);
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
    public void displayMenuItems(Map<String,List<String>> menu) {
        List<HashMap<String, String>> menuItems = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(this, menuItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        for (String key : menu.keySet()) {
            HashMap<String,String> itemStrings = new HashMap<>();
            String ingredients = "";
            List<String> item = menu.get(key);
            for (int j = 0; j<item.size(); j++) {
                ingredients += item.get(j);
                if (j < item.size()-1) {
                    ingredients += ", ";
                }
            }
            itemStrings.put("First Line",key);
            itemStrings.put("Second Line",ingredients);
            menuItems.add(itemStrings);
        }
        lstMenu = findViewById(R.id.lstEateryMenu);
        lstMenu.setAdapter(adapter);

    }

    public void openViewDashboard() {
        startActivity(new Intent(EditEateryActivity.this, DashboardActivity.class));
    }

    public void openChangeLocation() {
        startActivity(new Intent(EditEateryActivity.this, EateryLocationActivity.class));
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
}

