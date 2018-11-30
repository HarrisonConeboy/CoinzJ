package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class MainMenu extends AppCompatActivity {

    //Set all private variables

    //Get current date and format it correctly
    private LocalDateTime current = LocalDateTime.now();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private String todaysDate = current.format(formatter);

    //Set shared preferences filename
    private String preferencesFile = "MyPrefsFile";
    private String mapData;
    private String downloadDate;

    private String shil;
    private String dolr;
    private String quid;
    private String peny;
    private Intent svc;
    private String gold;

    //Initialize Firebase objects
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Check if user is null, get email
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
        }

        //Start a new background sound service in order to play music
        svc = new Intent(this, BackgroundSoundService.class);
        svc.setAction("com.example.s1658030.coinzj.BackgroundSoundService");

        //Get the value of the Gold banked
        getGold();

        //Check RecentDate
        updateDate();

        //Get switch in layout and set listener to stop/start music depending on changed state
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

        //Check the state of the switch, start music if is checked
        if (music.isChecked()) {
            startService(svc);
        }

        //Create Friends button listener which sends user to friends
        Button friendsButton = findViewById(R.id.friendsButton);
        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFriends();
            }
        });

        //Create ImageButton listener which send user to help page
        ImageButton help = findViewById(R.id.helpIcon);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHelp();
            }
        });

    }


    //When going to map, send it mapData info along with other info to be passed to bank
    public void goToMap(View view){
        Intent intent = new Intent(this, MapScreen.class);

        //This is the MapData
        intent.putExtra("mapData",mapData);

        //This is the Gold banked
        intent.putExtra("gold",gold);

        //This is the most recent winnings, which is always 0 when coming from main menu
        intent.putExtra("winnings","0");

        //Start activity
        startActivity(intent);
    }


    //When going to bank, send it gold and winnings data
    public void goToBank(View view) {
        Intent intent = new Intent(this, Bank.class);
        intent.putExtra("gold",gold);
        intent.putExtra("winnings","0");

        startActivity(intent);
    }


    //Simple function to change activity
    public void goToFriends() {
        Intent intent = new Intent(this, Friends.class);
        startActivity(intent);
    }


    //Simple function to change activity
    private void goToHelp() {
        Intent intent = new Intent(this, HelpPage.class);
        startActivity(intent);
    }


    @Override
    public void onStart() {
        super.onStart();

        //Here we get the date of the last time we downloaded a map, if it is the same as today's
        // date then we can also get all of the exchange rates for the different currencies
        // which are also stored in shared preferences
        SharedPreferences settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE);
        downloadDate = settings.getString("lastDownloadDate","");
        if (downloadDate.equals(todaysDate)) {
            mapData = settings.getString("lastMap", "");
            shil = settings.getString("shil","");
            peny = settings.getString("peny","");
            quid = settings.getString("quid","");
            dolr = settings.getString("dolr","");
        }
        //Otherwise we must download the new map
        else {
            //Toast alert to user, we are downloading a new map
            Toast.makeText(this, "Downloading map", Toast.LENGTH_LONG).show();

            //Set download date to be today's date
            downloadDate = todaysDate;

            //We set the path to access the new map
            String path =
                    "http://homepages.inf.ed.ac.uk/stg/coinz/" + todaysDate + "/coinzmap.geojson";

            //Now we must asynchronously download the map
            AsyncTask<String,Void,String> data = new DownloadFileTask().execute(path);

            //We need 2 catch blocks otherwise android studio is unhappy
            try {
                mapData = data.get();
            } catch (InterruptedException e){
                e.printStackTrace();
            } catch (ExecutionException e){
                e.printStackTrace();
            }

            //Next we create a JSON object in order to extract the exchange rates
            try {
                JSONObject object = new JSONObject(mapData);
                JSONObject rates = object.getJSONObject("rates");

                //We save the rates into private variables
                shil = rates.getString("SHIL");
                quid = rates.getString("QUID");
                peny = rates.getString("PENY");
                dolr = rates.getString("DOLR");
            }
            //Must be wary of exception
            catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    //Sign out function
    public void signOut(View view) {
        //Simply sign the current user out
        mAuth.signOut();

        //Stop the in game music
        stopService(svc);

        //Return back to the sign in page
        startActivity(new Intent(MainMenu.this, SignIn.class));
    }


    //Function to update private variable gold with the Gold banked
    private void getGold() {
        //Gold is saved as a field with the user email, under users
        db.collection("users").document(email).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

            //If we successfully access the database, saved the Gold
            // as a String in the private variable gold
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                gold = String.valueOf(documentSnapshot.getDouble("Gold"));
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    //If we fail to access the database then we alert the user with a Toast
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainMenu.this, "Failed to retrieve gold",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
    //This method is used to reset the number of bankable coins per day


    private void updateDate() {

        //We have to create another formatter to set the date in yyyy-MM-dd format
        DateTimeFormatter newFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todaysDateFormatted = current.format(newFormatter);

        //Going into database
        db.collection("users")
                .document(email).collection("RecentDate").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    //Check if the date in RecentDate is equal to today's date,
                    // if not update it and set Banked to be 0
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.getDocuments().get(0).getId().equals(todaysDateFormatted)) {

                            HashMap<String, Object> temp = new HashMap<>();
                            temp.put("Banked", 0);

                            //Update for recent
                            db.collection("users").document(email)
                                    .collection("RecentDate").document(todaysDateFormatted).set(temp);

                            //Delete previous
                            db.collection("users").document(email)
                                    .collection("RecentDate")
                                    .document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    //Toast error if unable to access database
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainMenu.this, "Error occurred " +
                                "retrieving date", Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public void onStop() {
        super.onStop();

        //We must save the date of last download, the exchange
        // rates and the mapdata to shared preferences
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
