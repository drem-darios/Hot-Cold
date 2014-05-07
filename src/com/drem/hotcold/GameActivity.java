package com.drem.hotcold;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.RelativeLayout;

import com.drem.hotcold.beacon.Beacon;

public class GameActivity extends Activity {

	// Scan for 5 seconds
	private static final long SCAN_TIME = 5000;
	// Stop scanning for 2 seconds
	private static final long STOP_TIME = 2000;
	private BluetoothManager manager;
	private BluetoothAdapter adapter;
	private Beacon beacon;
	private boolean scanningEnabled = false;
	private TransitionDrawable transition;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundResource(R.drawable.fade);
		this.transition = (TransitionDrawable) layout.getBackground();
		transition.startTransition(10000);
		setContentView(layout);
		
		this.manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		adapter = manager.getAdapter();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// If bluetooth not supported or hasn't been enabled, prompt to enable
		if (adapter == null || !adapter.isEnabled()) {
			Intent bluetoothRequest = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(bluetoothRequest);
			finish();
			return;
		}
		scanningEnabled = true;
		startScanning();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		// Turn off scan to save on battery if paused
		adapter.stopLeScan(leScanCallback);
		scanningEnabled = false;
	}
	
	
	private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
               @Override
               public void run() {
            	   Log.i("LeScanCallback", "Beacon detected. Updating beacon list " + scanRecord.length);
            	   
            	   beacon = Beacon.createBeacon(device, rssi, scanRecord);
            	   transition.reverseTransition(2000);
               }
           });
       }
    };
    
	private void startScanning() {
		if (scanningEnabled) {
			boolean scanStarted = adapter.startLeScan(leScanCallback);
			if (scanStarted) {
				Log.d("startScanning", "Scanning started...");
			} else {
				Log.e("startScanning", "Problem starting scan...");
			}
			setProgressBarIndeterminateVisibility(true);
			handler.postDelayed(stopScanner, SCAN_TIME);	
		}
	}
	
	private void stopScanning() {
		Log.d("stopScanning", "...Scanning ended");
		adapter.stopLeScan(leScanCallback);
		setProgressBarIndeterminateVisibility(false);
		handler.postDelayed(startScanner, STOP_TIME);	
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handler =  new Handler() {
		
//		@Override
//		public void handleMessage(Message message) {
//			Beacon beacon = (Beacon) message.obj;
////            beacons.put(beacon.getAddress(), beacon);
//			beacon = (Beacon) message.obj;
//     	   
//		}
	};
	
	private Runnable startScanner = new Runnable() {
		@Override
		public void run() {
			startScanning();
		}
	};
	
	private Runnable stopScanner = new Runnable() {
		@Override
		public void run() {
			stopScanning();
		}
	};
}
