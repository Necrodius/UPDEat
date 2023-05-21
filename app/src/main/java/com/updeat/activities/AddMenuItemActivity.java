package com.updeat.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddMenuItemActivity extends AppCompatActivity {

    private Button btnAddIngredient, btnConfirm;
    private ListView listIngredients;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    TextInputEditText textItemName;
    TextInputEditText textItemPrice;
    String EateryID;
    String ItemName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addmenuitem);

        btnAddIngredient = findViewById(R.id.btnAddIngredient);
        btnConfirm = findViewById(R.id.btnSaveMenu);
        textItemName = findViewById(R.id.edittxtItemName);
        textItemPrice = findViewById(R.id.edittxtItemPrice);
        listIngredients = findViewById(R.id.lstIngredients);

        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();
        String uid = firebaseUser.getUid();

        List<String> Ingredients = new ArrayList<>();

        DocumentReference usereateryRef = db.collection("UserEateryRelation").document(uid);
        usereateryRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            EateryID = documentSnapshot.getString("name");
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

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMenuItem(Ingredients);
                openViewEditMenu();
            }
        });

        btnAddIngredient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addIngredient(Ingredients);

            }
        });
    }

    public void createMenuItem(List<String> Ingredients) {

        String ItemName = textItemName.getText().toString();
        String ItemPrice = textItemPrice.getText().toString();

        if (!isOnline()) {
            Toast.makeText(getApplicationContext(),"No Internet Connection!",Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(ItemName)){
            textItemName.setError("Item Name cannot be empty!");
            textItemName.requestFocus();
        } else if (TextUtils.isEmpty(ItemPrice)){
            textItemPrice.setError("Item Price cannot be empty!");
            textItemPrice.requestFocus();
        }
        else {

            Map<String,Object> menu = new HashMap<>();
            String ingredients[] = new String[Ingredients.size()];
            for (int i = 0; i < Ingredients.size(); i++) {
                ingredients[i] = Ingredients.get(i);
            }
            Map<String,Object> item = new HashMap<>();
            String ItemMix = ItemName +" - "+ ItemPrice;
            item.put(ItemMix, Arrays.asList(ingredients));
            menu.put("Menu",item);

            db.collection("Eateries").document(EateryID).set(menu,SetOptions.merge());
        }
    }

    public void addIngredient(List<String> Ingredients) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Ingredient");


        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Ingredients.add(input.getText().toString());
                displayMenuItems(Ingredients);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    public void displayMenuItems(List<String> Ingredients) {
        List<HashMap<String, String>> menuItems = new ArrayList<>();

        SimpleAdapter adapter = new SimpleAdapter(this, menuItems, R.layout.list_item,
                new String[]{"First Line", "Second Line"},
                new int[]{R.id.text1, R.id.text2});

        for (String i : Ingredients) {
            HashMap<String,String> itemStrings = new HashMap<>();
            itemStrings.put("First Line",i);
            itemStrings.put("Second Line","");
            menuItems.add(itemStrings);
        }
        listIngredients = findViewById(R.id.lstIngredients);
        listIngredients.setAdapter(adapter);
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

    public void openViewEditMenu() {
        startActivity(new Intent(AddMenuItemActivity.this, EditMenuActivity.class));
    }
}