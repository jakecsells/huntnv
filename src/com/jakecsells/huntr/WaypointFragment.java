package com.jakecsells.huntr;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class WaypointFragment extends Fragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
	 super.onCreate(savedInstanceState);
	 getActivity().setContentView(R.layout.waypoint_frag_activity);
	 
	}
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    	View view = super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
    	view.setBackgroundColor(Color.WHITE);
    	return view;
    }
}
