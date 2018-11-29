package com.example.s1658030.coinzj;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

public class Friends extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private String email = mAuth.getCurrentUser().getEmail();

    private ListView listView;
    private ArrayList<String> allFriends = new ArrayList<>();

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        listView = findViewById(R.id.friends);

        updateFriendsList();

        Button mainMenu = findViewById(R.id.back2);
        mainMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMenu();
            }
        });

        Button addFriend = findViewById(R.id.addFriend);
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String friend = (String) parent.getItemAtPosition(position);
                Dialog dialog = onCreateDialog(friend);
                dialog.show();
            }
        });
    }

    public Dialog onCreateDialog(String friend) {
        String[] tws = new String[4];
        tws[0] = "Send Coins From Wallet";
        tws[1] = "Send Coins From Spare Change";
        tws[2] = "Remove Friend";
        tws[3] = "Cancel";
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Action")
                .setItems(tws, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            goToWalletSend(friend);
                        }
                        else if (which == 1) {
                            goToSpareChangeSend(friend);
                        }
                        else if(which == 2) {
                            db.collection("users").document(email).collection("Friends").document(friend).delete();
                            updateFriendsList();
                            Toast.makeText(Friends.this, "Friend Removed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return builder.create();
    }


    private void updateFriendsList() {
        allFriends.clear();
        db.collection("users").document(email).collection("Friends").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                            String friend = queryDocumentSnapshots.getDocuments().get(i).getId();
                            allFriends.add(friend);
                        }

                        HashSet hs = new HashSet();
                        hs.addAll(allFriends);
                        allFriends.clear();
                        allFriends.addAll(hs);

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

    private void addFriend() {
        EditText mFriendUsername = findViewById(R.id.friendUsername);
        if(!mFriendUsername.getText().toString().isEmpty()) {
            String friendUsername = mFriendUsername.getText().toString();
            Map<String,Object> friend = new HashMap<>();
            friend.put(friendUsername,"I LOVE THIS FRIEND");

            if(friendUsername.equals(email)) {

                Toast.makeText(this, "You cannot add yourself",
                        Toast.LENGTH_SHORT).show();

                mFriendUsername.setText("");
            }
            else {

                db.collection("users").document(friendUsername).get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {

                                    Toast.makeText(Friends.this, "Friend added",
                                            Toast.LENGTH_SHORT).show();

                                    db.collection("users").document(email)
                                            .collection("Friends")
                                            .document(friendUsername).set(friend);

                                    mFriendUsername.setText("");

                                    updateFriendsList();
                                }
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
        else {
            Toast.makeText(this, "Please enter friend email",
                    Toast.LENGTH_SHORT).show();
        }

    }

    private void backToMenu() {
        Intent intent = new Intent(this,MainMenu.class);
        startActivity(intent);
    }

    private void goToWalletSend(String friend) {
        Intent intent = new Intent(this,WalletSend.class);
        intent.putExtra("friend",friend);
        startActivity(intent);
    }

    private void goToSpareChangeSend(String friend) {
        Intent intent = new Intent(this,SpareChangeSend.class);
        intent.putExtra("friend",friend);
        startActivity(intent);
    }

    protected void onStart() {
        super.onStart();
    }
}
