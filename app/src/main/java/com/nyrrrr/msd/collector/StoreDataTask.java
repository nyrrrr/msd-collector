package com.nyrrrr.msd.collector;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;

/**
 * Asynchronous Task for storing the data, so the UI won't get blocked in the meantime
 * Created by nyrrrr on 04.12.2016.
 */

class StoreDataTask extends AsyncTask {

    @Override
    protected Object doInBackground(Object[] pObjects) {
        StorageManager storeManager = StorageManager.getInstance();
        try {
            storeManager.storeData((Context) pObjects[0]);
        } catch (IOException e) {
            Toast.makeText((Context) pObjects[0], "Unexpected Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return e;
        }
        return null;
    }
}
