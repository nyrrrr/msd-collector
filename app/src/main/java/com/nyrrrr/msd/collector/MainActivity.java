package com.nyrrrr.msd.collector;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static java.util.Arrays.asList;

/**
 * Main Class
 * When Capture mode is enabled, sensor data are being logged.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int INTEGER_MAX_DATA_LOGGED = 2;

    private static final int GLOBAL_SENSOR_SPEED = SensorManager.SENSOR_DELAY_FASTEST;

    private static final String NO_DATA_CAPTURED_MESSAGE = "No data was captured yet!";
    private static final String DATA_SUCCESSFULLY_STORED_MESSAGE = "The captured data has been stored.";
    private static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected error: ";
    private static final String STRING_DONE_CAPTURING_MESSAGE = "You are done capturing.\nPlease press SAVE now.";

    private static final String NEGATIVE_COLOR_CODE = "#FFFF4081";
    private static final String POSITIVE_COLOR_CODE = "#FF3F51B5";

    private final String CAPTURE_BUTTON_CAPTURE_TEXT = "Capture";
    private final String CAPTURE_BUTTON_STOP_TEXT = "Stop";

    private SensorManager oSensorManager;
    private Sensor oAcceleroMeter;
    private Sensor oGyroscope;
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;
    private SensorData oData;

    private Button uCaptureButton;
    private Button uSaveButton;
    private EditText uEditText;
    private Toast uToast;

    private boolean bIsInCaptureMode = false; // in capture mode, the app collects data and logs it
    private ArrayList<Integer> aKeyCountLog = new ArrayList<>(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    private int iTempVar = 0;
    private int iKeyCodeLogVar = -1;


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
        initUI();

        // init sensors & persistence
        initStorageManager();
        initAccelerometerSensor();
        initOrientationSensor();
    }

    /**
     * Detects when sensor values change and reacts
     * NOTE: currently it reacts to every single change
     *
     * @param pSensorEvent
     */
    @Override
    public void onSensorChanged(SensorEvent pSensorEvent) {

        if (bIsInCaptureMode) {

            if (oData == null) oData = new SensorData(pSensorEvent.timestamp);
            if (pSensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                oData.x = pSensorEvent.values[0];
                oData.y = pSensorEvent.values[1];
                oData.z = pSensorEvent.values[2];
            } else if (pSensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                oData.alpha = pSensorEvent.values[0];
                oData.beta = pSensorEvent.values[1];
                oData.gamma = pSensorEvent.values[2];
            }
            if (iKeyCodeLogVar != -1) {
                oData.keyPressed = KeyEvent.keyCodeToString(iKeyCodeLogVar);
                iKeyCodeLogVar = -1;
            }
            if (oData.x != 0 && oData.y != 0 && oData.z != 0 && oData.alpha != 0 && oData.beta != 0 && oData.gamma != 0) {


                // TODO key pos x, y
                oStorageManager.addSensorDataLogEntry(oData);
                Log.d("Data", oData.toCSVString());
                oData = null;
            }
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

    /**
     * Store relevant keys (0-9) in var for later usage
     * Only store a certain amount per key
     *
     * @param pKeyCode
     * @param pEvent
     * @return
     */
    @Override
    public boolean onKeyUp(int pKeyCode, KeyEvent pEvent) {
        if (pKeyCode >= 7 && pKeyCode <= 16) { // if key between 0 to 9
            if (bIsInCaptureMode) {
                iTempVar = aKeyCountLog.get(pKeyCode - 7);
                if (iTempVar < INTEGER_MAX_DATA_LOGGED) { // if key count is below the limit
                    iKeyCodeLogVar = pKeyCode;

                    aKeyCountLog.set(pKeyCode - 7, ++iTempVar);
                } else {
                    if (Collections.min(aKeyCountLog) == INTEGER_MAX_DATA_LOGGED) { // sufficient amount of data captured
                        stopCaptureMode();
                        uToast = Toast.makeText(getApplicationContext(), STRING_DONE_CAPTURING_MESSAGE, Toast.LENGTH_SHORT);
                        uToast.show();
                    }
                }
            }
        } else {
            // do nothing
            return true;
        }
        return false;
    }

    /*
     * custom methods
     * ---------------------------------------------------------------------------------------------
     */

    /**
     * Creates CAPTURE and SAVE button on UI and adds ClickListeners.
     * Capture button will start capturing sensor data.
     * Save button will stop capturing and store the results.
     */
    private void initUI() {

        initEditTextField();
        // CAPTURE button
        initCaptureButton();
        // SAVE button
        initSaveButton();
    }

    /**
     * Register onClick listener on save button.
     * Save button stops data capturing process and stores data
     */
    private void initSaveButton() {
        uSaveButton = (Button) findViewById(R.id.saveButton);
        uSaveButton.setBackgroundColor(Color.parseColor((NEGATIVE_COLOR_CODE))); // red
        uSaveButton.setOnClickListener(new View.OnClickListener() { // onClick
            public void onClick(View v) {
                if (oStorageManager.getSensorDataLogLength() > 0) { // if data has already been captured
                    triggerStorageOfLoggedData();
                } else { // if list is empty, show warning instead
                    uToast = Toast.makeText(getApplicationContext(), NO_DATA_CAPTURED_MESSAGE, Toast.LENGTH_SHORT);
                    uToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    uToast.show();
                }
                iTempVar = 0; // reset counter
                aKeyCountLog = new ArrayList<>(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // reset counter
            }
        });
    }

    /**
     * Triggers capture mode in onClick method
     */
    private void initCaptureButton() {
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
    }

    /**
     * monkey fix to make the input visible despite being a password field
     */
    private void initEditTextField() {
        uEditText = (EditText) findViewById(R.id.codeSequenceNumberInput);
        uEditText.setTransformationMethod(null);
    }

    /**
     * Reset Capture Mode and store logged sensor data
     */
    private void triggerStorageOfLoggedData() {
        stopCaptureMode();
        uSaveButton.setBackgroundColor(Color.parseColor(NEGATIVE_COLOR_CODE)); // red

        try {
            oStorageManager.storeData(getApplicationContext()); // store data
            uToast = Toast.makeText(getApplicationContext(), DATA_SUCCESSFULLY_STORED_MESSAGE, Toast.LENGTH_SHORT);
            uToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            uToast.show();
        } catch (IOException e) {
            uToast = Toast.makeText(getApplicationContext(), UNEXPECTED_ERROR_MESSAGE + e.getMessage(), Toast.LENGTH_SHORT);
            uToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            uToast.show();
            e.printStackTrace();
        }

    }

    // enables data capturing flag
    private void startCaptureMode() {
        uEditText.setText("");
        uCaptureButton.setText(CAPTURE_BUTTON_STOP_TEXT);
        uSaveButton.setBackgroundColor(Color.parseColor((POSITIVE_COLOR_CODE))); // blue
        bIsInCaptureMode = true;
    }

    // unset flag for data capture
    private void stopCaptureMode() {
        uCaptureButton.setText(CAPTURE_BUTTON_CAPTURE_TEXT);
        bIsInCaptureMode = false;
    }

    /**
     * Set-up of accelerometer sensor for data capturing.
     * Registers EventListener.
     */
    private void initAccelerometerSensor() {
        oSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);

        oAcceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_LINEAR_ACCELERATION);
        oSensorManager.registerListener(this, oAcceleroMeter, GLOBAL_SENSOR_SPEED);
    }

    /**
     * Set-up of Gyroscope sensor. Orientation data will be captured during runtime and
     * combined with the accelerometer data.
     */
    private void initOrientationSensor() {
        oGyroscope = oSensorReader.getSingleSensorOfType(Sensor.TYPE_GYROSCOPE);
        oSensorManager.registerListener(this, oGyroscope, GLOBAL_SENSOR_SPEED);
    }

    private void initStorageManager() {
        oStorageManager = StorageManager.getInstance();
    }
}