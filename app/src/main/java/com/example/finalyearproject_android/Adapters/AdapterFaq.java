package com.example.finalyearproject_android.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalyearproject_android.Models.ModelFaq;
import com.example.finalyearproject_android.R;

import java.util.List;

public class AdapterFaq extends RecyclerView.Adapter<AdapterFaq.ViewHolder> {

    List<ModelFaq> faqs;
    Context ctx;

    public AdapterFaq(List<ModelFaq> faqs, Context ctx) {
        this.faqs = faqs;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public AdapterFaq.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(ctx).inflate(R.layout.faq_recycler,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterFaq.ViewHolder holder, int position) {
        holder.setData(faqs, position);
    }

    @Override
    public int getItemCount() {
        return faqs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout quesLayout;
        TextView faqQuestion, faqDescription;
        ImageView button;
        LinearLayout ansLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            quesLayout = itemView.findViewById(R.id.faq_question_layout);
            ansLayout = itemView.findViewById(R.id.faq_answer_layout);
            faqQuestion = itemView.findViewById(R.id.faq_question);
            faqDescription = itemView.findViewById(R.id.faq_answer);
            button = itemView.findViewById(R.id.faq_d);
        }

        public void setData(List<ModelFaq> faqs, int position) {
            faqQuestion.setText(faqs.get(position).getQuestion());
            faqDescription.setText(faqs.get(position).getAnswer());
            button.setOnClickListener(v -> {
                if (ansLayout.getVisibility()==View.GONE){
                    ansLayout.setVisibility(View.VISIBLE);
                    Glide.with(ctx).load(R.drawable.ic_baseline_keyboard_arrow_up_24).into(button);
                }else{
                    Glide.with(ctx).load(R.drawable.ic_baseline_keyboard_arrow_down_24).into(button);
                    ansLayout.setVisibility(View.GONE);
                }
            });
            quesLayout.setOnClickListener(v -> {
                if (ansLayout.getVisibility()==View.GONE){
                    ansLayout.setVisibility(View.VISIBLE);
                    Glide.with(ctx).load(R.drawable.ic_baseline_keyboard_arrow_up_24).into(button);
                }else{
                    Glide.with(ctx).load(R.drawable.ic_baseline_keyboard_arrow_down_24).into(button);
                    ansLayout.setVisibility(View.GONE);
                }
            });
        }
    }
}
