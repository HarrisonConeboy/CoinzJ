package com.example.s1658030.coinzj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DecimalFormat;

public class Bank extends AppCompatActivity {

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);


        bundle = getIntent().getExtras();
        String gold = bundle.getString("gold");
        String winnings = bundle.getString("winnings");

        TextView currentGold = findViewById(R.id.currentGold3);
        DecimalFormat df = new DecimalFormat("#.##");
        Double gold2 = Double.parseDouble(gold);
        currentGold.setText(String.valueOf(df.format(gold2)));

        TextView earnings = findViewById(R.id.earnings);
        Double earnings2 = Double.parseDouble(winnings);
        earnings.setText(String.valueOf(df.format(earnings2)));


        Button mDeposit = findViewById(R.id.depositCoins);
        mDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDeposit();
            }
        });


        Button mBack = findViewById(R.id.backtomenu);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMenu();
            }
        });


        Button mTransfer = findViewById(R.id.transferSomeCoins);
        mTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSendToSpareChange();
            }
        });


        Button mGamble = findViewById(R.id.timeToGamble);
        mGamble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGamble();
            }
        });

    }

    private void toDeposit() {
        Intent intent = new Intent(this,DepositCoins.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void backToMenu() {
        Intent intent = new Intent(this,MainMenu.class);
        startActivity(intent);
    }

    private void goToSendToSpareChange() {
        Intent intent = new Intent(this, SendToSpareChange.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void goToGamble() {
        Intent intent = new Intent(this, Gambling.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    protected void onStart() {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
