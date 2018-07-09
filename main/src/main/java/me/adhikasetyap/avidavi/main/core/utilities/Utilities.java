package me.adhikasetyap.avidavi.main.core.utilities;

import android.content.Intent;
import android.hardware.Sensor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;

import me.adhikasetyap.avidavi.main.core.AvidaviApplication;

public class Utilities {
    private static final String TAG = Utilities.class.getName();

    public static final String CATEGORY_MODBUS = TAG + ".MODBUS";
    public static final String CATEGORY_SENSOR = TAG + ".SENSOR";

    public static final String ACTION_CONNECT = TAG + ".CONNECT";
    public static final String ACTION_CONNECTING = TAG + ".CONNECT";
    public static final String ACTION_CONNECTED = TAG + ".CONNECTED";
    public static final String ACTION_SENSOR_BROADCAST = TAG + ".SENSOR_BROADCAST";

    public static final String EXTRA_SENSOR_VALUE = TAG + ".SENSOR_VALUE";
    public static final String EXTRA_SENSOR_TYPE = TAG + ".SENSOR_TYPE";
    public static final String EXTRA_SENSOR_STATUS = TAG + ".SENSOR_STATUS";

    public static final SparseArray<String> SENSOR_NAME;

    static {
        SENSOR_NAME = new SparseArray<>();
        SENSOR_NAME.put(Sensor.TYPE_PROXIMITY, "Proximity Sensor");
    }

    public static <C> List<C> sparseArrayAsList(SparseArray<C> sparseArray) {
        if (sparseArray == null) return null;
        List<C> arrayList = new ArrayList<C>(sparseArray.size());
        for (int i = 0; i < sparseArray.size(); i++) {
            arrayList.add(sparseArray.valueAt(i));
        }
        return arrayList;
    }

    public static void broadcastSensorData(int sensorType, Float value) {
        Intent intent = new Intent(ACTION_SENSOR_BROADCAST);
        intent.putExtra(EXTRA_SENSOR_TYPE, SENSOR_NAME.get(sensorType, "sensor not identified"));
        intent.putExtra(EXTRA_SENSOR_VALUE, value);
        LocalBroadcastManager
                .getInstance(AvidaviApplication.getContext())
                .sendBroadcast(intent);
    }
}
