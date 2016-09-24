package com.nyrrrr.msd.collector;

import android.hardware.SensorEvent;
import android.os.Build;
import android.util.Log;

/**
 * Sensor Data object used to be saved later on
 * Created by nyrrrr on 24.09.2016.
 */

public class SensorData {

    private long dTimestamp = 0;
    private String sSensorType = "";
    private float[] fValues = new float[3];
    private int iAccuracy = 0;
    private float fFrequency = 0;

    /**
     * Constructor
     * @param pEvent
     * @param pFrequency
     */
    public SensorData(SensorEvent pEvent, int pFrequency) {
        dTimestamp = pEvent.timestamp;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            sSensorType = pEvent.sensor.getStringType();
        }
        fValues = pEvent.values;
        iAccuracy = pEvent.accuracy;
        fFrequency = pFrequency;
    }

    public void print() {
        Log.d("Timestamp", dTimestamp + "");
        Log.d("Sensor type", sSensorType);
        Log.d("Values", "x: " + fValues[0]+", y: " + fValues[1] + ", z: " + fValues[2] +  "");
        Log.d("Accurcary", iAccuracy+"");
        Log.d("Frequency", fFrequency+"");
    }
}
