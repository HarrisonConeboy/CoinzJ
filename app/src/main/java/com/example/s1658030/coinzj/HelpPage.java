package com.example.s1658030.coinzj;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class HelpPage extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_page);

        //Set listener to back button which calls method to send user back to menu
        Button back = findViewById(R.id.backToMenuHelp);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMenu();
            }
        });

        //Set listener to next button which calls method to send user to next help page
        Button next = findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextPage();
            }
        });

    }


    //Method to send user back to main menu
    private void backToMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }


    //Method to send user to the next help page
    private void nextPage() {
        Intent intent = new Intent(this, HelpPage2.class);
        startActivity(intent);
    }
}
