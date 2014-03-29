package com.jakecsells.huntnv;

import com.jakecsells.huntnv.R;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.EditText;

public class WaypointActivity extends Activity {

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
    	DataBaseHelper dbHelper = new DataBaseHelper(getApplicationContext());
    	String title = ((EditText)findViewById(R.id.input_title)).getText().toString();
    	double lat = Double.parseDouble(((EditText)findViewById(R.id.input_lat)).getText().toString());
    	double lng = Double.parseDouble(((EditText)findViewById(R.id.input_lng)).getText().toString());
    	if(title.length() < 2) {
    		title = "Waypoint at (" + String.valueOf(lat) + ", " + String.valueOf(lng) + ")";
    	}
    	dbHelper.saveWaypoint(title, lat, lng);
    	this.finish();
    }
}
