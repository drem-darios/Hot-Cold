package com.bluetooth.beacon.meter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
	    super.onStart();

	}
	
	public void playGame(View button) {
		Intent difficultySelectIntent = new Intent(getApplicationContext(), DifficultySelectActivity.class);
		startActivity(difficultySelectIntent);
	}
	
	public void createGame(View button) {
		
	}
	
	@Override 
	protected void onStop() { 
	    super.onStop();
	}
	
}
