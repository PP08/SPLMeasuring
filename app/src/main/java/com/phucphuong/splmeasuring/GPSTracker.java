package com.phucphuong.splmeasuring;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by phucphuong on 3/10/17.
 */

@SuppressWarnings("MissingPermission")
public class GPSTracker extends Service implements LocationListener {

    private Context context;
    boolean checkGPS = false;
    boolean checkNetWork = false;
    boolean canGetLocation = false;

    Location location;
    double latitude, longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    protected LocationManager locationManager;

    public GPSTracker(Context context){
        this.context = context;
        getLocation();
    }

    private Location getLocation(){

        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

            //getting GPS status
            checkGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            //gettting network status
            checkNetWork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!checkGPS && !checkNetWork){
                Toast.makeText(context, "No Service Provider Available", Toast.LENGTH_LONG).show();
            }else {

                this.canGetLocation = true;

                //first get location from network provider
                if (checkNetWork){
                    Toast.makeText(context, "Network", Toast.LENGTH_SHORT).show();

                    try {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 1, this);

                        Log.d("network", "network");
                        if (locationManager != null){
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }

                        if (location != null){
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }

                    }catch (SecurityException e){


                    }

                }

                if (checkGPS){
                    Toast.makeText(context, "GPS", Toast.LENGTH_SHORT).show();

                    if (location != null){

                        try{
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, this);
                            Log.d("GPS Enabled", "GPS Enabled");
                            if (locationManager != null){
                                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                                if (location != null){
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                }
                            }
                        } catch (SecurityException e){

                        }

                    }

                }

            }


        } catch (Exception e){
            e.printStackTrace();
        }

        return location;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        return longitude;
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        return latitude;
    }

    public boolean canGetLocation(){
        return this.canGetLocation;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);


        alertDialog.setTitle("GPS Not Enabled");

        alertDialog.setMessage("Do you wants to turn On GPS");


        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        alertDialog.show();
    }

    public void stopUsingGPS() {
        if (locationManager != null) {

            locationManager.removeUpdates(GPSTracker.this);
        }
    }


//    public void turnOnGps(Context context){
//        boolean isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        if (!isEnabled){
//            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//            context.startActivity(intent);
//        }
//    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.e("location: ", Double.toString(latitude) + ", " + Double.toString(longitude));

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        showSettingsAlert();
    }


}
