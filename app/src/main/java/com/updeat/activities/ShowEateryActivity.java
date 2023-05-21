package com.updeat.activities;

import static android.content.ContentValues.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.updeat.R;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowEateryActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView txtEatTimes;
    private TextView txtEateryName;
    private TextView txtAvePrice;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView lstMenu;

    LatLng latLng;
    GoogleMap mGoogleMap;

    ActivityResultLauncher<Intent> activityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode()==78){
                        Intent intent = result.getData();
                        if (intent != null){
                            String data = intent.getStringExtra("WorkTime");
                            txtEatTimes.setText(data);
                        }
                    }
                }
            }
    );
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showeatery);

        Bundle extras = getIntent().getExtras();
        String value = null;
        if (extras != null) {
            value = extras.getString("key");
            //The key argument here must match that used in the other activity
        }

        txtEatTimes = (TextView) findViewById(R.id.txtEatTime);
        txtEateryName = (TextView) findViewById(R.id.txtEateryName);
        txtAvePrice = (TextView) findViewById(R.id.txtAvePrice);
        lstMenu = (ListView) findViewById(R.id.lstEateryMenu);


        DocumentReference usereateryRef = db.collection("Eateries").document(value);
        usereateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String EateryID = documentSnapshot.getString("name");

                            if (EateryID != null) {
                                getEateryInfo(EateryID);
                            } else {
                                Toast.makeText(getApplicationContext(), "Invalid Eatery", Toast.LENGTH_SHORT).show();
                            }
                        } else {
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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapEateryLocation);
        mapFragment.getMapAsync(this);
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
    public void onMapReady(@NonNull GoogleMap googleMap) { mGoogleMap = googleMap; }
    public void getEateryInfo(String EateryID) {
        DocumentReference eateryRef = db.collection("Eateries").document(EateryID);
        eateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String EateryName = documentSnapshot.getString("name");
                            txtEateryName.setText(EateryName);
                            String TimeRange = documentSnapshot.getString("timerange");
                            Double AvePrice = documentSnapshot.getDouble("averageprice");
                            txtAvePrice.setText(Double.toString(AvePrice));
                            
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
                                txtEatTimes.setText(TimeRange);
                            }

                            Map<String,List<String>> menu = (HashMap<String,List<String>>) documentSnapshot.getData().get("Menu");
                            if (menu != null) {
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
        lstMenu.setAdapter(adapter);
    }
}
