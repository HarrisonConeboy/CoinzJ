package com.example.s1658030.coinzj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class MapScreen extends AppCompatActivity implements OnMapReadyCallback,
        LocationEngineListener, PermissionsListener {

    private String tag = "Map";
    private MapView mapView;
    private MapboxMap map;

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationLayerPlugin locationLayerPlugin;
    private Location originLocation;
    private String mapData;
    private MarkerOptions markerOptions;
    private String preferencesFile = "MyPrefsFiles";

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    String email = mAuth.getCurrentUser().getEmail();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private HashMap<String,Coin> coins = new HashMap<String,Coin>();
    private TextView mWallet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        Mapbox.getInstance(this, getString(R.string.access_token));

        mapView = findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Bundle bundle = getIntent().getExtras();
        mapData = bundle.getString("mapData");
        mWallet = findViewById(R.id.numberInWallet);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        if (mapboxMap == null) {
            Log.d(tag,"Map is null");
        } else {
            Log.d(tag, "Initializing map");
            map = mapboxMap;

            map.getUiSettings().setCompassEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);

            FeatureCollection featureCollection = FeatureCollection.fromJson(mapData);
            List<Feature> features = featureCollection.features();
            IconFactory iconFactory = IconFactory.getInstance(MapScreen.this);
            MarkerIcons markerIcons = new MarkerIcons();

            //Iterate over all of the features and create map markers for each
            for (Feature f : features) {
                if (f.geometry() instanceof Point) {

                    //Set important variables
                    String id = f.properties().get("id").getAsString();
                    Double value = f.properties().get("value").getAsDouble();
                    String currency = f.properties().get("currency").getAsString();


                    //Check to see if coin has been collected, if it hasn't then add it to the map
                    db.collection("users")
                            .document(email).collection("Collected").document(id).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    //First we check to see if the coin has been collected
                                    if (!documentSnapshot.exists()) {

                                        //If it has not been collected then we update the map
                                        String symbol = f.properties()
                                                .get("marker-symbol").getAsString();
                                        LatLng latLng = new LatLng((((Point) f.geometry()).latitude()),
                                                (((Point) f.geometry()).longitude()));

                                        //Create the new coin
                                        Coin tempCoin = new Coin(id,value,currency);

                                        //Update hashmap
                                        coins.put(id,tempCoin);

                                        //Determine which marker to use
                                        String key = currency + symbol;
                                        Integer res = markerIcons.masterKey.get(key);
                                        Icon icon = iconFactory.fromResource(res);

                                        //Placing marker on the map with relevant information
                                        String title = symbol + "-" + currency;
                                        map.addMarker(new MarkerOptions().title(title).snippet(id)
                                                .position(latLng).icon(icon));
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(MapScreen.this,
                                            "Unable to update map", Toast.LENGTH_SHORT).show();
                                }
                            });
                }


            }


            enableLocation();

            //Display initially how many coins are in the Wallet
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

    public void goBack(View view) {
        Intent intent = new Intent(this, MainMenu.class);
        startActivity(intent);
    }

    public void goBank(View view) {
        Intent intent = new Intent(this, Bank.class);
        Bundle bundle = getIntent().getExtras();
        intent.putExtras(bundle);

        startActivity(intent);
    }

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
            originLocation = lastLocation;
            setCameraPosition(lastLocation);
        } else {
            locationEngine.addLocationEngineListener(this);
        }
    }

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

    private void setCameraPosition(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(),
                location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected() {
        Log.d(tag,"Connected to google play services");
        locationEngine.requestLocationUpdates();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            Log.d(tag, "[onLocationChanged] location is null");
        } else {
            View view = new View(this);
            originLocation = location;
            setCameraPosition(location);
            FeatureCollection featureCollection = FeatureCollection.fromJson(mapData);
            List<Feature> features = featureCollection.features();

            for (Marker m : map.getMarkers()) {
                if      (((location.getLatitude() <= m.getPosition().getLatitude()+ 0.00025) &&
                        (location.getLatitude() >= m.getPosition().getLatitude() - 0.00025) &&
                        ((location.getLongitude() <= m.getPosition().getLongitude()+ 0.00025) &&
                                (location.getLongitude() >= m.getPosition().getLongitude() - 0.00025)))) {


                    //Create Map object to store in database
                    Map<String,Object> coin = new HashMap<>();

                    //Get relevant information and set it in the map
                    String id = m.getSnippet();
                    Double value = coins.get(id).getValue();
                    String currency = coins.get(id).getCurrency();
                    coin.put("value",value);
                    coin.put("currency",currency);


                    //Update Wallet or Spare Change when coin collected
                    db.collection("users")
                            .document(email).collection("Wallet").get()
                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.size() <= 50) {
                                updateWallet(coin, id);
                            } else {
                                updateSpareChange(coin, id);
                            }
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MapScreen.this, "Failed to retrieve Wallet",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });


                    //Add coin to collected section, to keep track of those all picked up
                    db.collection("users").document(email)
                            .collection("Collected").document(id).set(coin);

                    //Remove the marker from the map and play sound
                    removing(m);
                }
            }


            //Update and display the number of coins in the Wallet
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

    //Remove marker m and play noise
    private void removing(Marker m) {
        //Make ding noise
        MediaPlayer mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.coinding);
        mediaPlayer.start();

        map.removeMarker(m);
    }


    //Update the Wallet with coin and ID
    private void updateWallet(Map coin, String id) {

        Toast.makeText(this, "Collected " + coin.get("currency")
                + ": " + coin.get("value"), Toast.LENGTH_SHORT).show();

        db.collection("users")
                .document(email).collection("Wallet").document(id).set(coin);
    }


    //Update Spare Change with coin and ID
    private void updateSpareChange(Map coin, String id) {
        Toast.makeText(this, "Collected " + coin.get("currency")
                + ": " + coin.get("value"), Toast.LENGTH_SHORT).show();

        db.collection("users")
                .document(email).collection("Spare Change").document(id).set(coin);
    }


    //Must be overridden or android studio unhappy
    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }


    //Enable location if permissions granted
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation();
        } else {
            Log.d(tag,"Permissions must be enabled");
        }
    }


    //Start mapView
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

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
