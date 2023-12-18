package com.example.finalyearproject_android.DietPlan;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject_android.R;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CustomDietActivity extends AppCompatActivity {

    String foodTime;
    EditText name, calories;

    FirebaseUser user;
    DatabaseReference reference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_diet);

        foodTime = "";
        name = findViewById(R.id.diet_name_input);
        calories = findViewById(R.id.diet_calories_input);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Updating Data");
        progressDialog.setCancelable(false);

        reference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        String nameText = "no data";
        String caloriesText = "no data";
        try{
            nameText = getIntent().getStringExtra("name");
            caloriesText = getIntent().getStringExtra("calories");
        }catch (Exception e){
            Log.e("ERROR", "Error => "+e.getMessage());
        }

        try {
            if (!(nameText.equalsIgnoreCase(""))){
                name.setText(nameText);
                name.setEnabled(false);
            }
            if (!(caloriesText.equalsIgnoreCase(""))){
                calories.setText(caloriesText);
                calories.setEnabled(false);
            }
        }catch (Exception ignored) {}
    }

    public void addDiet(View view) {
        if (!validateData()){
            return;
        }
//        add data to database
        if (!CommonData.isNetworkAvailable(this)){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Needs Internet Connection")
                    .setMessage("Unable to connect to internet.")
                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }
//        update
        progressDialog.show();
//        List<ModelDiet> diets = new DietDatabase(this).getDiet();
//        diets.add(new ModelDiet(name.getText().toString(),calories.getText().toString(),foodTime));
//        new DietDatabase(this).addDiet(diets);
//        TODO:update online
//        reference.child("Diet").child(user.getUid()).child(name.getText().toString()).setValue(new ModelDiet(name.getText().toString(),calories.getText().toString(),foodTime))
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()){
//                        Toast.makeText(this, "Diet Updated", Toast.LENGTH_SHORT).show();
//                        finish();
//                    }
//                    progressDialog.dismiss();
//                }).addOnFailureListener(e -> {
//                    Toast.makeText(this, "Unable to add =>"+e.getMessage(), Toast.LENGTH_SHORT).show();
//                    try{
//                        progressDialog.show();
//                    }catch (Exception ignored){}
//                });
    }

    private boolean validateData() {
        if (foodTime.equalsIgnoreCase("")){
            ((RadioButton)findViewById(R.id.radio_breakfast)).setError("Required");
            return false;
        }
        if (name.getText().toString().equalsIgnoreCase("")){
            name.setError("Required Field");
            return false;
        }
        if (calories.getText().toString().equalsIgnoreCase("")){
            calories.setError("Required Field");
            return false;
        }
        return true;
    }

    public void breakfast(View view) {
        foodTime = "breakfast";
    }

    public void lunch(View view) {
        foodTime = "lunch";
    }

    public void dinner(View view) {
        foodTime = "dinner";
    }

    public void endActivity(View view) {
        finish();
    }
}