package me.adhikasetyap.avidavi.main;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class HomePage extends AppCompatActivity {

    private static final String TAG = HomePage.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        TextView textView = findViewById(R.id.sensor_list);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        printSensorList(sensorManager, textView);
    }

    public void printSensorList(SensorManager sensorManager, TextView textView) {
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

        Log.i(TAG, "Tes");
        StringBuilder sensorText = new StringBuilder();
        for (Sensor sensor : sensorList) {
            sensorText
                    .append(sensor.getName())
                    .append(" :: ")
                    .append(sensor.getVendor())
                    .append(" :: ")
                    .append(sensor.getPower())
                    .append(System.getProperty("line.separator"));
        }
        Log.i(TAG, sensorText.toString());
        textView.setText(sensorText);
    }
}
