package com.updeat.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.updeat.R;
import com.updeat.adapters.EateryRecyclerAdapter;
import com.updeat.listeners.RecyclerViewClickListener;
import com.updeat.models.Eatery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity implements RecyclerViewClickListener {

    private Button btnMyEatery, btnEatery;
    private SearchView searchView;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private boolean userHasEatery = false;
    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ArrayList<Eatery> eateryArrayList;
    EateryRecyclerAdapter eateryRecyclerAdapter;
    ProgressDialog progressDialog;


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
                }
                else {
                    filterList(query);
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        progressDialog =  new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Data...");
        progressDialog.show();

        btnMyEatery = (Button) findViewById(R.id.btnMyEatery);
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
                filterList("");
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        checkIfHasEatery();
        eventChangeListener();

    }

    private void filterList(String queryText) {
        if (!isOnline()) {
            Toast.makeText(getApplicationContext(), "Disconnected from database!", Toast.LENGTH_SHORT).show();
        }
        else{
            ArrayList<Eatery> filteredEateryList = new ArrayList<Eatery>();

            db.collection("Eateries")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                Eatery eatery = documentSnapshot.toObject(Eatery.class);
                                if (eatery.getName().toLowerCase().contains(queryText.toLowerCase())){
                                    filteredEateryList.add(eatery);
                                }
                            }
                            if (filteredEateryList.isEmpty()){
                                eateryRecyclerAdapter.setFilteredList(filteredEateryList);
                                Toast.makeText(getApplicationContext(), "No Eateries available.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                eateryRecyclerAdapter.setFilteredList(filteredEateryList);
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
        }
        else{
            db.collection("Eateries")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if(error != null){
                                if(progressDialog.isShowing())
                                    progressDialog.dismiss();
                                Log.e("Firestore Error", error.getMessage());
                            }

                            for (DocumentChange dc: value.getDocumentChanges()){
                                if (dc.getType() == DocumentChange.Type.ADDED){
                                    eateryArrayList.add(dc.getDocument().toObject(Eatery.class));
                                }
                                eateryRecyclerAdapter.notifyDataSetChanged();

                                if(progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        }
                    });
        }

    }

    @Override
    public void onBackPressed(){
        Intent currIntent = new Intent(DashboardActivity.this,MainActivity.class);

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
    public void onItemClick(int position) {
        Toast.makeText(getApplicationContext(), eateryArrayList.get(position).getName() + " was clicked!", Toast.LENGTH_SHORT).show();

    }
}
