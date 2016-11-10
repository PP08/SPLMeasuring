package com.phucphuong.splmeasuring;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.FileWriter;
import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by PhucPhuong on 22/10/2016.
 */

public class SoundMeter {

    //gps
    public Context context;
    public LocationManager locationManager;
    public LocationListener locationListener;
    private GPSTool gpsTool;
    private Location location;

    //thread
    private volatile boolean isRunning = true;
    public Handler handler;
    private Message data;
    private Bundle b;
    public boolean kill = false;

    //measuring
    public int value = 0;
    public int progress = 0;
    private double splValue = 0.0;

    //test
    Random rd = new Random();


    //params for audio
    private static final int FREQUENCY = 8000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private int BUFFSIZE = 320;
    private static final double P0 = 0.000002;
    private static final int CALIB_DEFAULT = -80;
    private static final int CALIB_INCREMENT = 3;
    private int caliberationValue = CALIB_DEFAULT;
    AudioRecord recordInstance = null;


    //log
    private FileWriter splLog = null;
    private static String LOGPATH = "/sdcard/splmeter_";

    public SoundMeter(Handler h, Context context) {
        this.handler = h;
        gpsTool = new GPSTool(context);
    }



    public void terminate() {
        isRunning = false;
    }

    public void calUp(){
        caliberationValue = caliberationValue + CALIB_INCREMENT;
        if (caliberationValue == 0)
        {
            caliberationValue = caliberationValue + 1;
        }
    }

    public void calDown(){
        caliberationValue = caliberationValue - CALIB_INCREMENT;
        if (caliberationValue == 0)
        {
            caliberationValue = caliberationValue - 1;
        }
    }

    public class myRunnable implements Runnable {

        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL, ENCODING, 8000);
                recordInstance.startRecording();

                short[] temBuffer = new short[BUFFSIZE];

                while (isRunning) {

//                    splValue = 0.0;
                    double rsmValue = 0.0;

                    for (int i = 0; i < BUFFSIZE - 1; i++) {
                        temBuffer[i] = 0;
                    }

                    recordInstance.read(temBuffer, 0, BUFFSIZE);
                    for (int i = 0; i < BUFFSIZE - 1; i++) {
                        rsmValue += temBuffer[i] * temBuffer[i];
                    }
                    rsmValue = rsmValue / BUFFSIZE;
                    rsmValue = Math.sqrt(rsmValue);
                    splValue = 20 * Math.log10(rsmValue / P0);
                    splValue += caliberationValue;
                    splValue = Math.round(splValue);

                    //get the location
                    location = gpsTool.getLocation();
                    Log.e("location: ", "latitude - " + location.getLatitude() + "longitude - " + location.getLongitude());

                    //start logging
                    Calendar cal = Calendar.getInstance();


                    Log.e("Date: ", cal.getTime().toString());

                    sendMessage(isRunning);

                }

                recordInstance.stop();
                recordInstance.release();
                kill = true;
                splValue = 0;
                sendMessage(isRunning);

            } catch (Exception e) {
                Log.e("MY TAG: ", "FAILUREEEEEEEEEEEE");
            }
        }
    }

    Thread t1 = new Thread(new myRunnable());

    public class myRunnable2 implements Runnable {

        @Override
        public void run() {
            while (isRunning) {

            }
        }
    }

    Thread t2 = new Thread(new myRunnable2());


    //functions

    private void sendMessage(boolean isRunning){
        data = Message.obtain();
        b = new Bundle();
        b.putDouble("x", splValue);

        if(isRunning){
            b.putString("state", "Running");
        }else {
            b.putString("state", "Stopped");
        }
        b.putBoolean("kill", isRunning);
        data.setData(b);
        handler.sendMessage(data);
    }

}