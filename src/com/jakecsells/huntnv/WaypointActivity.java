package com.jakecsells.huntnv;

import com.jakecsells.huntnv.R;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

public class WaypointActivity extends Activity {

	private double WEST_BOUND = -121.0;
	private double NORTH_BOUND = 43.0;
	private double EAST_BOUND = -113.0;
	private double SOUTH_BOUND = 34.0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_waypoint);
	}
    @Override
    public void onBackPressed() {
        this.finish();
    }
    public void buttonCurrentLoc(View view) {
    	LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE); 
    	Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    	double longitude = location.getLongitude();
    	double latitude = location.getLatitude();
    	EditText inputLat = (EditText)findViewById(R.id.input_lat);
    	EditText inputLng = (EditText)findViewById(R.id.input_lng);
    	inputLat.setText( String.valueOf(latitude));
    	inputLng.setText( String.valueOf(longitude));
    }
    public void buttonSubmit(View view) {
    	double lat = 0;
    	double lng = 0;
    	DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
    	String title = ((EditText)findViewById(R.id.input_title)).getText().toString();
    	if(((EditText) findViewById(R.id.input_lat)).getText().toString().length() > 0) {
    		lat = Double.parseDouble(((EditText)findViewById(R.id.input_lat)).getText().toString());
    	}
    	if(((EditText) findViewById(R.id.input_lng)).getText().toString().length() > 0) {
    		lng = Double.parseDouble(((EditText)findViewById(R.id.input_lng)).getText().toString());
    	}
    	if(title.length() < 2) {
    		title = "Waypoint at (" + String.valueOf(lat) + ", " + String.valueOf(lng) + ")";
    	}
    	Intent returnIntent = new Intent();
    	if(lat < NORTH_BOUND && lat > SOUTH_BOUND && lng < EAST_BOUND && lng > WEST_BOUND) {
        	long id = dbHelper.saveWaypoint(title, lat, lng);
        	returnIntent.putExtra("id", id);
        	setResult(RESULT_OK, returnIntent);  
        	this.finish();	
    	}
    	else {
    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WaypointActivity.this);
   	     
			// set title
			alertDialogBuilder.setTitle("Error");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("The GPS point is not in Nevada. Pressing continue will not create the waypoint.")
				.setCancelable(false)
				.setPositiveButton("Continue",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						WaypointActivity.this.finish();
					}
				  })
				.setNegativeButton("Go Back",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
    	}
    }
}
