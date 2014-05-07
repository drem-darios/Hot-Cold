package com.drem.hotcold;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class DifficultySelectActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.difficulty_select);
	}
	
	public void playEasy(View button) {
		// Select closest beacon, but not too close or that won't be fun.
		Intent easyGame = new Intent(getApplicationContext(), GameActivity.class);
		startActivity(easyGame);
	}
	
	public void playMedium(View button) {
		// Show arrow to the closest unknown beacon. This req GPS
	}
	
	public void playHard(View button) {
		// Internally select the closest unknown beacon, but don't give any clues
		// unit within reading range. 
	}
}
