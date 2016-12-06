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
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;
    private SensorData oData;

    private Button uCaptureButton;
    private Button uSaveButton;
    private Toast uToast;

    private boolean bIsInCaptureMode = false; // in capture mode, the app collects data and logs it
    private ArrayList<Integer> aKeyCountLog = new ArrayList<>(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
    private int iTempCounter = 0;
    private String sKeyCodeLogVar = "";
    private Button[] buttonList = new Button[10];
    private int iNextButton;
    private int iTempVar;

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
        // TODO
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
     * custom methods
     * ---------------------------------------------------------------------------------------------
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
                    if (bIsInCaptureMode && pMotionEvent.getAction() == MotionEvent.ACTION_UP) { // TODO use ACTION_DOWN instead?

                        iTempVar = Integer.parseInt(((Button) pView).getText().toString());

                        // did the user press the correct key
                        if (iTempVar == iNextButton) {
                            // store key data
                            oData.keyPressed = "KEYCODE_" + iTempVar + "";;
                            oData.key_x = pMotionEvent.getRawX();
                            oData.key_y = pMotionEvent.getRawY();
                            oData.key_down = pMotionEvent.getDownTime();
                            oData.key_released = pMotionEvent.getEventTime();

                            // reset vars
                            if (oData.x != 0 && oData.y != 0 && oData.z != 0 && oData.alpha != 0 && oData.beta != 0 && oData.gamma != 0) {
                                oStorageManager.addSensorDataLogEntry(oData);
                                Log.d("Data", oData.toCSVString());
                                oData = null;
                            }
                            aKeyCountLog.set(iTempVar, ++iTempCounter);
                            determineNextButtonToClick(iNextButton);
                        } else {
                            // TODO
                        }
                    }
                    return false;
                }
            });
            buttonList[Integer.parseInt(currentButton.getText().toString())] = currentButton;
        }
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
                    uToast.show();
                }
                aKeyCountLog = new ArrayList<>(asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)); // reset counter
            }
        });
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
     * Reset Capture Mode and store logged sensor data
     */
    private void triggerStorageOfLoggedData() {
        stopCaptureMode();
        uSaveButton.setBackgroundColor(Color.parseColor(NEGATIVE_COLOR_CODE)); // red

        final AsyncTask asyncTask;
        asyncTask = new StoreDataTask() {
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
     * Determines which button the user is supposed to press next
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
            stopCaptureMode();
            uToast = Toast.makeText(getApplicationContext(), STRING_DONE_CAPTURING_MESSAGE, Toast.LENGTH_SHORT);
            uToast.show();
            return;
        }
        // pick new button
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            iNextButton = ThreadLocalRandom.current().nextInt(0, 10);
        } else {
            iNextButton = new Random().nextInt((11));
        }
        iTempCounter = aKeyCountLog.get(iNextButton);
        if (iTempCounter < INTEGER_MAX_DATA_LOGGED) {
            // change button ui
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buttonList[iNextButton].setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                buttonList[iNextButton].setBackgroundColor(Color.GREEN);
            }
        } else {
            determineNextButtonToClick(iNextButton);
        }

    }

    /**
     * enables data capturing
     */
    private void startCaptureMode() {
        uCaptureButton.setText(CAPTURE_BUTTON_STOP_TEXT);
        uSaveButton.setBackgroundColor(Color.parseColor((POSITIVE_COLOR_CODE))); // blue
        bIsInCaptureMode = true;
        determineNextButtonToClick(-1);
    }

    /**
     * unset flag for data capture
     */
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

        Sensor acceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_LINEAR_ACCELERATION);
        oSensorManager.registerListener(this, acceleroMeter, GLOBAL_SENSOR_SPEED);
    }

    /**
     * Set-up of Gyroscope sensor. Orientation data will be captured during runtime and
     * combined with the accelerometer data.
     */
    private void initGyroscopeSensor() {
        Sensor gyroscope = oSensorReader.getSingleSensorOfType(Sensor.TYPE_GYROSCOPE);
        oSensorManager.registerListener(this, gyroscope, GLOBAL_SENSOR_SPEED);
    }

    private void initStorageManager() {
        oStorageManager = StorageManager.getInstance();
    }

    @Override
    public void onAccuracyChanged(Sensor pSensor, int pAccuracy) {
    }
}