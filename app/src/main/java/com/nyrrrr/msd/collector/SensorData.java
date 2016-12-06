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
    float key_x;
    float key_y;
//    long key_down;
//    long key_released;

    SensorData(long pTimestamp) {
        timestamp = pTimestamp;
    }

    String toCSVString() {
        return timestamp + ","
                + x + "," + y + "," + z + ","
                + alpha + "," + beta + "," + gamma + ","
                + keyPressed + "," + key_x + "," + key_y + "\n";
//                + "," + key_down + "," + key_released + "\n";
    }

    String getCsvHeaders() {
        return "Timestamp,x,y,z,alpha,beta,gamma,keyPressed,key_x,key_y,key_down,key_released\n";
    }

}
