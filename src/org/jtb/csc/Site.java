package org.jtb.csc;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Site implements Serializable {
    private static final Pattern SITE_PATTERN = Pattern.compile("([^|]+)\\|([^|]+)\\|([^|]+)");
	private static final String URL_PREFIX = "http://cleardarksky.com/c/";

    private String id;
    private String name;
    private String region;
    private double latitude;
    private double longitude;
    private float distance;
    private File cacheDir;
    
    public static class DistanceComparator<T> implements Comparator<Site> {
		public int compare(Site s1, Site s2) {
			return new Float(s1.getDistance()).compareTo(new Float(s2.getDistance()));
		}	
    }
    
    public Site(File cacheDir, String line) {
        Matcher m = SITE_PATTERN.matcher(line);

        if (!m.matches()) {
            // TODO: android log
            return;
        }
        id = m.group(1);
        region = m.group(2);
        name = m.group(3);
        
        this.cacheDir = cacheDir;
    }

    public void setSiteLocation(SiteLocation siteLocation) {
    	this.latitude = siteLocation.getLatitude();
    	this.longitude = siteLocation.getLongitude();
    }
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "{ id=" + id + ", name=" + name + ", region=" + region + ", latitude="  + latitude + ", longitude=" + longitude + " }";
    }

    public String getConditionsFileName() {
        return id + "csp.txt";
    }

    public String getSummaryImageFileName() {
        return id + "cs0.gif";
    }

    public String getHtmlFileName() {
        return id + "key.html";
    }

    public String getDetailImageFileName() {
        return id + "csk.gif";
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

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getDistance() {
		return distance;
	}

	public File getSummaryImageFile() {
		return new File(cacheDir + File.separator + getSummaryImageFileName());
	}

	public File getDetailImageFile() {
		return new File(cacheDir + File.separator + getDetailImageFileName());
	}
	
	public String getUrl() {
		return URL_PREFIX + getHtmlFileName();
	}
	
	public String getSummaryImageUrl() {
		return URL_PREFIX + getSummaryImageFileName();
	}
	
	public String getDetailImageUrl() {
		return URL_PREFIX + getDetailImageFileName();		
	}
}
