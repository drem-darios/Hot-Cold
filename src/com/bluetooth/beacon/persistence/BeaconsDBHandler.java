package com.bluetooth.beacon.persistence;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.bluetooth.beacon.models.Beacon;

//DB Handler for Beacon Model
public class BeaconsDBHandler {

	  private static final String TAG	= "Hot_COLD";
	  //Table Columns for Database Query
	  private String[] allColumns 		= { 
										  SQLiteDbHelper.TBL_COLUMN_BID,
										  SQLiteDbHelper.TBL_COLUMN_UUID,
										  SQLiteDbHelper.TBL_COLUMN_BNAME,
										  SQLiteDbHelper.TBL_COLUMN_BMAJOR,
										  SQLiteDbHelper.TBL_COLUMN_BMINOR};
	  // Database fields
	  private SQLiteDatabase database;
	  private SQLiteDbHelper dbHelper;
	  
	  public BeaconsDBHandler(Context context) {
	    dbHelper = new SQLiteDbHelper(context);
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }

	  //Insert User Entry based on the params
	  public boolean createBeaconEntry(String bname,String uuid, Integer bminor,Integer bmajor) {
	    ContentValues values = new ContentValues();
	    Log.d(TAG,"-createBeaconEntry with uname:" + bname + " and uuId:" + uuid);
	    
	    //See if the user Exists
	    Boolean newBeacon = checkBeacon(bminor);
	    
	    if(!newBeacon) {
	    	Log.d(TAG,"Creating Beacon");
		    values.put(SQLiteDbHelper.TBL_COLUMN_UUID, uuid);
		    values.put(SQLiteDbHelper.TBL_COLUMN_BNAME, bname);
		    values.put(SQLiteDbHelper.TBL_COLUMN_BMINOR, bminor);
		    values.put(SQLiteDbHelper.TBL_COLUMN_BMAJOR, bmajor);
		    Log.d(TAG,"Creating Beacon Entry Data" + uuid + " - " + bname + " - " + bminor + " " + bmajor);
		    
		    long insertId = database.insert(SQLiteDbHelper.TABLE_BEACONINFO, null,values);
		    Cursor cursor = database.query(SQLiteDbHelper.TABLE_BEACONINFO,
					        allColumns, SQLiteDbHelper.TBL_COLUMN_BID + " = " + insertId, null,
					        null, null, null);
		    if(cursor.getCount() > 0) {
			    cursor.moveToFirst();
		    }
		    cursor.close();
		    return true;
	    } else {
	    	Log.d(TAG,"Checking Beacon");
		    Cursor cursor = database.query(SQLiteDbHelper.TABLE_BEACONINFO,
			        allColumns, SQLiteDbHelper.TBL_COLUMN_BMINOR + " = " + bminor, null,
			        null, null, null);
		    
		    if(cursor.getCount() > 0) {
		    	Log.d(TAG,"Beacon Exists: " + uuid);
			    cursor.moveToFirst();
		    }
		    cursor.close();
		    return false;
		}
	  }
	  
	  //Get User based on the userId
	  public Boolean checkBeacon(Integer tMinor) {
	  	Log.d(TAG,"getUser:SEE If beacon exists" + tMinor);
	  	
	    Cursor cursor = database.query(SQLiteDbHelper.TABLE_BEACONINFO,
		        allColumns, SQLiteDbHelper.TBL_COLUMN_BMINOR + " = " + tMinor, null,
		        null, null, null);
	    
	    if(cursor.getCount() > 0) {
	    	Log.d(TAG,"getUser:Cursor not null: user exists:");
		    cursor.moveToFirst();		    
		    cursor.close();
		    return true;		  
	    }
	    else
	    	return false;
	  }
	  
	//Get User based on the userId
	  public String getBeaconName(Integer tMinor) {
	  	Log.d(TAG,"getUser:SEE If beacon exists" + tMinor);
	  	
	    Cursor cursor = database.query(SQLiteDbHelper.TABLE_BEACONINFO,
		        allColumns, SQLiteDbHelper.TBL_COLUMN_BMINOR + " = " + tMinor, null,
		        null, null, null);
	    
	    if(cursor.getCount() > 0) {
	    	Log.d(TAG,"getUser:Cursor not null: user exists:");
		    cursor.moveToFirst();		    
		    String beaconName = cursor.getString(2);
		    cursor.close();
		    return beaconName;		  
	    }
	    else
	    	return null;
	  }

	  //Get All the Beacons
	  @SuppressLint("UseSparseArrays")
	  public HashMap<Integer, Beacon> getAllBeacons() {
	    HashMap<Integer, Beacon> beacons = new HashMap<Integer, Beacon>();
	    Integer tCounter = 0;

	    //Get All the Beacons based on the database-id
	    Cursor cursor = database.query(SQLiteDbHelper.TABLE_BEACONINFO,
	    				allColumns, null, null, null, null, SQLiteDbHelper.TBL_COLUMN_BID + " Asc");
	    
		Log.d(TAG,"getAllBeacons:Getting Beacons now");
		if( cursor.getCount() > 0) {
		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		    	String tbUUID = cursor.getString(1);
		    	String tbName = cursor.getString(2);
		    	Integer tBMajor = cursor.getInt(3);
		    	Integer tBMinor = cursor.getInt(4);
		    	Beacon tBeacon = new Beacon(tbName, tbUUID, tBMajor, tBMinor);
		    	beacons.put(tCounter,tBeacon);
		    	cursor.moveToNext();
		    	tCounter++;
		    }	
		}

	    // Make sure to close the cursor
	    cursor.close();
	    return beacons;
	  }
	
	  //Get the current Beacons Count
	  public Integer getBeaconsCount() {
		Integer nBeacons = 0;
		String[]  nCountArg = {"Count(*)"};
		
		Cursor cursor = database.query(SQLiteDbHelper.TABLE_BEACONINFO,
						nCountArg, null, null, null, null, null);
		
	    if(cursor.getCount() > 0) {
		    cursor.moveToFirst();
	    	nBeacons 		  = cursor.getInt(0);
	    }
	    cursor.close();
		return nBeacons;
	  }

	  //Update the BeaconsList based on the uname and uId
	  public void updateBeaconsList(String uname,Integer uuid) {
	    ContentValues values = new ContentValues();

	    Log.d(TAG,"Inside updateBeaconsList with uname:" + uname + "uId:" + uuid);
	    //See if the user Exists
	    Boolean newUser = checkBeacon(uuid);
	    if(newUser == null) {
		    values.put(SQLiteDbHelper.TBL_COLUMN_UUID, uuid);
		    values.put(SQLiteDbHelper.TBL_COLUMN_BNAME, uname);
		    
		    long insertId = database.insert(SQLiteDbHelper.TABLE_BEACONINFO, null,values);
		    Cursor cursor = database.query(SQLiteDbHelper.TABLE_BEACONINFO,
					        allColumns, SQLiteDbHelper.TBL_COLUMN_BID + " = " + insertId, null,
					        null, null, null);
		    if(cursor.getCount() > 0) {
			    cursor.moveToFirst();
		    }
		    cursor.close();
	    }
	    //else update user
	    else {
		    Log.d(TAG,"User Already exists, nothing to do");
		}
	    return;
	  }
} 