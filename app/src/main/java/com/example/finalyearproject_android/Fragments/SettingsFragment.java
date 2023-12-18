package com.example.finalyearproject_android.Fragments;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.finalyearproject_android.AboutUsActivity;
import com.example.finalyearproject_android.BackgroundProcesses.LocalDatabase;
import com.example.finalyearproject_android.BackgroundProcesses.ServiceSensorsInitializer;
import com.example.finalyearproject_android.BackgroundProcesses.StartServiceMethods;
import com.example.finalyearproject_android.FAQActivity;
import com.example.finalyearproject_android.MainActivity;
import com.example.finalyearproject_android.R;
import com.example.finalyearproject_android.SetGoalActivity;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {

    SwitchCompat stepSwitch, darkModeSwitch;
    FirebaseAuth auth;
    DatabaseReference reference;

    int pageCount = 0;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        if (pageCount==0) {
            int selectedItemId = MainActivity.navBar.getSelectedItemId();
            if (selectedItemId != R.id.navigation_settings) {
                try {
                    MainActivity.navBar.setSelectedItemId(R.id.navigation_settings);
                }catch (Exception e){
                    Log.e("ERROR",e.getMessage());
                }
            }
            pageCount++;
        }

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        CircleImageView profile = view.findViewById(R.id.settings_profile_image);
        try{
            if (CommonData.userData.getImage()!=null){
                if (!CommonData.userData.getImage().equals("default")){
                    Glide.with(this).load(CommonData.userData.getImage()).into(profile);
                }
            }
            if(CommonData.userData.getName()!=null){
                if (!CommonData.userData.getName().equalsIgnoreCase("")){
                    ((TextView)view.findViewById(R.id.settings_user_name)).setText(CommonData.userData.getName());
                }
            }
        }catch (Exception ignored){}

        stepSwitch = view.findViewById(R.id.sensor_switch);
        darkModeSwitch = view.findViewById(R.id.switch_dark_mode_switch);


        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES){
            darkModeSwitch.setChecked(true);
        }
        if (isMyServiceRunning()){
            stepSwitch.setChecked(true);
        }
        for (UserInfo user: Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getProviderData()) {
            if (user.getProviderId().equals("google.com")) {
                view.findViewById(R.id.change_password_settings).setVisibility(View.GONE);
            }
        }

        stepSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                view.findViewById(R.id.step_recorder_message).setVisibility(View.GONE);
                new StartServiceMethods(requireContext(), requireActivity()).initService();
            }else{
                AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
                alert.setTitle("Alert!!!")
                        .setMessage("Do You Want To Stop Service?")
                        .setCancelable(false)
                        .setPositiveButton("YES", (dialog, which) -> {
                            requireActivity().stopService(new Intent(requireContext(), ServiceSensorsInitializer.class));
                            dialog.dismiss();
                            view.findViewById(R.id.step_recorder_message).setVisibility(View.VISIBLE);
                        })
                        .setNegativeButton("NO", (dialog, which) -> {
                            dialog.dismiss();
                            stepSwitch.setChecked(true);
                        })
                        .show();

            }
        });

        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                try {
                    new LocalDatabase(requireContext()).setDarkModeStatus(true);
                }catch (Exception ignored){}
            }else{
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                try {
                    new LocalDatabase(requireContext()).setDarkModeStatus(false);
                }catch (Exception ignored){}
            }
        });

        view.findViewById(R.id.goal_settings).setOnClickListener(v -> startActivity(new Intent(requireContext(), SetGoalActivity.class)));

        view.findViewById(R.id.change_password_settings).setOnClickListener(v -> {
            AlertDialog.Builder alert = new AlertDialog.Builder(requireContext());
            alert.setTitle("Reset Password")
                    .setMessage("Do You want to reset password?")
                    .setCancelable(false)
                    .setPositiveButton("YES", (dialog, which) -> {
                        Toast.makeText(requireContext(), "Sending Link", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        auth.sendPasswordResetEmail(Objects.requireNonNull(Objects.requireNonNull(auth.getCurrentUser()).getEmail())).addOnSuccessListener(aVoid -> {
                            Toast.makeText(requireContext(), "Password Reset Link sent to respective email ID", Toast.LENGTH_SHORT).show();
                        }).addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error => " + e, Toast.LENGTH_SHORT).show();
                            Log.e("ERROR", e + "");
                        });
                    })
                    .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                    .show();

        });

        view.findViewById(R.id.faq_section).setOnClickListener(v -> startActivity(new Intent(requireContext(), FAQActivity.class)));

        view.findViewById(R.id.about_us_section).setOnClickListener(v -> startActivity(new Intent(requireContext(), AboutUsActivity.class)));

        view.findViewById(R.id.logout_section).setOnClickListener(v -> {
            CommonData.logout(requireContext(), requireActivity());
            CommonData.goal = null;
            CommonData.steps = 0;
        });

        return view;
    }


    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (ServiceSensorsInitializer.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}