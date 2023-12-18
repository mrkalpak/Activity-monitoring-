package com.example.finalyearproject_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.finalyearproject_android.BackgroundProcesses.LocalDatabase;
import com.example.finalyearproject_android.BackgroundProcesses.StartServiceMethods;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    ProgressDialog progressDialog;

    FirebaseAuth auth;
    DatabaseReference reference;
    GoogleSignInClient mGoogleSignInClient;

    private final int RC_SIGN_IN = 10; //constant identifier for intent

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

//        initialize firebase
        auth = FirebaseAuth.getInstance();
        if(auth.getCurrentUser()!=null){
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        reference = FirebaseDatabase.getInstance().getReference();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("726445186162-ihoh4l1vgru2ahjebhh3u3269u21drte.apps.googleusercontent.com")
                .requestEmail()
                .build();
         mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

//        set default variables

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching Data!!!");
        progressDialog.setCancelable(false);
        try {
            if (new LocalDatabase(this).isDarkModeEnabled()){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }catch (Exception e){
            Log.e("ERROR",e.getMessage());
        }

//        sign in with google
        findViewById(R.id.signInWithGoogle).setOnClickListener(v -> {
//            onclick listener for sign in with google button
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            progressDialog.show();
        });

        findViewById(R.id.signInWithEmailAndPassword).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));

        findViewById(R.id.registerUserLink).setOnClickListener(v -> startActivity(new Intent(this, RegistrationActivity.class)));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//         Result returned from launching the Intent for google sign in
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
//                 Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
//                 Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Error While Login => "+e.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
//                 Sign in success, update UI with the signed-in user's information
                FirebaseUser user = auth.getCurrentUser();
//                check if the user is new or old
                reference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (user != null) {
                            if (snapshot.hasChild(user.getUid())){
                                startSensorService();
                                startActivity(new Intent(HomeActivity.this, MainActivity.class));
                            }else{
                                startActivity(new Intent(HomeActivity.this, NewUserActivity.class));
                            }
                        }
                        progressDialog.dismiss();
                        finish();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(HomeActivity.this, "Error => "+error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
            } else {
//                 If sign in fails, display a message to the user.
                Toast.makeText(this, "Error => " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void startSensorService() {
        try {
            new StartServiceMethods(this, this).initService();
        } catch (Exception exception) {
            Log.e("Service Error => ", exception.getMessage());
        }
    }
}