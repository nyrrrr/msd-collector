package com.nyrrrr.msd.collector;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Saves data on phone
 * Currently saves the data in JSON. I might reconsider this format.
 * TODO SQLite might make more sense for this task.
 * Created by nyrrrr on 27.09.2016.
 */

public class StorageManager {

    private static StorageManager oInstance = null;
    private final String STRING_FILE_NAME = "msd-data.json";
    public JSONObject oData;

    /*
        temporary vars
     */
    private int iSize;
    private byte[] bBuffer;

    /*
        singleton
     */
    protected StorageManager() {
    }

    public static StorageManager getInstance() {
        if (oInstance == null) {
            oInstance = new StorageManager();
        }
        return oInstance;
    }

    /**
     * Create and save data file (msd-data.json)
     *
     * @param pAppContext
     * @return JSON data object
     */
    public JSONObject storeData(Context pAppContext) {
        if (oData == null) {
            oData = new JSONObject();
        }
        try {
            FileWriter file = new FileWriter(pAppContext.getFilesDir().getPath() + "/" + STRING_FILE_NAME);
            file.write(oData.toString());
            file.flush();
            file.close();
        } catch (IOException e) {
            Log.e(e.getCause().toString(), "Error while writing: " + e.getMessage());
        }
        return oData;
    }

    // TODO remove, debug-only
    public JSONObject storeData(Context pAppContext, boolean pDebug) {
        oData = this.storeData(pAppContext);
        try {
            Log.d("JSON Write debug", oData.toString(4));
        } catch (JSONException e) {
            Log.e(e.getCause().toString(), "Error while creating JSON: " + e.getMessage());
        }
        return oData;
    }

    /**
     * Get data from JSON file
     * @param pAppContext
     * @return JSON data object
     */
    public JSONObject restoreData(Context pAppContext) {
        try {
            File file = new File(pAppContext.getFilesDir().getPath() + "/" + STRING_FILE_NAME);
            FileInputStream fileInputStream = new FileInputStream(file);
            iSize = fileInputStream.available();
            bBuffer = new byte[iSize];
            fileInputStream.read(bBuffer);
            fileInputStream.close();
            return oData = new JSONObject((new String(bBuffer)));                       // return
        } catch (FileNotFoundException e) {
            Log.e(e.getCause().toString(), "Error while reading: " + e.getMessage());
        } catch (IOException e) {
            Log.e(e.getCause().toString(), "Error while reading: " + e.getMessage());
        } catch (JSONException e) {
            Log.e(e.getCause().toString(), "Error while creating JSON: " + e.getMessage());
        }
        return null;
    }

    // TODO remove, debug-only
    public JSONObject restoreData(Context pAppContext, boolean pDebug) {
        oData = this.restoreData(pAppContext);
        try {
            Log.d("JSON Read debug", oData.toString(4));
        } catch (JSONException e) {
            Log.e(e.getCause().toString(), "Error while creating JSON: " + e.getMessage());
        }
        return oData;
    }

    public void setDataObject(JSONObject oData) {
        this.oData = oData;
    }
}
