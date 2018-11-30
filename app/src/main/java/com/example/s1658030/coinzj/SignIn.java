package com.example.s1658030.coinzj;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText mPasswordField;
    private EditText mEmailField;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mAuth = FirebaseAuth.getInstance();

        //Set EditTexts and Buttons
        mEmailField = findViewById(R.id.emailField);
        mPasswordField = findViewById(R.id.passwordField);
        Button mLoginButton = findViewById(R.id.loginButton);
        Button mSignUpButton = findViewById(R.id.signUpButton);

        //Set listener for login button which signs an already existing user in
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSignIn();
            }
        });

        //Set listener for sign up button which creates a new user and signs them in
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });

    }


    //Method to sign existing user in
    public void startSignIn() {
        //Retrieve the email and password fields
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        //Use Firebase's own method to sign in an existing user
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    //On complete check if task was successful
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //If successful and current user is not null then go to main menu
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                startActivity(new Intent(SignIn.this,MainMenu.class));
                            }
                        }//Otherwise Toast alert to user
                        else {
                            Toast.makeText(SignIn.this, "Sign in failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //Method to create a new account
    public void createAccount() {
        //Retrieve email and password fields
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        //Use Firebase's own create user method
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    //On complete check if task was successful
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //If successful check if current user is not null then setup the user
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {

                                //For new users we must set their current Gold field in the database
                                // to 0, and also set the RecentDate to an arbitary value which will
                                // later be changed by Main Menu as it will not be equal to today's date
                                String email = user.getEmail();
                                FirebaseFirestore db = FirebaseFirestore.getInstance();

                                HashMap<String,Object> nothing = new HashMap<>();
                                nothing.put("Gold",0);

                                //Set each of the database values
                                db.collection("users").document(email).set(nothing);

                                db.collection("users").document(email)
                                        .collection("RecentDate").document().set(nothing);

                                //Go to Main Menu
                                startActivity(new Intent(SignIn.this,MainMenu.class));
                            }
                        } else {
                            // If creation of account fails, display a message to the user.
                            Toast.makeText(SignIn.this, "Sign up failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //If already signed in, then go immediately to Main Menu
    @Override
    public void onStart() {
        super.onStart();

        //Small check for current user being null, else start activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(SignIn.this,MainMenu.class));
        }
    }
}
