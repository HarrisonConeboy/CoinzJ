package com.example.s1658030.coinzj;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


//In this Activity we can add friends
public class Friends extends AppCompatActivity {

    //Set Firebase variables
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email;

    private ListView listView;
    private ArrayList<String> allFriends = new ArrayList<>();

    private ArrayAdapter arrayAdapter;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        //This bit of code prevents the keyboard from appearing when activity is launched
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //Set email if currentUser is not null
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
        }

        //Set listView
        listView = findViewById(R.id.friends);

        //Update the listView
        updateFriendsList();


        //Set listener on the back button, when pressed takes user to Main Menu
        Button mainMenu = findViewById(R.id.back2);
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMenu();
            }
        });


        //Set listener to Add Friend button, when pressed calls method addFriend
        Button addFriend = findViewById(R.id.addFriend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });


        //Set listener on the listView, when friend on listView is pressed it brings up a dialog
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //We must first save the username of the friend pressed and pass it into method
                String friend = (String) parent.getItemAtPosition(position);
                Dialog dialog = onCreateDialog(friend);
                dialog.show();
            }
        });

    }


    //Method used to create Dialog box
    private Dialog onCreateDialog(String friend) {
        //Create list of Strings which represent the options in order
        String[] tws = new String[4];
        tws[0] = "Send Coins From Wallet";
        tws[1] = "Send Coins From Spare Change";
        tws[2] = "Remove Friend";
        tws[3] = "Cancel";

        //Next section of code was taken from android studio documentation with my own edits
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Action")
                .setItems(tws, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //When first option is pressed, we send friend coins from Wallet
                        if (which == 0) {
                            goToWalletSend(friend);
                        }
                        //When second option is pressed, we send friend coins from Spare Change
                        else if (which == 1) {
                            goToSpareChangeSend(friend);
                        }
                        //Third option will remove friend
                        else if (which == 2) {
                            //Go into the database and simply remove the friend
                            db.collection("users").document(email)
                                    .collection("Friends").document(friend).delete();

                            //Now we must update the listView to remove the deleted friend
                            updateFriendsList();

                            //Toast alert message to user
                            Toast.makeText(Friends.this, "Friend Removed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        //Return the Dialog
        return builder.create();
    }


    //Method is identical to that used in Deposit Coins, but arraylist is taken from Friends
    private void updateFriendsList() {
        allFriends.clear();
        db.collection("users").document(email).collection("Friends").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

                    //We iterate over all of the friends
                    // stored and add them to the ArrayList allFriends
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            String friend = queryDocumentSnapshots.getDocuments().get(i).getId();
                            allFriends.add(friend);
                        }

                        //Make sure there are no duplicates
                        HashSet hs = new HashSet();
                        hs.addAll(allFriends);
                        allFriends.clear();
                        allFriends.addAll(hs);

                        //Set the adapter and update the listView
                        arrayAdapter = new ArrayAdapter(com.example.s1658030.coinzj.
                                Friends.this, android.R.layout.simple_list_item_1, allFriends);
                        listView.setAdapter(arrayAdapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Friends.this, "Failed to update Friends",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    //The method called after pressing Add Friend
    private void addFriend() {
        //Get the text inside the EditText and check if not empty
        EditText mFriendUsername = findViewById(R.id.friendUsername);
        if(!mFriendUsername.getText().toString().isEmpty()) {

            //We create map to fill the friend field in the database
            String friendUsername = mFriendUsername.getText().toString();
            Map<String,Object> friend = new HashMap<>();
            friend.put(friendUsername,"I LOVE THIS FRIEND");

            //Must check if user is attempting to add themselves
            if(friendUsername.equals(email)) {

                //Toast message and remove the typed text
                Toast.makeText(this, "You cannot add yourself",
                        Toast.LENGTH_SHORT).show();

                mFriendUsername.setText("");
            }
            else {

                //Now we must check if the entered text a valid user in our database,
                // we attempt to fetch the user and check if that snapshot exists
                db.collection("users").document(friendUsername).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                //If they exist, we Toast a success message and add the friend
                                if (documentSnapshot.exists()) {

                                    Toast.makeText(Friends.this, "Friend added",
                                            Toast.LENGTH_SHORT).show();

                                    //Set the friend in user's Friends section
                                    db.collection("users").document(email)
                                            .collection("Friends")
                                            .document(friendUsername).set(friend);

                                    //Remove the typed text
                                    mFriendUsername.setText("");

                                    //Update the listView to show the added friend
                                    updateFriendsList();
                                }
                                //If username not present in database, Toast the non existence
                                else {
                                    Toast.makeText(Friends.this, "Friend " +
                                            "does not exist", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Friends.this, "Error getting friend",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }
        //Otherwise advise the user that they must enter a username in the EditText
        else {
            Toast.makeText(this, "Please enter friend email",
                    Toast.LENGTH_SHORT).show();
        }

    }


    //Method to go back to Main Menu
    private void backToMenu() {
        Intent intent = new Intent(this,MainMenu.class);
        startActivity(intent);
    }


    //Method to send friend coins from Wallet, we put the friend's username as intent Extras
    private void goToWalletSend(String friend) {
        Intent intent = new Intent(this,WalletSend.class);
        intent.putExtra("friend",friend);
        startActivity(intent);
    }


    //Method to send friend coins from Spare Change, we put the friend's username as intent Extras
    private void goToSpareChangeSend(String friend) {
        Intent intent = new Intent(this,SpareChangeSend.class);
        intent.putExtra("friend",friend);
        startActivity(intent);
    }

}
