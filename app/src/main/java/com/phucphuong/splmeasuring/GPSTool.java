package com.phucphuong.splmeasuring;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

/**
 * Created by PhucPhuong on 10/11/2016.
 */

public class GPSTool {

    private boolean isRunning = true;
    public Handler handler;

    String locationProvider = LocationManager.NETWORK_PROVIDER;
//    String locationProvider = LocationManager.GPS_PROVIDER;
    private LocationManager locationManager;
    private LocationListener locationListener;


    public GPSTool(final Context context){
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Location change
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                turnOnGps(context);
            }
        };
//        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
        startGpsUpdate();
    }

    public Location getLocation(){
        //noinspection MissingPermission
        return locationManager.getLastKnownLocation(locationProvider);
    }

    public void startGpsUpdate(){
        //noinspection MissingPermission
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
    }

    public void stopGpsUpdate(){
        //noinspection MissingPermission
        locationManager.removeUpdates(locationListener);
    }

    public void turnOnGps(Context context){
        boolean isEnabled = locationManager.isProviderEnabled(locationProvider);
        if (!isEnabled){
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            context.startActivity(intent);
        }
    }
}
