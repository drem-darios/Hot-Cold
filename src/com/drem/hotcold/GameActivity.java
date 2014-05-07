package com.drem.hotcold;

import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;

import com.drem.hotcold.beacon.Beacon;
import com.drem.hotcold.beacon.BeaconManager;

public class GameActivity extends Activity {
	private static final String HARDCODED_UUID = "61687109-905F-4436-91F8-E602F514C96D:3:1114";
	private BluetoothManager manager;
	private BluetoothAdapter adapter;
	private BeaconManager beaconManager;
	private Beacon beacon;
	private TransitionDrawable transition;
	private boolean transitionReversed = false;
	private int transitionTime = 10000;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RelativeLayout layout = new RelativeLayout(this);
		layout.setBackgroundResource(R.drawable.fade);
		this.transition = (TransitionDrawable) layout.getBackground();
		transition.setCrossFadeEnabled(true);
		setContentView(layout);
		
		this.manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		adapter = manager.getAdapter();
		
		beaconManager = BeaconManager.getInstance(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// If bluetooth not supported or hasn't been enabled, prompt to enable
		if (adapter == null || !adapter.isEnabled()) {
			Log.w("GameActivity.onResume", "Unable to use Bluetooth Adapter. Sending request.");
			Intent bluetoothRequest = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(bluetoothRequest);
			finish();
			return;
		}
		Log.d("GameActivity.onResume", "Starting BLE scan.");
		beaconManager.startScanning();
		handler.post(updateBeaconRunner);
	}
	
	private Beacon getBeacon() {
		List<Beacon> beacons = beaconManager.getNearbyBeacons();
		if (beacons.isEmpty()) {
			Log.i("GameActivity.getBeacon", "No nearby beacons detected.");
			return null;
		}
		for (Beacon b : beacons) {
			if (b.getUniqueName().equals(HARDCODED_UUID)) {
				return b;
			}
		}
		Log.i("GameActivity.getBeacon", "Hardcoded beacon not found.");
		return null;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("GameActivity.onPause", "Stopping BLE scan.");
		// Turn off scan to save on battery if paused
		beaconManager.stopScanning();
	}
	
	private Runnable updateBeaconRunner = new Runnable() {
		@Override
		public void run() {
			updateBeacon();
		}
	};
	
	private static Handler handler = new Handler() {};

	private void updateBeacon() {
		Beacon currentBeacon = getBeacon();
		if (currentBeacon == null) {
			// we didn't see this beacon anymore so either wait to scan again, or
			// throw a fit and say the beacon is too far...

			handler.postDelayed(updateBeaconRunner, 1000);
			return;
		}
		if (beacon == null) {
			beacon = currentBeacon;
		}
		
		if (beacon.compareTo(currentBeacon) > 0) {
			// This means we are getting closer
			// set the direction to walking towards.
			// set the pace that we are moving also
			// set the distance zone
			if (transitionReversed) {
				transition.reverseTransition(transitionTime);
				transitionReversed = false;
			}
			
		} else if (beacon.compareTo(currentBeacon) == 0) {
			// set the pace to sit. This is probably going to be rare.	
			
		} else {
			// This means we are getting farther away
			// set the direction to walking away.
			// set the pace that we are moving also
			// set the distance zone
			// reverse transition only if it hasn't been reversed already
			if (!transitionReversed) {
				transition.reverseTransition(transitionTime);
			}
			transitionReversed = true;
		}
		// Change the beacon when we reach a different zone.
		if (beacon.getDistanceZone().compareTo(currentBeacon.getDistanceZone()) != 0) {
			beacon = currentBeacon;	
		}
		handler.postDelayed(updateBeaconRunner, 500);
	}
}
