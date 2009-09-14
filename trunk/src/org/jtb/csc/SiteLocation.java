package org.jtb.csc;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SiteLocation {
	private static final Pattern SITE_LOCATION_PATTERN = Pattern
			.compile("([^|]+)\\|([^|]+)\\|([^|]+)\\|([^|]+)");
	private static final Pattern SITE_NO_LOCATION_PATTERN = Pattern
			.compile("([^|]+)\\|([^|]+)");

	private String id;
	private String region;
	private double latitude;
	private double longitude;
	private boolean locatable = true;

	public SiteLocation(String line) {
		/*
		Matcher m = SITE_LOCATION_PATTERN.matcher(line);
		if (m.matches()) {
			id = m.group(1);
			region = m.group(2);
			latitude = Double.parseDouble(m.group(3));
			longitude = Double.parseDouble(m.group(4));
		}

		m = SITE_NO_LOCATION_PATTERN.matcher(line);
		if (m.matches()) {
			locatable = false;
			id = m.group(1);
			region = m.group(2);
		}
		*/
		
		ArrayList<String> tokens = CSVParser.parse(line);
		if (tokens.size() == 2) {
			locatable = false;
			id = tokens.get(0);
			region = tokens.get(1);
		} else {
			id = tokens.get(0);
			region = tokens.get(1);			
			latitude = Double.parseDouble(tokens.get(2));
			longitude = Double.parseDouble(tokens.get(3));
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	@Override
	public String toString() {
		return "{ id=" + id + ", region=" + region + ", latitude=" + latitude
				+ ", longitude=" + longitude + " }";
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public boolean isLocatable() {
		return locatable;
	}
}
