package com.jakecsells.huntnv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.support.v4.view.MenuItemCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;
import org.w3c.dom.Document;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.*;
import com.jakecsells.huntnv.R;

public class MapFragActivity extends Activity {

	DataBaseHelper dbHelper = new DataBaseHelper(null);
	GoogleMap map;
	GMapV2Direction md;
	private HashMap<Marker, Waypoint> idMarker;
	private Polygon currentPolygon;
	private LatLngBounds currentBounds;
	private Polyline currentDirections;
	private Menu menu;
	private LocationManager serviceLoc;
	LatLng userLocation;
	
	// Global Constants
	private int CREATE_WAYPOINT_REQUEST = 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    	
    	setTitle("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_frag_activity);
        
        // Action bar setup for up functionality
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        idMarker = new HashMap<Marker, Waypoint>();
        
        // Get a handle to the Map Fragment
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(39.0, -117.0), 6));
        
        serviceLoc = (LocationManager) getSystemService(LOCATION_SERVICE);
        
        md = new GMapV2Direction();
        
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
        map.setMyLocationEnabled(true);
        
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
        		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapFragActivity.this);
    			alertDialogBuilder.setTitle("Delete");
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
    				AlertDialog alertDialog = alertDialogBuilder.create();
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
    	Intent intent;
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
                intent = new Intent(MapFragActivity.this, WaypointActivity.class);
                startActivityForResult(intent, CREATE_WAYPOINT_REQUEST);
            	return true;
            case R.id.action_about:
                intent = new Intent(MapFragActivity.this, AboutActivity.class);
                startActivityForResult(intent, CREATE_WAYPOINT_REQUEST);
            	return true;
            case R.id.action_directions:
            	getUserLocation();
            	if(currentBounds != null && userLocation != null) {
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapFragActivity.this);
        			alertDialogBuilder.setTitle("View Method");
        			alertDialogBuilder
        				.setMessage("Where would you like to view these directions?")
        				.setCancelable(false)
        				.setPositiveButton("Google Maps",new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int id) {
        						LatLng destination = currentBounds.getCenter();
        						String uri = "http://maps.google.com/maps?saddr="+userLocation.latitude+","+userLocation.longitude+"&daddr="+destination.latitude+","+destination.longitude+"&mode=driving";
        						// String uri = String.format(Locale.ENGLISH, "geo:%f,%f", destination.latitude, destination.longitude);
        						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        						startActivity(intent);
        					}
        				  })
        				.setNegativeButton("Here",new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int id) {
        						dialog.cancel();
        	    	   	    	// get center of current hunt unit bounds
        						getUserLocation();
        						if(userLocation != null) {
            	    	   	    	LatLng destination = currentBounds.getCenter();
            	            		Document doc = md.getDocument(userLocation, destination, GMapV2Direction.MODE_DRIVING);
            	            		int duration = md.getDurationValue(doc);
            	            		String distance = md.getDistanceText(doc);
            	            		String start_address = md.getStartAddress(doc);
            	            		String copy_right = md.getCopyRights(doc);
            	            		// get direction polylines and display
            	            		ArrayList<LatLng> directionPoint = md.getDirection(doc);
            	            		PolylineOptions rectLine = new PolylineOptions().width(3).color(0xffff00ff);
            	            		for(int i = 0 ; i < directionPoint.size() ; i++) {			
            	            			rectLine.add(directionPoint.get(i));
            	            		}
            	            		currentDirections = map.addPolyline(rectLine);
        						}
        					}
        				});
    				AlertDialog alertDialog = alertDialogBuilder.create();
    				alertDialog.show();
    				return true;
                }
            	else {
            		// need hunt unit view
            		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        			alertDialogBuilder.setTitle("Error");
        			alertDialogBuilder
        				.setMessage("There is no current hunt unit or there is no GPS coverage")
        				.setCancelable(false)
        				.setPositiveButton("Continue",new DialogInterface.OnClickListener() {
        					public void onClick(DialogInterface dialog, int id) {
        						dialog.cancel();
        					}
        				  });
        			AlertDialog alertDialog = alertDialogBuilder.create();
        			alertDialog.show();
            		return false;
            	}
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
            setTitle(formatquery);
            MenuItemCompat.collapseActionView(menu.findItem(R.id.action_search));
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CREATE_WAYPOINT_REQUEST && resultCode == RESULT_OK) {
        	long resultId = data.getLongExtra("id", 1);
        	displayWaypoint((int)resultId);
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
	    	 
	    	 getUserLocation();
	    	 if(userLocation != null) {
		    	 currentBounds = builder.build();
		    	 // move camera to the bounds of the hunt unit
		    	 CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(currentBounds.including(userLocation), 10);
		    	 map.animateCamera(cameraUpdate);
	    	 }
	    	 else {
		    	 currentBounds = builder.build();
		    	 // move camera to the bounds of the hunt unit
		    	 CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(currentBounds, 10);
		    	 map.animateCamera(cameraUpdate);
	    	 }
	    	 
	    	 return true;
    	}
    	else {
    		// hunt unit does not exist
    		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Error");
			alertDialogBuilder
				.setMessage("That hunt unit does not exist. Please try again.")
				.setCancelable(false)
				.setPositiveButton("Continue",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						dialog.cancel();
					}
				  });
				AlertDialog alertDialog = alertDialogBuilder.create();
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
    
    public LatLng getUserLocation() {
        if(!serviceLoc.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
	        map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	            @Override
	            public void onInfoWindowClick(final Marker marker) {
	        		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapFragActivity.this);
	    			alertDialogBuilder.setTitle("Error");
	    			alertDialogBuilder
	    				.setMessage("GPS localization is turned off, would you like to turn it on?")
	    				.setCancelable(false)
	    				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
	    					public void onClick(DialogInterface dialog, int id) {
	    						 startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	    						 dialog.cancel();
	    					}
	    				  })
	    				.setNegativeButton("No",new DialogInterface.OnClickListener() {
	    					public void onClick(DialogInterface dialog, int id) {
	    						 dialog.cancel();
	    					}
	    				});
	    				AlertDialog alertDialog = alertDialogBuilder.create();
	    				alertDialog.show();   
	            }
	        });

        }
    	 Criteria criteria = new Criteria();
    	 String provider = serviceLoc.getBestProvider(criteria, false);
    	 Location location = serviceLoc.getLastKnownLocation(provider);
    	 if(location != null) {
	    	 userLocation = new LatLng(location.getLatitude(),location.getLongitude());
	    	 return userLocation;	 
    	 }
    	 else {
    		 userLocation = null;
    		 return userLocation;
    	 }
    }
}