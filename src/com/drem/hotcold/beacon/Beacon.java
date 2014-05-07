package com.drem.hotcold.beacon;

import java.io.Serializable;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class Beacon implements Comparable<Beacon>, Serializable {

	/**
	 * @see Serializable
	 */
	private static final long serialVersionUID = -4301021243531981072L;
	private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
	private int signalPower, major, minor;
	private int txPower;
	private String name, address, uuid;
	private DistanceZone zone;
	
	public static Beacon createBeacon(BluetoothDevice device, int rssi, byte[] scanRecord) {
		Beacon beacon = parseByteArray(scanRecord);
		beacon.signalPower = rssi;
		if (device != null) {
            beacon.address = device.getAddress();
            beacon.name = device.getName();
        }
        return beacon;
	}
	
	static Beacon parseByteArray(byte[] scanRecord) {
		/*
		02 # Number of bytes that follow in first AD structure
		01 # Flags AD type
		1A # Flags value 0x1A = 000011010  
		   bit 0 (OFF) LE Limited Discoverable Mode
		   bit 1 (ON) LE General Discoverable Mode
		   bit 2 (OFF) BR/EDR Not Supported
		   bit 3 (ON) Simultaneous LE and BR/EDR to Same Device Capable (controller)
		   bit 4 (ON) Simultaneous LE and BR/EDR to Same Device Capable (Host)
		1A # Number of bytes that follow in second (and last) AD structure
		FF # Manufacturer specific data AD type
		4C 00 # Company identifier code (0x004C == Apple)
		02 # Byte 0 of iBeacon advertisement indicator
		15 # Byte 1 of iBeacon advertisement indicator
		e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon proximity uuid
		00 00 # major 
		00 00 # minor 
		c5 # The 2's complement of the calibrated Tx Power
		*/
		Beacon beacon = new Beacon();
		beacon.txPower = 0;
		
		int firstADStructure = (scanRecord[0] & 0xff);
		int flagADType = (scanRecord[1] & 0xff);
		int flagsValue = (scanRecord[2] & 0xff);
		int secondADStructure = (scanRecord[3] & 0xff);
		int manuData = (scanRecord[4] & 0xff);
		int companyIdCode = (scanRecord[5] & 0xff) * 0x100 + (scanRecord[6] & 0xff);
		int firstAdvInd = (scanRecord[7] & 0xff);
		int secondAdvInd = (scanRecord[8] & 0xff);
		
		beacon.uuid = getUUID(scanRecord);
		beacon.major = (scanRecord[25] & 0xff) * 0x100 + (scanRecord[26] & 0xff);
   		beacon.minor = (scanRecord[27] & 0xff) * 0x100 + (scanRecord[28] & 0xff);
   		beacon.txPower = (int)scanRecord[29];
   		Log.d("Beacon.parseByteArray", firstADStructure + " " + flagADType + " " + flagsValue + " " + 
   				secondADStructure + " " + manuData + " " + companyIdCode + " " + firstAdvInd + " " + 
   					secondAdvInd + " " + beacon.uuid + " " + beacon.major + " " + beacon.minor + " " + beacon.txPower);
   		return beacon;
	}

	static String getUUID(byte[] scanRecord) {

		byte[] uuidBytes = new byte[16];
		System.arraycopy(scanRecord, 9, uuidBytes, 0, 16); 
		String hexString = bytesToHex(uuidBytes);
		StringBuilder sb = new StringBuilder();
		sb.append(hexString.substring(0,8));
		sb.append("-");
		sb.append(hexString.substring(8,12));
		sb.append("-");
		sb.append(hexString.substring(12,16));
		sb.append("-");
		sb.append(hexString.substring(16,20));
		sb.append("-");
		sb.append(hexString.substring(20,32));
		
		return sb.toString();
	}

	public String getName() {
		if (name == null) {
			return "Unknown";
		}
		return name;
	}
	
	public String getAddress() {
		return address;
	}
	
	public int getSignal() {
		return signalPower;
	}
	
	public int getTxPower() {
		return txPower;
	}
	
	public int getSignalPower() {
		return signalPower;
	}

	public int getMajor() {
		return major;
	}

	public int getMinor() {
		return minor;
	}

	public String getUuid() {
		return uuid.toString();
	}
	
	public String getUniqueName() {
		return uuid.toString() +":"+ major +":"+ minor;
	}
	
	public DistanceZone getDistanceZone() {
		if (zone == null) {
			zone = DistanceZone.getDistanceZone(getDistance());
		}
		
		return zone;
	}
	
	@Override
	public String toString() {
		return getName() + " ID: " + getUniqueName() + " Distance: " + String.format("%.2f", getDistance());
	}
	
	private static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}

	@Override
	public int compareTo(Beacon beacon) {
		return beacon.getDistance() < getDistance() ? -1 : beacon.getDistance() == getDistance() ? 0 : 1;
	}
	
	/**
	 * Gets the last known distance this beacon was from the device by 
	 * calculating the linear ratio between the signal power and the 
	 * calibrated transmitter power. 
	 * @return
	 */
	protected double getDistance() {
		if (signalPower == 0) {
			return -1.0;
		}

		double ratio = signalPower*1.0/txPower;
		if (ratio < 1.0) {
			return Math.pow(ratio,10);
		}
		else {
			// known best fit curve for ratio
			double accuracy = (0.89976)*Math.pow(ratio,7.7095) + 0.111;	
			return accuracy;
		}
	}
}
