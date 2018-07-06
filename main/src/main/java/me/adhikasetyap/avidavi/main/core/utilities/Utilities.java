package me.adhikasetyap.avidavi.main.core.utilities;

import android.content.Intent;
import android.hardware.Sensor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseArray;

import me.adhikasetyap.avidavi.main.core.AvidaviApplication;

public class Utilities {
    private static final String TAG = Utilities.class.getName();

    public static final String ACTION_SENSOR_BROADCAST = TAG + ".SENSOR_BROADCAST";
    public static final String EXTRA_SENSOR_VALUE = TAG + ".SENSOR_VALUE";
    public static final String EXTRA_SENSOR_TYPE = TAG + ".SENSOR_TYPE";

    private static final SparseArray<String> sensorName;

    static {
        sensorName = new SparseArray<>();
        sensorName.put(Sensor.TYPE_PROXIMITY, "proximity sensor");
    }

    public static void broadcastSensorData(int sensorType, Float value) {
        Intent intent = new Intent(ACTION_SENSOR_BROADCAST);
        Log.i(TAG, "ts: " + sensorType);
        Log.i(TAG, "as: " + sensorName.get(sensorType));

        intent.putExtra(EXTRA_SENSOR_TYPE, sensorName.get(sensorType, "sensor not identified"));
        intent.putExtra(EXTRA_SENSOR_VALUE, value);
        LocalBroadcastManager
                .getInstance(AvidaviApplication.getContext())
                .sendBroadcast(intent);
    }
}
