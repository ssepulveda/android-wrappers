package com.ssepulveda.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;

public class BleService extends Service {
    private static final String TAG = "BleService";

    private final IBinder binder = new LocalBinder();
    private Ble ble;
    private ArrayList<BluetoothDevice> foundDevices;

    public class ACTIONS {
        private static final String b = "com.ssepulveda.bluetooth.bleService.ACTION_";
        public static final String READY = b + "READY";
        public static final String DEVICE_FOUND = b + "DEVICE_FOUND";
        public static final String GATT_CONNECTED = b + "GATT_CONNECTED";
        public static final String GATT_DISCONNECTED = b + "GATT_DISCONNECTED";
        public static final String GATT_NOTIFICATION = b + "GATT_NOTIFICATION";
        public static final String GATT_SERVICES_DISCOVERED = b + "GATT_SERVICES_DISCOVERED";
    }

    public class SCAN_VALUES {
        public static final String ADDRESS = "address";
    }

    public class GATT_NOTIFICATION_VALUES {
        public static final String UUID = "uuid";
        public static final String VALUE = "value";
    }

    public static IntentFilter bleServiceIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTIONS.READY);
        intentFilter.addAction(ACTIONS.GATT_NOTIFICATION);
        intentFilter.addAction(ACTIONS.GATT_DISCONNECTED);
        return intentFilter;
    }

    public static IntentFilter bleServiceScanIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTIONS.DEVICE_FOUND);
        intentFilter.addAction(ACTIONS.GATT_CONNECTED);
        intentFilter.addAction(ACTIONS.GATT_DISCONNECTED);
        intentFilter.addAction(ACTIONS.GATT_SERVICES_DISCOVERED);
        return intentFilter;
    }

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    public BluetoothDevice getDevice(String mac) {
        for (BluetoothDevice device : foundDevices) {
            if (device.getAddress().equals(mac))
            {
                return device;
            }
        }
        return null;
    }

    public void scan() {
        startScan(3);
    }

    public void connect(BluetoothDevice device) {
        ble.stopScan(leScanCallback);
        ble.connectGatt(device, gattCallback);
    }

    public void discover() {
        ble.serviceDiscovery();
    }

    public void subscribe(String service, String characteristic) {
        ble.subscribeNotification(service, characteristic);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        ble = new Ble(getApplicationContext());
        broadcastUpdate(ACTIONS.READY);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private void startScan(int scanTimeOutInSecs) {
        foundDevices = new ArrayList<>();
        ble.startScan(scanTimeOutInSecs, leScanCallback);
    }

    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            if (device.getName() != null && !foundDevices.contains(device)) {
                                foundDevices.add(device);
                                broadcastUpdate(ACTIONS.DEVICE_FOUND, device);
                            }
                        }
                    };
                    r.run();
                }
            };

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            Log.d(TAG, "onPhyUpdate");
            super.onPhyUpdate(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
            Log.d(TAG, "onPhyRead");
            super.onPhyRead(gatt, txPhy, rxPhy, status);
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.e(TAG, "onConnectionStateChange " + status + ", " + newState);
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState){
                case (BluetoothProfile.STATE_CONNECTED):
                    broadcastUpdate(ACTIONS.GATT_CONNECTED);
                    break;
                case (BluetoothProfile.STATE_DISCONNECTED):
                    broadcastUpdate(ACTIONS.GATT_DISCONNECTED);
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onServicesDiscovered");
            super.onServicesDiscovered(gatt, status);
            broadcastUpdate(ACTIONS.GATT_SERVICES_DISCOVERED);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead");
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite");
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "onCharacteristicChanged");
            super.onCharacteristicChanged(gatt, characteristic);
            broadcastUpdate(ACTIONS.GATT_NOTIFICATION, characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorRead");
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.d(TAG, "onDescriptorWrite");
            super.onDescriptorWrite(gatt, descriptor, status);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            Log.d(TAG, "onReliableWriteCompleted");
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            Log.d(TAG, "onReadRemoteRssi");
            super.onReadRemoteRssi(gatt, rssi, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.d(TAG, "onMtuChanged");
            super.onMtuChanged(gatt, mtu, status);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, BluetoothDevice device) {
        final Intent intent = new Intent(action);
        intent.putExtra(SCAN_VALUES.ADDRESS, device.getAddress());
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        intent.putExtra(GATT_NOTIFICATION_VALUES.UUID, characteristic.getUuid().toString());
        intent.putExtra(GATT_NOTIFICATION_VALUES.VALUE, characteristic.getValue());
        sendBroadcast(intent);
    }
}
