package com.nyrrrr.msd.collector;

import android.app.Activity;
import android.hardware.*;
import android.util.Log;

import java.util.List;

/**
 * Created by nyrrrr on 23.09.2016.
 */

public class SensorReader {

    boolean isAccelerometerAvailable = false;
    boolean isAccelerometerActivated = false;

    SensorManager sensorManager;
    List<Sensor> sensorList;

    public SensorReader(List<Sensor> sensorList) {
        this.sensorList = sensorList;
    }

    public void printListOfAvailableSensors() {
        for(Sensor s : sensorList) {
            Log.d("Sensor names", s.getName());
        }
    }
}
