package com.nyrrrr.msd.collector;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves data on phone
 * Currently saves the data in JSON. I might reconsider this format.
 * TODO SQLite might make more sense for this task.
 * Created by nyrrrr on 27.09.2016.
 */

public class StorageManager {

    private static StorageManager oInstance = null;
    private final String STRING_FILE_NAME = "msd-data.json"; // TODO use timestamp name?
    public JSONArray oData;

    private SensorData oSensorData;
    private List<SensorData> oSensorDataList;

    protected StorageManager() {
        oSensorDataList = new ArrayList<SensorData>();
    }

    public static StorageManager getInstance() {
        if (oInstance == null) {
            oInstance = new StorageManager();
        }
        return oInstance;
    }

    /**
     * puts captured data into a list for later storage
     *
     * @param pEvent       sensor data event
     * @param pOrientation orientation during capture
     * @param pKeyCode     key pressed (if any)
     * @return SensorData object
     * @// TODO: 08.11.2016 remove unnecessary data from list before storage?!
     */
    public SensorData addSensorDataLogEntry(SensorEvent pEvent, int pOrientation, String pKeyCode) {
        oSensorData = new SensorData(pEvent, pOrientation, pKeyCode);
        if (oSensorDataList.add(oSensorData)) {
            return oSensorData;
        }
        return null;
    }

    /**
     * Convert List object to JSONArray for storage
     *
     * @return JSONArray
     */
    private JSONArray convertSensorDataLogToJSON() {
        // TODO remove unnecessary entries first
        oData = new JSONArray();

        for (SensorData dataObject : oSensorDataList) {
            oData.put(dataObject.toJSONObject());
        }
        return oData;
    }

    /**
     * Create and save data file (TIMESTAMP-msd-data.json).
     * The list of data will first be converted to JSON.
     *
     * @param pAppContext
     * @return boolean  - true for successful save
     * @throws IOException
     * @throws JSONException
     */
    public void storeData(Context pAppContext) throws JSONException, IOException {
        oData = convertSensorDataLogToJSON();
        oSensorDataList = new ArrayList<SensorData>(); // reset list
        String fileName = "";

            fileName = oData.getJSONObject(0).get("Timestamp") + "-";
            FileWriter file = new FileWriter(pAppContext.getFilesDir().getPath() + "/" + fileName + STRING_FILE_NAME);
            file.write(oData.toString());
            file.flush();
            file.close();
    }

    // debug-only
    public void storeData(Context pAppContext, boolean pDebug) throws IOException, JSONException {
        this.storeData(pAppContext);
        Log.d("JSON Write debug", oData.toString(4));
    }

    /**
     * return size of list of logged data
     *
     * @return int
     */
    public int getSensorDataLogLength() {
        return oSensorDataList.size();
    }
}
