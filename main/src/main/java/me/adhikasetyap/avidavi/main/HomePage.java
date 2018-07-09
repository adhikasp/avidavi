package me.adhikasetyap.avidavi.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.adhikasetyap.avidavi.main.core.ModbusSlaveService;

import static me.adhikasetyap.avidavi.main.core.ModbusSlaveService.ACTION_CONNECT;
import static me.adhikasetyap.avidavi.main.core.ModbusSlaveService.ACTION_CONNECTED;
import static me.adhikasetyap.avidavi.main.core.ModbusSlaveService.EXTRA_SERVER_ADDRESS;
import static me.adhikasetyap.avidavi.main.core.ModbusSlaveService.EXTRA_SERVER_PORT;

public class HomePage extends Activity {

    private static final String TAG = HomePage.class.getName();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView connectionStatus = findViewById(R.id.connection_status);
            connectionStatus.setText("Connected");
            TextView serverAddress = findViewById(R.id.server_address);
            serverAddress.setText(
                    "IP Address: " + intent.getStringExtra(EXTRA_SERVER_ADDRESS)
                            + ":" + intent.getIntExtra(EXTRA_SERVER_PORT, 502));
            View connectedIcon = findViewById(R.id.connected_icon);
            connectedIcon.setBackground(getDrawable(R.drawable.status_connected));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        printSensorList(sensorManager);

        ListView sensorListView = findViewById(R.id.sensor_list);

        List<HashMap<String, String>> data = new ArrayList<>();
        HashMap<String, String> data1 = new HashMap<>();
        data1.put("sensor_name", "Proximity Sensor");
        data1.put("sensor_status", "Active");
        data.add(data1);

        String[] fromColumns = {"sensor_name", "sensor_status"};
        int[] toColumns = {R.id.sensor_name, R.id.sensor_status};

        ListAdapter listAdapter = new SimpleAdapter(
                this, data, R.layout.sensor_list_item_view, fromColumns, toColumns);

        sensorListView.setAdapter(listAdapter);

        // Start a ModbusSlaveService
        // https://stackoverflow.com/questions/2334955/start-a-service-from-activity
        Intent startModbusSlaveIntent = new Intent(this, ModbusSlaveService.class);
        startModbusSlaveIntent.setAction(ACTION_CONNECT);
        startModbusSlaveIntent.putExtra(EXTRA_SERVER_ADDRESS, "192.168.100.6");
        startModbusSlaveIntent.putExtra(EXTRA_SERVER_PORT, 5020);
        startService(startModbusSlaveIntent);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,
                new IntentFilter(ACTION_CONNECTED));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    public void printSensorList(SensorManager sensorManager) {
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
    }
}
