package com.example.finalyearproject_android.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject_android.DietPlan.AdapterFood;
import com.example.finalyearproject_android.DietPlan.DietDatabase;
import com.example.finalyearproject_android.DietSug.APIService;
import com.example.finalyearproject_android.DietSug.DietAddActivity;
import com.example.finalyearproject_android.DietSug.Food;
import com.example.finalyearproject_android.MainActivity;
import com.example.finalyearproject_android.Models.ModelUser;
import com.example.finalyearproject_android.R;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DietFragment extends Fragment {

    RecyclerView dietRecycler;

    FirebaseAuth auth;
    DatabaseReference reference;
    DietDatabase localDatabase;

    ProgressDialog progressDialog;
    Activity activity;
    Context context;

    int pageCount = 0;
    AdapterFood adapterFood;
    RecyclerView diet_recycler_view;
    List<Food> food = new ArrayList<>();
    TextView calIndicator;
    double average = 0;
    public DietFragment() {
        // Required empty public constructor
    }
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diet, container, false);
        activity = requireActivity();
        context = requireContext();
        if (pageCount==0) {
            int selectedItemId = MainActivity.navBar.getSelectedItemId();
            if (selectedItemId != R.id.navigation_diet) {
                try {
                    MainActivity.navBar.setSelectedItemId(R.id.navigation_diet);
                }catch (Exception e){
                    Log.e("ERROR",e.getMessage());
                }
            }
            pageCount++;
        }

        calIndicator = view.findViewById(R.id.calIndicator);

        dietRecycler = view.findViewById(R.id.diet_recycler_view);

        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser()==null){
            return view;
        }
        reference = FirebaseDatabase.getInstance().getReference();
        localDatabase = new DietDatabase(context);

        food = localDatabase.getDiet();
        System.out.println(food.size());
        adapterFood = new AdapterFood(context, food);
        dietRecycler.setLayoutManager(new LinearLayoutManager(context));
        dietRecycler.setAdapter(adapterFood);
        double totalCal = 0;
        for(Food f : food){
            totalCal += f.getCalories() * f.getQty();
        }
        calIndicator.setText(totalCal+" cal / "+(int)average+" cal");



        view.findViewById(R.id.fab_add_diet).setOnClickListener(v -> startActivity(new Intent(context, DietAddActivity.class)));

        view.findViewById(R.id.refreshButton).setOnClickListener(v->{
            food = localDatabase.getDiet();
            System.out.println(food.size());
            adapterFood = new AdapterFood(context, food);
            dietRecycler.setLayoutManager(new LinearLayoutManager(context));
            dietRecycler.setAdapter(adapterFood);
            double tc = 0;
            for(Food f : food){
                tc += f.getCalories() * f.getQty();
            }
            calIndicator.setText(tc+" cal / "+(int)average+" cal");
        });

        System.out.println("SAKJIHNDKJSANBDKJSANDJKASN");
        APIService apiService = new APIService();
        apiService.setOnDataReceivedListener(result -> {
            if (result != null) {
                System.out.println(result);
                try {
                    // Parse the JSON response
                    JSONObject jsonResponse = new JSONObject(result);
                    JSONObject predictions = jsonResponse.getJSONObject("predictions");

                    // Extract values for each model
                    double decisionValue = predictions.getDouble("Decision");
                    double gradientValue = predictions.getDouble("Gradient");
                    double linearValue = predictions.getDouble("Linear");
                    double randomValue = predictions.getDouble("Random");
                    double supportValue = predictions.getDouble("Support");

                    // Calculate the average
                    average = (decisionValue + gradientValue + linearValue + randomValue + supportValue) / 5;

                    // Print the average
                    System.out.println("Average: " + average);
                    double t = 0;
                    for(Food f : food){
                        t += f.getCalories() * f.getQty();
                    }
                    calIndicator.setText(t+" cal / "+(int)average+" cal");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Handle error or show a message to the user
                System.out.println("Error fetching data from API");
            }
        });
        String uid = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        reference.child("Users/"+uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser user = snapshot.getValue(ModelUser.class);
                if (user!=null){
                    String apiUrl = "https://finalyrproject.pythonanywhere.com/predict";
                    String jsonData = "{ \"age\": "+user.getAge()+", \"height\": "+user.getHeight()+", \"weight\": "+user.getWeight()+", \"gender\": "+(user.getGender().equals("male") ? 0 : 1)+" }";
                    System.out.println(jsonData);
                    apiService.execute(apiUrl, jsonData);
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
// Replace the URL with your actual API endpoint


        return view;
    }



}