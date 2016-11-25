package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.List;

/**
 * Class for reading Sensor data
 * Created by nyrrrr on 23.09.2016.
 */

public class SensorReader {

    private SensorManager oSensorManager;
    private List<Sensor> oSensorList;

    /**
     * constructor
     *
     * @param pSensorManager
     */
    public SensorReader(SensorManager pSensorManager) {
        this.oSensorManager = pSensorManager;
    }

    public Sensor getSingleSensorOfType(int pSensorType) {
        return oSensorManager.getDefaultSensor(pSensorType);
    }


    /**
     * Get all available sensors of a certain type
     *
     * @param pSensorType TYPE_ALL for all sensors
     * @return List<Sensor>
     */
    public List<Sensor> getSensorsOfType(int pSensorType) {
        return oSensorManager.getSensorList(pSensorType);
    }
}
