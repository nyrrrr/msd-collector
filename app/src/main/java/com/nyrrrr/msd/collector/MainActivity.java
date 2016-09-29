package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager oSensorManager;
    Sensor oAcceleroMeter;

    SensorReader oSensorReader;
    StorageManager oStorageManager;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(false) handleSensors();
        if(true) testPersistence();
    }

    int slow = 0;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(oSensorReader != null) {
            if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);
            oSensorReader.printSensorEventInformation(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * method only extracted for testing
     */
    private void handleSensors() {
        oSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);


        oAcceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_ACCELEROMETER);
        // TODO check if right position in code to do this
        oSensorManager.registerListener(this, oAcceleroMeter, SensorManager.SENSOR_DELAY_UI);
    }

    private void testPersistence() {
        oStorageManager = StorageManager.getInstance();
        oStorageManager.saveFile(getApplicationContext(), true);
        oStorageManager.getDataFromFile(getApplicationContext(), true);
        try {
            oStorageManager.setDataObject(new JSONObject("{test: { a:1, b:2}}"));
            oStorageManager.saveFile(getApplicationContext(), true);
        } catch (JSONException e) {
            Log.e(e.getCause().toString(), "Error while creating JSON: " + e.getMessage());
        }
    }
}
