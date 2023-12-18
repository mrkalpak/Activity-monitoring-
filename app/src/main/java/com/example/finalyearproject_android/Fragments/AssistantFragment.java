package com.example.finalyearproject_android.Fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalyearproject_android.AIAssistant.AssistantMethods;
import com.example.finalyearproject_android.AIAssistant.ModelMessage;
import com.example.finalyearproject_android.Adapters.AdapterAssistant;
import com.example.finalyearproject_android.MainActivity;
import com.example.finalyearproject_android.R;
import com.example.finalyearproject_android.SharedData.CommonData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AssistantFragment extends Fragment {

    EditText message;
    ImageButton microphoneBtn;
    ImageView sendBtn;

    RecyclerView assistantRecycler;
    AdapterAssistant assistantAdapter;

    SpeechRecognizer speechRecognizer;
    Intent speech;

    Context context;
    Activity activity;

    List<ModelMessage> messages;

    AssistantMethods assistantMethods;
    int pageCount = 0;

    public AssistantFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,  Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_assistant, container, false);
        context = requireContext();
        activity = requireActivity();
        if (pageCount==0) {
            int selectedItemId = MainActivity.navBar.getSelectedItemId();
            if (selectedItemId != R.id.navigation_assistant) {
                try {
                    MainActivity.navBar.setSelectedItemId(R.id.navigation_assistant);
                }catch (Exception e){
                    Log.e("ERROR",e.getMessage());
                }
            }
            pageCount++;
        }

        assistantMethods = new AssistantMethods(context, activity);

        ActivityCompat.requestPermissions(activity,new String[]{Manifest.permission.RECORD_AUDIO}, PackageManager.PERMISSION_GRANTED);

        message = view.findViewById(R.id.sendMessageInput);
        microphoneBtn = view.findViewById(R.id.btn_mic_assistant);
        sendBtn = view.findViewById(R.id.btn_send_message);
        assistantRecycler = view.findViewById(R.id.assistant_recycler);

        if (CommonData.messages!=null){
            if (CommonData.messages.size()>0){
                messages = CommonData.messages;
            }else{
                messages = new ArrayList<>();
            }
        }else{
            messages = new ArrayList<>();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setStackFromEnd(true);
        assistantRecycler.setLayoutManager(linearLayoutManager);
        assistantAdapter = new AdapterAssistant(context,messages);
        assistantRecycler.setAdapter(assistantAdapter);

        initializeSpeech(view);

        assistantMethods.initializeAssistant();

        microphoneBtn.setOnClickListener(v -> startRecord(view));

        sendBtn.setOnClickListener(v -> sendMessage());

        return  view;
    }

    private void sendMessage() {
        if (message.getText().toString().equalsIgnoreCase("")){
            return;
        }
        messages.add(new ModelMessage(messages.size()+"", message.getText().toString(),getCurrentDate(),true));

        String output = assistantMethods.getReply(message.getText().toString());

        message.setText("");
        messages.add(new ModelMessage(messages.size()+"", output,getCurrentDate(),false));
        assistantAdapter = new AdapterAssistant(context,messages);
        assistantRecycler.setAdapter(assistantAdapter);
        CommonData.messages = messages;
    }

    private String getCurrentDate(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    void startRecord(View view){
        speechRecognizer.startListening(speech);
        view.findViewById(R.id.main_assistant_layout).setVisibility(View.GONE);
        view.findViewById(R.id.recording_ui).setVisibility(View.VISIBLE);
    }

    void stopRecord(View view){
        view.findViewById(R.id.main_assistant_layout).setVisibility(View.VISIBLE);
        view.findViewById(R.id.recording_ui).setVisibility(View.GONE);
    }

    private void initializeSpeech(View view) {
        speech = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speech.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizer=SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new SpeechRecognitionAdapter(){
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> arrayList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (arrayList!=null){
                    String output = arrayList.get(0);
                    message.setText(output);
                    stopRecord(view);
                    sendMessage();
                }
            }

            @Override
            public void onEndOfSpeech() {
                stopRecord(view);
            }
        });
    }


    //adapter only for clean code in above listener
    private static class SpeechRecognitionAdapter implements RecognitionListener{
        @Override
        public void onReadyForSpeech(Bundle params) {}
        @Override
        public void onBeginningOfSpeech() {}
        @Override
        public void onRmsChanged(float rmsdB) {}
        @Override
        public void onBufferReceived(byte[] buffer) {}
        @Override
        public void onEndOfSpeech() {}
        @Override
        public void onError(int error) {
            Log.e("Error","Error=>"+error);
        }
        @Override
        public void onResults(Bundle results) {}
        @Override
        public void onPartialResults(Bundle partialResults) {}
        @Override
        public void onEvent(int eventType, Bundle params) {}
    }
}