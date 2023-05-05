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
import com.updeat.fragments.MapFragment;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class MyEateryActivity extends AppCompatActivity implements OnMapReadyCallback{
    private Button btnEditEatery;
    private TextView txtEatTimes;
    private TextView txtEateryName;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListView lstMenu;


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
        setContentView(R.layout.activity_myeatery);

        Fragment fragment = new MapFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameEateryLocation,fragment).commit();

        txtEatTimes = (TextView) findViewById(R.id.txtEatTime);
        btnEditEatery = (Button) findViewById(R.id.btnSave);
        txtEateryName = (TextView) findViewById(R.id.txtEateryName);
        lstMenu = (ListView) findViewById(R.id.lstEateryMenu);

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
                            String EateryID = documentSnapshot.getString("name");
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

        btnEditEatery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyEateryActivity.this, EditEateryActivity.class);
                if (!isOnline()) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                } else {
                    activityLauncher.launch(intent);
                }
            }
        });

//       SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapEateryLocation);
//       mapFragment.getMapAsync(this);
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

    // Updates the text shown on screen to show the eatery linked to the user
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
