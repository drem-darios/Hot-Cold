package com.bluetooth.beacon.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteDbHelper extends SQLiteOpenHelper {
	  //Database Information
	  private static final String DATABASE_NAME 		= "web_beaconInfo.db";
	  private static final int DATABASE_VERSION 		= 1;
	  //Database Tables and Columns being used
	  public static final String TABLE_BEACONINFO 		= "beaconInfo";
	  public static final String TBL_COLUMN_BID			= "b_id";
	  public static final String TBL_COLUMN_UUID 		= "b_uuid";
	  public static final String TBL_COLUMN_BNAME 		= "b_bname"; 
	  public static final String TBL_COLUMN_BMINOR 		= "b_minor"; 
	  public static final String TBL_COLUMN_BMAJOR 		= "b_major"; 

	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "CREATE TABLE IF NOT EXISTS "
	      + TABLE_BEACONINFO + "(" + 
			TBL_COLUMN_BID + " integer primary key autoincrement, " + 
			TBL_COLUMN_UUID + " text not null," +
			TBL_COLUMN_BNAME + " text not null," +
			TBL_COLUMN_BMINOR + " text not null," +
			TBL_COLUMN_BMAJOR + " text not null);";

	  public SQLiteDbHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);	    
	  }

	  //onCreate Event for Database creation
	  @Override
	  public void onCreate(SQLiteDatabase database) {
		    database.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(SQLiteDbHelper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_BEACONINFO);
	    onCreate(db);
	  }

} 
