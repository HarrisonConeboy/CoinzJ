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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

public class Gambling extends AppCompatActivity {

    private String shil;
    private String quid;
    private String peny;
    private String dolr;
    private Integer check = 0;
    private ListView listView;
    private ArrayList<String> mWallet = new ArrayList<String>(50);
    private ArrayList<Object> selectedCoins = new ArrayList<Object>();
    private HashMap<String, Coin> coins = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email = mAuth.getCurrentUser().getEmail();

    private ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gambling);

        listView = findViewById(R.id.walletInGamble);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        TextView mShil = findViewById(R.id.shilValue);
        TextView mPeny = findViewById(R.id.penyValue);
        TextView mDolr = findViewById(R.id.dolrValue);
        TextView mQuid = findViewById(R.id.quidValue);
        shil = settings.getString("shil", "");
        peny = settings.getString("peny", "");
        quid = settings.getString("quid", "");
        dolr = settings.getString("dolr", "");
        mShil.setText(shil);
        mQuid.setText(quid);
        mDolr.setText(dolr);
        mPeny.setText(peny);


        updateList();

        gambleCoins();
        check = 1;

        Bundle bundle = getIntent().getExtras();
        String temp = bundle.getString("gold");
        TextView currentGold = findViewById(R.id.currentGold5);
        DecimalFormat df = new DecimalFormat("#.##");
        Double temp2 = Double.parseDouble(temp);
        currentGold.setText(String.valueOf(df.format(temp2)));



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


        Button mDeposit = findViewById(R.id.gambleCoins);
        mDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = onCreateDialog(null);
                if (selectedCoins.size() == 0) {
                    Toast.makeText(Gambling.this, "Please select some coins", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.show();
                }
            }
        });



        Button back = findViewById(R.id.back3);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToBankNoChange();
            }
        });

    }


    private void gambleCoins() {

        Double total = 0.0;
        Double add = 0.0;

        Bundle bundle = getIntent().getExtras();

        Double result = Double.parseDouble(bundle.getString("gold"));

        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = selectedCoins.size();

        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(selectedCoins.get(i));

            if (coin.getCurrency().equals("SHIL")) {
                add = add + shilexchange * coin.getValue();
            } else if (coin.getCurrency().equals("QUID")) {
                add = add + quidexchange * coin.getValue();
            } else if (coin.getCurrency().equals("PENY")) {
                add = add + penyexchange * coin.getValue();
            } else if (coin.getCurrency().equals("DOLR")) {
                add = add + dolrexchange * coin.getValue();
            }

            db.collection("users").document(email).collection("Wallet").document(coin.getId()).delete();

        }

        if (selectedCoins.size() > 0) {
            Random r = new Random();
            int low = 0;
            int high = 100;
            int rand = r.nextInt(high-low) + low;

            if(rand <= 32) {
                Toast.makeText(this, "You Won!", Toast.LENGTH_SHORT).show();
                result = result + add*2;
                total = total + add*2;
            } else {
                Toast.makeText(this, "Better luck next time!", Toast.LENGTH_SHORT).show();
            }
            Map<String, Object> g = new HashMap<>();
            g.put("Gold", result);
            db.collection("users").document(email).set(g);
            Intent intent = new Intent(this, Bank.class);
            intent.putExtra("gold",String.valueOf(result));
            intent.putExtra("winnings",String.valueOf(total));
            backToBank(intent);

        }
    }


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



    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = "Are you sure? \nYou have a 33% chance of doubling value";
        String yes = "Yes";
        String cancel = "Cancel";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton(yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        gambleCoins();
                    }
                })
                .setNegativeButton(cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        return builder.create();
    }


    private void backToBank(Intent intent) {
        startActivity(intent);
    }

    private void backToBankNoChange() {
        Intent intent = new Intent(this, Bank.class);
        Bundle bundle = getIntent().getExtras();
        intent.putExtras(bundle);
        startActivity(intent);
    }



}
