package me.adhikasetyap.avidavi.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

import me.adhikasetyap.avidavi.main.core.ModbusSlaveService;

public class HomePage extends AppCompatActivity {

    private static final String TAG = HomePage.class.getName();
    private ModbusSlaveService modbusSlaveService;
    private ServiceConnection modbusServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            // Initialize the service
            // https://stackoverflow.com/questions/20594936/communication-between-activity-and-service
            modbusSlaveService = ((ModbusSlaveService.LocalBinder) iBinder).getService();
            int result = modbusSlaveService.testServiceComm(5, 3);
            Log.i(TAG, "Hasil = " + result);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

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
        bindService(startModbusSlaveIntent, modbusServiceConnection, Context.BIND_AUTO_CREATE);
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
