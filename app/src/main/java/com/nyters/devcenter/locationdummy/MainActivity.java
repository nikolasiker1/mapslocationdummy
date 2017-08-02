package com.nyters.devcenter.locationdummy;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    private double latitude, longitude;
    private Long time = (long) 0;

    private TextView txtLoaction, txtTime;
    private Button btnGatherLocation;
    private MapView mapsShowcase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        txtLoaction = (TextView) findViewById(R.id.txtLocation);
        txtTime = (TextView) findViewById(R.id.txtTime);
        btnGatherLocation = (Button) findViewById(R.id.btnGatherLocation);
        mapsShowcase = (MapView) findViewById(R.id.mapsShowcase);
        mapsShowcase.onCreate(savedInstanceState);
        MapsInitializer.initialize(MainActivity.this);






        provideRequestUpdate((long) 5000,(long) 5, LocationManager.NETWORK_PROVIDER);

        btnGatherLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtLoaction.setText(getCompleteAddressString(latitude, longitude));
                txtTime.setText("Time: " + formatTime(time));
                setMapLocation(mapsShowcase);

            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        time = location.getTime();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();

    }



    /*
    * Method for updating location
    * You can use locationManager.showLastKnownLocation() but if location is not updated it will return null
    * because of that this is better option
    * @param time - time interval to update
    * @param meters - distance in meters to update
    * @param provider - location system provider
    *
    * for more info check following link:
    * https://developer.android.com/reference/android/location/LocationManager.html#requestLocationUpdates(java.lang.String, long, float, android.location.LocationListener)
    */

    public void provideRequestUpdate(Long time, Long meters, String provider) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        else {
            locationManager.requestLocationUpdates(provider, time, meters, this);
        }

    }

    /*
    * Function for formatting time format from long unreadable format to familiar readable format
    * @param time - unreadable time format
    * returns - String readable time date format
    *
    */

    public String formatTime(long time){
        if (time != 0) {
            Date date = new Date(time);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            return simpleDateFormat.format(date);
        }

        else
            return "Error!";
    }

    /*
     * Function for getting location values from longitude and latitude values
     * returns - Sting format of city location and country
     *
     *
     * Thanks to: Mohamed Hisham Ibn Hanifa from StackOverflow
     * link: https://stackoverflow.com/a/19927013/8320675
     */

    private String getCompleteAddressString(double latitude, double longitude) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                strAdd = returnedAddress.getLocality() + ", " + returnedAddress.getCountryName();

            } else {
                Log.w("My Current loction address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setTitle("Error")
                    .setMessage("Program experienced error! Please try again.")
                    .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            provideRequestUpdate((long) 5000,(long) 5, LocationManager.NETWORK_PROVIDER);
                            txtLoaction.setText(getCompleteAddressString(MainActivity.this.latitude, MainActivity.this.longitude));
                            txtTime.setText("Time: " + formatTime(time));
                            setMapLocation(mapsShowcase);
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return strAdd;
    }

    public void setMapLocation(MapView mapsShowcase) {
        mapsShowcase.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.setMyLocationEnabled(true);

                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 10);
                googleMap.animateCamera(cameraUpdate);

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapsShowcase.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapsShowcase.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapsShowcase.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapsShowcase.onPause();
    }
}
