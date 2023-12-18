package com.example.finalyearproject_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalyearproject_android.BackgroundProcesses.LocalDatabase;
import com.example.finalyearproject_android.Models.ModelGoal;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetGoalActivity extends AppCompatActivity {

    EditText steps, calories;
    LinearLayout submitBtn;
    TextView updateGoalText;

    ModelGoal goal;

    FirebaseAuth auth;
    DatabaseReference reference;

    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_goal);

        //initialize firebase
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        if (auth.getCurrentUser()==null){
            Toast.makeText(this, "User Not Signed in", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching Your Data");
        progressDialog.setCancelable(false);
        progressDialog.show();

        //initialize locals
        steps = findViewById(R.id.step_goal_text_view);
        calories = findViewById(R.id.calories_goal_text_view);
        updateGoalText = findViewById(R.id.updateGoalText);
        submitBtn = findViewById(R.id.updateGoalButton);
        disableInputs();

        TextView name = findViewById(R.id.goal_user_name);
        name.setText(CommonData.userData.getName());
        CircleImageView imageView = findViewById(R.id.goal_profile_image);
        if (!CommonData.userData.getImage().equalsIgnoreCase("default")){
            Glide.with(this).load(CommonData.userData.getImage()).into(imageView);
        }


        reference.child("Goals").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                goal = snapshot.getValue(ModelGoal.class);
                if (goal==null) {
                    steps.setText("500");
                    calories.setText("200");
                    goal = new ModelGoal("500","200");
                    updateGoals();
                }else {
                    steps.setText(goal.getStepsGoal());
                    calories.setText(goal.getCaloriesGoal());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SetGoalActivity.this, "Error => "+error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });

        submitBtn.setOnClickListener(v -> {
            if(updateGoalText.getText().toString().equalsIgnoreCase("update goal")) {
                updateGoals();
            }else{
                enableInputs();
            }
        });
    }

    public void updateGoals(){
        if(!checkData()){
            return;
        }
        HashMap<String, Object> newGoal = new HashMap<>();
        newGoal.put("stepsGoal",steps.getText().toString());
        newGoal.put("caloriesGoal",calories.getText().toString());
        CommonData.goal.setCaloriesGoal(calories.getText().toString());
        CommonData.goal.setStepsGoal(steps.getText().toString());
        reference.child("Goals").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).updateChildren(newGoal).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                new LocalDatabase(this).insertGoal(auth.getCurrentUser().getUid(),steps.getText().toString(),calories.getText().toString());
                Toast.makeText(this, "Data Updated", Toast.LENGTH_SHORT).show();
                disableInputs();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error => "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void disableInputs() {
        updateGoalText.setText("Edit Goal");
        calories.setEnabled(false);
        steps.setEnabled(false);
    }

    private void enableInputs() {
        updateGoalText.setText("Update Goal");
        steps.setEnabled(true);
        calories.setEnabled(true);
    }


    private boolean checkData() {
        if (calories.getText().toString().equalsIgnoreCase("")){
            calories.setError("Required Field");
            return false;
        }
        if (steps.getText().toString().equalsIgnoreCase("")){
            steps.setError("Required Field");
            return false;
        }
        return true;
    }

    public void endActivity(View view) {
        finish();
    }
}