package me.adhikasetyap.avidavi.main.core;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

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

public class ModbusSlaveService extends Service {

    private final IBinder binder = new LocalBinder();

    private ModbusSlave slave;

    public int testServiceComm(int a, int b) {
        return a + b;
    }

    public void main() throws InterruptedException, ModbusIOException {
        TcpParameters tcpParameters = new TcpParameters();
        Modbus.setLogLevel(Modbus.LogLevel.LEVEL_DEBUG);

        try {
            tcpParameters.setHost(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        tcpParameters.setKeepAlive(true);
        tcpParameters.setPort(Modbus.TCP_PORT);

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

        this.start();
    }

    public void stop() throws ModbusIOException {
        if (slave.isListening()) {
            slave.shutdown();
        }
    }

    public void start() throws InterruptedException, ModbusIOException {
        if (!slave.isListening()) {
            slave.listen();
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

    public class LocalBinder extends Binder {
        public ModbusSlaveService getService() {
            return ModbusSlaveService.this;
        }
    }
}
