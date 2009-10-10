package org.jtb.csc;

import java.io.Serializable;

import com.google.android.maps.GeoPoint;

public class CSCLocation implements Serializable {

    private double latitude = 0;
    private double longitude = 0;
	private int latitudeE6;
	private int longitudeE6;
  
    public CSCLocation() {	
    }
    
    public CSCLocation(double latitude, double longitude) {
    	setLatitude(latitude);
    	setLongitude(longitude);
    }

    public CSCLocation(int latitudeE6, int longitudeE6) {
    	setLatitudeE6(latitudeE6);
    	setLongitudeE6(longitudeE6);
    }
    
    @Override
    public String toString() {
        return "{" + getLatitude() + "," + getLongitude() + "}";
    }
    
    public int getLatitudeE6() {
    	return latitudeE6;
    }
    
    public void setLatitudeE6(int latitudeE6) {
    	this.latitudeE6 = latitudeE6;
    	this.latitude = latitudeE6 / Math.pow(10, 6);
    }
    
    public int getLongitudeE6() {
    	return longitudeE6;
    }
    
    public void setLongitudeE6(int longitudeE6) {
    	this.longitudeE6 = longitudeE6;
    	this.longitude = longitudeE6 / Math.pow(10, 6);
    }

    public static int getE6(double d) {
    	return (int) (d*Math.pow(10, 6));
    }

	public void setLatitude(double latitude) {
		this.latitude = latitude;
		this.latitudeE6 = getE6(latitude);
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
		this.longitudeE6 = getE6(longitude);
	}

	public double getLongitude() {
		return longitude;
	}

	public GeoPoint getGeoPoint() {
		GeoPoint geoPoint = new GeoPoint(latitudeE6, longitudeE6);
		return geoPoint;
	}
	
	
}
