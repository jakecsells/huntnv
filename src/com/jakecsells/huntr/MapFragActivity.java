package com.jakecsells.huntr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.model.LatLngBounds.Builder;

public class MapFragActivity extends Activity {

	DataBaseHelper dbHelper = new DataBaseHelper(null);
	GoogleMap map;
	private HashMap<Marker, Waypoint> idMarker;
	private Polygon currentPolygon;
	private LatLngBounds currentBounds;
	private Menu menu;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_frag_activity);
        
        // Action bar setup for up functionality
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        idMarker = new HashMap<Marker, Waypoint>();
        
        // Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(39.0, -117.0), 6)); 
        map.setMyLocationEnabled(true);
        
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    	switch(Integer.valueOf(sharedPreferences.getString("pref_map_type", "1"))) {
	    	case 1: //terrain
	    		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	    		break;
	    	case 2: //satellite
	    		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	    		break;
	    	case 3: //hybrid
	    		map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	    		break;
	    	case 4: //normal
	    		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	    		break;
	    	default: //default to terrain
	    		map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
	    		break;
    	}
        
        dbHelper = new DataBaseHelper(this);
 
        try {
        	dbHelper.createDataBase();
        } catch (IOException ioe) {
        	throw new Error("Unable to create database");
        }
        try {
        	dbHelper.openDataBase();
        }catch(SQLException sqle){
        	throw sqle;
        }
    	int[] list = dbHelper.getAllWaypointIDs();
    	if(list != null) {
	    	for(int i = 0; i < list.length; i++) {
	    		displayWaypoint(list[i]);
	    	}
    	}
    	
        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(final Marker marker) {
            	// Delete Marker functionality
        		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapFragActivity.this);
        	     
    			// set title
    			alertDialogBuilder.setTitle("Delete");
     
    			// set dialog message
    			alertDialogBuilder
    				.setMessage("Permanently remove this waypoint?")
    				.setCancelable(false)
    				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						Waypoint tmpWaypoint = idMarker.get(marker);
    						dbHelper.deleteWaypoint(tmpWaypoint.getId());
    						marker.remove();
    					}
    				  })
    				.setNegativeButton("No",new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int id) {
    						dialog.cancel();
    					}
    				});
     
    				// create alert dialog
    				AlertDialog alertDialog = alertDialogBuilder.create();
     
    				// show it
    				alertDialog.show();   
            }
        });
    	
        dbHelper.close();
        
    }    
    @Override
    public boolean onNavigateUp(){
    	if(getFragmentManager().getBackStackEntryCount() > 0 ) {
	        getFragmentManager().popBackStack();
	        getActionBar().setDisplayHomeAsUpEnabled(false);
	        return true;
    	}
		return false;
    }
    
    @Override
    public void onBackPressed() {
    	if(getFragmentManager().getBackStackEntryCount() > 0 ) {
	        getFragmentManager().popBackStack();
	        getActionBar().setDisplayHomeAsUpEnabled(false);
    	}
    	else {
    		finish();
    	}
    }

    
    @SuppressLint("NewApi")
	@Override
    public boolean onCreateOptionsMenu(Menu newMenu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    	menu = newMenu;
        getMenuInflater().inflate(R.menu.main, menu);
        
        // search action bar setup
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
         SearchView searchView =
                 (SearchView) menu.findItem(R.id.action_search).getActionView();
         searchView.setSearchableInfo(
                 searchManager.getSearchableInfo(getComponentName()));
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
            	if(getFragmentManager().getBackStackEntryCount() == 0){
	                // Display the fragment as the main content.
	                getFragmentManager().beginTransaction()
	                        .add(android.R.id.content, new PrefsFragment())
	                        .addToBackStack(null)
	                        .commit();
	                }
                return true;
            case R.id.action_waypoint:
                Intent intent = new Intent(MapFragActivity.this, WaypointActivity.class);
                startActivity(intent);
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

    	// user inputs hunt unit
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            String formatquery = String.format("%03d", Integer.parseInt(query));
            this.displayHuntUnit(formatquery);
            setTitle("Unit " + formatquery);
            MenuItemCompat.collapseActionView(menu.findItem(R.id.action_search));
        }
    }
    
    
    private boolean displayHuntUnit(String huntunitnum) {
    	String rawcontent = dbHelper.getGeometry(huntunitnum);
    	if(rawcontent.length() > 1) {
	    	rawcontent = rawcontent.replaceAll("POLYGON \\(\\(|\\)\\)", "");
	    	String[] coords = rawcontent.split(",|\\s");
	    	PolygonOptions options = new PolygonOptions();
	    	LatLngBounds.Builder builder = new LatLngBounds.Builder();
	    	for(int i=1; i<coords.length-1; i+=2) {
	    		options.add(new LatLng(Double.valueOf(coords[i]).doubleValue(),Double.valueOf(coords[i+1]).doubleValue()));
	    		builder.include(new LatLng(Double.valueOf(coords[i]).doubleValue(),Double.valueOf(coords[i+1]).doubleValue()));
	    	}
	    	if(currentPolygon != null) {
	    		currentPolygon.remove();
	    	}
	    	 currentPolygon = map.addPolygon(options
	         .strokeColor(0x3333CC)
	         .strokeWidth(3)
	         .fillColor(0x453333CC));
	    	 
	    	 // get users current position and adds to builder
	    	 LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
	    	 Criteria criteria = new Criteria();
	    	 String provider = service.getBestProvider(criteria, false);
	    	 Location location = service.getLastKnownLocation(provider);
	    	 LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
//	    	 builder.include(userLocation);
	    	 currentBounds = builder.build();
	    	 // move camera to the bounds of the hunt unit
	    	 CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(currentBounds.including(userLocation), 10);
	    	 map.animateCamera(cameraUpdate);
	    	 
	    	 return true;
    	}
    	else {
    		// hunt unit does not exist
    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
     
			// set title
			alertDialogBuilder.setTitle("Error");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("That hunt unit does not exist. Please try again.")
				.setCancelable(false)
				.setPositiveButton("Continue",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				  });
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
				return false;
    	}
    }
    
    private void displayWaypoint(int id) {
    	Waypoint tmpWaypoint = dbHelper.getWaypoint(id);
    	Log.i("LAT", String.valueOf(tmpWaypoint.getLat()));
    	Log.i("LNG", String.valueOf(tmpWaypoint.getLng()));
    	map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

    	Marker marker = map.addMarker(new MarkerOptions()
    	        .position(new LatLng(tmpWaypoint.getLat(), tmpWaypoint.getLng()))
    	        .title(tmpWaypoint.getTitle()));
    	idMarker.put(marker, tmpWaypoint);
    }
}