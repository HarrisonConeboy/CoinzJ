package com.example.s1658030.coinzj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;


//Activity which gambles selected coins
public class Gambling extends AppCompatActivity {

    private String shil;
    private String quid;
    private String peny;
    private String dolr;

    private ListView listView;

    private ArrayList<String> mWallet = new ArrayList<>(50);
    private ArrayList<Object> selectedCoins = new ArrayList<>();
    private HashMap<String, Coin> coins = new HashMap<>();

    //Set Firebase variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;

    private ArrayAdapter arrayAdapter;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambling);

        //Set listView
        listView = findViewById(R.id.walletInGamble);

        //Get current user email if not null
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
        }

        //Get the exchange rates for the currencies
        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        shil = settings.getString("shil", "");
        peny = settings.getString("peny", "");
        quid = settings.getString("quid", "");
        dolr = settings.getString("dolr", "");

        //Set the respective textviews to the currency rates
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

        //Obtain the user's gold which is passed in through the bundle
        Bundle bundle = getIntent().getExtras();
        String temp = bundle.getString("gold");

        //Set the textView to the current gold formatted to 2 decimal places
        TextView currentGold = findViewById(R.id.currentGold5);
        DecimalFormat df = new DecimalFormat("#.##");
        Double temp2 = Double.parseDouble(temp);
        currentGold.setText(String.valueOf(df.format(temp2)));


        //Set an on click listener to the listView which adds/removes coins to selectedCoins
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


        //Set on click listener to the Gamble Coins button, which calls the createDialog function
        Button gambleCoins = findViewById(R.id.gambleCoins);
        gambleCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //First we check if the user has selected some coins
                if (selectedCoins.size() == 0) {
                    Toast.makeText(Gambling.this, "Please select some coins",
                            Toast.LENGTH_SHORT).show();
                }
                //If they have selected some coins, create and show the dialog object
                else {
                    Dialog dialog = onCreateDialog(null);
                    dialog.show();
                }
            }
        });


        //Set on click listener for the back button, when pressed returns the user to Bank
        Button back = findViewById(R.id.back3);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToBankNoChange();
            }
        });

    }


    //Method which gambles coins in selectedCoins
    private void gambleCoins() {
        //Total represents the value of user's winnings,
        // add represents total value in gold of all coins combined
        Double total = 0.0;
        Double add = 0.0;

        //Retrieve previous bundle, and get the user's current gold
        Bundle bundle = getIntent().getExtras();
        Double result = Double.parseDouble(bundle.getString("gold"));

        //Parse exchange rates
        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = selectedCoins.size();

        //Iterate over coins in selectedCoins
        for (int i = 0; i < size; i++) {
            //Retrieve respective coin in Hashmap
            Coin coin = coins.get(selectedCoins.get(i));

            //Calculate value in gold of coin
            if (coin.getCurrency().equals("SHIL")) {
                add = add + shilexchange * coin.getValue();
            } else if (coin.getCurrency().equals("QUID")) {
                add = add + quidexchange * coin.getValue();
            } else if (coin.getCurrency().equals("PENY")) {
                add = add + penyexchange * coin.getValue();
            } else if (coin.getCurrency().equals("DOLR")) {
                add = add + dolrexchange * coin.getValue();
            }

            //Remove coin from user's Wallet
            db.collection("users").document(email)
                    .collection("Wallet").document(coin.getId()).delete();

        }

        //Only enter if user selected more zero coins
        if (selectedCoins.size() > 0) {
            //Create a random number between 1 and 100
            Random r = new Random();
            int low = 1;
            int high = 100;
            int rand = r.nextInt(high-low) + low;

            //If random number is 33 or less, then user wins
            if(rand <= 33) {
                //Toast, then update result and total by adding double the value of add
                Toast.makeText(this, "You Won!", Toast.LENGTH_SHORT).show();
                result = result + add*2;
                total = total + add*2;
            }
            //User lost and don't update result or total, Toast condolences
            else {
                Toast.makeText(this, "Better luck next time!", Toast.LENGTH_SHORT).show();
            }
            //Create Map object to update user's gold with result
            Map<String, Object> g = new HashMap<>();
            g.put("Gold", result);
            //Update database
            db.collection("users").document(email).set(g);

            //Create intent with new values of gold and winnings to be displayed in Bank
            Intent intent = new Intent(this, Bank.class);
            intent.putExtra("gold",String.valueOf(result));
            intent.putExtra("winnings",String.valueOf(total));

            //Return user to Bank with Intent
            backToBank(intent);

        }
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
                        arrayAdapter = new ArrayAdapter(Gambling.this,
                                R.layout.my_layout, R.id.row_layout, mWallet);
                        listView.setAdapter(arrayAdapter);
                        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Gambling.this, "We failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });

    }


    //Method to create dialog window, taken from android documentation
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create message to display to user, as well as positive and negative buttons
        String message = "Are you sure? \nYou have a 33% chance of doubling value";
        String yes = "Yes";
        String cancel = "Cancel";

        //Create dialog with associated buttons and title
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    //When yes is pressed, it calls gambleCoins
                    public void onClick(DialogInterface dialog, int id) {
                        gambleCoins();
                    }
                })
                .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //We leave this blank as it closes the dialog
                    }
                });
        return builder.create();
    }


    //Method to return user to Bank with new intent
    private void backToBank(Intent intent) {
        startActivity(intent);
    }


    //Method to return user to Bank with no change in intent
    private void backToBankNoChange() {
        Intent intent = new Intent(this, Bank.class);
        Bundle bundle = getIntent().getExtras();
        intent.putExtras(bundle);
        startActivity(intent);
    }

}
