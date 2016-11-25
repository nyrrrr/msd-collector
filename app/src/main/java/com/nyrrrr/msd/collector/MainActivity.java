package com.nyrrrr.msd.collector;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;

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
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;

    private OrientationEventListener oOrientationEventListener;

    private Button uCaptureButton;
    private Button uSaveButton;
    private EditText uEditText;
    private Toast uToast;

    private boolean bIsInCaptureMode = false; // in capture mode, the app collects data and logs it
    private int iKeyCodeLogVar = KeyEvent.KEYCODE_UNKNOWN; // 0
    private int iOrientationLogVar = OrientationEventListener.ORIENTATION_UNKNOWN; // -1
    private ArrayList<Integer> aKeyCountLog = new ArrayList<>(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    private int iTempVar = 0;

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
     * @param pEvent
     */
    @Override
    public void onSensorChanged(SensorEvent pEvent) {

        if (bIsInCaptureMode) {
            if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);
            oStorageManager.addSensorDataLogEntry(
                    pEvent,
                    iOrientationLogVar,
                    KeyEvent.keyCodeToString(iKeyCodeLogVar)
            ).print(); // add .print() for debug
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
            oStorageManager.storeData(getApplicationContext(), true); // store data
            uToast = Toast.makeText(getApplicationContext(), DATA_SUCCESSFULLY_STORED_MESSAGE, Toast.LENGTH_SHORT);
            uToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            uToast.show();
        } catch (IOException e) {
            uToast = Toast.makeText(getApplicationContext(), UNEXPECTED_ERROR_MESSAGE + e.getMessage(), Toast.LENGTH_SHORT);
            uToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            uToast.show();
        } catch (JSONException e) {
            uToast = Toast.makeText(getApplicationContext(), UNEXPECTED_ERROR_MESSAGE + e.getMessage(), Toast.LENGTH_SHORT);
            uToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            uToast.show();
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

        oAcceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_ACCELEROMETER);
        oSensorManager.registerListener(this, oAcceleroMeter, GLOBAL_SENSOR_SPEED);
    }

    /**
     * Set-up of orientation sensor. Orientation data will be captured during runtime and
     * combined with the accelerometer data. (Control variable)
     */
    private void initOrientationSensor() {
        oOrientationEventListener = new OrientationEventListener(
                getApplicationContext(), SensorManager.SENSOR_DELAY_FASTEST) {
            @Override
            public void onOrientationChanged(int pOrientation) {
                iOrientationLogVar = pOrientation;
            }
        };
        if (oOrientationEventListener.canDetectOrientation()) {
            oOrientationEventListener.enable();
        }
    }

    private void initStorageManager() {
        oStorageManager = StorageManager.getInstance();
    }
}