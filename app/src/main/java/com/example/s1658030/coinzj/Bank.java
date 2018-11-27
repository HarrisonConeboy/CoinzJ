package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;

public class Bank extends AppCompatActivity {

    private String gold;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        SharedPreferences settings = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        TextView mShil = findViewById(R.id.shilValue);
        TextView mPeny = findViewById(R.id.penyValue);
        TextView mDolr = findViewById(R.id.dolrValue);
        TextView mQuid = findViewById(R.id.quidValue);
        String shil = settings.getString("shil","");
        String peny = settings.getString("peny","");
        String quid = settings.getString("quid","");
        String dolr = settings.getString("dolr","");
        mShil.setText(shil);
        mQuid.setText(quid);
        mDolr.setText(dolr);
        mPeny.setText(peny);

        bundle = getIntent().getExtras();
        gold = bundle.getString("gold");

        TextView currentGold = findViewById(R.id.currentGold2);
        DecimalFormat df = new DecimalFormat("#.##");
        Double gold2 = Double.parseDouble(gold);
        currentGold.setText(String.valueOf(df.format(gold2)));


        Button mDeposit = findViewById(R.id.deposit);
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
