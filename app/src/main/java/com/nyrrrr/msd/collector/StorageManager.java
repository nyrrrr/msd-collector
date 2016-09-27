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
 * Created by nyrrrr on 27.09.2016.
 */

public class StorageManager {

    private static StorageManager oInstance = null;
    private final String STRING_FILE_NAME = "msd-data.json";
    JSONObject oData;

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
     */
    public JSONObject createAndSaveFile(Context pAppContext) {
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

    /**
     * Get data from JSON file
     * @param pAppContext
     * @return
     */
    public JSONObject getDataFromFile(Context pAppContext) {
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
}
