package com.example.finalyearproject_android.Fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.finalyearproject_android.BackgroundProcesses.LocalDatabase;
import com.example.finalyearproject_android.HomeActivity;
import com.example.finalyearproject_android.MainActivity;
import com.example.finalyearproject_android.Models.ModelSteps;
import com.example.finalyearproject_android.R;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment {

    static FirebaseAuth auth;
    DatabaseReference reference;
    static LocalDatabase localDatabase;

    String today;
    int pageCount = 0;
    static BarChart chart;


    static ProgressBar stepCountProgress;
    static TextView stepCount,stepTextView, caloriesTextView, distanceTextView;

    private static final DecimalFormat df = new DecimalFormat("0.00");

    //receiver for data sent from service
    public static BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int steps = Integer.parseInt(intent.getStringExtra("steps"));
            double calories = steps * 0.04;
            double height = Double.parseDouble(CommonData.userData.getHeight());
            //distance calculation

//            Distance travelled during a walk is calculated by multiplying the steps taken by the person's stride length.
            double multiplier = 0.414;
            double stride = multiplier * height;

            double distanceCm = steps * stride;
            double distance = distanceCm/100;
            updateUI(steps, calories, distance);
        }

        private void updateUI(int steps, double calories, double distance) {
            try{
                long stepGoal = Long.parseLong(CommonData.goal.getStepsGoal());
                double percentage = steps*100.0/stepGoal;
                stepCount.setText(steps+"");
                stepCountProgress.setProgress((int) percentage);

                stepTextView.setText(steps+"");
                caloriesTextView.setText(df.format(calories));
                distanceTextView.setText(df.format(distance));
                try{
                    setBarData();
                }catch (Exception er){
                    Log.e("CHART ERROR",er+"");
                }
            }catch (Exception e){
                Log.e("ERROR",e+"");
            }
        }

        private void setBarData() {
            ArrayList<BarEntry> dataValues = new ArrayList<>();

            List<ModelSteps> stepList = localDatabase.getOldSteps(Objects.requireNonNull(auth.getCurrentUser()).getUid());
            ArrayList<String> xAxisLables = new ArrayList<>();

            for (int i = 0; i< stepList.size();i++){
                dataValues.add(new BarEntry(i, Float.parseFloat(stepList.get(i).getSteps())));
                xAxisLables.add(""+stepList.get(i).getDate());
            }

            //set chart data
            BarDataSet BarDataSet = new BarDataSet(dataValues, "Steps Set");
            ArrayList<IBarDataSet> dataSets = new ArrayList<>();
            dataSets.add(BarDataSet);
            BarData data = new BarData(dataSets);
            data.setBarWidth(0.5f);
            chart.setData(data);

            chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLables));

            chart.invalidate();

        }

    };

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for getContext() fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        if (pageCount==0){
            int selectedItemId = MainActivity.navBar.getSelectedItemId();
            if (selectedItemId!=R.id.navigation_home){
                try {
                    MainActivity.navBar.setSelectedItemId(R.id.navigation_home);
                }catch (Exception e){
                    Log.e("ERROR",e.getMessage());
                }
            }
            pageCount++;
        }

//        make firebase ready
        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference();

        localDatabase = new LocalDatabase(getContext());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        today = formatter.format(date);


        //validate the user is login or not
        if (auth.getCurrentUser() == null){
            Toast.makeText(getContext(), "User Not Logged In", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getContext(), HomeActivity.class));
            requireActivity().finish();
            return null;
        }
//        set default variables
        stepCount = view.findViewById(R.id.stepCount);
        stepCountProgress = view.findViewById(R.id.stepsProgressBar);
        stepTextView = view.findViewById(R.id.step_count_view);
        caloriesTextView = view.findViewById(R.id.calories_count_view);
        distanceTextView = view.findViewById(R.id.distance_count_view);
        TextView dateTextView = view.findViewById(R.id.date_text_view);
        dateTextView.setText(today);

        if (CommonData.userData==null){
            CommonData.userData = localDatabase.getUser(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        }
        setUIData();

        chart = view.findViewById(R.id.bar_chart);

        setChart();

        return view;
    }

    private void setUIData() {

        long steps = CommonData.steps;
        double calories = steps * 0.04;


        long stepGoal = 0;
        double percentage = 0;
        stepGoal = Long.parseLong(CommonData.goal.getStepsGoal());
        percentage = CommonData.steps*100.0/stepGoal;
        double height = Double.parseDouble(CommonData.userData.getHeight());

//            Distance travelled during a walk is calculated by multiplying the steps taken by the person's stride length.
        double multiplier = 0.614;
        double stride = multiplier * height;

        double distanceCm = steps * stride;
        double distance = distanceCm/100;


        stepCount.setText(steps+"");
        stepCountProgress.setProgress((int) percentage);

        stepTextView.setText(steps+"");
        caloriesTextView.setText(df.format(calories));
        distanceTextView.setText(df.format(distance));

    }

    private void setChart() {
        chart.setDrawGridBackground(false);
        chart.setDrawBorders(false);
        chart.getDescription().setEnabled(false);
        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);
        chart.setDrawBarShadow(false);
        chart.setDrawGridBackground(false);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = requireContext().getTheme();
        theme.resolveAttribute(R.attr.color, typedValue, true);
        @ColorInt int color = typedValue.data;

        chart.getAxisLeft().setTextColor(color);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setTextColor(color);
        chart.getLegend().setTextColor(color);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        chart.getXAxis().setDrawGridLines(false);
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisRight().setDrawGridLines(false);
        // add a nice and smooth animation
        chart.animateY(800);
        chart.getLegend().setEnabled(false);

        setBarData();

    }

    private void setBarData() {
        ArrayList<BarEntry> dataValues = new ArrayList<>();

        List<ModelSteps> stepList = localDatabase.getOldSteps(Objects.requireNonNull(auth.getCurrentUser()).getUid());
        ArrayList<String> xAxisLables = new ArrayList<>();

        for (int i = 0; i< stepList.size();i++){
            dataValues.add(new BarEntry(i, Float.parseFloat(stepList.get(i).getSteps())));
            xAxisLables.add(""+stepList.get(i).getDate());
        }

        //set chart data
        BarDataSet BarDataSet = new BarDataSet(dataValues, "Steps Set");
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(BarDataSet);
        BarData data = new BarData(dataSets);
        data.setBarWidth(0.5f);
        chart.setData(data);

        chart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(xAxisLables));

        chart.invalidate();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver);
        }catch (Exception ignored){}
        try {
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, new IntentFilter("AIBasedActivityMonitoring"));
        }catch (Exception ignored){}
    }
}