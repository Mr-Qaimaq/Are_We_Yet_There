package edu.msu.kalisamg.areweyettherekalisamg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager = null;

    private double latitude = 0;
    private double longitude = 0;
    private boolean valid = false;

    private double toLatitude = 0;
    private double toLongitude = 0;
    private String to = "";

    private SharedPreferences settings = null;
    private final static String TO = "to";
    private final static String TOLAT = "tolat";
    private final static String TOLONG = "tolong";

    private final String emptyString = "";

    private ActiveListener activeListener = new ActiveListener();

    private String transportMethod = "";

    private void registerListeners() {
        unregisterListeners();

        // Create a Criteria object
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestAvailable = locationManager.getBestProvider(criteria, true);

        if(bestAvailable != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
            TextView viewProvider = (TextView)findViewById(R.id.textProvider);
            viewProvider.setText(bestAvailable);
            Location location = locationManager.getLastKnownLocation(bestAvailable);
            onLocation(location);
        }
    }

    private void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }

    private void onLocation(Location location) {
        if(location == null) {
            return;
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        valid = true;

        setUI();
    }

    /**
     * Handle setting a new "to" location.
     * @param address Address to display
     * @param lat latitude
     * @param lon longitude
     */
    private void newTo(String address, double lat, double lon) {
        to = address;
        toLatitude = lat;
        toLongitude = lon;

        SharedPreferences.Editor prefsEdit = settings.edit();
        prefsEdit.putString(TO, to);
        prefsEdit.putString(TOLAT, String.valueOf(toLatitude));
        prefsEdit.putString(TOLONG, String.valueOf(toLongitude));
        prefsEdit.apply();
        // String test = settings.getString(TOLAT, "");
        setUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Handle an options menu selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.itemSparty:
                newTo("Sparty", 42.731138, -84.487508);
                return true;

            case R.id.itemHome:
                newTo("Home", 42.723783, -84.489489);
                return true;

            case R.id.item2250:
                newTo("2250 Engineering", 42.724303, -84.480507);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Also, dont forget to add overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //   int[] grantResults)
                // to handle the case where the user grants the permission.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        // Force the screen to say on and bright
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the location manager
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        latitude = 42.731138;
        longitude = -84.487508;
        valid = true;

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        to = settings.getString(TO, "2250 Engineering");
        toLatitude = Double.parseDouble(settings.getString(TOLAT, "42.724303"));
        toLongitude = Double.parseDouble(settings.getString(TOLONG, "-84.480507"));
    }

    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onResume() {
        super.onResume();

        TextView viewProvider = findViewById(R.id.textProvider);
        viewProvider.setText("");

        setUI();
        registerListeners();
    }

    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onPause() {
        unregisterListeners();
        super.onPause();
    }

    /**
     * Set all user interface components to the current state
     */
    @SuppressLint("DefaultLocale")
    private void setUI() {

        TextView textTo = findViewById(R.id.textTo);
        textTo.setText(to);

        TextView viewLatitude = findViewById(R.id.textLatitude);
        TextView viewLongitude = findViewById(R.id.textLongitude);
        TextView viewDistance = findViewById(R.id.textDistance);

        viewLatitude.setText(emptyString);
        viewLongitude.setText(emptyString);
        viewDistance.setText(emptyString);
        if(valid){
            float[] distance = new float[1];
            Location.distanceBetween(latitude, longitude, toLatitude, toLongitude, distance);

            viewLatitude.setText(String.valueOf(latitude));
            viewLongitude.setText(String.valueOf(longitude));
            viewDistance.setText(String.format("%1$6.1fm", distance[0]));

        }

    }

    private class ActiveListener implements LocationListener {

        @Override
        public void onLocationChanged(@NonNull Location location) {
            onLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            registerListeners();
        }
    };

    public void onNew(View view) {
        EditText location = (EditText)findViewById(R.id.editLocation);
        final String address = location.getText().toString().trim();
        newAddress(address);
    }

    private void newAddress(final String address) {
        if(address.equals("")) {
            // Don't do anything if the address is blank
            return;
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                lookupAddress(address);

            }

        }).start();
    }

    /**
     * Look up the provided address. This works in a thread!
     * @param address Address we are looking up
     */
    private void lookupAddress(String address) {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
        boolean exception = false;
        List<Address> locations;
        try {
            locations = geocoder.getFromLocationName(address, 1);
        } catch(IOException ex) {
            // Failed due to I/O exception
            locations = null;
            exception = true;
        }
        final List<Address> tempLocations = locations;
        final boolean tempException = exception;


        MainActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                newLocation(address, tempException, tempLocations);
            }

        });
    }

    private void newLocation(String address, boolean exception, List<Address> locations) {

        if(exception) {
            Toast.makeText(MainActivity.this, R.string.exception, Toast.LENGTH_SHORT).show();
        } else {
            if(locations == null || locations.size() == 0) {
                Toast.makeText(this, R.string.couldnotfind, Toast.LENGTH_SHORT).show();
                return;
            }

            EditText location = (EditText)findViewById(R.id.editLocation);
            location.setText("");

            // We have a valid new location
            Address a = locations.get(0);
            newTo(address, a.getLatitude(), a.getLongitude());

        }
    }

    // OnClick Listeners of Radio Buttons
    public void onWalking(View view) {
        transportMethod = "w";
    }

    public void onDriving(View view) {
        transportMethod = "d";
    }

    public void onBicycling(View view) {
        transportMethod = "b";
    }

    public void onRoute(View view) {
        if(transportMethod.equals("")){
            Toast.makeText(this, "Please, choose transportation method", Toast.LENGTH_LONG).show();
            return;
        }

        String temp = "google.navigation:q=" + String.valueOf(toLatitude) + "," + String.valueOf(toLongitude) + "&mode=" + transportMethod;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(temp));
        Intent chooser = Intent.createChooser(intent, "Launch Maps");
        startActivity(chooser);
    }

}