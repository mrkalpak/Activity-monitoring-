package com.example.finalyearproject_android.BackgroundProcesses;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.finalyearproject_android.HomeActivity;
import com.example.finalyearproject_android.MainActivity;
import com.example.finalyearproject_android.Models.ModelSteps;
import com.example.finalyearproject_android.R;
import com.example.finalyearproject_android.SharedData.CommonData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

import static java.lang.Math.sqrt;

public class ServiceSensorsInitializer extends Service implements SensorEventListener {
    public static String CHANNEL_ID = "AiBasedHealthMonitoring";
    public static String CHANNEL_NAME = "AI Based Health Monitoring";
    public static long steps = CommonData.steps;
    private long lastTime = 0;
    LocalDatabase localDatabase;
    String today;
    FirebaseUser user;
    DatabaseReference reference;

//    sensors
    Sensor accelerometer;
    SensorManager sensorManager;

    float cachedAcceleration=0.0f;
    public static final float ALPHA_VAL = (float) 0.65;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String steps = intent.getStringExtra("steps");
        if (steps == null){
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            stopSelf();
            steps = "0";
        }
        ServiceSensorsInitializer.steps = Long.parseLong(steps);

        localDatabase = new LocalDatabase(getApplicationContext());
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        today = formatter.format(date);
        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            Toast.makeText(this, "User Sign In Failure", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            this.stopSelf();
            return START_NOT_STICKY;
        }
        user = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference();

//        initialize sensors
        sensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationChannel channel = new  NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("CHANNEL_DESC");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , mainIntent, 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter")
                .setContentText("Counting")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);

        sensorManager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        long currentTime =System.currentTimeMillis();
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        cachedAcceleration = (float) ((1 - ALPHA_VAL) * cachedAcceleration + ALPHA_VAL * sqrt(x * x + y * y + z * z));
        if (cachedAcceleration > 15 && cachedAcceleration < 21) {
            if (currentTime-lastTime>590){
                try{
                    if (currentTime-CommonData.lastTime<590){
                        return;
                    }
                    steps++;
                    try{
                        CommonData.steps++;
                    }catch (Exception ignored2){}
                    lastTime=currentTime;
                    CommonData.lastTime = currentTime;
//                    Update the data to firebase and local database
                    ModelSteps stepsModel = new ModelSteps(today+"", steps+"");
                    localDatabase.insertSteps(steps+"",user.getUid());
                    if (isNetworkAvailable()){
//                        Store to remote database
                        reference.child("Data").child(user.getUid()).child(today).setValue(stepsModel);
                    }

//                    broadcasting the data through intent so can be fetched in UI
                    Intent intent = new Intent("AIBasedActivityMonitoring");
                    intent.putExtra("steps",steps+"");
                    LocalBroadcastManager.getInstance(this)
                            .sendBroadcast(intent);
                }catch (Exception ignored){}
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

//    method to check internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}