package me.adhikasetyap.avidavi.main.core.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import me.adhikasetyap.avidavi.main.core.utilities.Utilities;

public class ProximitySensorListener implements SensorEventListener {

    private static final int SENSOR_SENSITIVITY = 1; // TODO make this user configurable
    private final String TAG = this.getClass().getName();

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            Log.i(TAG, "Proximity sensor value: " + sensorEvent.values[0]);
            Utilities.broadcastSensorData(Sensor.TYPE_PROXIMITY, sensorEvent.values[0]);
            if (sensorEvent.values[0] < SENSOR_SENSITIVITY) {
                //near
                Log.i(TAG, "Proximity sensor: near");
            } else {
                //far
                Log.i(TAG, "Proximity sensor: far");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
