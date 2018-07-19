package me.adhikasetyap.avidavi.main.core;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Objects;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;
import me.adhikasetyap.avidavi.main.R;
import me.adhikasetyap.avidavi.main.core.utilities.Utilities;

import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_CONNECTED;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_DISCONNECT;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.ACTION_SENSOR_BROADCAST;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.CATEGORY_MODBUS;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_NAME;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_TYPE;
import static me.adhikasetyap.avidavi.main.core.utilities.Utilities.EXTRA_SENSOR_VALUE;

public class ModbusSlaveService extends Service {

    private static final String TAG = ModbusSlaveService.class.getName();

    public static final String EXTRA_SERVER_ADDRESS = TAG + ".SERVER_ADDRESS";
    public static final String EXTRA_SERVER_PORT = TAG + ".SERVER_PORT";

    SharedPreferences sharedPreferences;

    private ModbusClient client;
    private SensorManagerService sensorManagerService;
    private Boolean listening = false;
    private HandlerThread handlerThread;
    private Handler handler;

    private ServiceConnection sensorManagerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            sensorManagerService = ((SensorManagerService.LocalBinder) iBinder).getService();
            Log.i(TAG, sensorManagerService.toString());
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            sensorManagerService = null;
        }
    };
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (listening) {
                String sensorName = intent.getStringExtra(EXTRA_SENSOR_NAME);
                int[] sensorValues = intent.getIntArrayExtra(EXTRA_SENSOR_VALUE);
                int sentAddress = getSensorSentAddressPreference(intent.getIntExtra(EXTRA_SENSOR_TYPE, 0));
                handler.post(() -> {
                    try {
                        // TODO change startingAddress based on sensor type
                        // TODO make address user configurable
                        // TODO refactor this to outside function
                        Log.i(TAG, "Sending data to " + client.getipAddress());
                        Log.i(TAG, "Sensor : " + sensorName);
                        Log.i(TAG, "Value  : " + Arrays.toString(sensorValues));
                        for (int i = 0; i < sensorValues.length; i++) {
                            client.WriteSingleRegister(sentAddress + i, sensorValues[i]);
                        }
                    } catch (SocketTimeoutException e) {
                        restartConnection(false, 1, 1);
                    } catch (ModbusException | IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    };

    public ModbusSlaveService() {
        super();
    }

    public void startConnection(String serverAddress, int serverPort) {
        LocalBroadcastManager.getInstance(this).registerReceiver(
                broadcastReceiver,
                new IntentFilter(ACTION_SENSOR_BROADCAST));

        client = new ModbusClient(serverAddress, serverPort);
        client.addSendDataChangedListener(() -> Log.i(TAG, "Send Data fired"));

        handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        handler.post(() -> {
            try {
                client.Connect();
                client.setConnectionTimeout(5000);
                Log.i(TAG, "Modbus slave is listening.");
                listening = true;

                Intent successfullyConnect = new Intent(ACTION_CONNECTED);
                successfullyConnect.addCategory(CATEGORY_MODBUS);
                successfullyConnect.putExtra(EXTRA_SERVER_ADDRESS, serverAddress);
                successfullyConnect.putExtra(EXTRA_SERVER_PORT, serverPort);
                LocalBroadcastManager.getInstance(this).sendBroadcast(successfullyConnect);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void restartConnection(Boolean hasResendData, int resendAddress, int resendData) {
        try {
            if (listening) {
                client.Disconnect();
            }
            client.Connect();
            client.setConnectionTimeout(5000);
            if (hasResendData) {
                client.WriteSingleRegister(resendAddress, resendData);
            }
        } catch (IOException | ModbusException e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() {
        try {
            if (listening) {
                client.Disconnect();
            }
            Intent disconnected = new Intent(ACTION_DISCONNECT);
            disconnected.addCategory(CATEGORY_MODBUS);
            LocalBroadcastManager.getInstance(this).sendBroadcast(disconnected);
            listening = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");
        bindService(
                new Intent(ModbusSlaveService.this, SensorManagerService.class),
                sensorManagerConnection, BIND_AUTO_CREATE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (listening) {
            handlerThread.quitSafely();
            LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
            try {
                client.Disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            listening = false;
        }
        if (sensorManagerService != null) {
            sensorManagerService.unbindService(sensorManagerConnection);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        Log.i(TAG, "Starting service from intent");
        if (intent != null && Objects.equals(intent.getAction(), Utilities.ACTION_CONNECT) && !listening) {
            startConnection(intent.getStringExtra(EXTRA_SERVER_ADDRESS),
                    intent.getIntExtra(EXTRA_SERVER_PORT, 502));
        } else if (intent != null && Objects.equals(intent.getAction(), ACTION_DISCONNECT) && listening) {
            stopConnection();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private int getSensorSentAddressPreference(int sensorType) {
        switch (sensorType) {
            case Sensor.TYPE_GYROSCOPE:
                return Integer.valueOf(sharedPreferences.getString(
                        "pref_key_gyro_address",
                        getString(R.string.pref_default_gyroscope_address))
                );
            case Sensor.TYPE_PROXIMITY:
                return Integer.valueOf(sharedPreferences.getString(
                        "pref_key_proximity_address",
                        getString(R.string.pref_default_proximity_address))
                );
            default:
                return 0;
        }
    }
}
