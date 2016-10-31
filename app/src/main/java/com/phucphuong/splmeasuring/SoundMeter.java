package com.phucphuong.splmeasuring;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.IOException;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;

import java.util.Random;

import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by PhucPhuong on 22/10/2016.
 */

public class SoundMeter {

    public Context context;
    public LocationManager locationManager;
    public LocationListener locationListener;

    private volatile boolean running = true;

    public Handler handler;

    public boolean kill = false;

    public int value = 0;
    public int progress = 0;
    Random rd = new Random();


    //params for measure
    private static final int FREQUENCY = 8000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private int BUFFSIZE = 320;
    private static final double P0 = 0.000002;

    private static final int CALIB_DEFAULT = -80;
    private int caliberationValue = CALIB_DEFAULT;
    AudioRecord recordInstance = null;


    public SoundMeter(Handler h) {
        this.handler = h;
    }


    public void terminate() {
        running = false;
    }

    public class myRunnable implements Runnable {

        @Override
        public void run() {
//            Log.e("value", Integer.toString(value));
//            while (running) {
//
//                Message data = Message.obtain();
//                Bundle b = new Bundle();
//                b.putInt("x", progress++);
//                b.putString("state", "run");
//                b.putBoolean("kill", running);
//                data.setData(b);
//                SystemClock.sleep(300);
//                handler.sendMessage(data);
//
//            }
//            Message data = Message.obtain();
//            Bundle b = new Bundle();
//            b.putString("state", "stop");
//            b.putBoolean("kill", running);
//            data.setData(b);
//            handler.sendMessage(data);
//            kill = true;

            try {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                recordInstance = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQUENCY, CHANNEL, ENCODING, 8000);
                recordInstance.startRecording();

                short[] temBuffer = new short[BUFFSIZE];

                while (running) {

                    double splValue = 0.0;
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

                    Message data = Message.obtain();
                    Bundle b = new Bundle();
                    b.putDouble("x", splValue);
                    b.putString("state", "run");
                    b.putBoolean("kill", running);
                    data.setData(b);
                    handler.sendMessage(data);

                }

                recordInstance.stop();
                kill = true;
                Message data = Message.obtain();
                Bundle b = new Bundle();
                b.putString("state", "stop");
                b.putBoolean("kill", running);
                data.setData(b);
                handler.sendMessage(data);
            } catch (Exception e) {
                Log.e("MY TAG: ", "FAILUREEEEEEEEEEEE");
            }
        }
    }

    Thread t1 = new Thread(new myRunnable());

    public class myRunnable2 implements Runnable {

        @Override
        public void run() {
            while (running) {
                value = rd.nextInt(10) + 1;
                SystemClock.sleep(300);
                Log.e("test", Integer.toString(value));
                if (value >= 10) {
                    terminate();
                }
            }
        }
    }

    Thread t2 = new Thread(new myRunnable2());
}