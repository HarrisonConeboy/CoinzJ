package com.example.s1658030.coinzj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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

public class SendToSpareChange extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> mWallet = new ArrayList<String>(50);
    private ArrayList<Object> selectedCoins = new ArrayList<Object>();
    private HashMap<String, Coin> coins = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email = mAuth.getCurrentUser().getEmail();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_to_spare_change);


        updateList();

        listView = findViewById(R.id.sendToSC);

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

        Button mBack = findViewById(R.id.backBttn);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBack();
            }
        });

        Button mTransfer = findViewById(R.id.transferSelected);
        mTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferSelected();
            }
        });


        Button mTransferAll = findViewById(R.id.transferAll);
        mTransferAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                transferAll();
            }
        });

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

                ArrayAdapter arrayAdapter = new ArrayAdapter(com.example.s1658030.coinzj.SendToSpareChange.this, R.layout.my_layout, R.id.row_layout, mWallet);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });
    }


    private void transferSelected() {

        int size = selectedCoins.size();

        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(selectedCoins.get(i));

            HashMap<String,Object> temp = new HashMap<>();

            Double value = coin.getValue();
            String currency = coin.getCurrency();
            temp.put("value",value);
            temp.put("currency",currency);

            db.collection("users").document(email).collection("Spare Change").document(coin.getId()).set(temp);

            db.collection("users").document(email).collection("Wallet").document(coin.getId()).delete();

        }

        if (selectedCoins.size() > 0) {
            goBack();
        } else {
            Toast.makeText(this, "Please select some coins", Toast.LENGTH_SHORT).show();
        }
    }


    private void transferAll() {

        int size = mWallet.size();

        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(mWallet.get(i));

            HashMap<String,Object> temp = new HashMap<>();

            Double value = coin.getValue();
            String currency = coin.getCurrency();
            temp.put("value",value);
            temp.put("currency",currency);

            db.collection("users").document(email).collection("Spare Change").document(coin.getId()).set(temp);

            db.collection("users").document(email).collection("Wallet").document(coin.getId()).delete();

        }

        goBack();

    }



    private void goBack() {
        Intent intent = new Intent(this, Bank.class);
        Bundle bundle = getIntent().getExtras();
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
