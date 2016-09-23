package com.nyrrrr.msd.collector;

import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        SensorReader sra = new SensorReader(sensorManager);
//        sra.printListOfAvailableSensors(Sensor.TYPE_ACCELEROMETER);
        sra.printInfoOfServices(Sensor.TYPE_ACCELEROMETER);
    }

}
