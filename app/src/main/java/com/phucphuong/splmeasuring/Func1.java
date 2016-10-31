package com.phucphuong.splmeasuring;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.CompoundButton;

public class Func1 extends AppCompatActivity {

    //GPS
    LocationManager locationManager;
    LocationListener locationListener;

    ProgressBar pbCounter;
    TextView tv_message, tv_pressure;
    public SoundMeter test;

    protected double val = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_func1);


        tv_message = (TextView) findViewById(R.id.tv_message);
        tv_pressure = (TextView) findViewById(R.id.tv_pressure);
        pbCounter = (ProgressBar) findViewById(R.id.pb_counter);
        pbCounter.setMax(140);


        final ToggleButton toggle = (ToggleButton) findViewById(R.id.btn_measure1);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    toggle.setText("Stop");
                    test = new SoundMeter(handler);
//                    test.t3.start();
//                    test.t2.start();
                    test.t1.start();

                } else {
                    // The toggle is disabled
                    toggle.setText("Start");

                    test.terminate();
                    test.t1.interrupt();
                }
            }
        });
    }

    protected Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            handler.obtainMessage();
//            pbCounter.setProgress(msg.getData().getInt("x"));

//            val = Double.parseDouble(msg.obj.toString());

            val = msg.getData().getDouble("x");
            String[] arr=String.valueOf(val).split("\\.");
            int[] intArr=new int[2];
            intArr[0]=Integer.parseInt(arr[0]); // 1
            pbCounter.setProgress(intArr[0]);

            tv_message.setText(msg.getData().getString("state"));
            tv_pressure.setText(Double.toString(msg.getData().getDouble("x")) + " dB");

            if(msg.getData().getBoolean("kill")){
                test.t1.interrupt();
//                test.t2.interrupt();

                Log.e("thread1", Boolean.toString(test.t1.isAlive()));
                Log.e("thread2", Boolean.toString(test.t2.isAlive()));
            }
        }
    };

}
