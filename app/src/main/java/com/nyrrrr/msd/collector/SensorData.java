package com.nyrrrr.msd.collector;

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
    String keyPressed;
    int key_x;
    int key_y;

    SensorData () {
        this(System.currentTimeMillis());
    }

    private SensorData(long pTimeStamp) {
        timestamp = pTimeStamp;
    }

    String toCSVString() {
        return timestamp + ","
                + x + "," + y + "," + z + ","
                + alpha + "," + beta + "," + gamma + ","
                + keyPressed + "," + key_x + "," + key_y + "\n";
    }

    String getCsvHeaders() {
        return "Timestamp,x,y,z,alpha,beta,gamma,keyPressed,key_x,key_y";
    }
}
