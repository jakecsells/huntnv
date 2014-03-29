package com.jakecsells.huntnv;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper{
	
    private final Context myContext;
    private static String DB_PATH = "/data/data/com.jakecsells.huntnv/databases/";
    private static String DB_NAME = "hunt-nv.sqlite";
    private static String TABLE_HUNTUNIT = "ndow_huntunits_2013";
    private static String TABLE_WAYPOINTS = "waypoints";
    private static String ID = "_id";
    private static String HUNTUNIT = "huntunit";
    private static String GEOMETRY = "geometry";
    private static String TITLE = "title";
    private static String LAT = "lat";
    private static String LNG = "lng";
    private static final String[] WAYPOINT_COLUMNS = {ID,TITLE,LAT,LNG};
    private SQLiteDatabase myDataBase; 
 
    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {
 
    	super(context, DB_NAME, null, 1);
        this.myContext = context;
    }	
 
  /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException{
 
    	boolean dbExist = checkDataBase();
 
    	if(dbExist){
    		//do nothing - database already exist
    	}else{
 
    		//By calling this method and empty database will be created into the default system path
               //of your application so we are gonna be able to overwrite that database with our database.
        	this.getReadableDatabase();
 
        	try {
 
    			copyDataBase();
 
    		} catch (IOException e) {
 
        		throw new Error("Error copying database");
 
        	}
    	}
 
    }
 
    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){
 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + DB_NAME;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    	}catch(SQLiteException e){
 
    		//database does't exist yet.
 
    	}
 
    	if(checkDB != null){
 
    		checkDB.close();
 
    	}
 
    	return checkDB != null ? true : false;
    }
 
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{
 
    	//Open your local db as the input stream
    	InputStream myInput = myContext.getAssets().open(DB_NAME);
 
    	// Path to the just created empty db
    	String outFileName = DB_PATH + DB_NAME;
 
    	//Open the empty db as the output stream
    	OutputStream myOutput = new FileOutputStream(outFileName);
 
    	//transfer bytes from the inputfile to the outputfile
    	byte[] buffer = new byte[1024];
    	int length;
    	while ((length = myInput.read(buffer))>0){
    		myOutput.write(buffer, 0, length);
    	}
 
    	//Close the streams
    	myOutput.flush();
    	myOutput.close();
    	myInput.close();
 
    }
 
    public void openDataBase() throws SQLException{
 
    	//Open the database
        String myPath = DB_PATH + DB_NAME;
    	myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
 
    }
 
    @Override
	public synchronized void close() {
 
    	    if(myDataBase != null)
    		    myDataBase.close();
 
    	    super.close();
 
	}

 
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older books table if existed
        db.execSQL("DROP TABLE IF EXISTS waypoints");
 
        // create fresh books table
        this.onCreate(db);
	}
 
        // Add your public helper methods to access and get content from the database.
       // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
       // to you to create adapters for your views.
	
	   public int getHuntUnitCount() {
	        String countQuery = "SELECT * FROM " + TABLE_HUNTUNIT;
	        SQLiteDatabase db = this.getReadableDatabase();
	        Cursor cursor = db.rawQuery(countQuery, null);
	        int count = cursor.getCount();
	        cursor.close();
	        // return count
	        return count;
	    }
	   
	   public String getGeometry(String huntunitnum) {
		   String geoQuery = "SELECT " + GEOMETRY + " FROM " + TABLE_HUNTUNIT + " WHERE " + HUNTUNIT + " = '" + huntunitnum + "'";
		   SQLiteDatabase db = this.getReadableDatabase();
		   Cursor cursor = db.rawQuery(geoQuery, null);
		   if(cursor.getCount() > 0) {
			   cursor.moveToFirst();
			   String content = cursor.getString(cursor.getColumnIndex(GEOMETRY));
			   cursor.close();
			   return content;
		   }
		   db.close();
		   return "";
	   }
	   
	   public int[] getAllWaypointIDs() {
		   String query = "SELECT " + ID + " FROM " + TABLE_WAYPOINTS;
		   SQLiteDatabase db = this.getReadableDatabase();
		   Cursor cursor = db.rawQuery(query, null);
		   if(cursor.getCount() > 0 ) {
			   int[] list = new int[cursor.getCount()];
			   int j = 0;
			   for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
				   list[j] = cursor.getInt(cursor.getColumnIndex(ID));
				   j++;
			   }
			   Log.i("WAYPOINTS", String.valueOf(j));
			   return list;
		   }
		   return null;
	   }
	   
	   public Waypoint getWaypoint(int id) {
		   
		    // get db
		    SQLiteDatabase db = this.getReadableDatabase();
		 
		    // query db
		    Cursor cursor = 
		            db.query(TABLE_WAYPOINTS, // a. table
		            WAYPOINT_COLUMNS, // b. column names
		            " _id = ?", // c. selections 
		            new String[] { String.valueOf(id) }, // d. selections args
		            null, // e. group by
		            null, // f. having
		            null, // g. order by
		            null); // h. limit
		    if (cursor != null)
		        cursor.moveToFirst();
		    // create new waypoint to return
		    Waypoint temp_waypoint = new Waypoint(
		    		Integer.parseInt(cursor.getString(0)),
		    		cursor.getString(1),
		    		Double.parseDouble(cursor.getString(2)),
		    		Double.parseDouble(cursor.getString(3)));

		    return temp_waypoint;
	   }
	   
	   public void saveWaypoint(String title, double lat, double lng) {
	        // get db
	        SQLiteDatabase db = this.getWritableDatabase();
	 
	        // create a new record to insert
	        ContentValues values = new ContentValues();
	        values.put(TITLE, title);
	        values.put(LAT, lat);
	        values.put(LNG, lng);
	 
	        db.insert(TABLE_WAYPOINTS, null, values);
	        db.close(); 

	   }
	   
	   public boolean deleteWaypoint(int id) {
		   SQLiteDatabase db = this.getWritableDatabase();

		   return db.delete(TABLE_WAYPOINTS, ID + "=" + id, null) > 0;
	   }

	@Override
	public void onCreate(SQLiteDatabase db) {
		
	}
}
