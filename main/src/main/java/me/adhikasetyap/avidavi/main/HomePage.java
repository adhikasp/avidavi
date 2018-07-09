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
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import me.adhikasetyap.avidavi.main.core.ModbusSlaveService;

import static me.adhikasetyap.avidavi.main.core.ModbusSlaveService.EXTRA_SERVER_ADDRESS;
import static me.adhikasetyap.avidavi.main.core.ModbusSlaveService.EXTRA_SERVER_PORT;
import static me.adhikasetyap.avidavi.main.core.SensorManagerService.SUPPORTED_SENSOR;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_CONNECT;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_CONNECTED;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_CONNECTING;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_MODBUS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_SENSOR;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_STATUS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_TYPE;

public class HomePage extends Activity {

    private static final String TAG = HomePage.class.getName();

    private SimpleAdapter sensorListAdapter;
    private List<HashMap<String, String>> sensorList;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Receive broadcast intent " + intent.toString());
            if (Objects.equals(intent.getAction(), ACTION_CONNECTED) &&
                    intent.getCategories().contains(CATEGORY_MODBUS)) {
                TextView connectionStatus = findViewById(R.id.connection_status);
                connectionStatus.setText("Connected");
                TextView serverAddress = findViewById(R.id.server_address);
                serverAddress.setText(
                        "IP Address: " + intent.getStringExtra(EXTRA_SERVER_ADDRESS)
                                + ":" + intent.getIntExtra(EXTRA_SERVER_PORT, 502));
                View connectedIcon = findViewById(R.id.connected_icon);
                connectedIcon.setBackground(getDrawable(R.drawable.status_connected));
            } else if (Objects.equals(intent.getAction(), ACTION_CONNECTED) &&
                    intent.getCategories().contains(CATEGORY_SENSOR)) {
                String sensorType = intent.getStringExtra(EXTRA_SENSOR_TYPE);
                String sensorStatus = intent.getStringExtra(EXTRA_SENSOR_STATUS);
                for (HashMap<String, String> sensorItem : sensorList) {
                    if (sensorItem.get(EXTRA_SENSOR_TYPE).equals(sensorType)) {
                        // TODO refactor this code, func to update a SensorListItemView
                        sensorItem.put(EXTRA_SENSOR_STATUS, sensorStatus);
                        sensorListAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        printSensorList(sensorManager);

        // Init sensor list
        ListView sensorListView = findViewById(R.id.sensor_list);

        sensorList = new ArrayList<>();
        for (String sensorName : SUPPORTED_SENSOR) {
            HashMap<String, String> sensorItem = new HashMap<>();
            sensorItem.put(EXTRA_SENSOR_TYPE, sensorName);
            sensorItem.put(EXTRA_SENSOR_STATUS, "Inactive");
            // TODO add sensor icon
            sensorList.add(sensorItem);
        }

        // TODO add sensor icon
        String[] fromColumns = {EXTRA_SENSOR_TYPE, EXTRA_SENSOR_STATUS};
        int[] toColumns = {R.id.sensor_name, R.id.sensor_status};

        sensorListAdapter = new SimpleAdapter(
                this, sensorList, R.layout.sensor_list_item_view, fromColumns, toColumns);
        sensorListView.setAdapter(sensorListAdapter);

        // Start a ModbusSlaveService
        // https://stackoverflow.com/questions/2334955/start-a-service-from-activity
        Intent startModbusSlaveIntent = new Intent(this, ModbusSlaveService.class);
        startModbusSlaveIntent.setAction(ACTION_CONNECT);
        startModbusSlaveIntent.putExtra(EXTRA_SERVER_ADDRESS, "192.168.100.6");
        startModbusSlaveIntent.putExtra(EXTRA_SERVER_PORT, 5020);
        startService(startModbusSlaveIntent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECTED);
        filter.addAction(ACTION_CONNECT);
        filter.addAction(ACTION_CONNECTING);
        filter.addCategory(CATEGORY_MODBUS);
        filter.addCategory(CATEGORY_SENSOR);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, filter);
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
