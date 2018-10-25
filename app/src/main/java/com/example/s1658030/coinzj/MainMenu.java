package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

    }

    public void gotomap(View view){
        Intent intent = new Intent(this, Map.class);
        intent.putExtra("mapData",mapData);
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = settings.getString("lastDownloadDate","");
        if (downloadDate.equals(todaysDate)) {
            Log.d(tag, "Already downloaded today's map");
            Toast.makeText(this, "Already downloaded today's map", Toast.LENGTH_LONG).show();
            mapData = settings.getString("lastMap", "");
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
        }


    }

    @Override
    public void onStop() {
        super.onStop();

        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastDownloadDate",downloadDate);
        editor.putString("lastMap",mapData);
        editor.apply();
    }

}
