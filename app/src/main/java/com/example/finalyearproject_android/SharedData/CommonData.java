package com.example.finalyearproject_android.SharedData;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.finalyearproject_android.AIAssistant.ModelMessage;
import com.example.finalyearproject_android.AIAssistant.ModelOpenPage;
import com.example.finalyearproject_android.HomeActivity;
import com.example.finalyearproject_android.Models.ModelGoal;
import com.example.finalyearproject_android.Models.ModelUser;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CommonData {
    public static long steps = 0;
    public static ModelUser userData = null;
    public static List<ModelMessage> messages = new ArrayList<>();
    public static List<ModelOpenPage> openPageList;
    public static long lastTime = 0;
    public static ModelGoal goal = null;

    public static boolean isNetworkAvailable(Context ctx){
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void logout(Context context, Activity activity){
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
