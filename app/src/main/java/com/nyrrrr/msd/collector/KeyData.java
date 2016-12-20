package com.nyrrrr.msd.collector;

/**
 * Key Data object for saving key input
 * Created by nyrrrr on 20.12.2016.
 */

class KeyData {

    String keyPressed;
    private long downTime;
    long eventTime;

    KeyData() {
        this(System.currentTimeMillis());
    }

    private KeyData(long pTimeStamp) {
        downTime = pTimeStamp;
    }

    String toCSVString() {
        return downTime + "," + eventTime + "," + keyPressed + "\n";
    }

    String getCsvHeaders() {
        return "DownTime,EventTime,Keypress";
    }
}
