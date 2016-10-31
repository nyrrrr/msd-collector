package com.nyrrrr.msd.collector;

import android.graphics.Color;
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

/**
 * Main Class
 * When Capture mode is enabled, sensor data are being logged.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int GLOBAL_SENSOR_SPEED = SensorManager.SENSOR_DELAY_FASTEST;
    private static final boolean RUNS_IN_DEBUG_MODE = true;
    private static final boolean RUNS_IN_DEPLOYMENT_MODE = false;
    private final String CAPTURE_BUTTON_CAPTURE_TEXT = "Capture";
    private final String CAPTURE_BUTTON_STOP_TEXT = "Stop";

    private SensorManager oSensorManager;
    private Sensor oAcceleroMeter;
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;

    private OrientationEventListener oOrientationEventListener;

    private Button uCaptureButton;
    private Button uSaveButton;

    private boolean bIsInCaptureMode = false; // in capture mode, the app collects data and logs it
    private int iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN; // 0
    private int iOrientationLogVar = OrientationEventListener.ORIENTATION_UNKNOWN; // -1
    private int iCounter = 0;

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
        setUpStorageManager();
        setUpAccelerometerSensor();
        setUpOrientationSensor();
    }


    /**
     * Store relevant keys (0-9) in var for later usage
     *
     * @param pKeyCode
     * @param pEvent
     * @return
     */
    @Override
    public boolean onKeyUp(int pKeyCode, KeyEvent pEvent) {
        if (pKeyCode >= 7 && pKeyCode <= 16) {
            iKeyCodeLogVar = pKeyCode;
        } else {
            // irrelevant key
            iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN;
        }
        if (pKeyCode == KeyEvent.KEYCODE_ENTER) {
            // do nothing
            return true;
        }
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

        if (bIsInCaptureMode) {
            // TODO only capture while keyboard is open?!
            if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);

            oStorageManager.addSensorDataLogEntry(
                    pEvent,
                    iOrientationLogVar,
                    KeyEvent.keyCodeToString(iKeyCodeLogVar)
            ); // add .print() for debug
            Log.d("Entry added", (++iCounter)+"");
            iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN;
        }
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
     * set up button element for capture mode
     */
    private void setUpUserInterface() {
        uCaptureButton = (Button) findViewById(R.id.captureButton);
        uCaptureButton.setText(CAPTURE_BUTTON_CAPTURE_TEXT);
        uCaptureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (uCaptureButton.getText().toString() == CAPTURE_BUTTON_CAPTURE_TEXT) {
                    startCaptureMode();
                } else if (uCaptureButton.getText().toString() == CAPTURE_BUTTON_STOP_TEXT) {
                    stopCaptureMode();
                }
            }
        });

        uSaveButton = (Button) findViewById(R.id.saveButton);
        uSaveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // TODO change color and block functionality until data is stored.
                stopCaptureMode();
                uCaptureButton.setEnabled(false);
                uSaveButton.setBackgroundColor(Color.parseColor(("#FFFF4081"))); // TODO: original = #FF3F51B5

                oStorageManager.storeData(getApplicationContext(), RUNS_IN_DEBUG_MODE); //RUNS_IN_DEPLOYMENT_MODE
            }
        });
    }

    private void startCaptureMode() {
        uCaptureButton.setText(CAPTURE_BUTTON_STOP_TEXT);
        bIsInCaptureMode = true;
    }

    private void stopCaptureMode() {
        uCaptureButton.setText(CAPTURE_BUTTON_CAPTURE_TEXT);
        bIsInCaptureMode = false;
    }

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
}