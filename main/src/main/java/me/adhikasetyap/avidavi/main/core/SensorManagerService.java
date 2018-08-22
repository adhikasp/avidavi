package me.adhikasetyap.avidavi.main.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.Objects;

import me.adhikasetyap.avidavi.main.R;
import me.adhikasetyap.avidavi.main.core.listener.AccelerationSensorListener;
import me.adhikasetyap.avidavi.main.core.listener.GyroscopeSensorListener;
import me.adhikasetyap.avidavi.main.core.listener.ProximitySensorListener;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_CONNECTED;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_DISCONNECT;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_SENSOR_OFF;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_SENSOR_ON;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_SENSOR;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_NAME;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_STATUS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_TYPE;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.SENSOR_NAME;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.sparseArrayAsList;

public class SensorManagerService extends Service {

    private static final String TAG = SensorManagerService.class.getName();

    public static final List<String> SUPPORTED_SENSOR;

    private final LocalBinder binder = new LocalBinder();

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private Sensor gyroscopeSensor;
    private Sensor magnetometerSensor;
    private Sensor proximitySensor;

    private SensorEventListener accelerometerSensorListener;
    private SensorEventListener proximitySensorListener;
    private SensorEventListener gyroscopeSensorListener;

    IntentFilter sensorSwitchFilter;

    static {
        SUPPORTED_SENSOR = sparseArrayAsList(SENSOR_NAME);
    }

    private SharedPreferences sharedPreferences;


    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO unbind all sensor listener
        sensorManager.unregisterListener(accelerometerSensorListener);
        sensorManager.unregisterListener(gyroscopeSensorListener);
        sensorManager.unregisterListener(proximitySensorListener);
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting from intent " + intent);

        return super.onStartCommand(intent, flags, startId);
    }

    private void startAccelerometerSensor() {
        boolean a = sensorManager.registerListener(
                accelerometerSensorListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                getSensorBroadcastDelayPref()*1000 // to microsecond
        );
        boolean b = sensorManager.registerListener(
                accelerometerSensorListener,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                getSensorBroadcastDelayPref()*1000 // to microsecond
        );
        boolean c = sensorManager.registerListener(
                accelerometerSensorListener,
                magnetometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                getSensorBroadcastDelayPref()*1000 // to microsecond
        );
        onSensorStartingSuccess(a && b && c, Sensor.TYPE_ACCELEROMETER);
    }

    private void startGyroscopeSensor() {
        // Gyro need fusion of 3 different sensors
        boolean a = sensorManager.registerListener(
                gyroscopeSensorListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                getSensorBroadcastDelayPref()*1000 // to microsecond
        );
        boolean b = sensorManager.registerListener(
                gyroscopeSensorListener,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                getSensorBroadcastDelayPref()*1000 // to microsecond
        );
        boolean c = sensorManager.registerListener(
                gyroscopeSensorListener,
                magnetometerSensor,
                SensorManager.SENSOR_DELAY_NORMAL,
                getSensorBroadcastDelayPref()*1000 // to microsecond
        );
        onSensorStartingSuccess(a && b && c, Sensor.TYPE_GYROSCOPE);
    }

    private void startProximitySensor() {
        Boolean a = sensorManager.registerListener(
                proximitySensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        onSensorStartingSuccess(a, Sensor.TYPE_PROXIMITY);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        accelerometerSensorListener = new AccelerationSensorListener(getSensorBroadcastDelayPref());
        proximitySensorListener = new ProximitySensorListener();
        gyroscopeSensorListener = new GyroscopeSensorListener(getSensorBroadcastDelayPref());

        startProximitySensor();
        startGyroscopeSensor();
        startAccelerometerSensor();

        sensorSwitchFilter = new IntentFilter();
        sensorSwitchFilter.addAction(ACTION_SENSOR_ON);
        sensorSwitchFilter.addAction(ACTION_SENSOR_OFF);
        LocalBroadcastManager.getInstance(this).registerReceiver(onSensorOffRequest, sensorSwitchFilter);

        return binder;
    }

    public class LocalBinder extends Binder {
        SensorManagerService getService() {
            return SensorManagerService.this;
        }
    }

    private void onSensorStartingSuccess(Boolean success, int sensorType) {
        String sensorName = SENSOR_NAME.get(sensorType);
        if (!success) {
            Log.e(TAG, "Failing to start sensor " + sensorName);
            return;
        }

        Log.i(TAG, "Starting sensor " + sensorName);

        Intent sensorSuccess = new Intent(ACTION_CONNECTED);
        sensorSuccess.addCategory(CATEGORY_SENSOR);
        sensorSuccess.putExtra(EXTRA_SENSOR_TYPE, sensorName);
        sensorSuccess.putExtra(EXTRA_SENSOR_STATUS, "Active");
        LocalBroadcastManager.getInstance(this).sendBroadcast(sensorSuccess);
    }


    private void onSensorStopSuccess(int sensorType) {
        String sensorName = SENSOR_NAME.get(sensorType);

        Log.i(TAG, "Stopping sensor " + sensorName);

        Intent sensorSuccess = new Intent(ACTION_DISCONNECT);
        sensorSuccess.addCategory(CATEGORY_SENSOR);
        sensorSuccess.putExtra(EXTRA_SENSOR_TYPE, sensorName);
        sensorSuccess.putExtra(EXTRA_SENSOR_STATUS, "Inactive");
        LocalBroadcastManager.getInstance(this).sendBroadcast(sensorSuccess);
    }

    private BroadcastReceiver onSensorOffRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Objects.equals(intent.getAction(), ACTION_SENSOR_OFF)) {
                return;
            }
            String sensorName = intent.getStringExtra(EXTRA_SENSOR_NAME);
            switch (sensorName) {
                case "Accelerometer Sensor":
                    sensorManager.unregisterListener(accelerometerSensorListener);
                    onSensorStopSuccess(Sensor.TYPE_ACCELEROMETER);
                    break;
                case "Proximity Sensor":
                    sensorManager.unregisterListener(gyroscopeSensorListener);
                    onSensorStopSuccess(Sensor.TYPE_PROXIMITY);
                    break;
                case "Gyroscope Sensor":
                    sensorManager.unregisterListener(proximitySensorListener);
                    onSensorStopSuccess(Sensor.TYPE_GYROSCOPE);
                    break;
            }

            LocalBroadcastManager
                    .getInstance(SensorManagerService.this)
                    .unregisterReceiver(onSensorOffRequest);
            LocalBroadcastManager
                    .getInstance(SensorManagerService.this)
                    .registerReceiver(onSensorOnRequest, sensorSwitchFilter);
        }
    };

    private BroadcastReceiver onSensorOnRequest = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!Objects.equals(intent.getAction(), ACTION_SENSOR_ON)) {
                return;
            }
            String sensorName = intent.getStringExtra(EXTRA_SENSOR_NAME);
            switch (sensorName) {
                case "Accelerometer Sensor":
                    startAccelerometerSensor();
                    break;
                case "Proximity Sensor":
                    startProximitySensor();
                    break;
                case "Gyroscope Sensor":
                    startGyroscopeSensor();
                    break;
            }

            LocalBroadcastManager
                    .getInstance(SensorManagerService.this)
                    .unregisterReceiver(onSensorOnRequest);
            LocalBroadcastManager
                    .getInstance(SensorManagerService.this)
                    .registerReceiver(onSensorOffRequest, sensorSwitchFilter);
        }
    };

    private int getSensorBroadcastDelayPref() {
        return Integer.valueOf(sharedPreferences.getString(
                "pref_key_sensor_broadcast_delay",
                getString(R.string.pref_default_accelerometer_address)));
    }

}
