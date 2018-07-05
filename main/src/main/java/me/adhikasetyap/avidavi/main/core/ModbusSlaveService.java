package me.adhikasetyap.avidavi.main.core;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import me.adhikasetyap.avidavi.main.core.utilities.Utilities;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_VALUE;

public class ModbusSlaveService extends IntentService {

    private static final String TAG = ModbusSlaveService.class.getName();

    public static final String ACTION_CONNECT = TAG + ".CONNECT";
    public static final String ACTION_CONNECTED = TAG + ".CONNECTED";

    public static final String EXTRA_SERVER_ADDRESS = TAG + ".SERVER_ADDRESS";
    public static final String EXTRA_SERVER_PORT = TAG + ".SERVER_PORT";

    private ModbusClient client;
    private SensorManagerService sensorManagerService;
    private Boolean listening = false;

    private ServiceConnection sensorManagerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sensorManagerService = ((SensorManagerService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    public ModbusSlaveService() {
        super(ModbusSlaveService.class.getName());
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (listening) {
                int value = (int) intent.getFloatExtra(EXTRA_SENSOR_VALUE, 0.0f);
                new Thread(() -> {
                    try {
                        // TODO change startingAddress based on sensor type
                        // TODO make address user configurable
                        // TODO refactor this to outside function
                        client.WriteSingleRegister(1, value);
                    } catch (ModbusException | IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    };

    public void startConnection(String serverAddress, int serverPort) {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,
                new IntentFilter(Utilities.ACTION_SENSOR_BROADCAST));

        client = new ModbusClient(serverAddress, serverPort);
        client.addSendDataChangedListener(() -> Log.i(TAG, "Send Data fired"));

        try {
            client.Connect();
            listening = true;
            Intent successfullyConnect = new Intent(ACTION_CONNECTED);
            successfullyConnect.putExtra(EXTRA_SERVER_ADDRESS, serverAddress);
            successfullyConnect.putExtra(EXTRA_SERVER_PORT, serverPort);
            LocalBroadcastManager.getInstance(this).sendBroadcast(successfullyConnect);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        bindService(
                new Intent(ModbusSlaveService.this, SensorManagerService.class),
                sensorManagerConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listening) {
            try {
                client.Disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            listening = false;
        }
        if (sensorManagerService != null) {
            sensorManagerService.unbindService(sensorManagerConnection);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "Starting service from intent");
        if (intent != null && Objects.equals(intent.getAction(), ACTION_CONNECT)) {
            this.startConnection(intent.getStringExtra(EXTRA_SERVER_ADDRESS),
                    intent.getIntExtra(EXTRA_SERVER_PORT, 502));
        }
    }
}
