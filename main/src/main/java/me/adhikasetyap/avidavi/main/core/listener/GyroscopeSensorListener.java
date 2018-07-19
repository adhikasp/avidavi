package me.adhikasetyap.avidavi.main.core.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.OrientationFusedKalman;
import com.kircherelectronics.fsensor.util.rotation.RotationUtil;

import me.adhikasetyap.avidavi.main.core.utilities.Utilities;

public class GyroscopeSensorListener implements SensorEventListener {

    private String TAG = GyroscopeSensorListener.class.getName();

    private boolean hasAcceleration;
    private boolean hasMagnetic;

    private float[] fusedOrientation = new float[3];
    private int[] castedOrientation = new int[3];
    private float[] acceleration = new float[4];
    private float[] magnetic = new float[3];
    private float[] rotation = new float[3];

    private MeanFilter meanFilter;

    private OrientationFusedKalman orientationKalmanFusion;

    public GyroscopeSensorListener() {
        orientationKalmanFusion = new OrientationFusedKalman();
        orientationKalmanFusion.startFusion();
        meanFilter = new MeanFilter();
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
    }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
            hasAcceleration = true;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, magnetic, 0, event.values.length);
            hasMagnetic = true;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            // Android reuses events, so you probably want a copy
            System.arraycopy(event.values, 0, rotation, 0, event.values.length);

            if (!orientationKalmanFusion.isBaseOrientationSet()) {
                if (hasAcceleration && hasMagnetic) {
                    orientationKalmanFusion.setBaseOrientation(
                            RotationUtil.getOrientationQuaternionFromAccelerationMagnetic(
                                    acceleration, magnetic));
                }
            } else {
                fusedOrientation = orientationKalmanFusion.calculateFusedOrientation(
                        rotation, event.timestamp, acceleration, magnetic);
            }
            fusedOrientation = meanFilter.filter(fusedOrientation);
//            Log.i(TAG, "Orientation : " + Arrays.toString(fusedOrientation));

            for (int i = 0; i < fusedOrientation.length; i++) {
                castedOrientation[i] = (int) Utilities.radianToDegree(fusedOrientation[i]);
            }
            Utilities.broadcastSensorData(Sensor.TYPE_GYROSCOPE, castedOrientation);
        }
    }
}
