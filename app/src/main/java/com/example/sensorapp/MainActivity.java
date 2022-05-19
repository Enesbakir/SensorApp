package com.example.sensorapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView stateText;
    private float[] myGravity;
    private double myAccel;
    private double myCurrentAcc;
    private double myLastAcc;
    int lightState;
    int movementState;
    private Sensor movementSensor;
    private int hitCount = 0;
    private double hitSum = 0;
    private double hitR = 0;
    private final int SAMPLE_SIZE = 30;
    private final double THRESHOLD = 0.1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        movementSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        stateText = (TextView) findViewById(R.id.state);
        myCurrentAcc = SensorManager.GRAVITY_EARTH;
        myLastAcc = SensorManager.GRAVITY_EARTH;
        myAccel = 0.00f;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT){
            if(sensorEvent.values[0]>0){
                lightState =1;
            }else{
                lightState = 0;
            }
        }if(sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            myGravity=sensorEvent.values.clone();
            myLastAcc = myCurrentAcc;
            myCurrentAcc= Math.sqrt(myGravity[0]*myGravity[0]+myGravity[1]*myGravity[1]+myGravity[2]*myGravity[2]);
            myAccel = myAccel * 0.9f + (myCurrentAcc - myLastAcc);
            if (hitCount <= SAMPLE_SIZE) {
                hitCount++;
                hitSum += Math.abs(myAccel);
            }else {
                hitR = hitSum / SAMPLE_SIZE;
                if (hitR > THRESHOLD) {
                    movementState =1;
                } else {
                    movementState =0;
                }
                hitCount = 0;
                hitSum = 0;
                hitR = 0;
            }
        }
        Intent intent = new Intent("com.example.application.Broadcast");
        if(lightState==1 && movementState ==0 ){
            intent.putExtra("state","turnOn");
            stateText.setText("Telefon Masada ve Işıklı");
        }else if(movementState == 1){
            stateText.setText("Telefon Cepte");
            intent.putExtra("state","turnOn");
        }else{
            stateText.setText("Telefon masada ve Işıksız");
            intent.putExtra("state","turnOff");
        }
        sendBroadcast(intent);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,movementSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}