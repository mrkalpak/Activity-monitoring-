package com.example.finalyearproject_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalyearproject_android.BackgroundProcesses.LocalDatabase;
import com.example.finalyearproject_android.BackgroundProcesses.StartServiceMethods;
import com.example.finalyearproject_android.Models.ModelGoal;
import com.example.finalyearproject_android.Models.ModelUser;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewUserActivity extends AppCompatActivity {

    EditText name, phone, height, weight, age;
    TextView email;
    CircleImageView profileImage;
    String gender = "";

    FirebaseAuth auth;
    DatabaseReference reference;
    StorageReference storageReference;

    ModelUser user;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

//        initialize locals
        name = findViewById(R.id.new_user_name);
        phone = findViewById(R.id.new_user_phone);
        email = findViewById(R.id.new_user_email);
        profileImage = findViewById(R.id.new_user_image);
        height = findViewById(R.id.new_user_height);
        weight = findViewById(R.id.new_user_weight);
        age = findViewById(R.id.new_user_age);


//        initialize firebase
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

//        check if user is logged in
        if(auth.getCurrentUser()==null){
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

//        get email of the user
        String emailId = auth.getCurrentUser().getEmail();
        email.setText(emailId);
        if (auth.getCurrentUser().getDisplayName()!=null) {
            name.setText(auth.getCurrentUser().getDisplayName());
        }
        if (auth.getCurrentUser().getPhoneNumber()!=null){
            phone.setText(auth.getCurrentUser().getPhoneNumber());
        }

        user = new ModelUser(auth.getCurrentUser().getUid(),emailId);

//        listeners

        findViewById(R.id.new_register_btn).setOnClickListener(v -> {
            if (!validateData()){
                return;
            }
            user.setName(name.getText().toString());
            user.setPhone(phone.getText().toString());
            user.setHeight(height.getText().toString());
            user.setWeight(weight.getText().toString());
            user.setAge(age.getText().toString());
            user.setGender(gender);
            saveData();
        });

        findViewById(R.id.editProfileDiv).setOnClickListener(v -> getImage());
        findViewById(R.id.editImageBtn).setOnClickListener(v -> getImage());
        findViewById(R.id.resetBtn).setOnClickListener(v -> setDefaults());
    }

    private void setDefaults() {
        try {
            email.setText(Objects.requireNonNull(auth.getCurrentUser()).getEmail());
            name.setText(auth.getCurrentUser().getDisplayName());
            if (auth.getCurrentUser().getPhoneNumber() != null) {
                phone.setText(auth.getCurrentUser().getPhoneNumber());
            }
            phone.setText("");
            height.setText("");
            weight.setText("");
            age.setText("");
            ((RadioButton) findViewById(R.id.maleRadioButton)).setChecked(false);
            ((RadioButton) findViewById(R.id.femaleRadioButton)).setChecked(false);
        }catch (Exception e){
            Toast.makeText(this, "Error => "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,11);
    }

    private void saveData()  {
        reference.child("Users").child(user.getuId()).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ModelGoal goal = new ModelGoal("500","200");
                reference.child("Goals").child(Objects.requireNonNull(auth.getCurrentUser()).getUid()).setValue(goal).addOnCompleteListener(task1 -> {
                    Toast.makeText(NewUserActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                    new LocalDatabase(getApplicationContext()).createUser(user);
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    try {
                        new StartServiceMethods(NewUserActivity.this, NewUserActivity.this).initService();
                    } catch (Exception exception) {
                        Log.e("Service Error => ", exception.getMessage());
                    }
                    finish();
                });
            }
        }).addOnFailureListener(e -> Toast.makeText(NewUserActivity.this, "Error => "+e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private boolean validateData() {
        if (name.getText().toString().equalsIgnoreCase("")){
            name.setError("Required Field");
            return false;
        }
        if (phone.getText().toString().equalsIgnoreCase("")){
            phone.setError("Required Field");
            return false;
        }
        if (height.getText().toString().equalsIgnoreCase("")){
            height.setText(R.string.default_height);
        }
        if (age.getText().toString().equalsIgnoreCase("")){
            age.setText("20");
        }
        if (gender.equalsIgnoreCase("")){
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setCancelable(true)
                    .setTitle("ERROR")
                    .setMessage("Required Field : gender")
                    .setPositiveButton("OK", (dialog, which) -> dialog.cancel())
                    .show();
            return false;
        }
        if (weight.getText().toString().equalsIgnoreCase("")){
            weight.setError("Required Field");
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==11 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            Uri imageUri = data.getData();
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Image Uploading");
            progressDialog.show();

            StorageReference uploadImage = storageReference.child("images/"+auth.getCurrentUser().getUid());

            uploadImage.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewUserActivity.this, "Image Uploaded", Toast.LENGTH_SHORT).show();
                        Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                        task.addOnSuccessListener(uri -> {
                            String imageUrl=uri.toString();
                            user.setImage(imageUrl);
                            Glide.with(this).load(imageUri).into(profileImage);
                        });

                    })
                    .addOnFailureListener(exception -> {
                        progressDialog.dismiss();
                        Toast.makeText(NewUserActivity.this, "Failed to upload"+exception, Toast.LENGTH_SHORT).show();
                    }).addOnProgressListener(snapshot -> {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        progressDialog.setMessage("Uploading :- "+(int)progressPercent+" %");
                    });
        }else{
            Toast.makeText(this, "Operation Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void setGenderFemale(View view) {
        gender = "female";
    }

    public void setGenderMale(View view) {
        gender = "male";
    }

    public void goHome(View view) {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}