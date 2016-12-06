package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorManager;

/**
 * Class for reading Sensor data
 * Created by nyrrrr on 23.09.2016.
 */

class SensorReader {

    private SensorManager oSensorManager;

    /**
     * constructor
     *
     * @param pSensorManager sensor manager
     */
    SensorReader(SensorManager pSensorManager) {
        this.oSensorManager = pSensorManager;
    }

    Sensor getSingleSensorOfType(int pSensorType) {
        return oSensorManager.getDefaultSensor(pSensorType);
    }
}