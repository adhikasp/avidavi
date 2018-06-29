package me.adhikasetyap.avidavi.main.core;

import android.app.IntentService;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.intelligt.modbus.jlibmodbus.Modbus;
import com.intelligt.modbus.jlibmodbus.data.ModbusHoldingRegisters;
import com.intelligt.modbus.jlibmodbus.exception.ModbusIOException;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlave;
import com.intelligt.modbus.jlibmodbus.slave.ModbusSlaveFactory;
import com.intelligt.modbus.jlibmodbus.tcp.TcpParameters;
import com.intelligt.modbus.jlibmodbus.utils.DataUtils;
import com.intelligt.modbus.jlibmodbus.utils.FrameEvent;
import com.intelligt.modbus.jlibmodbus.utils.FrameEventListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class ModbusSlaveService extends IntentService {

    private static final String TAG = IntentService.class.getName();
    private final IBinder binder = new LocalBinder();
    private ModbusSlave slave;

    public ModbusSlaveService() {
        super(ModbusSlaveService.class.getName());
    }

    public void main() {
        TcpParameters tcpParameters = new TcpParameters();
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);

        try {
            tcpParameters.setHost(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        tcpParameters.setKeepAlive(true);
        // Default Modbus port is 503, but it need priviledged access
        tcpParameters.setPort(5003);

        slave = ModbusSlaveFactory.createModbusSlaveTCP(tcpParameters);

        FrameEventListener listener = new FrameEventListener() {
            @Override
            public void frameSentEvent(FrameEvent event) {
            }

            @Override
            public void frameReceivedEvent(FrameEvent event) {
                // Activate sensor mode
                System.out.println("frame recv " + DataUtils.toAscii(event.getBytes()));
            }
        };

        ModbusHoldingRegisters holdingRegisters = new ModbusHoldingRegisters(1000);
        slave.getDataHolder().setHoldingRegisters(holdingRegisters);

        try {
            this.start();
        } catch (InterruptedException | ModbusIOException e) {
            e.printStackTrace();
        }
    }

    public void stop() throws ModbusIOException {
        if (slave.isListening()) {
            slave.shutdown();
        }
    }

    public void start() throws InterruptedException, ModbusIOException {
        if (!slave.isListening()) {
            slave.listen();
            Log.i(TAG, "Modbus Slave is listening");
        }

        if (slave.isListening()) {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    synchronized (slave) {
                        slave.notifyAll();
                    }
                }
            });
            synchronized (slave) {
                slave.wait();
            }
            slave.shutdown();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "Starting service from intent");
        this.main();
    }

    public class LocalBinder extends Binder {
        public ModbusSlaveService getService() {
            return ModbusSlaveService.this;
        }
    }
}
