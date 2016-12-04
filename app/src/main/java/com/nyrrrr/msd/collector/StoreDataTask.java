package com.nyrrrr.msd.collector;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

/**
 * Asynchronous Task for storing the data, so the UI won't get blocked in the meantime
 * Created by nyrrrr on 04.12.2016.
 */

public class StoreDataTask extends AsyncTask {

    StorageManager oStoreManager;

    @Override
    protected Object doInBackground(Object[] pObjects) {
        oStoreManager = StorageManager.getInstance();
        try {
            oStoreManager.storeData((Context) pObjects[0]);
        } catch (IOException e) {
            Toast.makeText((Context) pObjects[0], "Unexpected Error: " + e.getMessage(), Toast.LENGTH_SHORT);
            e.printStackTrace();
            return e;
        }
        return null;
    }
}
