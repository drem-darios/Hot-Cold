package com.bluetooth.beacon.meter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bluetooth.beacon.persistence.BeaconsDBHandler;

public class MainActivity extends Activity {
	private BeaconsDBHandler mDbHandler;
	private Button playButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.playButton = (Button) findViewById(R.id.play_button);
		mDbHandler = new BeaconsDBHandler(this);
		mDbHandler.open();
		
		if (mDbHandler.getBeaconsCount() == 0) {
			playButton.setEnabled(false);
		} else {
			playButton.setEnabled(true);
		}
	}

	@Override
	protected void onStart() {
	    super.onStart();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if (mDbHandler.getBeaconsCount() == 0) {
			playButton.setEnabled(false);
		} else {
			playButton.setEnabled(true);
		}
	}
	
	public void playGame(View button) {
		Intent difficultySelectIntent = new Intent(getApplicationContext(), DifficultySelectActivity.class);
		startActivity(difficultySelectIntent);
	}
	
	public void createGame(View button) {
		Intent newGame = new Intent(getApplicationContext(), AddBeaconActivity.class);
		startActivity(newGame);		
	}
	
	@Override 
	protected void onStop() { 
	    super.onStop();
	}
	
}
