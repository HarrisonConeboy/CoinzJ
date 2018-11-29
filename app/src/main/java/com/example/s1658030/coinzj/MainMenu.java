package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.Map;

import javax.annotation.Nullable;


public class MainMenu extends AppCompatActivity {

    private LocalDateTime current = LocalDateTime.now();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private String todaysDate = current.format(formatter);
    private String mapData;
    private String preferencesFile = "MyPrefsFile";
    private String downloadDate;
    private String shil;
    private String dolr;
    private String quid;
    private String peny;
    private Intent svc;
    private String gold;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;

    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);



        svc = new Intent(this, BackgroundSoundService.class);
        svc.setAction("com.example.s1658030.coinzj.BackgroundSoundService");


        getGold();
        
        Switch music = findViewById(R.id.musicSwitch);
        music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(svc);
                } else {
                    stopService(svc);
                }
            }
        });
        if (music.isChecked()) {
            startService(svc);
        }


        Button friendsButton = findViewById(R.id.friendsButton);
        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFriends();
            }
        });

        ImageButton help = findViewById(R.id.helpIcon);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHelp();
            }
        });

    }

    public void goToMap(View view){
        Intent intent = new Intent(this, MapScreen.class);
        intent.putExtra("mapData",mapData);
        intent.putExtra("gold",gold);
        intent.putExtra("winnings","0");
        startActivity(intent);
    }

    public void goToBank(View view) {
        getGold();
        Intent intent = new Intent(this, Bank.class);
        intent.putExtra("gold",gold);
        intent.putExtra("winnings","0");

        startActivity(intent);
    }

    public void goToFriends() {
        Intent intent = new Intent(this, Friends.class);
        startActivity(intent);
    }

    private void goToHelp() {
        Intent intent = new Intent(this, HelpPage.class);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = settings.getString("lastDownloadDate","");
        if (downloadDate.equals(todaysDate)) {
            mapData = settings.getString("lastMap", "");
            shil = settings.getString("shil","");
            peny = settings.getString("peny","");
            quid = settings.getString("quid","");
            dolr = settings.getString("dolr","");
        }
        else {

            Toast.makeText(this, "Downloading map", Toast.LENGTH_LONG).show();
            downloadDate = todaysDate;
            String path = "http://homepages.inf.ed.ac.uk/stg/coinz/" + todaysDate + "/coinzmap.geojson";
            AsyncTask<String,Void,String> data = new DownloadFileTask().execute(path);
            try {
                mapData = data.get();
            } catch (InterruptedException e){
                e.printStackTrace();
            } catch (ExecutionException e){
                e.printStackTrace();
            }
            try {
                JSONObject object = new JSONObject(mapData);
                JSONObject rates = object.getJSONObject("rates");
                shil = rates.getString("SHIL");
                quid = rates.getString("QUID");
                peny = rates.getString("PENY");
                dolr = rates.getString("DOLR");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void signOut(View view) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        stopService(svc);
        startActivity(new Intent(MainMenu.this, SignIn.class));
    }

    private void getGold() {
        db.collection("users").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                gold = String.valueOf(documentSnapshot.getDouble("Gold"));
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainMenu.this, "Failed to retrieve gold",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStop() {
        super.onStop();

        if (listener != null) {
            listener.remove();
        }

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate",downloadDate);
        editor.putString("lastMap",mapData);
        editor.putString("shil",shil);
        editor.putString("dolr",dolr);
        editor.putString("quid",quid);
        editor.putString("peny",peny);
        editor.apply();
    }

}
