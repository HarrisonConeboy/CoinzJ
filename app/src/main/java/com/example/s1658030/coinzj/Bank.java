package com.example.s1658030.coinzj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.text.DecimalFormat;

public class Bank extends AppCompatActivity {

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        //Get the previous bundle varriables
        bundle = getIntent().getExtras();
        String gold = bundle.getString("gold");
        String winnings = bundle.getString("winnings");

        //Here we set the current gold banked in our database, this was passed from intent
        TextView currentGold = findViewById(R.id.currentGold3);
        DecimalFormat df = new DecimalFormat("#.##");
        Double gold2 = Double.parseDouble(gold);
        currentGold.setText(String.valueOf(df.format(gold2)));


        //Here we set the most recent earnings from our intent
        TextView earnings = findViewById(R.id.earnings);
        Double earnings2 = Double.parseDouble(winnings);
        earnings.setText(String.valueOf(df.format(earnings2)));


        //Create listener for Depositing Coins, simply calls function
        Button mDeposit = findViewById(R.id.depositCoins);
        mDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toDeposit();
            }
        });


        //Create listener for Back button, simply calls function
        Button mBack = findViewById(R.id.backtomenu);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMenu();
            }
        });


        //Create listener for Transfering Coins, simply calls function
        Button mTransfer = findViewById(R.id.transferSomeCoins);
        mTransfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToSendToSpareChange();
            }
        });


        //Create listener for Gamble, simply calls function
        Button mGamble = findViewById(R.id.timeToGamble);
        mGamble.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGamble();
            }
        });

    }


    //Simple function to start activity
    private void toDeposit() {
        //Create intent to change activity and pass previous bundle
        Intent intent = new Intent(this,DepositCoins.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    //Simple function to start activity
    private void backToMenu() {
        //Create intent to change activity
        Intent intent = new Intent(this,MainMenu.class);
        startActivity(intent);
    }


    //Simple function to start activity
    private void goToSendToSpareChange() {
        //Create intent to change activity and pass previous bundle
        Intent intent = new Intent(this, SendToSpareChange.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    //Simple function to start activity
    private void goToGamble() {
        //Create intent to change activity and pass previous bundle
        Intent intent = new Intent(this, Gambling.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

}