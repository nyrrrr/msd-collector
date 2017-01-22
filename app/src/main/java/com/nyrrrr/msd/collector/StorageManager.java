package com.nyrrrr.msd.collector;

import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves data on phone
 * Currently saves the data in JSON. I might reconsider this format.
 * <p>
 * Created by nyrrrr on 27.09.2016.
 */

class StorageManager {

    private static final String STRING_SENSOR_TRAINING_CSV_FILE_NAME = "sensor-dataset-training-RAW.csv";
    private static final String STRING_KEY_TRAINING_CSV_FILE_NAME = "key-dataset-training-RAW.csv";
    private static final String STRING_KEY_TESTING_CSV_FILE_NAME = "key-dataset-test-RAW.csv";

    private static StorageManager oInstance = null;

    private List<SensorData> oSensorDataList;
    private List<KeyData> oKeyDataList;

    private StorageManager() {
        oSensorDataList = new ArrayList<>();
        oKeyDataList = new ArrayList<>();
    }

    static StorageManager getInstance() {
        if (oInstance == null) {
            oInstance = new StorageManager();
        }
        return oInstance;
    }

    void addSensorDataLogEntry(SensorData pData) {
        oSensorDataList.add(pData);
        Log.d("Sensor", pData.toCSVString());
    }

    void addKeyDataLogEntry(KeyData pData) {
        oKeyDataList.add(pData);
        Log.d("Key", pData.toCSVString());
    }

    /**
     * Create and save data file (TIMESTAMP-msd-data.json).
     * The list of data will first be converted to JSON.
     *
     * @param pAppContext app context
     * @throws IOException
     */
    void storeData(Context pAppContext, boolean isTrainingData) throws IOException {

        String fileName;
        FileWriter file;
        BufferedWriter bw;
        PrintWriter out;
        SimpleDateFormat date = new SimpleDateFormat("yyMMddHHmm");
        String filenamePrefix = date.format(new java.sql.Timestamp(System.currentTimeMillis()));
        if (isTrainingData) {
            // write sensor csv file
            fileName = filenamePrefix + "-" + STRING_SENSOR_TRAINING_CSV_FILE_NAME;
            file = new FileWriter(pAppContext.getFilesDir().getPath() + "/" + fileName, true);
            bw = new BufferedWriter(file);
            out = new PrintWriter(bw);

            out.println(oSensorDataList.get(0).getCsvHeaders());
            for (SensorData dataObject : oSensorDataList) {
                out.print(dataObject.toCSVString());
            }
            out.close();
        }
        // write key csv file
        fileName = filenamePrefix + "-" + (isTrainingData ? STRING_KEY_TRAINING_CSV_FILE_NAME : STRING_KEY_TESTING_CSV_FILE_NAME); // TODO
        file = new FileWriter(pAppContext.getFilesDir().getPath() + "/" + fileName, true);
        bw = new BufferedWriter(file);
        out = new PrintWriter(bw);

        out.println(oKeyDataList.get(0).getCsvHeaders());
        for (KeyData dataObject : oKeyDataList) {
            out.print(dataObject.toCSVString());
        }
        out.close();

        Log.d("Data logged", oKeyDataList.size() + "");

        oSensorDataList = new ArrayList<>(); // reset list
        oKeyDataList = new ArrayList<>(); // reset
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
