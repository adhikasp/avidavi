package me.adhikasetyap.avidavi.main.core.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class GyroscopeSensorListener implements SensorEventListener {

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        float[] mGravity = new float[0];
        float[] mGeomagnetic = new float[0];

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;

        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];

            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                Log.i("MY_DEBUG", "Reading device rotation angle: azimuth %f, pitch %f, roll %f");
//                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
//                pitch = orientation[1];
//                roll = orientation[2];
            }
        }
    }
}
