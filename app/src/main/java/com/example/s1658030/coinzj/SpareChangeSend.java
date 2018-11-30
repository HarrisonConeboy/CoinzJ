package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


//Activity which send coins from Spare Change to friend
public class SpareChangeSend extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> mSpareChange = new ArrayList<>();
    private ArrayList<Object> selectedCoins = new ArrayList<>();
    private HashMap<String, Coin> coins = new HashMap<>();

    //Set Firebase variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;
    private String friend;

    private String shil;
    private String quid;
    private String peny;
    private String dolr;
    private Double send;
    private Double gold;
    private ArrayAdapter arrayAdapter;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spare_change_send);

        //Get friends username from bundle and save it into private variable
        Bundle bundle = getIntent().getExtras();
        friend = bundle.getString("friend");

        //Get current user email if not null
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
        }

        //Get coin exchange rates from shared preferences
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);

        shil = settings.getString("shil", "");
        peny = settings.getString("peny", "");
        quid = settings.getString("quid", "");
        dolr = settings.getString("dolr", "");

        //Set listView
        listView = findViewById(R.id.sparechange);

        //Update the listView
        updateList();


        //Get friends gold by accessing their database
        // section and saving gold into a private variable
        db.collection("users").document(friend).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        gold = documentSnapshot.getDouble("Gold");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SpareChangeSend.this, "Failed to retrieve gold",
                                Toast.LENGTH_SHORT).show();
                    }
                });


        //Set listView listener, when a coin is pressed add/remove it from selectedCoins
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (selectedCoins.contains(parent.getItemAtPosition(position))) {
                    selectedCoins.remove(parent.getItemAtPosition(position));
                } else {
                    selectedCoins.add(parent.getItemAtPosition(position));
                }
            }
        });


        //Set TextView to display who user is sending coins to
        TextView displayUsername = findViewById(R.id.displayUsernameSP);
        displayUsername.setText(friend);


        //Set listener to Send Coins button, when pressed call the method sendCoins
        Button mSendCoins = findViewById(R.id.sendcoinsbutton);
        mSendCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCoins();
            }
        });


        //Set listener to Back button, when pressed returns the user to Friends Activity
        Button back = findViewById(R.id.back2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToFriends();
            }
        });


        //Set listener to Send All button, when pressed call the method sendAll
        Button sendAll = findViewById(R.id.sendAll);
        sendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAllCoins();
            }
        });

    }


    //Method which sends user's Friend the gold value of the coins in selectedCoins
    private void sendCoins() {
        //Total represents the total gold to send, send represents the gold value of each coin
        Double total = 0.0;
        send = 0.0;

        //Parse each of the exchange rates
        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = selectedCoins.size();

        //Second attempt at getting friend's gold as first always fails
        db.collection("users").document(friend).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                    //Save the gold into private variable
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        gold = documentSnapshot.getDouble("Gold");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SpareChangeSend.this, "Failed to retrieve gold",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        //Result is what we set friend's gold to
        Double result = gold;

        //Iterate over all of the coins in selectedCoins
        for (int i = 0; i < size; i++) {

            //Get the respective coin in coins
            Coin coin = coins.get(selectedCoins.get(i));

            //Reset then calculate the value of each coin in gold
            send = 0.0;

            if (coin.getCurrency().equals("SHIL")) {
                send = send + shilexchange * coin.getValue();
            } else if (coin.getCurrency().equals("QUID")) {
                send = send + quidexchange * coin.getValue();
            } else if (coin.getCurrency().equals("PENY")) {
                send = send + penyexchange * coin.getValue();
            } else if (coin.getCurrency().equals("DOLR")) {
                send = send + dolrexchange * coin.getValue();
            }


            //Update result and total
            total = total + send;
            result = result + send;

            //Delete the coin from the user's Spare Change
            db.collection("users").document(email)
                    .collection("Spare Change").document(coin.getId()).delete();

        }
        //Only enter if user has selected some coins
        if (selectedCoins.size() > 0) {

            //Create Map object used to set the new result of friend's gold
            Map<String, Object> g = new HashMap<>();
            g.put("Gold", result);
            //Update database
            db.collection("users").document(friend).set(g);

            //Toast a message to the user declaring how much they sent their friend
            DecimalFormat df = new DecimalFormat("#.###");

            Toast.makeText(this, "Sent "+ friend + ":\n"
                    + String.valueOf(df.format(total)) + " gold", Toast.LENGTH_LONG).show();

            //Return user to Friends Activity
            backToFriends();
        }
        //Otherwise advise user to select some coins
        else {
            Toast.makeText(this, "Please select coins to send",
                    Toast.LENGTH_SHORT).show();
        }
    }


    //Similar the sendCoins method, except we iterate over the entire Spare Change
    private void sendAllCoins() {
        Double total = 0.0;
        send = 0.0;

        //Get exchange rates
        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = mSpareChange.size();

        //Get friend gold
        db.collection("users").document(friend).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        gold = documentSnapshot.getDouble("Gold");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SpareChangeSend.this, "Failed to retrieve gold",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        Double result = gold;

        //Iterate over all coins in user's Spare Change
        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(mSpareChange.get(i));

            send = 0.0;

            //Calculate value of coin in gold
            if (coin.getCurrency().equals("SHIL")) {
                send = send + shilexchange * coin.getValue();
            } else if (coin.getCurrency().equals("QUID")) {
                send = send + quidexchange * coin.getValue();
            } else if (coin.getCurrency().equals("PENY")) {
                send = send + penyexchange * coin.getValue();
            } else if (coin.getCurrency().equals("DOLR")) {
                send = send + dolrexchange * coin.getValue();
            }

            //Update variables
            total = total + send;
            result = result + send;

            //Remove the coin from user's Spare Change
            db.collection("users").document(email)
                    .collection("Spare Change").document(coin.getId()).delete();

        }

        //Set friend's new gold value
        Map<String, Object> g = new HashMap<>();
        g.put("Gold", result);
        db.collection("users").document(friend).set(g);

        //Toast how much gold the user sent their friend
        DecimalFormat df = new DecimalFormat("#.###");

        Toast.makeText(this, "Sent "+ friend + ":\n"
                + String.valueOf(df.format(total)) + " gold", Toast.LENGTH_LONG).show();

        //If user pressed Send All, send them back to Friends Activity regardless
        backToFriends();

    }


    //Method is identical to that used in Deposit Coins, except iterates over Spare Change
    private void updateList() {
        //In this method we update and fill the listView
        db.collection("users")
                .document(email).collection("Spare Change").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //Iterate over all of the coins in Spare Change
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {

                            //Retrieve the value, currency and ID of each coin
                            String value = queryDocumentSnapshots.getDocuments()
                                    .get(i).get("value").toString();
                            String currency = queryDocumentSnapshots.getDocuments()
                                    .get(i).get("currency").toString();
                            String id = queryDocumentSnapshots.getDocuments().get(i).getId();

                            //Create the display name as res and parse the value for creating a coin
                            String res = currency + ": " + value;
                            Double val = Double.parseDouble(value);

                            //Create coin object, add res to Array for listView, add coin to HashMap
                            Coin coin = new Coin(id, val, currency);
                            mSpareChange.add(res);
                            coins.put(res, coin);

                        }

                        //Making sure there are no duplicates by creating a Set
                        HashSet hs = new HashSet();
                        hs.addAll(mSpareChange);
                        mSpareChange.clear();
                        mSpareChange.addAll(hs);

                        //Set arrayadapter and update the listView
                        arrayAdapter = new ArrayAdapter(SpareChangeSend.this,
                                R.layout.my_layout, R.id.row_layout, mSpareChange);
                        listView.setAdapter(arrayAdapter);
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SpareChangeSend.this, "We failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    //Method to return user back to Friends
    private void backToFriends() {
        Intent intent = new Intent(this,Friends.class);
        startActivity(intent);
    }

}
