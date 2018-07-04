package me.adhikasetyap.avidavi.main.core;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

import de.re.easymodbus.exceptions.ModbusException;
import de.re.easymodbus.modbusclient.ModbusClient;

public class ModbusSlaveService extends IntentService {

    private static final String TAG = ModbusSlaveService.class.getName();
    private ModbusClient client;
    private SensorManagerService sensorManagerService;

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

    public void main() {
        bindService(
                new Intent(ModbusSlaveService.this, SensorManagerService.class),
                sensorManagerConnection, BIND_AUTO_CREATE);

        client = new ModbusClient("192.168.100.6", 5020);

        client.addSendDataChangedListener(() -> Log.i(TAG, "Send Data fired"));

        try {
            client.Connect();
            client.WriteSingleCoil(1, true);
            client.WriteSingleRegister(1, 1234);
        } catch (ModbusException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "Starting service from intent");
        this.main();
    }
}
