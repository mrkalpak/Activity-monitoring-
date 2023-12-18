package com.example.finalyearproject_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegistrationActivity extends AppCompatActivity {

    EditText email, password, confirmPassword;

    ProgressDialog progressDialog;

    FirebaseAuth auth;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        email = findViewById(R.id.reg_email_address);
        password = findViewById(R.id.reg_password);
        confirmPassword = findViewById(R.id.reg_confirm_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading!!!");
        progressDialog.setMessage("Creating user");
        progressDialog.setCancelable(false);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser()!=null){
            auth.signOut();
        }
        reference = FirebaseDatabase.getInstance().getReference();

    }

    public void registerUser(View view) {
        if (!validateData()){
            return;
        }
        progressDialog.show();
        auth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString()).addOnSuccessListener(authResult -> {
            FirebaseUser user = auth.getCurrentUser();
            // check if the user is new or old
            reference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (user != null) {
                        if (snapshot.hasChild(user.getUid())){
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }else{
                            Intent intent = new Intent(getApplicationContext(), NewUserActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);                            }
                    }
                    progressDialog.dismiss();
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getApplicationContext(), "Error => "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error=>" + e.getMessage(), Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });
    }

    private boolean validateData() {
        if (email.getText().toString().equalsIgnoreCase("")){
            email.setError("Required Field");
            return false;
        }
        if (password.getText().toString().equalsIgnoreCase("")){
            password.setError("Required Field");
            return false;
        }
        if (confirmPassword.getText().toString().equalsIgnoreCase("")){
            confirmPassword.setError("Required Field");
            return false;
        }
        if (!password.getText().toString().equalsIgnoreCase(confirmPassword.getText().toString())){
            password.setError("Passwords not matched...");
            return false;
        }
        return true;
    }
}