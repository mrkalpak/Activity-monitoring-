package com.example.finalyearproject_android;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject_android.Adapters.AdapterFaq;
import com.example.finalyearproject_android.BackgroundProcesses.LocalDatabase;
import com.example.finalyearproject_android.Models.ModelFaq;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FAQActivity extends AppCompatActivity {

    RecyclerView faqRecycler;
    List<ModelFaq> faqs;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_f_a_q);

        faqRecycler = findViewById(R.id.faq_recycler_view);
        reference = FirebaseDatabase.getInstance().getReference();

        faqs = new ArrayList<>();

        faqs = new LocalDatabase(this).getFaqList();

        if (CommonData.isNetworkAvailable(this)){
            reference.child("FAQ").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<ModelFaq> faqList = new ArrayList<>();
                    for (DataSnapshot snapshot1:snapshot.getChildren()){
                        if (snapshot1!=null){
                            ModelFaq faq = snapshot1.getValue(ModelFaq.class);
                            if((faq != null ? faq.getId() : null) !=null){
                                faqList.add(faq);
                            }
                        }
                    }
                    faqs = faqList;
                    new LocalDatabase(getApplicationContext()).addFaq(faqList);

                    faqRecycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    faqRecycler.setAdapter(new AdapterFaq(faqs, getApplicationContext()));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        faqRecycler.setLayoutManager(new LinearLayoutManager(this));
        faqRecycler.setAdapter(new AdapterFaq(faqs, this));

    }
}