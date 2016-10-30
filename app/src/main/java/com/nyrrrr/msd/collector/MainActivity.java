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
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // TODO   private static final int GLOBAL_SENSOR_SPEED = SensorManager.SENSOR_DELAY_FASTEST;
    private static final int GLOBAL_SENSOR_SPEED = SensorManager.SENSOR_DELAY_UI;

    private Button uCaptureButton;

    private SensorManager oSensorManager;
    private Sensor oAcceleroMeter;
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;

    private OrientationEventListener oOrientationEventListener;

    private int iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN; // 0
    // only one key can be pressed at a time
    private int iOrientationLogVar = OrientationEventListener.ORIENTATION_UNKNOWN; // -1


    /*
     * standard methods
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * First method to run when app is started.
     *
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init ui
        setUpUserInterface();

        // init sensors & persistence
//        setUpStorageManager();
//        setUpAccelerometerSensor();
//        setUpOrientationSensor();
    }

    /**
     *
     */
    private void setUpUserInterface() {

        uCaptureButton = (Button) findViewById(R.id.captureButton);
        uCaptureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.d("TEXT", uCaptureButton.getText().toString());
                if(uCaptureButton.getText().toString() == "Capture") {
                    uCaptureButton.setText("Stop Recording");
                } else {
                    uCaptureButton.setText("Capture");
                }
            }
        });
    }


    /**
     * @param pEvent
     * @return
     * @// TODO: 17.10.2016 add doc
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent pEvent) {
        int pKeyCode = pEvent.getKeyCode();
        if (pEvent.getAction() == KeyEvent.ACTION_DOWN) { // onKeyDown
            if (BuildConfig.DEBUG) Log.d("dKEYDOWN", pKeyCode + "");
            if (pKeyCode >= 7 && pKeyCode <= 16) {
                iKeyCodeLogVar = pKeyCode;
            } else {
                if (BuildConfig.DEBUG)
                    Log.e("KEY NOT RELEVANT", pKeyCode + " reset to KEYCODE_UNKNOWN");
                iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN;
            }
            if (pKeyCode == KeyEvent.KEYCODE_ENTER) { // onKeyUp
                // TODO convert and store data

                return true;
            }
        }
//        else if (pEvent.getAction() == KeyEvent.ACTION_UP) {
//            iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN; // reset key;
//            if (BuildConfig.DEBUG) Log.d("dKEYUP", pKeyCode + "");
//        }
        return false;
    }

    /**
     * Detects when sensor values change and reacts
     * NOTE: currently it reacts to every single change
     *
     * @param pEvent
     */
    @Override
    public void onSensorChanged(SensorEvent pEvent) {

        // TODO only capture while keyboard is open?!
        if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);

        oStorageManager.addSensorDataLogEntry(
                pEvent,
                iOrientationLogVar,
                KeyEvent.keyCodeToString(iKeyCodeLogVar)
        ).print(); // TODO debug only ; remove
        iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN;
    }

    /**
     * TODO: determine sensors accuracy and frequency and make sure it stays constant
     *
     * @param pSensor
     * @param pAccuracy
     */
    @Override
    public void onAccuracyChanged(Sensor pSensor, int pAccuracy) {

    }

    /*
     * custom methods
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * Set-up of accelerometer sensor for data capturing.
     * Registers EventListener.
     */
    private void setUpAccelerometerSensor() {
        oSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);

        oAcceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_ACCELEROMETER);
        oSensorManager.registerListener(this, oAcceleroMeter, GLOBAL_SENSOR_SPEED);
    }

    /**
     * Set-up of orientation sensor. Orientation data will be captured during runtime and
     * combined with the accelerometer data. (Control variable)
     */
    private void setUpOrientationSensor() {
        oOrientationEventListener = new OrientationEventListener(
                getApplicationContext(), SensorManager.SENSOR_DELAY_FASTEST) {
            @Override
            public void onOrientationChanged(int pOrientation) {
                // TODO refine values
                if ((pOrientation < 65 || pOrientation > 115) && pOrientation != -1) {
//                    if (BuildConfig.DEBUG) {
//                        Log.e("MOVE PHONE", "Phone not in right orientation mode");
//                    }
                    iOrientationLogVar = pOrientation;
                } else {
                    iOrientationLogVar = OrientationEventListener.ORIENTATION_UNKNOWN;
                }
            }
        };
        if (oOrientationEventListener.canDetectOrientation()) {
            oOrientationEventListener.enable();
        }
    }

    private void setUpStorageManager() {
        oStorageManager = StorageManager.getInstance();
    }

    /*
     * helper methods
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * only in for testing (so-far)
     *
     * @deprecated
     */
//    private void testPersistence() {
//        oStorageManager = StorageManager.getInstance();
//        oStorageManager.storeData(getApplicationContext(), true);
//        oStorageManager.restoreData(getApplicationContext(), true);
//        try {
//            oStorageManager.setDataObject(new JSONObject("{test: { a:1, b:2}}"));
//            oStorageManager.storeData(getApplicationContext(), true);
//        } catch (JSONException e) {
//            Log.e(e.getCause().toString(), "Error while creating JSON: " + e.getMessage());
//        }
//    }

//    TODO does not work for some reason, fix later
//    private void initializeUI() {
//        oEditText = (EditText) findViewById (R.id.codeSequenceNumberInput);
//        Log.d("password", oEditText.getText().toString());
//        oEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
//    }
}
