package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * provide sensor info
 * Created by nyrrrr on 23.09.2016.
 */

public class SensorReader {

    private SensorManager oSensorManager;
    private List<Sensor> oSensorList;
    private SensorData oSensorData; // TODO do I rly need this?

    private List<SensorData> oSensorDataList;

    /**
     * constructor
     *
     * @param pSensorManager
     */
    public SensorReader(SensorManager pSensorManager) {
        this.oSensorManager = pSensorManager;
        oSensorDataList = new ArrayList<SensorData>();
    }

    /**
     * puts captured data into a list for later storage (and removal of unneeded objects)
     * @param pEvent sensor data event
     * @param pOrientation orientation during capture
     * @param pKeyCode key pressed (if any)
     * @return SensorData object
     */
    public SensorData addSensorDataLog(SensorEvent pEvent, int pOrientation, int pKeyCode) {
        oSensorData = new SensorData(pEvent, pOrientation, pKeyCode);
        if (oSensorDataList.add(oSensorData)) return oSensorData;
        return null;
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


    /**
     * @param pEvent
     * @// TODO: 16.10.2016 remove or alter
     * @deprecated
     */
    public void printSensorEventInformation(SensorEvent pEvent) {
        oSensorData = new SensorData(pEvent, -1, -1, -1); // DEPRECATED
        oSensorData.print();
    }

    /**
     * print all available sensors of type
     *
     * @param pSensorType
     * @// TODO: 16.10.2016 remove
     * @deprecated remove
     */
    public void printListOfAvailableSensors(int pSensorType) {
        oSensorList = this.getSensorsOfType(pSensorType);

        for (Sensor s : this.oSensorList) {
            Log.d("Sensor info", s.getName());
        }
        Log.d("Sensor info", oSensorList.size() + "");
    }

    /**
     * print service info to console
     *
     * @param pSensorType
     * @// TODO: 16.10.2016 remove
     * @deprecated remove
     */
    public void printInfoOfServices(int pSensorType) {
        // get sensors
        oSensorList = this.getSensorsOfType(pSensorType);

        // print details
        for (Sensor s : oSensorList) {
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

}
