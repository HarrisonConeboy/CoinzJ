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

        //Set listener to the back button which calls method to send user back a page
        Button back = findViewById(R.id.SpBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backPage();
            }
        });


        //Set listener to Menu button which calls method to send user back to menu
        Button backMenu = findViewById(R.id.SpMenu);
        backMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backMenu();
            }
        });

    }


    //Method to send user to previous help page
    private void backPage() {
        Intent intent = new Intent(this, HelpPage.class);
        startActivity(intent);
    }


    //Method to send user to Main Menu
    private void backMenu() {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

}
