package com.nyrrrr.msd.collector;

import android.hardware.*;
import android.os.Build;
import android.util.Log;

import java.util.List;

/**
 * provide sensor info
 * Created by nyrrrr on 23.09.2016.
 */

public class SensorReader {

    boolean isAccelerometerAvailable = false;
    boolean isAccelerometerActivated = false;

    SensorManager sensorManager;
    List<Sensor> sensorList;

    /**
     * constructor
     *
     * @param pSensorManager
     */
    public SensorReader(SensorManager pSensorManager) {
        //this.sensorList = sensorList;
        this.sensorManager = pSensorManager;
    }

    /**
     * print all available sensors of type
     *
     * @param pSensorType
     */
    public void printListOfAvailableSensors(int pSensorType) {
        sensorList = this.getSensorsOfType(pSensorType);

        for (Sensor s : this.sensorList) {
            Log.d("Sensor info", s.getName());
        }
        Log.d("Sensor info", sensorList.size() + "");
    }

    /**
     * print service info to console
     *
     * @param pSensorType
     */
    public void printInfoOfServices(int pSensorType) {
        // get sensors
        sensorList = this.getSensorsOfType(pSensorType);

        // print details
        for (Sensor s : sensorList) {
            Log.d("Sensor Name", s.getName());
            Log.d("Sensor Vendor", s.getVendor());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.d("Sensor FifoMaxEvent#", s.getFifoMaxEventCount() + "");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Log.d("Sensor FifoRsrvdEvnt#", s.getFifoReservedEventCount() + "");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("Sensor Max Delay", s.getMaxDelay() + "");
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("Sensor Min Delay", s.getMinDelay() + "");
            }
            Log.d("Sensor Max Range", s.getMaximumRange() + "");
            Log.d("Sensor Power", s.getPower() + "");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.d("Sensor Reporting Mode", s.getReportingMode() + "");
            }
            Log.d("Sensor Resolution", s.getResolution() + "");
            Log.d("Sensor Type", s.getType() + "");
            Log.d("Sensor Version", s.getVersion() + "");
        }
    }

    /**
     * get all sensors of a certain type
     *
     * @param pSensorType TYPE_ALL for all sensors
     * @return List<Sensor>
     */
    private List<Sensor> getSensorsOfType(int pSensorType) {
        return sensorManager.getSensorList(pSensorType);
    }
}
