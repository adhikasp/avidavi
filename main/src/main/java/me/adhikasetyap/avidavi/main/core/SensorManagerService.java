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

import me.adhikasetyap.avidavi.main.core.listener.ProximitySensorListener;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_CONNECTED;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_SENSOR;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_STATUS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_TYPE;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.SENSOR_NAME;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.sparseArrayAsList;

public class SensorManagerService extends Service {

    public static final List<String> SUPPORTED_SENSOR;

    private final LocalBinder binder = new LocalBinder();
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximitySensorListener;
    private static final String TAG = SensorManagerService.class.getName();

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
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proximitySensorListener = new ProximitySensorListener();

        Boolean proximityActive = sensorManager.registerListener(
                proximitySensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
        onSensorStartingSuccess(proximityActive, Sensor.TYPE_PROXIMITY);

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
            Log.i(TAG, "Failing to start sensor " + sensorName);
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
