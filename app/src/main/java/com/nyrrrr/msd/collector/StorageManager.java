package com.nyrrrr.msd.collector;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves data on phone
 * Currently saves the data in JSON. I might reconsider this format.
 * <p>
 * Created by nyrrrr on 27.09.2016.
 */

class StorageManager {

    private static final String STRING_CSV_FILE_NAME = "training-data-set.csv";

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
        oSensorDataList.add(pData);
         Log.d("CSV", pData.toCSVString());
    }

    /**
     * Create and save data file (TIMESTAMP-msd-data.json).
     * The list of data will first be converted to JSON.
     *
     * @param pAppContext app context
     * @throws IOException
     */
    void storeData(Context pAppContext) throws IOException {

        String fileName = oSensorDataList.get(0).timestamp + "-" + STRING_CSV_FILE_NAME;

        // write csv
        FileWriter file = new FileWriter(pAppContext.getFilesDir().getPath() + "/" + fileName, true);
        BufferedWriter bw = new BufferedWriter(file);
        PrintWriter out = new PrintWriter(bw);

        out.println(oSensorDataList.get(0).getCsvHeaders());
        for(SensorData dataObject : oSensorDataList) {
            out.print(dataObject.toCSVString());
        }

        out.close();

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
