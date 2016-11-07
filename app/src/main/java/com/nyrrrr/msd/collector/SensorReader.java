package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.List;

/**
 * provide sensor info
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

    Sensor getSingleSensorOfType(int pSensorType) {
        return oSensorManager.getDefaultSensor(pSensorType);
    }


    /**
     * get all sensors of a certain type
     *
     * @param pSensorType TYPE_ALL for all sensors
     * @return List<Sensor>
     */
    private List<Sensor> getSensorsOfType(int pSensorType) {
        return oSensorManager.getSensorList(pSensorType);
    }
}
