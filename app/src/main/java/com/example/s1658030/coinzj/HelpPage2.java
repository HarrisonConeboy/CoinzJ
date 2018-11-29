package com.example.s1658030.coinzj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HelpPage2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page2);

        Button back = findViewById(R.id.SpBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPage();
            }
        });

        Button backMenu = findViewById(R.id.SpMenu);
        backMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backMenu();
            }
        });

    }


    private void backPage() {
        Intent intent = new Intent(this, HelpPage.class);
        startActivity(intent);
    }

    private void backMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }
}
