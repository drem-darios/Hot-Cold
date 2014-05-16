package com.bluetooth.beacon.meter;

import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bluetooth.beacon.models.Beacon;
import com.bluetooth.beacon.models.BeaconManager;
import com.bluetooth.beacon.models.DistanceZone;
import com.bluetooth.beacon.persistence.BeaconsDBHandler;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GameActivity extends Activity implements OnItemClickListener {
	private BluetoothManager manager;
	private BluetoothAdapter adapter;
	private BeaconManager beaconManager;
	private Beacon beacon;
	private String[] beaconListKeys;
	private String[] beaconListValues;
	private String listeningBeaconId;
	private Beaconometer meter;
	private ListView beaconListView;
	private View selectedView;
	private int beaconFoundCounter = 0;
	private BeaconsDBHandler dbhandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beacon_scan_display);
		this.dbhandler = new BeaconsDBHandler(this);
		this.dbhandler.open();
		this.manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		adapter = manager.getAdapter();
		beaconManager = BeaconManager.getInstance(this);
		meter = (Beaconometer) findViewById(R.id.thermometer);

		initBeaconsFromDB();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, beaconListKeys);

		beaconListView = (ListView) findViewById(R.id.beaconListview);
		beaconListView.setAdapter(adapter);
		beaconListView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		// If bluetooth not supported or hasn't been enabled, prompt to enable
		if (adapter == null || !adapter.isEnabled()) {
			Log.w("GameActivity.onResume",
					"Unable to use Bluetooth Adapter. Sending request.");
			Intent bluetoothRequest = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivity(bluetoothRequest);
			finish();
			return;
		}
		Log.d("GameActivity.onResume", "Starting BLE scan.");
		beaconManager.startScanning();
		handler.post(updateBeaconRunner);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d("GameActivity.onPause", "Stopping BLE scan.");
		// Turn off scan to save on battery if paused
		beaconManager.stopScanning();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		if (!view.isEnabled()) {
			return;
		}
		if (listeningBeaconId == beaconListValues[position]) {
			if (beacon == null) {
				return;
			}
			if (beacon.getDistanceZone().equals(DistanceZone.MAX_HOT)) {

				view.setEnabled(false);
				beaconFoundCounter++;
				listeningBeaconId = null;
				if (beaconFoundCounter == beaconListValues.length) {
					// You've found all beacons!
					endGame();
				}
				return;
			} else {
				// if we are already listening to this id, just return
				return;
			}
		}

		if (selectedView == null) {
			selectedView = view;
		}
		if (selectedView.isEnabled()) {
			selectedView.setBackgroundColor(Color.TRANSPARENT);
		}
		listeningBeaconId = beaconListValues[position];
		beacon = null;
		meter.init();
		view.setBackgroundColor(Color.DKGRAY);
		selectedView = view;
	}

	public void endGame() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("You found all items!").setTitle("Yay!");
		AlertDialog dialog = builder.create();
		dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Horray!",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
		dialog.show();
	}
	
	private Runnable updateBeaconRunner = new Runnable() {
		@Override
		public void run() {
			updateBeacon();
		}
	};

	private static Handler handler = new Handler() {};

	/**
	 * Gets all stored beacons from local db to initialize the game
	 */
	private void initBeaconsFromDB() {
		Map<Integer, Beacon> beacons = dbhandler.getAllBeacons();

		beaconListKeys = new String[beacons.size()];
		beaconListValues = new String[beacons.size()];
		int index = 0;
		for (Beacon b : beacons.values()) {
			beaconListKeys[index] = b.getName();
			beaconListValues[index] = b.getUniqueName();
			index++;
		}
	}

	/**
	 * Gets the beacon that the user has selected to listen for. If the beacon
	 * was not found during the scan, null is returned.
	 */
	private Beacon getBeacon() {
		List<Beacon> beacons = beaconManager.getNearbyBeacons();
		if (beacons.isEmpty()) {
			Log.w("GameActivity.getBeacon", "No nearby beacons detected.");
			return null;
		}
		if (listeningBeaconId == null) {
			Log.w("GameActivity.getBeacon", "No beacon selected to listen to.");
			return null;
		}
		for (Beacon b : beacons) {
			// Find the beacon we care to listen for by unique name
			if (b.getUniqueName().equals(listeningBeaconId)) {
				return b;
			}
		}
		Log.i("GameActivity.getBeacon", "Hardcoded beacon not found.");
		return null;
	}

	/**
	 * Updates the current beacon with the latest data from the last scan
	 */
	private void updateBeacon() {
		Beacon currentBeacon = getBeacon();
		if (currentBeacon == null) {
			// we didn't see this beacon anymore so either wait to scan again,
			// or
			// throw a fit and say the beacon is too far...
			handler.postDelayed(updateBeaconRunner, 500);
			return;
		}

		beacon = currentBeacon;
		meter.changeTransition(beacon.getDistanceZone());
		if (beacon.getDistanceZone().equals(DistanceZone.MAX_HOT)) {
			// Make it so the row stands out for the user to select it
			selectedView.setBackgroundColor(Color.GREEN);
		} else {
			// Change the background to just the selected color
			selectedView.setBackgroundColor(Color.DKGRAY);
		}
		handler.postDelayed(updateBeaconRunner, 500);
	}
}