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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class DepositCoins extends AppCompatActivity {

    private Bundle bundle;

    //Get todays date formatted correctly
    private LocalDateTime current = LocalDateTime.now();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private String todaysDate = current.format(formatter);

    private String shil;
    private String quid;
    private String peny;
    private String dolr;
    private Integer banked;

    //Set a variable check as we must run a method twice
    private Integer check = 0;

    //These are listView variables
    private ListView listView;
    //mWallet represents the coins in Wallet
    private ArrayList<String> mWallet = new ArrayList<>(50);
    //walletList represents coins selected
    private ArrayList<Object> walletList = new ArrayList<>();
    //coins is a HashMap relating coins stored in the listView to objects
    private HashMap<String, Coin> coins = new HashMap<>();
    //Array adapter to edit listView
    private ArrayAdapter arrayAdapter;

    //Create Firebase variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;

    //Textview for displaying how many coins user banked today
    private TextView displayBanked;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_coins);

        //Get previous bundle of Extras
        bundle = getIntent().getExtras();

        //If user is not null, get email
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
        }

        //Set listView object
        listView = findViewById(R.id.walletInDeposit);

        //Get all of the exchange rates from shared preferences
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        shil = settings.getString("shil", "");
        peny = settings.getString("peny", "");
        quid = settings.getString("quid", "");
        dolr = settings.getString("dolr", "");

        //Set the TextViews to display the exchange rates
        TextView mShil = findViewById(R.id.shilValue);
        TextView mPeny = findViewById(R.id.penyValue);
        TextView mDolr = findViewById(R.id.dolrValue);
        TextView mQuid = findViewById(R.id.quidValue);
        mShil.setText(shil);
        mQuid.setText(quid);
        mDolr.setText(dolr);
        mPeny.setText(peny);

        //Update the listView
        updateList();

        //There is a strange glitch that results in me not being able to access coins banked on
        // the first attempt, I worked around this by calling the method twice and having a
        // simple checker at the start to dictate which branch it should follow
        depositCoins();
        check = 1;

        //Now I can retrieve Gold banked which was passed through the intent
        Bundle bundle = getIntent().getExtras();
        String temp = bundle.getString("gold");
        //We set the TextView to the current gold after formatting it to two decimal places
        TextView currentGold = findViewById(R.id.currentGold3);
        DecimalFormat df = new DecimalFormat("#.##");
        Double temp2 = Double.parseDouble(temp);
        currentGold.setText(String.valueOf(df.format(temp2)));

        //Display the number of coins banked today with the method
        displayBanked = findViewById(R.id.displayBanked);
        updateCoinsBanked();


        //Set a listener which adds and removes coins to the walletList if pressed on the listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (walletList.contains(parent.getItemAtPosition(position))) {
                    walletList.remove(parent.getItemAtPosition(position));
                } else {
                    walletList.add(parent.getItemAtPosition(position));
                }
            }
        });


        //Set listener for deposit coins button, if pressed call method
        Button mDeposit = findViewById(R.id.depositCoins);
        mDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depositCoins();
            }
        });


        //Set listener for back button, if pressed call method
        Button back = findViewById(R.id.back2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToBankNoChange();
            }
        });

    }


    //Method used for depositing selected coins
    private void depositCoins() {
        //This is the first iteration to obtain coins banked

        if (check == 0) {
            //Get number of banked coins today first attempt,
            // this will make sure the second attempt succeeds
            db.collection("users")
                    .document(email).collection("RecentDate").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                //If we successfully access the database we check
                // if date saved is equal to today's date
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    //Check if today's date is equal to date saved in database
                    if (!queryDocumentSnapshots.getDocuments().get(0).getId().equals(todaysDate)) {

                        //If it's not then we create a map object to refresh Banked to 0
                        HashMap<String,Object> fresh = new HashMap<>();
                        fresh.put("Banked",0);

                        //We add the new object into the database
                        db.collection("users").document(email).collection
                                ("RecentDate").document(todaysDate).set(fresh);

                        //We delete the previous object
                        db.collection("users")
                                .document(email).collection("RecentDate")
                                .document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();

                        banked = 0;

                    }//Otherwise we simply set banked to the field value
                    else {
                        banked = Integer.parseInt(queryDocumentSnapshots
                                .getDocuments().get(0).get("Banked").toString());
                    }
                }
            })
                    //Add failure listener in case we are unable to access the database
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DepositCoins.this, "Failed to retrieve banked coins",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            //total represents the total coins to be added to the users current gold
            Double total = 0.0;

            //Retrieve the banked gold value that was passed from Bank
            Bundle bundle = getIntent().getExtras();
            Double gold = Double.parseDouble(bundle.getString("gold"));


            //This is the second attempt at getting the number of coins banked today, we also check
            //if it's a new date and if so we update the database and set the number of banked coins
            //to be 0. It is identical to the first attempt.
            db.collection("users")
                    .document(email).collection("RecentDate").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            if (!queryDocumentSnapshots.getDocuments().get(0).getId().equals(todaysDate)) {

                                HashMap<String,Object> fresh = new HashMap<>();
                                fresh.put("Banked",0);

                                db.collection("users").document(email).collection(
                                        "RecentDate").document(todaysDate).set(fresh);

                                db.collection("users")
                                        .document(email).collection("RecentDate")
                                        .document(queryDocumentSnapshots.getDocuments().get(0).getId()).delete();

                                banked = 0;

                            }
                            else {
                                banked = Integer.parseInt(queryDocumentSnapshots
                                        .getDocuments().get(0).get("Banked").toString());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(DepositCoins.this, "Failed to retrieve banked coins",
                            Toast.LENGTH_SHORT).show();
                }
            });


            //Create result variable which will be used to update the database to the new gold value
            Double result = gold;

            //Parse exchange rates from String to Double
            Double shilexchange = Double.parseDouble(shil);
            Double quidexchange = Double.parseDouble(quid);
            Double dolrexchange = Double.parseDouble(dolr);
            Double penyexchange = Double.parseDouble(peny);


            //We must check if they have exceeded their daily banking limit,
            // if they have we Toast an alert to tell them
            int size = walletList.size();
            if (size > 25 - banked) {
                Toast.makeText(this, "You can only bank 25 coins per day",
                        Toast.LENGTH_SHORT).show();
            }
            //Method to update gold banked, the number of coins banked and finally the wallet
            else {

                //Iterate over the selected coins size and obtain the associated coin from the Map
                for (int i = 0; i < size; i++) {
                    Coin coin = coins.get(walletList.get(i));

                    //add represents the gold value of a single coin
                    Double add = 0.0;

                    //Calculate the value in gold of the coin
                    if (coin.getCurrency().equals("SHIL")) {
                        add = add + shilexchange * coin.getValue();
                    } else if (coin.getCurrency().equals("QUID")) {
                        add = add + quidexchange * coin.getValue();
                    } else if (coin.getCurrency().equals("PENY")) {
                        add = add + penyexchange * coin.getValue();
                    } else if (coin.getCurrency().equals("DOLR")) {
                        add = add + dolrexchange * coin.getValue();
                    }

                    //Now delete the coin from Wallet and add the totals
                    db.collection("users").document(email)
                            .collection("Wallet").document(coin.getId()).delete();

                    //Update result and total
                    result = result + add;
                    total = total + add;

                }

                //Only update if there are some selected coins
                if (walletList.size() > 0) {

                    //Create Gold field and use result to update users Gold
                    Map<String, Object> g = new HashMap<>();
                    g.put("Gold", result);
                    db.collection("users").document(email).set(g);

                    //Create object to update the number of coins banked today
                    HashMap<String,Object> newBanked = new HashMap<>();
                    newBanked.put("Banked",(walletList.size() + banked));

                    db.collection("users")
                            .document(email).collection("RecentDate")
                            .document(todaysDate).set(newBanked);

                    //Create intent with updated gold and earnings values
                    Intent intent = new Intent(this, Bank.class);
                    intent.putExtra("gold",String.valueOf(result));
                    intent.putExtra("winnings",String.valueOf(total));

                    //Display the number of coins they have banked today to them
                    Toast.makeText(this, "You have banked " + String.
                            valueOf(walletList.size() + banked) + " coins today",
                            Toast.LENGTH_SHORT).show();

                    //Pass into method which leaves activity with updated values
                    backToBank(intent);
                }
            }
        }
    }


    //Method to update the listView
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
                arrayAdapter = new ArrayAdapter(DepositCoins.this,
                        R.layout.my_layout, R.id.row_layout, mWallet);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }

        })
                .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DepositCoins.this, "We failed",
                        Toast.LENGTH_SHORT).show();
            }
        });

    }


    //Method to check and possibly update the number of coins banked to the textView
    private void updateCoinsBanked() {
        //Method is similar to that used in MainMenu
        db.collection("users")
                .document(email).collection("RecentDate").document(todaysDate).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {

                    //Set displayBanked to the number of coins banked
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        displayBanked.setText(documentSnapshot.get("Banked").toString());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DepositCoins.this, "Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //Simple method to go back to bank
    private void backToBank(Intent intent) {
        startActivity(intent);
    }


    //Method to go back to bank with the same bundle
    private void backToBankNoChange() {
        //For the back button we retrieve the extras given
        // to this activity and send them back with no change

        Intent intent = new Intent(this, Bank.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}