package com.nyrrrr.msd.collector;

/**
 * Sensor Data object used to be saved later on
 * Stores Accelerometer data and orientation
 * Created by nyrrrr on 24.09.2016.
 */

class SensorData {

    float x; // x acc
    float y; // y acc
    float z; // z acc
    float a; // x gyro
    float b; // y gyro
    float c; // z gyro
    float alpha; // alpha
    float beta; // beta
    float gamma; // gamma
    private long timestamp;


    SensorData () {
        this(System.currentTimeMillis());
    }

    private SensorData(long pTimeStamp) {
        timestamp = pTimeStamp;
    }

    String toCSVString() {
        return timestamp + ","
                + x + "," + y + "," + z + ","
                + a + "," + b + "," + c + ","
                + alpha + "," + beta + "," + gamma + "\n";
    }

    String getCsvHeaders() {
        return "Timestamp,x,y,z,a,b,c,alpha,beta,gamma";
    }
}
