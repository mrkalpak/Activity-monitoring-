package com.example.finalyearproject_android.Fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.finalyearproject_android.MainActivity;
import com.example.finalyearproject_android.Models.ModelUser;
import com.example.finalyearproject_android.NewUserActivity;
import com.example.finalyearproject_android.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    int pageCount = 0;

    FirebaseAuth auth;
    DatabaseReference reference;

    CircleImageView profileImage;
    EditText profileName, profileEmail, profileHeight, profileWeight;
    TextView saveBtn;

    public ProfileFragment() {}
    // Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        if (pageCount==0) {
            int selectedItemId = MainActivity.navBar.getSelectedItemId();
            if (selectedItemId != R.id.navigation_profile) {
                try {
                    MainActivity.navBar.setSelectedItemId(R.id.navigation_profile);
                }catch (Exception e){
                    Log.e("ERROR",e.getMessage());
                }
            }
            pageCount++;
        }

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        profileImage = view.findViewById(R.id.profile_image);
        profileName = view.findViewById(R.id.profile_name);
        profileEmail = view.findViewById(R.id.profile_email);
        profileHeight = view.findViewById(R.id.profile_height);
        profileWeight = view.findViewById(R.id.profile_weight);
        saveBtn = view.findViewById(R.id.saveBtn);

        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        reference.child("Users/"+uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if (user!=null){
                    setView(user);
                }else{
                    Toast.makeText(requireContext(), "Invalid URL", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                try {
                    Toast.makeText(requireContext(), "Error => " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }catch (Exception ignored){}
            }
        });

        saveBtn.setOnClickListener(v -> updateData());

        view.findViewById(R.id.profile_image).setOnClickListener(v -> getImage());
        view.findViewById(R.id.profile_image_icon).setOnClickListener(v -> getImage());
        return view;
    }


    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,11);
    }


    private void updateData() {
        HashMap<String, Object> updateData = new HashMap<>();
        updateData.put("name",profileName.getText().toString());
        updateData.put("email",profileEmail.getText().toString());
        updateData.put("height" , profileHeight.getText().toString());
        updateData.put("weight", profileWeight.getText().toString());
        reference.child("Users/"+auth.getCurrentUser().getUid()).updateChildren(updateData).addOnCompleteListener(task -> {
            Toast.makeText(requireContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
        });
    }

    private void setView(ModelUser user) {
        profileName.setText(user.getName());
        profileEmail.setText(user.getEmail());
        profileHeight.setText(user.getHeight());
        profileWeight.setText(user.getWeight());
        if (!user.getImage().equalsIgnoreCase("default")){
            Glide.with(requireActivity()).load(user.getImage()).into(profileImage);
        }
    }
}