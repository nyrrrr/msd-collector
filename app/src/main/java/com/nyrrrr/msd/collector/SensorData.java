package com.nyrrrr.msd.collector;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Sensor Data object used to be saved later on
 * Stores Accelerometer data and orientation
 * Created by nyrrrr on 24.09.2016.
 */

class SensorData {

    float x;
    float y;
    float z;
    float alpha;
    float beta;
    float gamma;
    long timestamp;
    private String dateTime;
    String keyPressed;
    float key_x;
    float key_y;

    SensorData () {
        this(System.currentTimeMillis());
    }

    private SensorData(long pTimeStamp) {
        timestamp = pTimeStamp;
        dateTime = getSimpleDateFormat(timestamp);
    }

    String toCSVString() {
        return timestamp + "," + dateTime + ","
                + x + "," + y + "," + z + ","
                + alpha + "," + beta + "," + gamma + ","
                + keyPressed + "," + key_x + "," + key_y + "\n";
    }

    String getCsvHeaders() {
        return "Timestamp,DateTime,x,y,z,alpha,beta,gamma,keyPressed,key_x,key_y\n";
    }


    private static String getSimpleDateFormat(long pTimestamp) {
        SimpleDateFormat date = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm:ss:SSSS");
        return date.format(new Date(pTimestamp));
    }
}
