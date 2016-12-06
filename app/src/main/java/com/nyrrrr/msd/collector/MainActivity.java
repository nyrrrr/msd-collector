package com.nyrrrr.msd.collector;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.util.Arrays.asList;

/**
 * Main Class
 * When Capture mode is enabled, sensor data are being logged.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final String STRING_KEYCODES_ONLY = "Keycodes-only";
    public static final String STRING_LOGGING = "Logging...";
    private static final int INTEGER_MAX_DATA_LOGGED = 30;
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
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;
    private SensorData oData;

    private Button uCaptureButton;
    private Button uSaveButton;
    private Toast uToast;

    private boolean bIsInCaptureMode = false; // in capture mode, the app collects data and logs it
    private ArrayList<Integer> aKeyCountLog = new ArrayList<>(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    private int iTempCounter = 0;
    private Button[] buttonList = new Button[10];
    private int iNextButton;
    private int iTempVar;
    private int[] aLastKeysSelected = {-1, -1, -1};
    private Button uKeyCodesOnlyButton;
    private boolean bIsInKeyloggerMode;
    private Sensor oGyroscope;
    private Sensor oAcceleroMeter;

    /*
     * standard methods
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init ui
        initUI();

        // init sensors & persistence
        initStorageManager();
        initAccelerometerSensor();
        initGyroscopeSensor();
    }

    /**
     * Detects when sensor values change and reacts
     * NOTE: currently it reacts to every single change
     *
     * @param pSensorEvent Accelerometer or Gyroscope event
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
        }
    }

    /*
     * --------------------------------- Modes -----------------------------------------------------
     */

    /**
     * enables data capturing
     */
    private void startCaptureMode() {
        uKeyCodesOnlyButton.setEnabled(false);

        registerSensorListeners();

        uCaptureButton.setText(CAPTURE_BUTTON_STOP_TEXT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uSaveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(POSITIVE_COLOR_CODE)));
        } else {
            uSaveButton.setBackgroundColor(Color.parseColor((POSITIVE_COLOR_CODE))); // red
        } // blue
        bIsInCaptureMode = true;
        bIsInKeyloggerMode = false;
        determineNextButtonToClick(-1);

    }

    /**
     * unset flag for data capture
     */
    private void stopCaptureMode() {
        uKeyCodesOnlyButton.setEnabled(true);
        uCaptureButton.setText(CAPTURE_BUTTON_CAPTURE_TEXT);
        bIsInCaptureMode = false;
        unregisterSensorListeners();
        reInitKeyboard();
    }

    /**
     * Similar to Capture mode, but no sensor logging; keys-only
     */
    private void startKeyloggerMode() {

        bIsInKeyloggerMode = true;
        bIsInCaptureMode = false;

        uKeyCodesOnlyButton.setText(STRING_LOGGING);

        uCaptureButton.setEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uSaveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(POSITIVE_COLOR_CODE)));
        } else {
            uSaveButton.setBackgroundColor(Color.parseColor((POSITIVE_COLOR_CODE))); // red
        } // blue

        determineNextButtonToClick(-1);
    }

    /**
     * Stop keylogging mode
     */
    private void stopKeyloggerMode() {
        uKeyCodesOnlyButton.setText(STRING_KEYCODES_ONLY);
        uCaptureButton.setEnabled(true);
        bIsInKeyloggerMode = false;
        reInitKeyboard();
    }

    /**
     * Register sensor listeners
     */
    private void registerSensorListeners() {
        oSensorManager.registerListener(this, oAcceleroMeter, GLOBAL_SENSOR_SPEED);
        oSensorManager.registerListener(this, oGyroscope, GLOBAL_SENSOR_SPEED);
    }

    /**
     * unregister sensor listeners
     */
    private void unregisterSensorListeners() {
        oSensorManager.unregisterListener(this, oAcceleroMeter);
        oSensorManager.unregisterListener(this, oGyroscope);
    }

    /*
     * --------------------------------- UI --------------------------------------------------------
     */

    /**
     * Creates CAPTURE and SAVE button on UI and adds ClickListeners.
     * Capture button will start capturing sensor data.
     * Save button will stop capturing and store the results.
     */
    private void initUI() {
        // KEYBOARD
        initKeyboard();
        // CAPTURE button
        initCaptureButton();
        // SAVE button
        initSaveButton();

        initKeyCodeOnlyModeButton();
    }

    /**
     * Init my custom keyboard.
     * Register OnTouchListeners on Buttons.
     */
    private void initKeyboard() {
        Button currentButton;
        ViewGroup keyboard = (ViewGroup) findViewById(R.id.keyboard);
        for (int i = 0; i < (keyboard != null ? keyboard.getChildCount() : 0); i++) { // init all buttons
            currentButton = (Button) keyboard.getChildAt(i);
            currentButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View pView, MotionEvent pMotionEvent) {
                    // is capture mode on and was the button just released?
                    if ((bIsInCaptureMode || bIsInKeyloggerMode) && pMotionEvent.getAction() == MotionEvent.ACTION_UP) { // TODO use ACTION_DOWN instead?

                        iTempVar = Integer.parseInt(((Button) pView).getText().toString());

                        // did the user press the correct key
                        if (iTempVar == iNextButton) {
                            // store key data
                            if (oData == null) {
                                if (bIsInKeyloggerMode) {
                                    oData = new SensorData(pMotionEvent.getDownTime());
                                } else return false;
                            }
                            oData.keyPressed = "KEYCODE_" + iTempVar + "";
                            oData.key_x = pMotionEvent.getRawX();
                            oData.key_y = pMotionEvent.getRawY();
//                            oData.key_down = pMotionEvent.getDownTime();
//                            oData.key_released = pMotionEvent.getEventTime();

                            // reset vars
                            if (bIsInKeyloggerMode || (oData.x != 0 && oData.y != 0 && oData.z != 0 && oData.alpha != 0 && oData.beta != 0 && oData.gamma != 0)) {
                                oStorageManager.addSensorDataLogEntry(oData);
                                Log.d("Data", oData.toCSVString());
                                oData = null;
                            }
                            aKeyCountLog.set(iTempVar, ++iTempCounter);
                            determineNextButtonToClick(iNextButton);
                        }
                    }
                    return false;
                }
            });
            buttonList[Integer.parseInt(currentButton.getText().toString())] = currentButton;
        }
    }

    /**
     * Triggers capture mode in onClick method
     */
    private void initCaptureButton() {
        uCaptureButton = (Button) findViewById(R.id.captureButton);
        if (uCaptureButton != null) {
            uCaptureButton.setText(CAPTURE_BUTTON_CAPTURE_TEXT);
        }
        uCaptureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (uCaptureButton.getText().toString().equals(CAPTURE_BUTTON_CAPTURE_TEXT)) {
                    startCaptureMode();
                } else if (uCaptureButton.getText().toString().equals(CAPTURE_BUTTON_STOP_TEXT)) {
                    stopCaptureMode();
                }
            }
        });
    }

    /**
     * Register onClick listener on save button.
     * Save button stops data capturing process and stores data
     */
    private void initSaveButton() {
        uSaveButton = (Button) findViewById(R.id.saveButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uSaveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(NEGATIVE_COLOR_CODE)));
        } else {
            uSaveButton.setBackgroundColor(Color.parseColor((NEGATIVE_COLOR_CODE))); // red
        }
        uSaveButton.setOnClickListener(new View.OnClickListener() { // onClick
            public void onClick(View v) {
                if (oStorageManager.getSensorDataLogLength() > 0) { // if data has already been captured
                    triggerStorageOfLoggedData();
                } else { // if list is empty, show warning instead
                    uToast = Toast.makeText(getApplicationContext(), NO_DATA_CAPTURED_MESSAGE, Toast.LENGTH_SHORT);
                    uToast.show();
                }
                aKeyCountLog = new ArrayList<>(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // reset counter
            }
        });
    }

    private void initKeyCodeOnlyModeButton() {
        uKeyCodesOnlyButton = (Button) findViewById(R.id.keycodesOnlyButton);
        if (uKeyCodesOnlyButton != null) {
            uKeyCodesOnlyButton.setText(STRING_KEYCODES_ONLY); // TODO
        }
        uKeyCodesOnlyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (uKeyCodesOnlyButton.getText().toString().equals(STRING_KEYCODES_ONLY)) { // TODO
                    startKeyloggerMode();
                } else if (uKeyCodesOnlyButton.getText().toString().equals(STRING_LOGGING)) { // TODO
                    stopKeyloggerMode();
                }
            }
        });
    }

    /*
     * --------------------------------- Helper functions ------------------------------------------
     */

    private void reInitKeyboard() {
        for (Button button : buttonList) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                button.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            } else {
                button.setBackgroundColor(Color.LTGRAY);
            }
        }
    }

    /**
     * Reset Capture Mode and store logged sensor data
     */
    private void triggerStorageOfLoggedData() {
        if (bIsInCaptureMode) stopCaptureMode();
        else if (bIsInKeyloggerMode) stopKeyloggerMode();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            uSaveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(NEGATIVE_COLOR_CODE)));
        } else {
            uSaveButton.setBackgroundColor(Color.parseColor((NEGATIVE_COLOR_CODE))); // red
        }

        final AsyncTask asyncTask = new StoreDataTask() {
            @Override
            protected void onPostExecute(Object pO) {
                if (pO == null) {
                    uToast = Toast.makeText(getApplicationContext(), DATA_SUCCESSFULLY_STORED_MESSAGE, Toast.LENGTH_SHORT);
                    uToast.show();
                } else {
                    uToast = Toast.makeText(getApplicationContext(), UNEXPECTED_ERROR_MESSAGE + ((IOException) pO).getMessage(), Toast.LENGTH_SHORT);
                    uToast.show();
                }
                iTempCounter = 0; // reset counter
            }
        };
        asyncTask.execute(getApplicationContext());

    }

    /**
     * Determine which button the user is supposed to press next.
     *
     * @param pCurrentButton last pressed button
     * @// TODO: 06.12.2016 prevent "infinited" loop
     */
    private void determineNextButtonToClick(int pCurrentButton) {
        // change old button back to normal
        if (pCurrentButton != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buttonList[pCurrentButton].setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            } else {
                buttonList[pCurrentButton].setBackgroundColor(Color.LTGRAY);
            }
        }
        // enough samples for all keys?
        if (Collections.min(aKeyCountLog) == INTEGER_MAX_DATA_LOGGED) {
            if (bIsInCaptureMode) stopCaptureMode();
            else if (bIsInKeyloggerMode) stopKeyloggerMode();
            uToast = Toast.makeText(getApplicationContext(), STRING_DONE_CAPTURING_MESSAGE, Toast.LENGTH_SHORT);
            uToast.show();

        } else {
            // pick new button
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                iNextButton = ThreadLocalRandom.current().nextInt(0, 10);
            } else {
                iNextButton = new Random().nextInt((11));
            }
            iTempCounter = aKeyCountLog.get(iNextButton);
            // are there enough samples of that key?
            if (iTempCounter < INTEGER_MAX_DATA_LOGGED) {

                // prevent sequences
                if (iNextButton == aLastKeysSelected[0] && iNextButton == aLastKeysSelected[1] && iNextButton == aLastKeysSelected[2]) {
                    determineNextButtonToClick(iNextButton);
                } else {
                    // change button ui
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        buttonList[iNextButton].setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
                    } else {
                        buttonList[iNextButton].setBackgroundColor(Color.GREEN);
                    }
                    aLastKeysSelected[0] = aLastKeysSelected[1];
                    aLastKeysSelected[1] = aLastKeysSelected[2];
                    aLastKeysSelected[2] = iNextButton;
                }
            } else {
                determineNextButtonToClick(iNextButton);
            }
        }
    }

    /*
     * --------------------------------- Sensors ---------------------------------------------------
     */

    /**
     * Set-up of accelerometer sensor for data capturing.
     * Registers EventListener.
     */
    private void initAccelerometerSensor() {
        oSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (oSensorReader == null) oSensorReader = new SensorReader(oSensorManager);

        oAcceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    /**
     * Set-up of Gyroscope sensor. Orientation data will be captured during runtime and
     * combined with the accelerometer data.
     */
    private void initGyroscopeSensor() {
        oGyroscope = oSensorReader.getSingleSensorOfType(Sensor.TYPE_GYROSCOPE);
    }

    /**
     * Init Storage Manager
     */
    private void initStorageManager() {
        oStorageManager = StorageManager.getInstance();
    }

    @Override
    public void onAccuracyChanged(Sensor pSensor, int pAccuracy) {
    }
}