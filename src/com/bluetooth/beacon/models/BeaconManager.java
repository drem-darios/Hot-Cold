package com.bluetooth.beacon.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BeaconManager {
	private static int delayTime = 0;
	private static int scanTime = 1100;
	
	private static Handler handler = new Handler(){};
	private BluetoothAdapter adapter;
	private Map<String, Beacon> beacons = new HashMap<String, Beacon>();
	
	private BeaconManager(BluetoothAdapter adapter) {
		this.adapter = adapter;
	}

	public synchronized List<Beacon> getNearbyBeacons() {
		return new ArrayList<Beacon>(beacons.values());
	}
	
	public synchronized Beacon getClosestBeacon() {
		// Ignore anything past max cold distance
		double minDistance = DistanceZone.MAX_COLD.getMax();
		Beacon closest = null;
		for (Beacon beacon : beacons.values()) {
			if (beacon.getDistance() < minDistance) {
				minDistance = beacon.getDistance();
				closest = beacon;
			}
		}
		return closest;
	}
	
	public synchronized void startScanning() {
		boolean scanStarted = adapter.startLeScan(leScanCallback);
		if (scanStarted) {
			Log.d("startScanning", "Scanning started...");
		} else {
			Log.e("startScanning", "Problem starting scan...");
		}
		
		handler.postDelayed(delayScanner, scanTime);
	}

	public synchronized void stopScanning() {
		adapter.stopLeScan(leScanCallback);
		handler.removeCallbacks(startScanner);
		handler.removeCallbacks(delayScanner);
	}
	
    public static synchronized BeaconManager getInstance(Context context) {
    	BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter adapter = manager.getAdapter();
    	return new BeaconManager(adapter);
    }
	
	synchronized void delayScanning() {
		Log.d("stopScanning", "...Scanning ended");
		adapter.stopLeScan(leScanCallback);
		handler.postDelayed(startScanner, delayTime);	
	}
	
	public void setScanTime(int duration) {
		scanTime = duration;
	}
	
	public void setDelayTime(int delay) {
		delayTime = delay;
	}
	
	private Runnable startScanner = new Runnable() {
		@Override
		public void run() {
			startScanning();
		}
	};
	
	private Runnable delayScanner = new Runnable() {
		@Override
		public void run() {
			delayScanning();
		}
	};
	
	private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                final byte[] scanRecord) {
        	
        	Beacon beacon = Beacon.createBeacon(device, rssi, scanRecord);
        	beacons.put(beacon.getUniqueName(), beacon);
        	Log.i("LeScanCallback", "Beacon detected. Updating beacon map: " + beacon.getUniqueName());
       }
    };
}
