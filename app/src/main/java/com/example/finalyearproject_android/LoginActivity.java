package com.example.finalyearproject_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalyearproject_android.BackgroundProcesses.StartServiceMethods;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth auth;
    DatabaseReference reference;
    EditText emailInput, passwordInput;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            auth.signOut();
        }
        reference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Logging You In.");
        progressDialog.setCancelable(false);

        emailInput = findViewById(R.id.loginEmailInput);
        passwordInput = findViewById(R.id.loginPasswordInput);

    }

    public void goHome(View view) {
        finish();
    }

    public void loginUser(View view) {
        if(!validateData()){
            return;
        }
        progressDialog.show();
        auth.signInWithEmailAndPassword(emailInput.getText().toString(),passwordInput.getText().toString()).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                FirebaseUser user = auth.getCurrentUser();
//                check if the user is new or old
                reference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (user != null) {
                            if (snapshot.hasChild(user.getUid())){
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startSensorService();
                                startActivity(intent);
                            }else{
                                Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                        progressDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getApplicationContext(), "Error => " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(LoginActivity.this, "Error => " + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private void startSensorService() {
        try {
            new StartServiceMethods(this, this).initService();
        } catch (Exception exception) {
            Log.e("Service Error => ", exception.getMessage());
        }
    }

    public boolean validateData() {
        if (emailInput.getText().toString().equalsIgnoreCase("")) {
            emailInput.setError("Required Field");
            return false;
        }
        if (passwordInput.getText().toString().equalsIgnoreCase("")) {
            passwordInput.setError("Required Field");
            return false;
        }
        return true;
    }
}