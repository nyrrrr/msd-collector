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

    private final boolean IS_IN_DEBUG_MODE = false;

    private SensorManager oSensorManager;
    private Sensor oAcceleroMeter;
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;

    private OrientationEventListener oOrientationEventListener;

    private int iKeyCodeLogVar = -1;
    private int iOrientationLogVar = -1;

    /*
     * standard methods
     * ---------------------------------------------------------------------------------------------
     */

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUpAccelerometerSensor();
        //testPersistence();
        setUpOrientationSensor();
    }

    @Override
    public boolean onKeyUp(int pKeyCode, KeyEvent pKeyEvent) {
        if (pKeyCode >= 7 && pKeyCode <= 16) {
            iKeyCodeLogVar = pKeyCode;
            return true;
        } else iKeyCodeLogVar = -1;
        return false;
    }

    /**
     * Detects when sensor values change and reacts
     * NOTE: currently it reacts to every single change
     * TODO: only collect data when user taps number on keyboard
     *
     * @param pEvent
     */
    @Override
    public void onSensorChanged(SensorEvent pEvent) {
        if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);

        oSensorReader.addSensorDataLog(pEvent, iOrientationLogVar, iKeyCodeLogVar).print();
        iKeyCodeLogVar = -1;
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
     * set up orientation sensor needed for ensuring that the orientation is kept
     */
    private void setUpOrientationSensor() {
        oOrientationEventListener = new OrientationEventListener(
                getApplicationContext(), SensorManager.SENSOR_DELAY_FASTEST) {
            @Override
            public void onOrientationChanged(int pOrientation) {
                // TODO refine values
                if ((pOrientation < 65 || pOrientation > 115) && pOrientation != -1) {
                    if (IS_IN_DEBUG_MODE) {
                        Log.e("MOVE PHONE", "Phone not in right orientation mode");
                    }
                    iOrientationLogVar = pOrientation;
                } else {
                    if (IS_IN_DEBUG_MODE) {
                        Log.d("orientation changed", pOrientation + "");
                    }
                    iOrientationLogVar = -1;
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
     *
     * @deprecated
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
