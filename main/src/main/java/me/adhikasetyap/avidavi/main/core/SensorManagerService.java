package me.adhikasetyap.avidavi.main.core;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;

import me.adhikasetyap.avidavi.main.core.listener.GyroscopeSensorListener;
import me.adhikasetyap.avidavi.main.core.listener.ProximitySensorListener;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_CONNECTED;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_SENSOR;
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

    private SensorEventListener proximitySensorListener;
    private SensorEventListener gyroscopeSensorListener;

    static {
        SUPPORTED_SENSOR = sparseArrayAsList(SENSOR_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO unbind all sensor listener
        sensorManager.unregisterListener(gyroscopeSensorListener);
        sensorManager.unregisterListener(proximitySensorListener);
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting from intent " + intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        proximitySensorListener = new ProximitySensorListener();
        gyroscopeSensorListener = new GyroscopeSensorListener();

        Boolean proximityActive = sensorManager.registerListener(
                proximitySensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        onSensorStartingSuccess(proximityActive, Sensor.TYPE_PROXIMITY);

        // Gyro need fusion of 3 different sensors
        boolean a = sensorManager.registerListener(
                gyroscopeSensorListener,
                accelerometerSensor,
                SensorManager.SENSOR_DELAY_FASTEST
        );
        boolean b = sensorManager.registerListener(
                gyroscopeSensorListener,
                gyroscopeSensor,
                SensorManager.SENSOR_DELAY_FASTEST
        );
        boolean c = sensorManager.registerListener(
                gyroscopeSensorListener,
                magnetometerSensor,
                SensorManager.SENSOR_DELAY_FASTEST
        );
        onSensorStartingSuccess(a && b && c, Sensor.TYPE_GYROSCOPE);

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
}
