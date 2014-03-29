package com.jakecsells.huntnv;

public class Waypoint {
	 
    private int id;
    private String title;
    private double lat;
    private double lng;
 
    public Waypoint(){}
 
    public Waypoint(int id, String title, double latitude, double longitude) {
        super();
        this.id = id;
        this.title = title;
        this.lat = latitude;
        this.lng = longitude;
    }
 
    //getters & setters
    public int getId() {
    	return this.id;
    }
    
    public String getTitle() {
    	return this.title;
    }
    
    public double getLat() {
    	return this.lat;
    }
    
    public double getLng() {
    	return this.lng;
    }
 
    @Override
    public String toString() {
        return "Book [id=" + id + ", title=" + title + ", latitude=" + lat
                + ", longitude= "+ lng + "]";
    }
}
