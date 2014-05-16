package com.bluetooth.beacon.meter;

import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.bluetooth.beacon.models.Beacon;
import com.bluetooth.beacon.models.BeaconManager;
import com.bluetooth.beacon.persistence.BeaconsDBHandler;

public class AddBeaconActivity extends ListActivity {
	// Scan for 5 seconds
	private static final long SCAN_TIME = 5000;
	// Stop scanning for just over 1 second
	private static final long STOP_TIME = 1100;
	private static final String TAG = "AddBeaconActivity";
	
	private boolean scanningEnabled = false;
	private BeaconsDBHandler mDbHandler;
	private BluetoothAdapter adapter;
	private BeaconManager beaconManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.beacon_list);
		
		beaconManager = BeaconManager.getInstance(this);
		mDbHandler = new BeaconsDBHandler(this);
		Log.d(TAG,"Db Handler open");
		mDbHandler.open();
		// Get a reference to the Bluetooth Manager.
		BluetoothManager manager =  (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		this.adapter = manager.getAdapter();
		scanningEnabled = true;
		startScanning();
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
		scanningEnabled = false;
		setProgressBarIndeterminateVisibility(false);
		beaconManager.stopScanning();
	}

	@Override
    public void onListItemClick(ListView parent, View v, int position, long id)
    {
        Beacon beacon = (Beacon)getListView().getItemAtPosition(position);
        
        Integer tMinor =  beacon.getMinor();
        String tUuid = beacon.getUuid();
		Integer tMajor = beacon.getMajor();
		
		boolean beaconExists = mDbHandler.checkBeacon(tMinor);
        if (beaconExists) {
        	Toast.makeText(this, "Beacon " + mDbHandler.getBeaconName(tMinor) + " already exists!" , Toast.LENGTH_SHORT).show();
        } else {
    		Log.d(TAG, "Item selected: " + beacon.getAddress());
            Log.d(TAG, "Item getMajor: " + beacon.getMajor());
            Log.d(TAG, "Item getMinor: " + beacon.getMinor());
            Log.d(TAG, "Item getuuid: " + beacon.getUuid());
            
            createBeacon(this, tUuid, tMinor, tMajor);
    			
        }
    }

	/**
	 * Scans for available beacons and sets them on the list
	 */
	private void startScanning() {
		if (scanningEnabled) {
			Log.d(TAG, "Scanning started...");
			beaconManager.startScanning();
			setProgressBarIndeterminateVisibility(true);
			runOnUiThread(new Runnable() {
	          @Override
	          public void run() {
	      		List<Beacon> beacons = beaconManager.getNearbyBeacons();
	      		ArrayAdapter<Beacon> listAdapter = new ArrayAdapter<Beacon>(getApplicationContext(),
	                   android.R.layout.simple_list_item_1, beacons);
	           setListAdapter(listAdapter);
	           listAdapter.notifyDataSetChanged();
	          }});
			handler.postDelayed(stopScanner, SCAN_TIME);	
		}
	}
	
	/**
	 * Turns off scanning and hides the progress spinner
	 */
	private void stopScanning() {
		Log.d(TAG, "...Scanning ended");
		beaconManager.stopScanning();
		setProgressBarIndeterminateVisibility(false);
		handler.postDelayed(startScanner, STOP_TIME);	
	}

	/**
	 * Creates a beacon from the data passed in. This requires a context to prompt the user for a beacon name.
	 * If no name is provided, the beacon will not be added.
	 */
	private void createBeacon(final Context context, final String tUuid, final Integer tMinor, final Integer tMajor) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Name me!");
		alert.setMessage("Enter a beacon name");
		
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  	String beaconName = input.getText().toString();
		  	boolean isNew = mDbHandler.createBeaconEntry(beaconName, tUuid, tMinor, tMajor);

			if (isNew) {
				Toast.makeText(context, "Beacon " + beaconName + " added!" , Toast.LENGTH_SHORT).show();
			} 
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
			  // do nothing
		  }
		});

		alert.show();
	}

	private static Handler handler =  new Handler() {};
	
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
