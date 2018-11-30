package com.example.s1658030.coinzj;

import android.content.Intent;
import android.location.Location;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode;
import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Nearly all of the code in this class is given in the lecture slides or in the MapBox tutorials
// online, so I will only be commenting on the sections which I additionally implemented

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback,
        LocationEngineListener, PermissionsListener {

    private String tag = "Map";
    private MapView mapView;
    private MapboxMap map;

    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private String mapData;
    private PermissionsManager permissionsManager;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String email;

    private HashMap<String,Coin> coins = new HashMap<>();
    private TextView mWallet;


    //==============================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        //Check if current user is null, if not then set email variable
        if (mAuth.getCurrentUser() != null) {
            email = mAuth.getCurrentUser().getEmail();
        }

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        mapData = bundle.getString("mapData");

        //mWallet represents the TextView which displays the number of coins currently in the wallet
        mWallet = findViewById(R.id.numberInWallet);
    }


    //Initialize the map
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag,"Map is null");
        }
        else {
            Log.d(tag, "Initializing map");
            map = mapboxMap;

            //Additional UI settings
            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            //Enable the player's location
            enableLocation();

            //Get all of the marker data from the mapData which was passed as an intent from Main
            // menu, we create a list of features and setup our icon factory for the marker icons
            FeatureCollection featureCollection = FeatureCollection.fromJson(mapData);
            List<Feature> features = featureCollection.features();
            IconFactory iconFactory = IconFactory.getInstance(MapScreen.this);
            MarkerIcons markerIcons = new MarkerIcons();

            //Iterate over all of the features and create map markers for each
            for (Feature f : features) {
                //Check if each feature is a geometry point
                if (f.geometry() instanceof Point) {

                    //Set important variables
                    String id = f.properties().get("id").getAsString();
                    Double value = f.properties().get("value").getAsDouble();
                    String currency = f.properties().get("currency").getAsString();


                    //Check to see if coin has been collected, if it hasn't then add it to the map
                    //Collected coins are stored in users/currUser/Collected
                    db.collection("users")
                            .document(email).collection("Collected").document(id).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    //First we check to see if the coin has been collected, by
                                    // checking if it has been recorded in Collected
                                    if (!documentSnapshot.exists()) {

                                        //If it has not been collected then we update the map, with
                                        // its floored number and geometrical point
                                        String symbol = f.properties()
                                                .get("marker-symbol").getAsString();
                                        LatLng latLng = new LatLng((((Point) f.geometry()).latitude()),
                                                (((Point) f.geometry()).longitude()));

                                        //Create the new coin object
                                        Coin tempCoin = new Coin(id,value,currency);

                                        //Update hashmap with the coin
                                        coins.put(id,tempCoin);

                                        //Determine which marker to use by referring to the master
                                        // key class created, which maps Strings to markers
                                        String key = currency + symbol;
                                        Integer res = markerIcons.masterKey.get(key);
                                        Icon icon = iconFactory.fromResource(res);

                                        //Placing marker on the map with relevant information,
                                        // we combine the symbol and currency and keep the ID as the snippet
                                        String title = symbol + "-" + currency;
                                        map.addMarker(new MarkerOptions().title(title).snippet(id)
                                                .position(latLng).icon(icon));
                                    }
                                }
                            })//Add failure listener in case of failure to update map
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MapScreen.this,
                                            "Unable to update map", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            //Display initially how many coins are in the Wallet by setting the TextView
            // to display the size of our Wallet collection
            db.collection("users")
                    .document(email).collection("Wallet").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            mWallet.setText(String.valueOf(queryDocumentSnapshots.size()));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapScreen.this, "Failed to get Wallet size",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    //Method to go back to main menu
    public void goBack(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }


    //Method to go to bank
    public void goBank(View view) {
        Intent intent = new Intent(this, Bank.class);

        //To pass to bank we need to pass the bundle obtained from Main Menu
        Bundle bundle = getIntent().getExtras();
        intent.putExtras(bundle);

        startActivity(intent);
    }


    //Method seen in slides
    private void enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            Log.d(tag, "Permissions have been granted");
            initializeLocationEngine();
            initializeLocationLayer();
        } else {
            Log.d(tag,"Permissions not yet granted, adding manager");
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }


    //Method seen in slides
    @SuppressWarnings("MissingPermission")
    private void initializeLocationEngine() {
        Log.d(tag, "Initializing location engine");
        locationEngine = new LocationEngineProvider(this)
                .obtainBestLocationEngineAvailable();
        locationEngine.setInterval(5000);
        locationEngine.setFastestInterval(1000);
        locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngine.activate();

        Location lastLocation = locationEngine.getLastLocation();
        if(lastLocation != null) {
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }


    //Method seen in slides
    @SuppressWarnings("MissingPermission")
    private void initializeLocationLayer() {
        if (mapView == null) {
            Log.d(tag,"[initializeLocationLayer] mapView is null");
        } else {
            if (map == null) {
                Log.d(tag, "[initializeLocationLayer] map is null");
            } else {
                Log.d(tag, "Initializing location layer");
                locationLayerPlugin = new LocationLayerPlugin(mapView,
                        map, locationEngine);
                locationLayerPlugin.setLocationLayerEnabled(true);
                locationLayerPlugin.setCameraMode(CameraMode.TRACKING);
                locationLayerPlugin.setRenderMode(RenderMode.NORMAL);
            }
        }
    }


    //Method seen in slides
    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }


    //Method seen in slides
    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag,"Connected to google play services");
        locationEngine.requestLocationUpdates();
    }


    //When location is changed we must check if we are in radius of a coin
    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        }
        else {
            View view = new View(this);
            setCameraPosition(location);

            //We check if each marker on the is within 0.00025 in
            // any direction away from the user's current location
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            for (Marker m : map.getMarkers()) {
                if (((latitude <= m.getPosition().getLatitude()+ 0.00025) &&
                        (latitude >= m.getPosition().getLatitude() - 0.00025) &&
                        ((longitude <= m.getPosition().getLongitude()+ 0.00025) &&
                                (longitude >= m.getPosition().getLongitude() - 0.00025)))) {


                    //Create Map object to store in database
                    Map<String,Object> coin = new HashMap<>();

                    //Get relevant information and set it in the map, ie value and currency
                    String id = m.getSnippet();
                    Double value = coins.get(id).getValue();
                    String currency = coins.get(id).getCurrency();
                    coin.put("value",value);
                    coin.put("currency",currency);


                    //Update Wallet or Spare Change when coin collected,
                    // by checking whether the wallet has 50 coins in it already
                    db.collection("users")
                            .document(email).collection("Wallet").get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            //If wallet has less than 50 coins, add coin into wallet
                            if(queryDocumentSnapshots.size() < 50) {
                                updateWallet(coin, id);

                            }//If wallet has more or equal to 50, add it to Spare Change
                            else {
                                updateSpareChange(coin, id);
                            }
                        }
                    })//We add failure listener in case we could not access the user's wallet
                            .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapScreen.this, "Failed to retrieve Wallet",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


                    //Add coin to collected section, to keep track of those all picked up,
                    // and we also update the number of coins present in Wallet
                    db.collection("users").document(email)
                            .collection("Collected").document(id).set(coin)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Integer size = Integer.parseInt(mWallet.getText().toString());
                            mWallet.setText(String.valueOf(size + 1));
                        }
                    });

                    //Remove the marker from the map and play sound
                    removing(m);

                }
            }

        }
    }


    //Remove marker m and play noise
    private void removing(Marker m) {
        //Make ding noise
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.coinding);
        mediaPlayer.start();

        //Remove the marker from the map
        map.removeMarker(m);
    }


    //Update the Wallet with coin and ID
    private void updateWallet(Map coin, String id) {

        //Create formatter object to format value to 3dp
        DecimalFormat df = new DecimalFormat("#.###");

        //Toast collection of formatted coin
        Toast.makeText(this, "Collected " + coin.get("currency")
                + ": " + String.valueOf(df.format(Double.parseDouble(coin.get("value").toString())))
                , Toast.LENGTH_SHORT).show();

        //Add the coin to the database
        db.collection("users")
                .document(email).collection("Wallet").document(id).set(coin);
    }


    //Update Spare Change with coin and ID
    private void updateSpareChange(Map coin, String id) {

        //Create formatter object to format value to 3dp
        DecimalFormat df = new DecimalFormat("#.###");

        //Toast the collection of formatted coin
        Toast.makeText(this, "Collected " + coin.get("currency")
                + ": " + String.valueOf(df.format(Double.parseDouble(coin.get("value").toString())))
                , Toast.LENGTH_SHORT).show();

        //Add coin to database
        db.collection("users")
                .document(email).collection("Spare Change").document(id).set(coin);
    }


    //Must be overridden or android studio unhappy, although left empty
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) { }


    //Enable location if permissions granted
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation();
        } else {
            Log.d(tag,"Permissions must be enabled");
        }
    }


    //Method in slides
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }


    //Start mapView
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }


    //Stop location engine and layer
    @Override
    protected void onStop() {
        super.onStop();

        if (locationEngine != null) {
            locationEngine.removeLocationUpdates();
        }
        if (locationLayerPlugin != null) {
            locationLayerPlugin.onStop();
        }
    }


    //Following methods are found on slides
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outstate) {
        super.onSaveInstanceState(outstate);
        mapView.onSaveInstanceState(outstate);
    }

}
