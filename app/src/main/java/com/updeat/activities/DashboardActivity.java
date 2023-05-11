package com.updeat.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.updeat.R;
import com.updeat.adapters.EateryRecyclerAdapter;
import com.updeat.models.listeners.RecyclerViewClickListener;
import com.updeat.models.Eatery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private Button btnMyEatery;
    private ImageView imagePreferencesButton, setLocToCurrentImg;
    private SearchView searchView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean userHasEatery = false;
    CheckBox allShrimp, allPeanut, allMilk, allCheese, mealVegetarian, mealHalal, bgtP, bgtPP, bgtPPP;
    private TextView setLocToCurrentTxt;
    boolean allShrimpChecked = false, allPeanutChecked = false, allMilkChecked = false, allCheeseChecked= false, mealVegetarianChecked = false, mealHalalChecked = false, locSet = false;
    boolean bgtPChecked = true, bgtPPChecked = true, bgtPPPChecked = true;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ArrayList<Eatery> eateryArrayList;
    EateryRecyclerAdapter eateryRecyclerAdapter;
    ProgressDialog progressDialog;
    Double currLat, currLong;
    FusedLocationProviderClient fusedLocationProviderClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        searchView = findViewById(R.id.SearchBar);
        searchView.clearFocus();
        searchView
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (!isOnline()) {
                            Toast.makeText(getApplicationContext(), "Disconnected from database!", Toast.LENGTH_SHORT).show();
                            return false;
                        } else {
                            filterList(query);
                        }
                        searchView.clearFocus();
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });


        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        btnMyEatery = (Button) findViewById(R.id.btnMyEatery);
        imagePreferencesButton = (ImageButton) findViewById(R.id.btnShowPref);
        imagePreferencesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPreferenceDialog();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        recyclerView = findViewById(R.id.dashEateryRec);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        eateryArrayList = new ArrayList<Eatery>();
        eateryRecyclerAdapter = new EateryRecyclerAdapter(this, eateryArrayList, this);

        recyclerView.setAdapter(eateryRecyclerAdapter);

        swipeRefreshLayout = findViewById(R.id.refreshLayout);
        swipeRefreshLayout
                .setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        filterList(searchView.getQuery().toString());
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        checkIfHasEatery();
        eventChangeListener();

    }

    private void openPreferenceDialog() {
        BottomSheetDialog btmShtDlg = new BottomSheetDialog(DashboardActivity.this, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_preferencesheet, (findViewById(R.id.bottomSheetContainer)));
        btmShtDlg.setCancelable(false);

        allShrimp = bottomSheetView.findViewById(R.id.chkShrimp);
        allPeanut = bottomSheetView.findViewById(R.id.chkNut);
        allMilk = bottomSheetView.findViewById(R.id.chkMilk);
        allCheese = bottomSheetView.findViewById(R.id.chkCheese);
        mealVegetarian = bottomSheetView.findViewById(R.id.chkVeg);
        mealHalal = bottomSheetView.findViewById(R.id.chkHalal);
        bgtP = bottomSheetView.findViewById(R.id.chkP);
        bgtPP = bottomSheetView.findViewById(R.id.chkPP);
        bgtPPP = bottomSheetView.findViewById(R.id.chkPPP);
        setLocToCurrentTxt = bottomSheetView.findViewById(R.id.btnSetLocText);
        setLocToCurrentImg = bottomSheetView.findViewById(R.id.btnSetLocImg);

        allShrimp.setChecked(allShrimpChecked);
        allPeanut.setChecked(allPeanutChecked);
        allMilk.setChecked(allMilkChecked);
        allCheese.setChecked(allCheeseChecked);
        mealVegetarian.setChecked(mealVegetarianChecked);
        mealHalal.setChecked(mealHalalChecked);
        bgtP.setChecked(bgtPChecked);
        bgtPP.setChecked(bgtPPChecked);
        bgtPPP.setChecked(bgtPPPChecked);

        if(locSet){
            String loc = currLat.toString() + ", " + currLong.toString();
            setLocToCurrentTxt.setText(loc);
        }

        setLocToCurrentTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getAndSetCurrentLocation();
                    btmShtDlg.dismiss();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                    } else {
                        ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                    }
                }
            }
        });

        setLocToCurrentImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getAndSetCurrentLocation();
                    btmShtDlg.dismiss();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(DashboardActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                    } else {
                        ActivityCompat.requestPermissions(DashboardActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1234);
                    }
                }
            }
        });

        bottomSheetView.findViewById(R.id.btnSetPreferences).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allShrimpChecked = allShrimp.isChecked();
                allPeanutChecked = allPeanut.isChecked();
                allMilkChecked = allMilk.isChecked();
                allCheeseChecked = allCheese.isChecked();
                mealVegetarianChecked = mealVegetarian.isChecked();
                mealHalalChecked = mealHalal.isChecked();
                bgtPChecked = bgtP.isChecked();
                bgtPPChecked = bgtPP.isChecked();
                bgtPPPChecked = bgtPPP.isChecked();

                if ((bgtPChecked || bgtPPChecked || bgtPPPChecked) && locSet) {

                    filterList(searchView.getQuery().toString());
                    btmShtDlg.dismiss();
                } else {
                    if (!locSet) {
                        Toast.makeText(getApplicationContext(), "Please set your location preference", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Please set your budget preference", Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });
        btmShtDlg.setContentView(bottomSheetView);
        btmShtDlg.show();
    }

    private void filterList(String queryText) {
        if (!isOnline()) {
            Toast.makeText(getApplicationContext(), "Disconnected from database!", Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<Eatery> filteredEateryList = new ArrayList<Eatery>();

            db.collection("Eateries")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Eatery eatery = documentSnapshot.toObject(Eatery.class);
                                if (eatery.getName().toLowerCase().contains(queryText.toLowerCase())) {
                                    if (checkEateryPreferences(eatery)){
                                        if (bgtPChecked && eatery.getBudget() == 1) {
                                            filteredEateryList.add(eatery);
                                        } else if (bgtPPChecked && eatery.getBudget() == 2) {
                                            filteredEateryList.add(eatery);

                                        } else if (bgtPPPChecked && eatery.getBudget() == 3) {
                                            filteredEateryList.add(eatery);
                                        }
                                    }
                                }
                            }
                            if (filteredEateryList.isEmpty()) {
                                if ((bgtPChecked || bgtPPChecked || bgtPPPChecked) && locSet) {
                                    eateryRecyclerAdapter.setFilteredList(filteredEateryList);
                                    Toast.makeText(getApplicationContext(), "No Eateries available.", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (!locSet) {
                                        Toast.makeText(getApplicationContext(), "Please set your location preference", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Please set your budget preference", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else {
                                if ((bgtPChecked || bgtPPChecked || bgtPPPChecked) && locSet) {
                                    quickSort(filteredEateryList, 0, filteredEateryList.size()-1);
                                    eateryRecyclerAdapter.setFilteredList(filteredEateryList);
                                } else {
                                    if (!locSet) {
                                        Toast.makeText(getApplicationContext(), "Please set your location preference", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Please set your budget preference", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

    }


    private void eventChangeListener() {
        if (!isOnline()) {
            Toast.makeText(getApplicationContext(), "Disconnected from database!", Toast.LENGTH_SHORT).show();
        } else {
            db.collection("Eateries")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                                Log.e("Firestore Error", error.getMessage());
                            }

                            for (DocumentChange dc : value.getDocumentChanges()) {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    eateryArrayList.add(dc.getDocument().toObject(Eatery.class));
                                }
                                eateryRecyclerAdapter.notifyDataSetChanged();

                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        }
                    });
        }

    }

    @Override
    public void onBackPressed() {
        Intent currIntent = new Intent(DashboardActivity.this, MainActivity.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this);
        builder.setCancelable(true);
        builder.setMessage("Log out?");
        builder.setPositiveButton("Confirm",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut();
                        startActivity(currIntent);
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
                        } else {
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

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void onItemClick(int position) {
        Toast.makeText(getApplicationContext(), eateryArrayList.get(position).getName() + " was clicked!", Toast.LENGTH_SHORT).show();
    }

    public void getAndSetCurrentLocation() {
        Task<Location> locTask = fusedLocationProviderClient.getLastLocation();
        locTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    currLat = location.getLatitude();
                    currLong = location.getLongitude();
                    locSet = true;
                    openPreferenceDialog();
                }
            }
        });

        locTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                getAndSetCurrentLocation();
            }
        });

    }

    public double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        Integer R = 6371; // Radius of the earth in km
        double dLat = deg2rad(lat2-lat1);  // deg2rad below
        double dLon = deg2rad(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    public double deg2rad(double deg) {
        return deg * (Math.PI/180);
    }

    public void quickSort(ArrayList<Eatery> eateries, int begin, int end) {
        if (begin < end) {
            int partitionIndex = partition(eateries, begin, end);

            quickSort(eateries, begin, partitionIndex-1);
            quickSort(eateries, partitionIndex+1, end);
        }
    }

    private int partition(ArrayList<Eatery> eateries, int begin, int end) {
        double pivot = getDistanceFromLatLonInKm(currLat, currLong, eateries.get(end).getLatitude(), eateries.get(end).getLongitude());

        int i = (begin-1);

        for (int j = begin; j < end; j++) {
            if (getDistanceFromLatLonInKm(currLat, currLong, eateries.get(j).getLatitude(), eateries.get(j).getLongitude()) <= pivot) {
                i++;

                Eatery swapTemp = eateries.get(i);
                eateries.set(i, eateries.get(j));
                eateries.set(j, swapTemp);
            }
        }

        Eatery swapTemp = eateries.get(i+1);
        eateries.set(i+1, eateries.get(end));
        eateries.set(end, swapTemp);

        return i+1;
    }

    public boolean checkEateryPreferences(Eatery eatery){
        for (Map.Entry<String, List<String>> entry : eatery.getMenu().entrySet()){
           if((entry.getValue().contains("Shrimp") && allShrimpChecked) ||
                   (entry.getValue().contains("Milk") && allMilkChecked) ||
                   (entry.getValue().contains("Peanut") && allPeanutChecked) ||
                   (entry.getValue().contains("Cheese") && allCheeseChecked) ||
                   (entry.getValue().contains("Pork") && mealHalalChecked) ||
                   ((entry.getValue().contains("Pork") || entry.getValue().contains("Chicken") || entry.getValue().contains("Beef")) && mealVegetarianChecked)){
               return false;
           }
        }

        return true;
    }

}
