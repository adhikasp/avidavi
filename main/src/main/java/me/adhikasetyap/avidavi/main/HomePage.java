package me.adhikasetyap.avidavi.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import me.adhikasetyap.avidavi.main.core.ModbusSlaveService;

public class HomePage extends Activity {

    private static final String TAG = HomePage.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        TextView textView = findViewById(R.id.sensor_list);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        printSensorList(sensorManager, textView);

        // Start a ModbusSlaveService
        // https://stackoverflow.com/questions/2334955/start-a-service-from-activity
        Intent startModbusSlaveIntent = new Intent(this, ModbusSlaveService.class);
        startService(startModbusSlaveIntent);
    }

    public void printSensorList(SensorManager sensorManager, TextView textView) {
        Log.i(TAG, "List of sensor available in this device:");
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

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
//        textView.setText(sensorText);
    }
}
