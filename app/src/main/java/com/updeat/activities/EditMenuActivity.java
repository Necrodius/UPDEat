package com.updeat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.updeat.R;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditMenuActivity extends AppCompatActivity {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private ListView lstMenu;
    private Button btnAddItem, btnConfirm;
    private TextView txtAvePrice;
    String EateryID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editmenu);

        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        String uid = firebaseUser.getUid();

        lstMenu = findViewById(R.id.lstEateryMenu);
        btnAddItem = findViewById(R.id.btnAddItem);
        btnConfirm = findViewById(R.id.btnSaveMenu);
        txtAvePrice = findViewById(R.id.txtAvePrice);

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

        btnAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewAddMenuItem();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewEditEatery();
            }
        });

    }
    public void getEateryInfo(String EateryID) {
        DocumentReference eateryRef = db.collection("Eateries").document(EateryID);
        eateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Map<String, List<String>> menu = (HashMap<String,List<String>>) documentSnapshot.getData().get("Menu");
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
    public void displayMenuItems(Map<String,List<String>> menu) {
        List<HashMap<String, String>> menuItems = new ArrayList<>();
        List<String> menuItemNames = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(this, menuItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        for (String key : menu.keySet()) {
            menuItemNames.add(key);
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
        double Total = 0;
        for (int k = 0; k<menuItemNames.size(); k++){
            String[] separated = menuItemNames.get(k).split("-");
            String name = separated[0];
            double price = Double.parseDouble(separated[1]);
            Total += price;
        }

        double a = Total/menuItemNames.size();
        double AveragePrice = Math.round(a*100.0)/100.0;
        // If AveragePrice {0-200} == Budget: 1
        // {200-400} == Budget: 2, else; Budget: 3
        int budget;
        if (AveragePrice <= 200){
            budget = 1;
        } else if (AveragePrice > 200 && AveragePrice <= 400){
            budget = 2;
        } else {
            budget = 3;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("averageprice", AveragePrice);
        data.put("budget", budget);

        txtAvePrice.setText(Double.toString(AveragePrice));

        db.collection("Eateries").document(EateryID)
                .set(data, SetOptions.merge());
        lstMenu = findViewById(R.id.lstEateryMenu);
        lstMenu.setAdapter(adapter);

        lstMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getApplicationContext(), menuItemNames.get(position), Toast.LENGTH_SHORT).show();

                AlertDialog.Builder builder = new AlertDialog.Builder(EditMenuActivity.this);
                builder.setTitle(menuItemNames.get(position));

                builder.setMessage("Remove this Menu Item?");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!isOnline()) {
                            Toast.makeText(getApplicationContext(),"No Internet Connection!",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Map<String, Object> deleteItem = new HashMap<>();
                            deleteItem.put("Menu." + menuItemNames.get(position), FieldValue.delete());

                            FirebaseFirestore.getInstance()
                                    .collection("Eateries")
                                    .document(EateryID)
                                    .update(deleteItem);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });

                builder.show();
            }
        });
    }

    public void openViewAddMenuItem() {
        startActivity(new Intent(this, AddMenuItemActivity.class));

    }

    public void openViewEditEatery() {
        startActivity(new Intent(this, EditEateryActivity.class));
    }


}