package com.nyrrrr.msd.collector;

import android.content.Context;
import android.util.Log;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves data on phone
 * Currently saves the data in JSON. I might reconsider this format.
 * <p>
 * Created by nyrrrr on 27.09.2016.
 */

class StorageManager {

    private static final String STRING_CSV_FILE_NAME = "training-data.csv";

    private static StorageManager oInstance = null;

    private List<SensorData> oSensorDataList;

    private StorageManager() {
        oSensorDataList = new ArrayList<>();
    }

    static StorageManager getInstance() {
        if (oInstance == null) {
            oInstance = new StorageManager();
        }
        return oInstance;
    }

    void addSensorDataLogEntry(SensorData pData) {
        //oSensorDataList.add(pData);
        Log.d("CSV", pData.toCSVString());
    }

    /**
     * Convert List of SensorData to CSV String.
     * Can also remove unnecessary entries from the original list
     *
     * @return CSV String
     */
    private String convertSensorDataLogToCSV() {
        String csvString = oSensorDataList.get(0).getCsvHeaders();
        for (SensorData dataObject : oSensorDataList) {
            csvString += dataObject.toCSVString();
        }
        return csvString;
    }

    /**
     * Create and save data file (TIMESTAMP-msd-data.json).
     * The list of data will first be converted to JSON.
     *
     * @param pAppContext app context
     * @throws IOException
     */
    void storeData(Context pAppContext) throws IOException {

        String fileName = oSensorDataList.get(0).timestamp + "-";

        // write csv
        FileWriter file = new FileWriter(pAppContext.getFilesDir().getPath() + "/" + fileName + STRING_CSV_FILE_NAME);
        file.write(convertSensorDataLogToCSV());
        file.flush();
        file.close();

        Log.d("Data logged", oSensorDataList.size() + "");

        oSensorDataList = new ArrayList<>(); // reset list
    }

    /**
     * return size of list of logged data
     *
     * @return int
     */
    int getSensorDataLogLength() {
        return oSensorDataList.size();
    }

}
