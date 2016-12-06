package com.nyrrrr.msd.collector;

import java.text.SimpleDateFormat;

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
    public String timestampString;
    public long timestamp;
    String keyPressed;
    float key_x;
    float key_y;
//    long key_down;
//    long key_released;

    public SensorData(long pTimestamp) {
        timestamp = pTimestamp;
        SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss:SSSS");
        timestampString = date.format(new java.sql.Timestamp(System.currentTimeMillis()));
    }

    String toCSVString() {
        return timestampString + ","
                + x + "," + y + "," + z + ","
                + alpha + "," + beta + "," + gamma + ","
                + keyPressed + "," + key_x + "," + key_y + "\n";
//                + "," + key_down + "," + key_released + "\n";
    }

    String getCsvHeaders() {
        return "Timestamp,x,y,z,alpha,beta,gamma,keyPressed,key_x,key_y,key_down,key_released\n";
    }

}
