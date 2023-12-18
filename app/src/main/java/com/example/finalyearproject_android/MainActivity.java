package com.example.finalyearproject_android;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.finalyearproject_android.BackgroundProcesses.LocalDatabase;
import com.example.finalyearproject_android.Fragments.AssistantFragment;
import com.example.finalyearproject_android.Fragments.DietFragment;
import com.example.finalyearproject_android.Fragments.HomeFragment;
import com.example.finalyearproject_android.Fragments.ProfileFragment;
import com.example.finalyearproject_android.Fragments.SettingsFragment;
import com.example.finalyearproject_android.Models.ModelFaq;
import com.example.finalyearproject_android.Models.ModelGoal;
import com.example.finalyearproject_android.Models.ModelSteps;
import com.example.finalyearproject_android.Models.ModelUser;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static BottomNavigationView navBar;
    public static FragmentManager fragmentManager;

    private ProgressDialog progressDialog;
    private DatabaseReference reference;
    private FirebaseUser user;
    private LocalDatabase localDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navBar = findViewById(R.id.navigation_bar);
        navBar.setOnNavigationItemSelectedListener(this);
        localDatabase = new LocalDatabase(this);
        try {
            if (localDatabase.isDarkModeEnabled()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }catch (Exception e){
            Log.e("ERROR",e.getMessage());
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user==null){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }
        reference = FirebaseDatabase.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching Required Data");
        progressDialog.setCancelable(false);
        try {
            progressDialog.show();
        }catch (Exception ignored){}
        if (CommonData.isNetworkAvailable(this)){
            reference.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ModelUser userModel = snapshot.getValue(ModelUser.class);
                    if (userModel!=null){
                        CommonData.userData=userModel;
                    }else{
                        CommonData.userData = localDatabase.getUser(user.getUid());
                    }
                    reference.child("Data").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            List<ModelSteps> stepsList = new ArrayList<>();
                            for (DataSnapshot snapshot1:snapshot.getChildren()){
                                ModelSteps stepModel = snapshot1.getValue(ModelSteps.class);
                                if (stepModel != null){
                                    stepsList.add(stepModel);
                                }
                            }
//                            add step list in database
                            localDatabase.addOldSteps(stepsList, user.getUid());
//                            start displaying fragments
                            reference.child("Goals").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    ModelGoal goal = snapshot.getValue(ModelGoal.class);
                                    if (goal!=null){
                                        CommonData.goal = goal;
                                        localDatabase.insertGoal(user.getUid(),goal.getStepsGoal(),goal.getCaloriesGoal());
                                    }else{
                                        CommonData.goal = localDatabase.getGoal(user.getUid());
                                    }
                                    attachFragment();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(MainActivity.this, "Error Database 2 => "+error.getMessage(), Toast.LENGTH_SHORT).show();
                                    attachFragment();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(MainActivity.this, "Error => "+error.getMessage(), Toast.LENGTH_SHORT).show();
                            attachFragment();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Error Database 1 => "+error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            reference.child("FAQ").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<ModelFaq> faqs = new ArrayList<>();
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        if (snapshot1!=null){
                            ModelFaq faq = snapshot1.getValue(ModelFaq.class);
                            if((faq != null ? faq.getId() : null) !=null){
                                faqs.add(faq);
                            }
                        }
                    }
                    localDatabase.addFaq(faqs);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }else{
            CommonData.userData = new LocalDatabase(this).getUser(user.getUid());
            attachFragment();
        }

    }

    private void attachFragment() {
        try {
            fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.bottom_navigation_frame, new HomeFragment()).commit();
            progressDialog.dismiss();
        } catch (Exception ignored){}
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = new HomeFragment();
        switch (item.getItemId()){
            case R.id.navigation_home:
                selectedFragment = new HomeFragment();
                break;
            case R.id.navigation_diet:
                selectedFragment = new DietFragment();
                break;
            case R.id.navigation_assistant:
                selectedFragment = new AssistantFragment();
                break;
            case R.id.navigation_settings:
                selectedFragment = new SettingsFragment();
                break;
            case R.id.navigation_profile:
                selectedFragment = new ProfileFragment();
                break;
            default:
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                break;
        }
        fragmentManager.beginTransaction().replace(R.id.bottom_navigation_frame, selectedFragment).commit();
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( progressDialog!=null && progressDialog.isShowing() ){
            progressDialog.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if ( progressDialog!=null && progressDialog.isShowing() ){
            progressDialog.cancel();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==11 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri imageUri = data.getData();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Image Uploading");
            progressDialog.show();

            StorageReference uploadImage = FirebaseStorage.getInstance().getReference().child("images/"+user.getUid());

            uploadImage.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(uri -> {
                            String imageUrl=uri.toString();
                            reference.child("Users/"+user.getUid()+"/image").setValue(imageUrl);
                        });

                    })
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Failed to upload"+exception, Toast.LENGTH_SHORT).show();
                    }).addOnProgressListener(snapshot -> {
                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                progressDialog.setMessage("Uploading :- "+(int)progressPercent+" %");
            });
        }else{
            Toast.makeText(this, "Operation Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

}