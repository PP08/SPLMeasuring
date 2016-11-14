package com.phucphuong.splmeasuring;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Func1 extends AppCompatActivity {

    //for preferences
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;
    private int calibrateValue = 0;

    ProgressBar pbCounter;
    TextView tv_message, tv_pressure;

    protected Button btn_calUp, btn_calDown;

    public SoundMeter test;
    protected double val = 0;

    private boolean btn1_clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_func1);

        tv_message = (TextView) findViewById(R.id.tv_message);
        tv_pressure = (TextView) findViewById(R.id.tv_pressure);
        pbCounter = (ProgressBar) findViewById(R.id.pb_counter);

        btn_calUp = (Button) findViewById(R.id.btn_calUp);
        btn_calDown = (Button) findViewById(R.id.btn_calDown);

        pbCounter.setMax(140);

        final ToggleButton toggle = (ToggleButton) findViewById(R.id.btn_measure1);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    readPref();
                    Log.e("calvalue: ", Integer.toString(calibrateValue));
                    btn1_clicked = true;
                    toggle.setText("Stop");
                    test = new SoundMeter(handler, Func1.this, calibrateValue);
                    test.t1.start();
//                    test.t2.start();

                    btn_calUp.setVisibility(View.VISIBLE);
                    btn_calDown.setVisibility(View.VISIBLE);

                } else {
                    // The toggle is disabled
                    writePref();
                    Log.e("calvalue: ", Integer.toString(calibrateValue));

                    toggle.setText("Start");
                    test.terminate();
                    test.t1.interrupt();
//                    test.t2.interrupt();
                    btn_calUp.setVisibility(View.INVISIBLE);
                    btn_calDown.setVisibility(View.INVISIBLE);

                }
            }
        });


        btn_calUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test.calUp();
            }
        });
        btn_calDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test.calDown();
            }
        });
    }

    protected Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            super.handleMessage(msg);
            handler.obtainMessage();
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

//                Log.e("thread1", Boolean.toString(test.t1.isAlive()));
//                Log.e("thread2", Boolean.toString(test.t2.isAlive()));
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(btn1_clicked && test.t1.isAlive()){
            test.terminate();
            test.t1.interrupt();
//            test.t2.interrupt();
            Toast toast = Toast.makeText(this, "The measuring has stopped..", Toast.LENGTH_SHORT);
            toast.show();
        }
        writePref();
    }

    private void writePref(){
        sharedPref = Func1.this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putInt(getString(R.string.calibrate_value), test.caliberationValue);
        editor.commit();
    }

    private void readPref(){
        sharedPref = Func1.this.getPreferences(Context.MODE_PRIVATE);
        calibrateValue = sharedPref.getInt(getString(R.string.calibrate_value), 80);
    }
}
