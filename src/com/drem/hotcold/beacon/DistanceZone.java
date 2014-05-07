package com.drem.hotcold.beacon;

public enum DistanceZone {

	IMMEDIATE(0.5),
	NEAR(2.0),
	FAR(30), 
	UNKNOWN(50);
	
	private double max;

	DistanceZone(double max) {
		this.max = max;
	}

	public double getMax() {
		return max;
	}
	
	public DistanceZone getDistanceZone(double distance) {
		if (distance < 0) {
			return UNKNOWN;
		}
		if (distance < IMMEDIATE.max) {
			return IMMEDIATE;
		}
		if (distance < NEAR.max) {
			return NEAR;
		} 
		if (distance < FAR.max) {
			return FAR;
		}
		
		return UNKNOWN;
	}
	
	/**
	 * The direction in which 
	 * @author Drem
	 *
	 */
	public static enum Direction {
		FORWARD,
		BACKWARD;
	}
}
