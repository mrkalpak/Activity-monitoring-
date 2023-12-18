package com.example.finalyearproject_android.BackgroundProcesses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.finalyearproject_android.Fragments.HomeFragment;
import com.example.finalyearproject_android.LoginActivity;
import com.example.finalyearproject_android.Models.ModelUser;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class StartServiceMethods {
    
    ProgressDialog progressDialog;
    Context context;
    Activity activity;
    FirebaseAuth auth;
    DatabaseReference reference;
    LocalDatabase localDatabase;
    String today;
    long oldSteps= 0;

    public StartServiceMethods(Context context, Activity activity) {

        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching Data!!!");
        progressDialog.setCancelable(false);

        this.context = context;
        this.activity = activity;

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        localDatabase = new LocalDatabase(context);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        today = formatter.format(date);
    }

    public void initService() {
        progressDialog.show();
        //fetch the user data
        String uid = auth.getCurrentUser().getUid();
        if (CommonData.isNetworkAvailable(context)){

            reference.child("Users/"+uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try {
                        CommonData.userData=snapshot.getValue(ModelUser.class);
                        assert CommonData.userData != null;
                        localDatabase.setUserData(CommonData.userData);
                        progressDialog.cancel();
                    }catch (Exception ignored){
                        CommonData.userData = localDatabase.getUser(uid);
                        progressDialog.cancel();
                    }
                    startSensorService();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error to fetch user data", Toast.LENGTH_SHORT).show();
                    context.startActivity(new Intent(context, LoginActivity.class));
                    progressDialog.cancel();
                    activity.finish();
                }
            });
        }else{
            CommonData.userData = localDatabase.getUser(uid);
            progressDialog.cancel();
            startSensorService();
        }
    }

    private void startSensorService(){
        long localSteps = Long.parseLong(localDatabase.getSteps(Objects.requireNonNull(auth.getCurrentUser()).getUid(),today));

        try {
            if (CommonData.isNetworkAvailable(context)){
                reference.child("Data").child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(today)){
                            try {
                                oldSteps = Long.parseLong(Objects.requireNonNull(snapshot.child(today).getValue(String.class)));
                            }catch (Exception e){
                                Log.e("ERROR","Error = "+e.getMessage());
                            }
                        }else{
                            oldSteps = 0;
                        }
                        long steps = oldSteps;
                        //validation of data fetched and local stored data
                        if (localSteps > oldSteps){
                            steps = localSteps;
                        }
                        CommonData.steps=steps;
                        try {
                            Intent serviceIntent = new Intent(context, ServiceSensorsInitializer.class);
                            serviceIntent.putExtra("steps",steps+"");
                            activity.startService(serviceIntent);
                        }catch (Exception ignored){}
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Error => "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }else{
                CommonData.steps=localSteps;
                Intent serviceIntent = new Intent(context, ServiceSensorsInitializer.class);
                serviceIntent.putExtra("steps",localSteps+"");
                activity.startService(serviceIntent);
            }
            LocalBroadcastManager.getInstance(context).registerReceiver(HomeFragment.broadcastReceiver,new IntentFilter("AIBasedActivityMonitoring"));
        }catch (Exception e){
            Log.e("Error ", e.getMessage());
        }
    }
}
