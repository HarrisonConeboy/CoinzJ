package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

public class DepositCoins extends AppCompatActivity {

    private String shil;
    private String quid;
    private String peny;
    private String dolr;
    private Double gold;
    private Double add;
    private Integer check = 0;
    private ListView listView;
    private ArrayList<String> mWallet = new ArrayList<String>(50);
    private ArrayList<Object> walletList = new ArrayList<Object>();
    private HashMap<String, Coin> coins = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email = mAuth.getCurrentUser().getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_coins);

        listView = findViewById(R.id.wallet);

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

        depositCoins();
        check = 1;



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


        Button mDeposit = findViewById(R.id.deposit);
        mDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depositCoins();
            }
        });


        Button depositAll = findViewById(R.id.depositAll);
        depositAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                depositAll();
            }
        });


        Button back = findViewById(R.id.back5);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToBank();
            }
        });

    }


    private void depositCoins() {
        if (check == 0) {
            db.collection("users").document(email).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    gold = documentSnapshot.getDouble("Gold");
                }
            });
        } else {
            Double total = 0.0;
            add = 0.0;
            db.collection("users").document(email).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    gold = documentSnapshot.getDouble("Gold");
                }
            });


            Double result = gold;

            Double shilexchange = Double.parseDouble(shil);
            Double quidexchange = Double.parseDouble(quid);
            Double dolrexchange = Double.parseDouble(dolr);
            Double penyexchange = Double.parseDouble(peny);

            int size = walletList.size();

            for (int i = 0; i < size; i++) {
                Coin coin = coins.get(walletList.get(i));

                if (coin.getCurrency().equals("SHIL")) {
                    add = add + shilexchange * coin.getValue();
                } else if (coin.getCurrency().equals("QUID")) {
                    add = add + quidexchange * coin.getValue();
                } else if (coin.getCurrency().equals("PENY")) {
                    add = add + penyexchange * coin.getValue();
                } else if (coin.getCurrency().equals("DOLR")) {
                    add = add + dolrexchange * coin.getValue();
                }


                if (gold != null) {
                    db.collection("users").document(email).collection("Wallet").document(coin.getId()).delete();
                    result = result + add;
                    total = total + add;
                }
            }

            if ((gold != null) & (walletList.size() > 0)) {
                Map<String, Object> g = new HashMap<>();
                g.put("Gold", result);
                db.collection("users").document(email).set(g);
                Toast.makeText(this, "Deposited: " + String.valueOf(total), Toast.LENGTH_SHORT).show();
                backToBank();
            } else {
                Toast.makeText(this, "Please select some coins", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void depositAll() {
        Double total = 0.0;

        db.collection("users").document(email).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                gold = documentSnapshot.getDouble("Gold");
            }
        });

        Double result = gold;

        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = mWallet.size();

        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(mWallet.get(i));

            if (coin.getCurrency().equals("SHIL")) {
                add = add + shilexchange * coin.getValue();
            } else if (coin.getCurrency().equals("QUID")) {
                add = add + quidexchange * coin.getValue();
            } else if (coin.getCurrency().equals("PENY")) {
                add = add + penyexchange * coin.getValue();
            } else if (coin.getCurrency().equals("DOLR")) {
                add = add + dolrexchange * coin.getValue();
            }

            if (gold != null) {
                db.collection("users").document(email).collection("Wallet").document(coin.getId()).delete();
                result = result + add;
                total = total + add;
            }
        }

        if (gold != null) {
            Map<String, Object> g = new HashMap<>();
            g.put("Gold", result);
            db.collection("users").document(email).set(g);
            Toast.makeText(this, "Deposited: " + String.valueOf(total), Toast.LENGTH_SHORT).show();
            backToBank();
        }
    }


    private void updateList() {
        mWallet.clear();
        db.collection("users").document(email).collection("Wallet").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    String currency = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                    String res = currency + ": " + value;
                    Double val = Double.parseDouble(value);
                    String id = queryDocumentSnapshots.getDocuments().get(i).getId();
                    Coin coin = new Coin(id, val, currency);
                    mWallet.add(res);
                    coins.put(res, coin);
                }

                HashSet hs = new HashSet();
                hs.addAll(mWallet);
                mWallet.clear();
                mWallet.addAll(hs);

                ArrayAdapter arrayAdapter = new ArrayAdapter(com.example.s1658030.coinzj.DepositCoins.this, R.layout.my_layout, R.id.row_layout, mWallet);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });
    }


    protected void onStart() {
        super.onStart();
    }


    private void backToBank() {
        Intent intent = new Intent(this,Bank.class);
        startActivity(intent);
    }


    protected void onStop() {
        super.onStop();
    }


    protected void onDestroy() {
        super.onDestroy();
    }


}


