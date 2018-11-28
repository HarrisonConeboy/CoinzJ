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

public class SpareChangeSend extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> mSpareChange = new ArrayList<String>();
    private ArrayList<Object> selectedCoins = new ArrayList<Object>();
    private HashMap<String, Coin> coins = new HashMap<>();

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email = mAuth.getCurrentUser().getEmail();
    private String friend;

    private String shil;
    private String quid;
    private String peny;
    private String dolr;
    private Double send;

    private Double gold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spare_change_send);

        Bundle bundle = getIntent().getExtras();
        friend = bundle.getString("friend");

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);

        shil = settings.getString("shil", "");
        peny = settings.getString("peny", "");
        quid = settings.getString("quid", "");
        dolr = settings.getString("dolr", "");

        listView = findViewById(R.id.sparechange);

        updateList();

        db.collection("users").document(friend).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                gold = documentSnapshot.getDouble("Gold");
            }
        });


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


        Button mSendCoins = findViewById(R.id.sendcoinsbutton);
        mSendCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCoins();
            }
        });


        Button back = findViewById(R.id.back2);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToFriends();
            }
        });

        Button sendAll = findViewById(R.id.sendAll);
        sendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendAllCoins();
            }
        });

    }

    private void sendCoins() {
        Double total = 0.0;
        send = 0.0;

        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = selectedCoins.size();

        db.collection("users").document(friend).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                gold = documentSnapshot.getDouble("Gold");
            }
        });

        Double result = gold;

        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(selectedCoins.get(i));

            if (coin.getCurrency().equals("SHIL")) {
                send = send + shilexchange * coin.getValue();
            } else if (coin.getCurrency().equals("QUID")) {
                send = send + quidexchange * coin.getValue();
            } else if (coin.getCurrency().equals("PENY")) {
                send = send + penyexchange * coin.getValue();
            } else if (coin.getCurrency().equals("DOLR")) {
                send = send + dolrexchange * coin.getValue();
            }



            total = total + send;
            result = result + send;

            db.collection("users").document(email).collection("Spare Change").document(coin.getId()).delete();

        }
        if ((gold != null) & (selectedCoins.size() > 0)) {
            Map<String, Object> g = new HashMap<>();
            g.put("Gold", result);
            db.collection("users").document(friend).set(g);
            Toast.makeText(this, "Sent "+ friend + ":\n" + String.valueOf(total) + " gold", Toast.LENGTH_LONG).show();
            backToFriends();
        } else {
            Toast.makeText(this, "Please select coins to send", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendAllCoins() {
        Double total = 0.0;
        send = 0.0;

        Double shilexchange = Double.parseDouble(shil);
        Double quidexchange = Double.parseDouble(quid);
        Double dolrexchange = Double.parseDouble(dolr);
        Double penyexchange = Double.parseDouble(peny);

        int size = mSpareChange.size();

        db.collection("users").document(friend).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                gold = documentSnapshot.getDouble("Gold");
            }
        });

        Double result = gold;

        for (int i = 0; i < size; i++) {
            Coin coin = coins.get(mSpareChange.get(i));

            if (coin.getCurrency().equals("SHIL")) {
                send = send + shilexchange * coin.getValue();
            } else if (coin.getCurrency().equals("QUID")) {
                send = send + quidexchange * coin.getValue();
            } else if (coin.getCurrency().equals("PENY")) {
                send = send + penyexchange * coin.getValue();
            } else if (coin.getCurrency().equals("DOLR")) {
                send = send + dolrexchange * coin.getValue();
            }



            total = total + send;
            result = result + send;

            db.collection("users").document(email).collection("Spare Change").document(coin.getId()).delete();

        }
        if ((gold != null)) {
            Map<String, Object> g = new HashMap<>();
            g.put("Gold", result);
            db.collection("users").document(friend).set(g);
            Toast.makeText(this, "Sent "+ friend + ":\n" + String.valueOf(total) + " gold", Toast.LENGTH_LONG).show();
            backToFriends();
        } else {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            backToFriends();
        }
    }



    private void updateList() {
        mSpareChange.clear();
        db.collection("users").document(email).collection("Spare Change").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                    String value = queryDocumentSnapshots.getDocuments().get(i).get("value").toString();
                    String currency = queryDocumentSnapshots.getDocuments().get(i).get("currency").toString();
                    String res = currency + ": " + value;
                    Double val = Double.parseDouble(value);
                    String id = queryDocumentSnapshots.getDocuments().get(i).getId();
                    Coin coin = new Coin(id, val, currency);
                    mSpareChange.add(res);
                    coins.put(res, coin);
                }
                HashSet hs = new HashSet();
                hs.addAll(mSpareChange);
                mSpareChange.clear();
                mSpareChange.addAll(hs);

                ArrayAdapter arrayAdapter = new ArrayAdapter(com.example.s1658030.coinzj.SpareChangeSend.this, R.layout.my_layout, R.id.row_layout, mSpareChange);
                listView.setAdapter(arrayAdapter);
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            }
        });
    }


    protected void onStart() {
        super.onStart();
    }


    private void backToFriends() {
        Intent intent = new Intent(this,Friends.class);
        startActivity(intent);
    }


    protected void onStop() {
        super.onStop();
    }


    protected void onDestroy() {
        super.onDestroy();
    }


}
