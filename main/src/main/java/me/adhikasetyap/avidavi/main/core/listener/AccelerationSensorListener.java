package me.adhikasetyap.avidavi.main.core.listener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

import com.kircherelectronics.fsensor.filter.averaging.MeanFilter;
import com.kircherelectronics.fsensor.filter.gyroscope.fusion.kalman.OrientationFusedKalman;
import com.kircherelectronics.fsensor.linearacceleration.LinearAccelerationFusion;

import java.util.Arrays;

import me.adhikasetyap.avidavi.main.core.utilities.Utilities;

/**
 * This implementation of Fused acceleration sensor with Kalman Filter
 * is taken from AccelerationExplorer here https://github.com/KalebKE/AccelerationExplorer
 * with some modification.
 */
public class AccelerationSensorListener implements SensorEventListener {

    private static final String TAG = AccelerationSensorListener.class.getName();

    private float[] magnetic = new float[3];
    private float[] acceleration = new float[4];
    private float[] rotation = new float[3];

    private MeanFilter meanFilter;
    private OrientationFusedKalman orientationFusedKalman;
    private LinearAccelerationFusion linearAccelerationFusion;
    private float startTime = 0;
    private int count = 0;

    public AccelerationSensorListener() {
        meanFilter = new MeanFilter();
        orientationFusedKalman = new OrientationFusedKalman();
        linearAccelerationFusion = new LinearAccelerationFusion(orientationFusedKalman);
        orientationFusedKalman.startFusion();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, acceleration, 0, event.values.length);
            processAcceleration(linearAccelerationFusion.filter(acceleration));
            Utilities.broadcastSensorData(
                    Sensor.TYPE_ACCELEROMETER,
                    convertAccelerationToDM(acceleration)
            );
            Log.i(TAG, "Acceleration : " + Arrays.toString(acceleration));
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(magnetic, 0, this.magnetic, 0, magnetic.length);
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            System.arraycopy(rotation, 0, this.rotation, 0, rotation.length);
        }
    }

    private void processAcceleration(float[] acceleration) {
        acceleration = meanFilter.filter(acceleration);
        System.arraycopy(acceleration, 0, this.acceleration, 0, acceleration.length);
        this.acceleration[3] = calculateSensorFrequency();
    }

    private float calculateSensorFrequency() {
        // Initialize the start time.
        if (startTime == 0) {
            startTime = System.nanoTime();
        }

        long timestamp = System.nanoTime();

        // Find the sample period (between updates) and convert from
        // nanoseconds to seconds. Note that the sensor delivery rates can
        // individually vary by a relatively large time frame, so we use an
        // averaging technique with the number of sensor updates to
        // determine the delivery rate.
        float hz = (count++ / ((timestamp - startTime) / 1000000000.0f));

        return hz;
    }

    private int[] convertAccelerationToDM(float[] accelM) {
        int[] accelDM = new int[3];
        accelDM[0] = (int) (accelM[0] * 10) + 32767;
        accelDM[1] = (int) (accelM[1] * 10) + 32767;
        accelDM[2] = (int) (accelM[2] * 10) + 32767;
        return accelDM;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
