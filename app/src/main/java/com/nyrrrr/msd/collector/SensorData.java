package com.nyrrrr.msd.collector;

import android.hardware.SensorEvent;
import android.os.Build;
import android.util.Log;

/**
 * Sensor Data object used to be saved later on
 * Stores Accelerometer data and orientation
 * Created by nyrrrr on 24.09.2016.
 */

public class SensorData {

    private long dTimestamp = -1;
    private String sSensorType = "";
    private float[] fValues = new float[3];
    private int iAccuracy = -1;
    private float fFrequency = -1;
    private int iOrientation = -1;
    private String sKeyPressed = "";

    /**
     * @param pEvent
     * @param pOrientation
     * @param pKeyPressedCode
     */
    public SensorData(SensorEvent pEvent, int pOrientation, String pKeyPressedCode) {
        this(pEvent, pOrientation, pKeyPressedCode, -1);
    }

    /**
     * @param pEvent
     * @param pOrientation
     * @param pKeyPressedCode
     * @param pFrequency
     */
    public SensorData(SensorEvent pEvent, int pOrientation, String pKeyPressedCode, int pFrequency) {
        dTimestamp = pEvent.timestamp;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            sSensorType = pEvent.sensor.getStringType();
        }
        fValues = pEvent.values;
        iAccuracy = pEvent.accuracy;
        fFrequency = pFrequency;
        iOrientation = pOrientation;
        sKeyPressed = pKeyPressedCode;
        ;
    }

    public void print() {
        Log.d("Timestamp", dTimestamp + "");
        Log.d("Sensor type", sSensorType);
        Log.d("Values", "x: " + fValues[0] + ", y: " + fValues[1] + ", z: " + fValues[2] + "");
        Log.d("Accuracy", iAccuracy + "");
        Log.d("Key Pressed", sKeyPressed);
        Log.d("Orientation", iOrientation + "");
        Log.d("Frequency", fFrequency + "");
        Log.d("-----------------------", "-------------------------------------------------------");
    }
}
