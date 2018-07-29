package me.adhikasetyap.avidavi.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_DISCONNECT;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_MODBUS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_SENSOR;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_STATUS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_TYPE;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.PREFERENCE_SERVER_ADDRESS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.PREFERENCE_SERVER_PORT;

public class HomePageActivity extends AppCompatActivity {

    private static final String TAG = HomePageActivity.class.getName();

    private SharedPreferences sharedPreferences;
    private SimpleAdapter sensorListAdapter;
    private List<HashMap<String, String>> sensorList;

    private boolean listening = false;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO separate the if-else as different receiver
            Log.i(TAG, "Receive broadcast intent " + intent.toString());
            if (Objects.equals(intent.getAction(), ACTION_CONNECTED) &&
                    intent.getCategories().contains(CATEGORY_MODBUS)) {
                listening = true;
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
            } else if (Objects.equals(intent.getAction(), ACTION_DISCONNECT) &&
                    intent.getCategories().contains(CATEGORY_MODBUS)) {
                listening = false;
                TextView connectionStatus = findViewById(R.id.connection_status);
                connectionStatus.setText("Disconnected");
                TextView serverAddress = findViewById(R.id.server_address);
                serverAddress.setText("IP Address: -");
                View connectedIcon = findViewById(R.id.connected_icon);
                connectedIcon.setBackground(getDrawable(R.drawable.status_disconnected));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        Toolbar myToolbar = findViewById(R.id.app_toolbar);
        setSupportActionBar(myToolbar);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        printSensorList(sensorManager);

        //

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

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

        connectToServer();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECTED);
        filter.addAction(ACTION_CONNECT);
        filter.addAction(ACTION_CONNECTING);
        filter.addAction(ACTION_DISCONNECT);
        filter.addCategory(CATEGORY_MODBUS);
        filter.addCategory(CATEGORY_SENSOR);
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.default_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int selection = menuItem.getItemId();
        if (selection == R.id.action_settings) {
            startActivity(new Intent(this, SettingActivity.class));
            return true;
        } else if (selection == R.id.action_connect) {
            connectOrDisconnect();
            return true;
        } else if (selection == R.id.action_modbus_memory) {
            startActivity(new Intent(this, ModbusMemoryActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void connectOrDisconnect() {
        if (listening) {
            disconnectFromServer();
        } else {
            connectToServer();
        }
    }

    private void connectToServer() {
        // Start a ModbusSlaveService
        // https://stackoverflow.com/questions/2334955/start-a-service-from-activity
        // TODO reconnect if first attempt failed.
        int port = Integer.parseInt(sharedPreferences.getString(PREFERENCE_SERVER_PORT, "5020"));

        Intent startModbusSlaveIntent = new Intent(this, ModbusSlaveService.class);
        startModbusSlaveIntent.setAction(ACTION_CONNECT);
        startModbusSlaveIntent.putExtra(
                EXTRA_SERVER_ADDRESS,
                sharedPreferences.getString(PREFERENCE_SERVER_ADDRESS, "192.168.100.6"));
        startModbusSlaveIntent.putExtra(
                EXTRA_SERVER_PORT,
                port);
        Log.i(TAG, "connectToServer");
        startService(startModbusSlaveIntent);
    }

    private void disconnectFromServer() {
        Intent stopModbusSlaveIntent = new Intent(this, ModbusSlaveService.class);
        stopModbusSlaveIntent.setAction(ACTION_DISCONNECT);

        Log.i(TAG, "disconnectFromServer");
        startService(stopModbusSlaveIntent);
        // TODO show progress
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
