package me.adhikasetyap.avidavi.main.core.utilities;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import me.adhikasetyap.avidavi.main.core.AvidaviApplication;

public class Utilities {
    private static final String TAG = Utilities.class.getName();

    public static final String ACTION_SENSOR_BROADCAST = TAG + ".SENSOR_BROADCAST";
    public static final String EXTRA_SENSOR_VALUE = TAG + ".SENSOR_VALUE";
    public static final String EXTRA_SENSOR_TYPE = TAG + ".SENSOR_TYPE";

    public static void broadcastSensorData(int sensorType, Float value) {
        Intent intent = new Intent(ACTION_SENSOR_BROADCAST);
        intent.putExtra(EXTRA_SENSOR_TYPE, sensorType);
        intent.putExtra(EXTRA_SENSOR_VALUE, value);
        LocalBroadcastManager
                .getInstance(AvidaviApplication.getContext())
                .sendBroadcast(intent);
    }
}
