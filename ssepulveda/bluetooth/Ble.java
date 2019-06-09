package com.ssepulveda.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.ssepulveda.commons.Tools;

import java.util.List;
import java.util.UUID;


class Ble  {
    private final String TAG = "Ble";
    private final int RETRY_COUNT = 3;
    private final int RETRY_SLEEP = 100;

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt gatt;

    Ble(Context context) {
        Log.d(TAG, "Constructor");
        this.context = context;
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
    }

    void startScan(final int scanTimeInSec, final BluetoothAdapter.LeScanCallback leScanCallback) {
        Log.d(TAG, "startscan");
        int time = scanTimeInSec * 1000;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               stopScan(leScanCallback);
            }
        }, time);
        bluetoothAdapter.startLeScan(leScanCallback);
    }

    void stopScan(final BluetoothAdapter.LeScanCallback leScanCallback) {
        bluetoothAdapter.stopLeScan(leScanCallback);
    }

    void connectGatt(BluetoothDevice device, BluetoothGattCallback gattCallback) {
        gatt = device.connectGatt(context, false, gattCallback);
    }

    void serviceDiscovery()
    {
        gatt.discoverServices();
    }

    void subscribeNotification(String serviceUUID, String characteristicUUID) {
        BluetoothGattService s = getService(serviceUUID);
        BluetoothGattCharacteristic c = getCharacteristic(s, characteristicUUID);
        enableNotification(c);
    }

    private BluetoothGattService getService(String uuid) {
        BluetoothGattService s = null;
        int retry = RETRY_COUNT;
        while (s == null && retry > 0) {
            s = gatt.getService(UUID.fromString(uuid));
            if (s == null) {
                retry--;
                Tools.sleep(RETRY_SLEEP);
            }
        }
        return s;
    }

    private BluetoothGattCharacteristic getCharacteristic(BluetoothGattService service,
                                                          String uuid) {
        BluetoothGattCharacteristic c = null;
        int retry = RETRY_COUNT;
        while (c == null && retry > 0)
        {
            c = service.getCharacteristic(UUID.fromString(uuid));
            if (c == null) {
                retry--;
                Tools.sleep(RETRY_SLEEP);
            }
        }
        return c;
    }

    private void enableNotification(BluetoothGattCharacteristic characteristic) {
        gatt.setCharacteristicNotification(characteristic, true);
        List<BluetoothGattDescriptor> descriptorList = characteristic.getDescriptors();
        for (BluetoothGattDescriptor descriptor : descriptorList)
        {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            gatt.writeDescriptor(descriptor);
        }
    }
}
