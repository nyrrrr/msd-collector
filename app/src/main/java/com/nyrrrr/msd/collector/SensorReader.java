package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.ArrayList;
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
        oSensorList = new ArrayList<>();
    }

    public Sensor getSingleSensorOfType(int pSensorType) {
        oSensorList.add(oSensorManager.getDefaultSensor(pSensorType));
        return oSensorManager.getDefaultSensor(pSensorType);
    }
}