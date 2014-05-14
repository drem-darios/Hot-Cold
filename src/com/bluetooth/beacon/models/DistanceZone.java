package com.bluetooth.beacon.models;

public enum DistanceZone {

	MAX_HOT(1.0),
	VERY_HOT(2.0),
	HOT(3.0),
	COLD(5.0),
	VERY_COLD(8.0),
	MAX_COLD(13.0);
	
	private double max;

	DistanceZone(double max) {
		this.max = max;
	}

	public double getMax() {
		return max;
	}
	
	public static DistanceZone getDistanceZone(double distance) {
		if (distance <= MAX_HOT.max) {
			return MAX_HOT;
		}
		if (distance <= VERY_HOT.max) {
			return VERY_HOT;
		}
		if (distance <= HOT.max) {
			return HOT;
		} 
		if (distance <= COLD.max) {
			return COLD;
		}
		if (distance <= VERY_COLD.max) {
			return VERY_COLD;
		}
		
		return MAX_COLD;
	}
}
