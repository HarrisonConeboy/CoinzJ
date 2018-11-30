package com.example.s1658030.coinzj;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


//This class has many similarities with DepositCoins
public class SendToSpareChange extends AppCompatActivity {

    private ListView listView;
    //mWallet represents the wallet
    private ArrayList<String> mWallet = new ArrayList<>(50);
    private ArrayList<Object> selectedCoins = new ArrayList<>();
    private HashMap<String, Coin> coins = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email = mAuth.getCurrentUser().getEmail();

    private ArrayAdapter arrayAdapter;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to_spare_change);

        //Update the listView and show wallet
        updateList();

        listView = findViewById(R.id.sendToSC);

        //Set on click listener to listView, when a coins is clicked add/remove it to selected
        // coins depending if it was previously present in the ArrayList
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

        //Set listener for back button, when pressed go back to Bank
        Button mBack = findViewById(R.id.backBttn);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });


        //Set listener for transfer button, when pressed transfer
        // coins in selectedCoins from Wallet to Spare Change
        Button mTransfer = findViewById(R.id.transferSelected);
        mTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferSelected();
            }
        });


        //Set listener for transfer all button, when pressed transfer all the coins in Wallet
        Button mTransferAll = findViewById(R.id.transferAll);
        mTransferAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferAll();
            }
        });

    }


    //Identical method to that in Deposit Coins
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
                        arrayAdapter = new ArrayAdapter(SendToSpareChange.this,
                                R.layout.my_layout, R.id.row_layout, mWallet);
                        listView.setAdapter(arrayAdapter);
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SendToSpareChange.this, "We failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    //Method for transferring the coins in selectedCoins
    private void transferSelected() {

        int size = selectedCoins.size();

        //Iterate over the size of selected coins
        for (int i = 0; i < size; i++) {
            //Get the respective coin from the hashmap
            Coin coin = coins.get(selectedCoins.get(i));

            //Create a new map object representing the coin to be added into the database
            HashMap<String,Object> temp = new HashMap<>();

            Double value = coin.getValue();
            String currency = coin.getCurrency();
            temp.put("value",value);
            temp.put("currency",currency);

            //Delete the coin from user's wallet
            db.collection("users").document(email)
                    .collection("Wallet").document(coin.getId()).delete();
            //Add the coin into user's Spare Change
            db.collection("users").document(email)
                    .collection("Spare Change").document(coin.getId()).set(temp);
        }

        //Only if the user selected coins do we go here
        if (size > 0) {

            //If the user selected more than one coin to transfer we Toast and alert saying they
            // successfully transferred one coi, else we use the plural: coins
            if (selectedCoins.size() == 1) {
                Toast.makeText(this, "You transferred: " + String
                        .valueOf(selectedCoins.size()) + " coin", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "You transferred: " + String
                        .valueOf(selectedCoins.size()) + " coins", Toast.LENGTH_SHORT).show();
            }

            //Go back to Bank
            goBack();

        }//If the user did not select coins we Toast alert
        else {
            Toast.makeText(this, "Please select some coins", Toast.LENGTH_SHORT).show();
        }
    }


    //Method is identical to transferSelected, with the exception of using mWallet
    // which represents all the coins within the Wallet to transfer
    private void transferAll() {

        int size = mWallet.size();

        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(mWallet.get(i));

            //Create coin map
            HashMap<String,Object> temp = new HashMap<>();

            Double value = coin.getValue();
            String currency = coin.getCurrency();
            temp.put("value",value);
            temp.put("currency",currency);

            //Add coin to Spare Change
            db.collection("users").document(email)
                    .collection("Spare Change").document(coin.getId()).set(temp);

            //Remove coin from Wallet
            db.collection("users").document(email)
                    .collection("Wallet").document(coin.getId()).delete();
        }

        //Select to Toast coin/coins
        if (size == 1) {
            Toast.makeText(this, "You transferred: " + String
                    .valueOf(mWallet.size()) + " coin", Toast.LENGTH_SHORT).show();
        }
        else if (size > 1) {
            Toast.makeText(this, "You transferred: " + String
                    .valueOf(mWallet.size()) + " coins", Toast.LENGTH_SHORT).show();
        }

        //If the user pressed transferAll they are sent back to Bank regardless
        goBack();
    }


    //Method called to go back to bank, which passes the bundle back it was given
    private void goBack() {
        Intent intent = new Intent(this, Bank.class);
        Bundle bundle = getIntent().getExtras();
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
