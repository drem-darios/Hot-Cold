package com.drem.hotcold.beacon;

public enum Pace {

	RUN(15),
	JOG(8),
	WALK(3),
	SIT(1);
	
	// average meters per second
	private double speed;
	
	Pace(double speed) {
		this.speed = speed;
	}
	
	/**
	 * Gets the pace based on the difference between the distance of the two beacons
	 * and the time interval at which the distances were taken. This is to take into
	 * consideration that the longer time interval the reading was taken from, the 
	 * larger distance they will cover.
	 * 
	 * @param beacon1 - The start beacon
	 * @param beacon2 - The end beacon
	 * @param timeInterval - The time at which the distance was covered in milliseconds
	 * @return - Pace at which the device is moving
	 */
	public static Pace getPace(Beacon beacon1, Beacon beacon2, int timeInterval) {
		double diff = Math.abs(beacon1.getDistance() - beacon2.getDistance());
		// find distance per millisecond
		double diffRatio = diff / timeInterval;
		// get distance per hour
		double pace = diffRatio * 3600;
		if (pace > Pace.JOG.speed) {
			return Pace.RUN;
		} else if (pace > Pace.WALK.speed) {
			return Pace.JOG;
		} else if (pace > Pace.SIT.speed) {
			return Pace.WALK;
		} else {
			return Pace.SIT;
		}
	}
}
