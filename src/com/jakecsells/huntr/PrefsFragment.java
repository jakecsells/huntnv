package com.jakecsells.huntr;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

//        getView().setBackgroundColor(Color.WHITE);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(Color.WHITE);

        return view;
    }
    
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals("pref_map_type")) {
        	GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        	switch(Integer.valueOf(sharedPreferences.getString(key, "1"))) {
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
        }
    }
    
    @Override
	public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
	public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}