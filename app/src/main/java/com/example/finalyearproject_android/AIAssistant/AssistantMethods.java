package com.example.finalyearproject_android.AIAssistant;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.finalyearproject_android.AboutUsActivity;
import com.example.finalyearproject_android.BackgroundProcesses.ServiceSensorsInitializer;
import com.example.finalyearproject_android.BackgroundProcesses.StartServiceMethods;
import com.example.finalyearproject_android.Fragments.HomeFragment;
import com.example.finalyearproject_android.Fragments.ProfileFragment;
import com.example.finalyearproject_android.Fragments.SettingsFragment;
import com.example.finalyearproject_android.MainActivity;
import com.example.finalyearproject_android.R;
import com.example.finalyearproject_android.SetGoalActivity;
import com.example.finalyearproject_android.SharedData.CommonData;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AssistantMethods implements TextToSpeech.OnInitListener{

    Context context;
    Activity activity;

    String replyString = null;
    TextToSpeech tts;

    public AssistantMethods(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void initializeAssistant() {
        List<ModelOpenPage> openPages = new ArrayList<>();
        openPages.add(new ModelOpenPage("main",true ,MainActivity.class));
        openPages.add(new ModelOpenPage("home",true ,MainActivity.class));
        openPages.add(new ModelOpenPage("settings",false, new SettingsFragment()));
        openPages.add(new ModelOpenPage("profile", false, new ProfileFragment()));
        openPages.add(new ModelOpenPage("about", true, AboutUsActivity.class));
        openPages.add(new ModelOpenPage("goal", true, SetGoalActivity.class));
//        add stop service, initialize service,diet planner , logout
        CommonData.openPageList = openPages;

        if (tts==null) {
            tts = new TextToSpeech(context, this);
        }
    }



    public String getReply(String inputString){

        replyString = "Unable to understand your request";

        if (inputString.toLowerCase().contains("open") || inputString.toLowerCase().contains("goto") || inputString.toLowerCase().contains("go to")){

            for (ModelOpenPage pages : CommonData.openPageList){
                if (inputString.toLowerCase().contains(pages.getPage())){
                    if (pages.isActivity()){
                        context.startActivity(new Intent(context, (Class<?>) pages.getActivity()));
                    }else{
                        MainActivity.fragmentManager.beginTransaction().replace(R.id.bottom_navigation_frame, (Fragment) pages.getActivity()).commit();
                    }
                    return speakText("opening");
                }
            }
            replyString = speakText("no match found for your query");

        }else if (inputString.toLowerCase().contains("show") || inputString.toLowerCase().contains("view") || inputString.toLowerCase().contains("give")){

            if (inputString.toLowerCase().contains("steps")){
                replyString = speakText("Today's steps count is "+ CommonData.steps);
            }else if (inputString.toLowerCase().contains("calories")){
                replyString = speakText("Today's calories count is "+ CommonData.steps * 0.04);
            }else {
                replyString = speakText("No such data present.");
            }

        }else if (inputString.toLowerCase().contains("update") || inputString.toLowerCase().contains("change") || inputString.toLowerCase().contains("set")){

            if (inputString.toLowerCase().contains("goal")){
                context.startActivity(new Intent(context, SetGoalActivity.class));
            }else if (inputString.toLowerCase().contains("profile")){
                MainActivity.fragmentManager.beginTransaction().replace(R.id.bottom_navigation_frame, new ProfileFragment()).commit();
            }else{
                replyString = speakText("No Such Data Present");
            }

        }else {
//            basic data
            if (inputString.toLowerCase().contains("hi") || inputString.toLowerCase().contains("hello")){
                replyString = speakText("Hello! How Can I Help?");
            } else if (inputString.toLowerCase().contains("logout") || inputString.toLowerCase().contains("signout") || inputString.toLowerCase().contains("sign out")){
                CommonData.logout(context, activity);
                replyString = speakText("OK");
            }else if (inputString.toLowerCase().contains("service")){
                if (inputString.toLowerCase().contains("stop") ){
                    replyString = speakText("Stopping Service...");
                    activity.stopService(new Intent(context, ServiceSensorsInitializer.class));
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(HomeFragment.broadcastReceiver);
                }else if(inputString.toLowerCase().contains("start")){
                    replyString = speakText("Starting Service...");
                    new StartServiceMethods(context,activity).initService();
                }
            }
        }

        if (replyString.equalsIgnoreCase("Unable to understand your request")){
            speakText(replyString);
        }

        return replyString;
    }

    private String speakText(String string) {
        if (!tts.isSpeaking()){
            tts.speak(string,TextToSpeech.QUEUE_FLUSH,null);
            Log.e("SPEAK","speaking");
        }
        return string;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
}
