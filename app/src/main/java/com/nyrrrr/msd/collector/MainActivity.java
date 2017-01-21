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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Main Class
 * When Capture mode is enabled, sensor data are being logged.
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int INTEGER_MAX_DATA_LOGGED = 50;
    private static final int INTEGER_MAX_DATA_LOGGED_KEYLOGGER_MODE = 5;

    private static final String STRING_KEYCODES_ONLY = "Keys";
    private static final String CAPTURE_BUTTON_CAPTURE_TEXT = "Sensor+Keys";
    private static final String STRING_LOGGING = "Logging...";
    private static final String NO_DATA_CAPTURED_MESSAGE = "No data was captured yet!";
    private static final String DATA_SUCCESSFULLY_STORED_MESSAGE = "The captured data has been stored.";
    private static final String UNEXPECTED_ERROR_MESSAGE = "Unexpected error: ";
    private static final String STRING_KEYPRESSES_LEFT = "Key presses left: ";
    private static final String NEGATIVE_COLOR_CODE = "#FFFF4081";
    private static final String POSITIVE_COLOR_CODE = "#FF3F51B5";
    private static final int GLOBAL_SENSOR_SPEED = SensorManager.SENSOR_DELAY_FASTEST;
    private static final int INTEGER_BUTTON_LIST_CLEARED = -1;
    private static final String STRING_PREPARE_SAVING = "Trying to save the data";

    private SensorManager oSensorManager;
    private SensorReader oSensorReader;
    private StorageManager oStorageManager;
    private SensorData oSensorData;
    private KeyData oKeyData;
    private Sensor oGyroscope;
    private Sensor oLinAcceleroMeter;
    private Button[] buttonList;
    private Button uKeyCodesOnlyButton;
    private Button uCaptureButton;
    private Button uSaveButton;
    private Toast uToast;
    private TextView uTextView;

    private int iNextButton;
    private int iCurrentButton;

    private ArrayList<Integer> aButtonPressOrder;

    private MachineState fsmState;

    private int iNumberOfLogs;
    private Sensor oAccelerometer;

    /*
     * standard methods
     * ---------------------------------------------------------------------------------------------
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fsmState = MachineState.INIT;

        startInitMode();
    }

    /**
     * Detects when sensor values change and reacts
     * NOTE: currently it reacts to every single change
     *
     * @param pSensorEvent Accelerometer or Gyroscope event
     */
    @Override
    public void onSensorChanged(SensorEvent pSensorEvent) {

        if (fsmState == MachineState.CAPTURE) {
            // create new data object
            if (oSensorData == null) oSensorData = new SensorData(System.currentTimeMillis());
            // fill data obj
            if (pSensorEvent.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                oSensorData.x = pSensorEvent.values[0];
                oSensorData.y = pSensorEvent.values[1];
                oSensorData.z = pSensorEvent.values[2];
            } else if (pSensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                oSensorData.a = pSensorEvent.values[0];
                oSensorData.b = pSensorEvent.values[1];
                oSensorData.c = pSensorEvent.values[2];
            } else if (pSensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                oSensorData.x_ra = pSensorEvent.values[0];
                oSensorData.y_ra = pSensorEvent.values[1];
                oSensorData.z_ra = pSensorEvent.values[2];
            }
            if (isSensorDataObjectComplete(oSensorData)) {
                oStorageManager.addSensorDataLogEntry(oSensorData);
                oSensorData = null;
            }
        }
    }

    private boolean isSensorDataObjectComplete(SensorData pData) {
        return oSensorData != null && pData.x != 0 && pData.y != 0 && pData.z != 0 && pData.x_ra != 0 && pData.y_ra != 0 && pData.z_ra != 0 && pData.a != 0 && pData.b != 0 && pData.c != 0;
    }

    /**
     * Handles the input of the custom number keyboard
     *
     * @param pView        view that triggered the event
     * @param pMotionEvent motion event
     * @return boolean
     */
    private boolean onKeyEvent(Button pView, MotionEvent pMotionEvent) {
        if (iNextButton == Integer.parseInt(pView.getText().toString())) {
            // create data object
            if (pMotionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                oKeyData = createDataObject(iNextButton, System.currentTimeMillis());
            } else if (pMotionEvent.getAction() == MotionEvent.ACTION_UP) {

                oKeyData.eventTime = System.currentTimeMillis();

                oStorageManager.addKeyDataLogEntry(oKeyData);
                oKeyData = null; // reset
                updateButtonPressTextView(uTextView, aButtonPressOrder);

                iCurrentButton = iNextButton;
                iNextButton = determineNextButtonToClick(aButtonPressOrder);

                if (iNextButton == INTEGER_BUTTON_LIST_CLEARED) {
                    if (fsmState == MachineState.KEYLOGGER) stopKeyloggerMode();
                    if (fsmState == MachineState.CAPTURE) stopCaptureMode();
                } else {
                    displayNextButtonOnKeyboard(iNextButton, iCurrentButton);
                }
            }
        }
        return false;
    }

    /**
     * Create SensorData object for KEYLOGGER mode
     *
     * @param pKey       key info
     * @param pEventTime timestamp
     * @return SensorData
     */
    private KeyData createDataObject(int pKey, long pEventTime) {
        KeyData data = new KeyData(pEventTime);
        data.keyPressed = "KEYCODE_" + pKey;
        return data;
    }

    private void updateButtonPressTextView(TextView pTextView, ArrayList<Integer> pButtonPressOrder) {
        pTextView.setText(MessageFormat.format("{0}{1}", new Object[]{STRING_KEYPRESSES_LEFT, pButtonPressOrder.size()}));
    }

    /*
     * --------------------------------- Modes -----------------------------------------------------
     */

    /**
     * inits ui and sensors
     * resets vars
     */
    private void startInitMode() {
        if (fsmState == MachineState.INIT) {
            iNextButton = -1;
            iCurrentButton = -1;
            oSensorData = null;
            buttonList = new Button[10];

            // init ui
            // KEYBOARD
            buttonList = initKeyboard();
            uCaptureButton = initCaptureButton();
            uKeyCodesOnlyButton = initKeyCodeOnlyModeButton();
            uSaveButton = initSaveButton();
            uTextView = initTextView();

            // init sensors & persistence
            initStorageManager();
            initLinearAccelerometerSensor();
            initGyroscopeSensor();
            initAccelerometerSensor();
        } else {
            if (fsmState.possibleFollowUps().contains(MachineState.INIT)) {
                fsmState = MachineState.INIT;

                iNextButton = -1;
                iCurrentButton = -1;
                oSensorData = null;
                aButtonPressOrder = null;

                // (re)color keyboard
                for (Button currentButton : buttonList) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        currentButton.setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
                    } else {
                        currentButton.setBackgroundColor(Color.LTGRAY);
                    }
                }
                uCaptureButton.setEnabled(true);
                uCaptureButton.setText(CAPTURE_BUTTON_CAPTURE_TEXT);
                uKeyCodesOnlyButton.setEnabled(true);
                uKeyCodesOnlyButton.setText(STRING_KEYCODES_ONLY);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    uSaveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(NEGATIVE_COLOR_CODE)));
                } else {
                    uSaveButton.setBackgroundColor(Color.parseColor((NEGATIVE_COLOR_CODE))); // red
                }
            }
        }
    }

    /**
     * enables data capturing
     */
    private void startCaptureMode() {
        if (!checkAvailabilityOfSensors()) return;
        if (fsmState.possibleFollowUps().contains(MachineState.CAPTURE)) {
            iNumberOfLogs = INTEGER_MAX_DATA_LOGGED;
            // UI
            prepareKeyboardForLogging();
            fsmState = MachineState.CAPTURE;
            prepareUiForLogging();
            // register listeners
            registerSensorListeners();

        }
    }

    private void prepareKeyboardForLogging() {
        // determine first button
        aButtonPressOrder = createButtonOrder(iNumberOfLogs);
        iNextButton = determineNextButtonToClick(aButtonPressOrder);
        displayNextButtonOnKeyboard(iNextButton, iCurrentButton);
    }

    /**
     * unset flag for data capture
     */
    private void stopCaptureMode() {
        startSaveMode();
    }

    /**
     * Similar to Capture mode, but no sensor logging; keys-only
     */
    private void startKeyloggerMode() {
        if (!checkAvailabilityOfSensors()) return;
        if (fsmState.possibleFollowUps().contains(MachineState.KEYLOGGER)) {
            iNumberOfLogs = INTEGER_MAX_DATA_LOGGED_KEYLOGGER_MODE;
            prepareKeyboardForLogging();
            fsmState = MachineState.KEYLOGGER;
            prepareUiForLogging();
        }
    }

    /**
     * Stop keylogging mode
     */
    private void stopKeyloggerMode() {
        if (fsmState.possibleFollowUps().contains(MachineState.SAVE)) {
            startSaveMode();
        }
    }

    /**
     * Stores data and then switches to INIT
     */
    private void startSaveMode() {
        boolean isTrainingDataMode = false;
        if (fsmState.possibleFollowUps().contains(MachineState.SAVE)) {
            if (fsmState == MachineState.CAPTURE) {
                unregisterSensorListeners();
                isTrainingDataMode = true;
            }
            fsmState = MachineState.SAVE;
            Toast.makeText(getApplicationContext(), STRING_PREPARE_SAVING, Toast.LENGTH_SHORT).show();
            // store data
            storeData(isTrainingDataMode);
        }
    }

    /**
     * Create a list of button presses with no runs longer than n = 2;
     *
     * @param pNumberOfLogsPerKey determineshow often each key needs to be pressed
     */
    private ArrayList<Integer> createButtonOrder(int pNumberOfLogsPerKey) {
        aButtonPressOrder = new ArrayList<>(10 * pNumberOfLogsPerKey);
        ArrayList<Integer> subList = new ArrayList<>(10);

        for (int i = 0; i < pNumberOfLogsPerKey; i++) {
            for (int j = 0; j < 10; j++) {
                subList.add(j);
            }
            Collections.shuffle(subList);
            aButtonPressOrder.addAll(subList);
            subList.clear();
        }
        return aButtonPressOrder;
    }

    /**
     * see name
     */
    private Button activateSaveButton(Button pSaveButton) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            pSaveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(POSITIVE_COLOR_CODE)));
        } else {
            pSaveButton.setBackgroundColor(Color.parseColor((POSITIVE_COLOR_CODE))); // blue
        }
        return pSaveButton;
    }

    /**
     * Register sensor listeners
     */
    private void registerSensorListeners() {
        oSensorManager.registerListener(this, oLinAcceleroMeter, GLOBAL_SENSOR_SPEED);
        oSensorManager.registerListener(this, oGyroscope, GLOBAL_SENSOR_SPEED);
        oSensorManager.registerListener(this, oAccelerometer, GLOBAL_SENSOR_SPEED);
    }

    /**
     * unregister sensor listeners
     */
    private void unregisterSensorListeners() {
        oSensorManager.unregisterListener(this, oLinAcceleroMeter);
        oSensorManager.unregisterListener(this, oGyroscope);
        oSensorManager.unregisterListener(this, oAccelerometer);
    }

    /*
     * --------------------------------- UI --------------------------------------------------------
     */

    /**
     * called by CAPTURE and KEYLOGGER mode to set the UI up
     */
    private void prepareUiForLogging() {
        // UI
        blockUnusedButtons(uCaptureButton, uKeyCodesOnlyButton);
        activateSaveButton(uSaveButton);
        activateButtonPressTextView(uTextView);
    }

    /**
     * set invisible
     */
    private TextView initTextView() {
        TextView textView = (TextView) findViewById(R.id.textView);
        if (textView != null) {
            textView.setVisibility(View.INVISIBLE);
        }
        return textView;
    }

    /**
     * Init my custom keyboard.
     * Register OnTouchListeners on Buttons.
     *
     * @return Button[]
     */
    private Button[] initKeyboard() {
        Button currentButton;
        Button[] list = new Button[10];
        // get keyboard
        ViewGroup keyboard = (ViewGroup) findViewById(R.id.keyboard);
        for (int i = 0; i < (keyboard != null ? keyboard.getChildCount() : 0); i++) { // init all buttons
            currentButton = (Button) keyboard.getChildAt(i);
            // touch even listener
            currentButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View pView, MotionEvent pMotionEvent) {
                    return onKeyEvent((Button) pView, pMotionEvent);
                }
            });
            // store in list
            list[Integer.parseInt(currentButton.getText().toString())] = currentButton;
        }
        return list;
    }

    /**
     * Triggers capture mode in onClick method
     */
    private Button initCaptureButton() {
        Button captureButton = (Button) findViewById(R.id.captureButton);

        if (captureButton != null) {
            captureButton.setText(CAPTURE_BUTTON_CAPTURE_TEXT);
            captureButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    if (((Button) v).getText().toString().equals(CAPTURE_BUTTON_CAPTURE_TEXT)) {
                        startCaptureMode();
                    }
                }
            });
        }

        return captureButton;
    }

    /**
     * Register onClick listener on save button.
     * Save button stops data capturing process and stores data
     */
    private Button initSaveButton() {
        Button saveButton = (Button) findViewById(R.id.saveButton);
        if (saveButton != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                saveButton.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(NEGATIVE_COLOR_CODE)));
            } else {
                saveButton.setBackgroundColor(Color.parseColor((NEGATIVE_COLOR_CODE))); // red
            }
            saveButton.setOnClickListener(new View.OnClickListener() { // onClick
                public void onClick(View v) {
                    if (oStorageManager.getSensorDataLogLength() > 0) { // if data has already been captured
                        if (fsmState == MachineState.CAPTURE) startSaveMode();
                        else stopKeyloggerMode();
                    } else { // if list is empty, show warning instead
                        uToast = Toast.makeText(getApplicationContext(), NO_DATA_CAPTURED_MESSAGE, Toast.LENGTH_SHORT);
                        uToast.show();
                    }
                }
            });
        }
        return saveButton;
    }

    /**
     * button for keylogger mode (used for attacking scenario)
     */
    private Button initKeyCodeOnlyModeButton() {
        Button keylogButton = (Button) findViewById(R.id.keycodesOnlyButton);

        if (keylogButton != null) {
            keylogButton.setText(STRING_KEYCODES_ONLY);
            keylogButton.setEnabled(true);
            keylogButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
                    if (((Button) v).getText().toString().equals(STRING_KEYCODES_ONLY)) {
                        startKeyloggerMode();
                    }
                }
            });
        }
        return keylogButton;
    }

    private void activateButtonPressTextView(TextView pTextView) {
        if (pTextView == null) pTextView = initTextView();
        pTextView.setText(MessageFormat.format("{0}{1}", new Object[]{STRING_KEYPRESSES_LEFT, 10 * iNumberOfLogs}));
        pTextView.setVisibility(View.VISIBLE);
    }

    private void blockUnusedButtons(Button pCaptureButton, Button pKeyCodesOnlyButton) {
        pCaptureButton.setEnabled(false);
        if (fsmState == MachineState.CAPTURE)
            pCaptureButton.setText(STRING_LOGGING);
        else if (fsmState == MachineState.KEYLOGGER)
            pKeyCodesOnlyButton.setText(STRING_LOGGING);
        pKeyCodesOnlyButton.setEnabled(false);
    }

    /**
     * Highlights the next button the user is supposed to press.
     * If applicable, the previous button will be colored in its original color again
     *
     * @param pNextButton    next button
     * @param pCurrentButton previously pressed button
     */
    private void displayNextButtonOnKeyboard(int pNextButton, int pCurrentButton) {
        if (fsmState == MachineState.CAPTURE || fsmState == MachineState.KEYLOGGER) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                buttonList[pCurrentButton].setBackgroundTintList(ColorStateList.valueOf(Color.LTGRAY));
            } else {
                buttonList[pCurrentButton].setBackgroundColor(Color.LTGRAY);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            buttonList[pNextButton].setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
        } else {
            buttonList[pNextButton].setBackgroundColor(Color.GREEN);
        }
    }

    /*
     * --------------------------------- Helper functions ------------------------------------------
     */

    /**
     * Execute AsyncTask for data storage
     */
    private void storeData(boolean isTrainingData) {

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
                startInitMode(); // switch mode
            }
        };
        asyncTask.execute(getApplicationContext(), isTrainingData);
    }

    private boolean checkAvailabilityOfSensors() {
        return oStorageManager != null && oLinAcceleroMeter != null && oGyroscope != null;
    }

    /**
     * Determine which button the user is supposed to press next.
     *
     * @param pKeyOrderList order in which buttons are pressed
     * @return integer (negative when finished)
     */
    private int determineNextButtonToClick(ArrayList<Integer> pKeyOrderList) {

        if (pKeyOrderList.size() > 0) {
            return pKeyOrderList.remove(0);
        } else {
            return INTEGER_BUTTON_LIST_CLEARED;
        }
    }

    /*
     * --------------------------------- Sensors ---------------------------------------------------
     */

    /**
     * Set-up of accelerometer sensor for data capturing.
     * Registers EventListener.
     */
    private void initLinearAccelerometerSensor() {
        initSensorReader();
        oLinAcceleroMeter = oSensorReader.getSingleSensorOfType(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    private void initAccelerometerSensor() {
        initSensorReader();
        oAccelerometer = oSensorReader.getSingleSensorOfType(Sensor.TYPE_ACCELEROMETER);
    }

    /**
     * Set-up of Gyroscope sensor. data will be captured during runtime and
     * combined with the accelerometer data.
     */
    private void initGyroscopeSensor() {
        initSensorReader();
        oGyroscope = oSensorReader.getSingleSensorOfType(Sensor.TYPE_GYROSCOPE);
    }

    /**
     * init relevant classes for gyro and acc sensors
     */
    private void initSensorReader() {
        if (oSensorManager == null) {
            oSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        }
        if (oSensorReader == null) {
            oSensorReader = new SensorReader(oSensorManager);
        }
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