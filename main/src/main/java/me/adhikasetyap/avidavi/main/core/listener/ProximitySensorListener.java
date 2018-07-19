package me.adhikasetyap.avidavi.main.core.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import me.adhikasetyap.avidavi.main.core.utilities.Utilities;

public class ProximitySensorListener implements SensorEventListener {

    private final String TAG = this.getClass().getName();
    private static final int SENSOR_SENSITIVITY = 1; // TODO make this user configurable
    private int[] castedProximity = new int[1];

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            castedProximity[0] = (int) sensorEvent.values[0];
            Log.d(TAG, "Proximity sensor value: " + castedProximity[0]);
            Utilities.broadcastSensorData(Sensor.TYPE_PROXIMITY, castedProximity);
            if (sensorEvent.values[0] < SENSOR_SENSITIVITY) {
                //near
                Log.d(TAG, "Proximity sensor: near");
            } else {
                //far
                Log.d(TAG, "Proximity sensor: far");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
