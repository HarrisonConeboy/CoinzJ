package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;


public class MainMenu extends AppCompatActivity {

    private String tag = "MainMenu";
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

    private Switch music;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        music = findViewById(R.id.musicSwitch);

        svc = new Intent(this, BackgroundSoundService.class);
        svc.setAction("com.example.s1658030.coinzj.BackgroundSoundService");

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

        }

    public void goToMap(View view){
        Intent intent = new Intent(this, Map.class);
        intent.putExtra("mapData",mapData);
        intent.putExtra("shil",shil);
        intent.putExtra("dolr",dolr);
        intent.putExtra("quid",quid);
        intent.putExtra("peny",peny);
        stopService(svc);
        startActivity(intent);
    }

    public void goToBank(View view) {
        Intent intent = new Intent(this, Bank.class);
        intent.putExtra("mapData",mapData);
        intent.putExtra("shil",shil);
        intent.putExtra("dolr",dolr);
        intent.putExtra("quid",quid);
        intent.putExtra("peny",peny);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = settings.getString("lastDownloadDate","");
        if (downloadDate.equals(todaysDate)) {
            Log.d(tag, "Already downloaded today's map");
            mapData = settings.getString("lastMap", "");
            shil = settings.getString("shil","");
            dolr = settings.getString("dolr","");
            quid = settings.getString("quid","");
            peny = settings.getString("peny","");
        } else {
            Log.d(tag, "Downloading today's map");
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

    @Override
    public void onStop() {
        super.onStop();

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
