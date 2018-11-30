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


//This class is similar to that of SpareChangeSend
public class WalletSend extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> mWallet = new ArrayList<>();
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
        setContentView(R.layout.activity_wallet_send);

        //Get friend's username from bundle
        Bundle bundle = getIntent().getExtras();
        friend = bundle.getString("friend");

        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
        }

        //Get exchange rates from shared preferences
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);

        shil = settings.getString("shil", "");
        peny = settings.getString("peny", "");
        quid = settings.getString("quid", "");
        dolr = settings.getString("dolr", "");

        //Set listview
        listView = findViewById(R.id.walletSend);

        //Update the listView
        updateList();

        //First attempt at getting friend's gold (it will return null)
        db.collection("users").document(friend).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                gold = documentSnapshot.getDouble("Gold");
            }
        });


        //Display the username of the friend in TextView
        TextView displayUsername = findViewById(R.id.displayUsername);
        displayUsername.setText(friend);


        //Set on click listener on the list view,
        // when coins are pressed add/remove them from selectedCoins
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


        //Set on click listener to Send Coins button, when pressed calls method sendCoins
        Button mSendCoins = findViewById(R.id.sendcoinsbutton2);
        mSendCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCoins();
            }
        });


        //Set on click listener to Back button, when pressed returns user to Friends Activity
        Button back = findViewById(R.id.back6);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToFriends();
            }
        });


        //Set on click listener to Send All button, when pressed calls method sendAll
        Button sendAll = findViewById(R.id.sendAll2);
        sendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAllCoins();
            }
        });

    }


    //Method to send the value in gold of the coins in selectedCoins to friend
    private void sendCoins() {
        //Total represents the total value of gold to give friend,
        // send represents value in gold of each coin
        Double total = 0.0;
        send = 0.0;

        //Parse exchange rates
        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = selectedCoins.size();

        //Second attempt at getting friend's gold which succeeds,
        // and we save it in a private variable
        db.collection("users").document(friend).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                gold = documentSnapshot.getDouble("Gold");
            }
        });

        //Result stores the value which we will set friend's gold to
        Double result = gold;

        //Iterate over all of the coins in selectedCoins
        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(selectedCoins.get(i));

            //Reset coin value
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


            //Update the total and result
            total = total + send;
            result = result + send;

            //Delete the coin from user's Wallet
            db.collection("users").document(email).collection("Wallet")
                    .document(coin.getId()).delete();

        }
        //Only enter if user has selected coins
        if (selectedCoins.size() > 0) {

            //Create Map object to update friend's gold with the 'result'
            Map<String, Object> g = new HashMap<>();
            g.put("Gold", result);
            //Update database
            db.collection("users").document(friend).set(g);

            //Format and Toast the result to user
            DecimalFormat df = new DecimalFormat("#.###");
            Toast.makeText(this, "Sent "+ friend + ":\n" +
                    String.valueOf(df.format(total)) + " gold", Toast.LENGTH_LONG).show();

            //Return user back to Friends activity
            backToFriends();
        }
        //Otherwise advise user to select coins
        else {
            Toast.makeText(this, "Please select coins to send", Toast.LENGTH_SHORT).show();
        }
    }


    //Method is similar to sendCoins, except we select coins from mWallet
    private void sendAllCoins() {
        Double total = 0.0;
        send = 0.0;

        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = mWallet.size();

        //Retrieve friend's gold
        db.collection("users").document(friend).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                gold = documentSnapshot.getDouble("Gold");
            }
        });

        Double result = gold;

        //Iterate over all coins within wallet
        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(mWallet.get(i));

            send = 0.0;

            //Calculate coin value
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

            //Delete coin from wallet
            db.collection("users").document(email)
                    .collection("Wallet").document(coin.getId()).delete();

        }

        //Update friend's new gold with result
        Map<String, Object> g = new HashMap<>();
        g.put("Gold", result);
        //Update database
        db.collection("users").document(friend).set(g);

        //Toast the formatted result to user
        DecimalFormat df = new DecimalFormat("#.###");
        Toast.makeText(this, "Sent "+ friend + ":\n" +
                String.valueOf(df.format(total)) + " gold", Toast.LENGTH_LONG).show();

        //Return user back to Friends Activity
        backToFriends();

    }


    //Method is identical to that in DepositCoins
    private void updateList() {
        //In this method we update and fill the listView
        db.collection("users")
                .document(email).collection("Wallet").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        //Iterate over all of the coins in Wallet
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
                            mWallet.add(res);
                            coins.put(res, coin);

                        }

                        //Making sure there are no duplicates by creating a Set
                        HashSet hs = new HashSet();
                        hs.addAll(mWallet);
                        mWallet.clear();
                        mWallet.addAll(hs);

                        //Set arrayadapter and update the listView
                        arrayAdapter = new ArrayAdapter(WalletSend.this,
                                R.layout.my_layout, R.id.row_layout, mWallet);
                        listView.setAdapter(arrayAdapter);
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WalletSend.this, "We failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    //Method to return the user to Friends
    private void backToFriends() {
        Intent intent = new Intent(this,Friends.class);
        startActivity(intent);
    }

}

