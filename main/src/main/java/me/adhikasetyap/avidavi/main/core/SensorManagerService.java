package me.adhikasetyap.avidavi.main.core;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import me.adhikasetyap.avidavi.main.core.listener.ProximitySensorListener;

public class SensorManagerService extends Service {

    private final LocalBinder binder = new LocalBinder();
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private SensorEventListener proximitySensorListener;

    @Override
    public void onCreate() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proximitySensorListener = new ProximitySensorListener();

        sensorManager.registerListener(
                proximitySensorListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onDestroy() {
        // TODO unbind all sensor listener
        sensorManager.unregisterListener(proximitySensorListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        SensorManagerService getService() {
            return SensorManagerService.this;
        }
    }
}
