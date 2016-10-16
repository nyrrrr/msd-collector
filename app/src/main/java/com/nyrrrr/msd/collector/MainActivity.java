package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    SensorManager oSensorManager;
    Sensor oAcceleroMeter;

    SensorReader oSensorReader;
    StorageManager oStorageManager;

    OrientationEventListener oOrientationEventListener;

    /*
     * standard methods
     * ---------------------------------------------------------------------------------------------
     */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setUpAccelerometerSensor();
        //testPersistence();
        //TODO continue here setUpOrientationSensor();
    }

    @Override
    public boolean onKeyUp(int pKeyCode, KeyEvent pKeyEvent) {

        if (pKeyCode >= 7 && pKeyCode <= 16) {
            // TODO do something

            return true;
        }

        return false;
    }


//    @Override
//    protected void onStart() {
//        super.onStart();
//        Log.d("START", System.currentTimeMillis() + "");
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.d("RESUME", System.currentTimeMillis() + "");
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Log.d("PAUSE", System.currentTimeMillis() + "");
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d("STOP", System.currentTimeMillis() + "");
//    }

    /**
     * Detects when sensor values change and reacts
     * NOTE: currently it reacts to every single change
     * TODO: only collect data when user taps number on keyboard
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (oSensorReader != null) {
            if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);
            oSensorReader.printSensorEventInformation(event);
        }
    }

    /**
     * TODO: determine sensors accuracy and frequency and make sure it stays constant
     *
     * @param sensor
     * @param accuracy
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
     * custom methods
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * set up orientation sensor needed for ensuring that the orientation is kept
     */
    private void setUpOrientationSensor() {
        oOrientationEventListener = new OrientationEventListener(
                getApplicationContext(), SensorManager.SENSOR_DELAY_FASTEST) {
            @Override
            public void onOrientationChanged(int pOrientation) {
                // TODO refine values
                if ((pOrientation < 65 || pOrientation > 115) && pOrientation != -1) {
                    Log.e("MOVE PHONE", "Phone not in right orientation mode");
                    // TODO prevent the user from putting in wrong values
                } else {
                    Log.d("orientation changed", pOrientation + "");
                    // actually do nothing
                }
            }
        };
        if (oOrientationEventListener.canDetectOrientation()) {
            oOrientationEventListener.enable();
        }
    }

    /**
     * sets up the accelerometer for listening to sensor data
     */
    private void setUpAccelerometerSensor() {
        oSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);

        oAcceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_ACCELEROMETER);
        oSensorManager.registerListener(this, oAcceleroMeter, SensorManager.SENSOR_DELAY_FASTEST);
    }

    /**
     * only in for testing (so-far)
     */
    private void testPersistence() {
        oStorageManager = StorageManager.getInstance();
        oStorageManager.storeData(getApplicationContext(), true);
        oStorageManager.restoreData(getApplicationContext(), true);
        try {
            oStorageManager.setDataObject(new JSONObject("{test: { a:1, b:2}}"));
            oStorageManager.storeData(getApplicationContext(), true);
        } catch (JSONException e) {
            Log.e(e.getCause().toString(), "Error while creating JSON: " + e.getMessage());
        }
    }

    /*
     * helper methods
     * ---------------------------------------------------------------------------------------------
     */
//    TODO does not work for some reason, fix later
//    private void initializeUI() {
//        oEditText = (EditText) findViewById (R.id.codeSequenceNumberInput);
//        Log.d("password", oEditText.getText().toString());
//        oEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
//    }
}
