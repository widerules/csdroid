package org.jtb.csc;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Site implements Serializable {
    private static final Pattern SITE_PATTERN = Pattern.compile("([^|]+)\\|([^|]+)\\|([^|]+)");
	private static final String URL_PREFIX = "http://cleardarksky.com/c/";
	private static final String CONDITIONS_URL_PREFIX = "http://cleardarksky.com/txtc/";

    private String id = null;
    private String name;
    private String region;
    private String nameLc;
    private String regionLc;
    private double latitude;
    private double longitude;
    private float distance;
    private File cacheDir;
    
    public static class DistanceComparator<T> implements Comparator<Site> {
		public int compare(Site s1, Site s2) {
			float d1 = s1.getDistance();
			float d2 = s2.getDistance();
			
			if (d1 > d2) {
				return 1;
			}
			if (d1 < d2) {
				return -1;				
			}
			return 0;
		}	
    }
    
    public Site(File cacheDir, String line) {
    	/*
        Matcher m = SITE_PATTERN.matcher(line);

        if (!m.matches()) {
            Log.w(getClass().getSimpleName(), "could not parse site, line: " + line);
            return;
        }
        id = m.group(1);
        region = m.group(2);
        regionLc = region.toLowerCase();
        name = m.group(3);
        nameLc = name.toLowerCase();
		*/
    	
    	ArrayList<String> tokens = CSVParser.parse(line);
    	id = tokens.get(0);
    	region = tokens.get(1);
    	regionLc = region.toLowerCase();
    	name = tokens.get(2);
    	nameLc = name.toLowerCase();
        
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

	public int getLatitudeE6() {
		return (int) (latitude*Math.pow(10, 6));
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public int getLongitudeE6() {
		return (int) (longitude*Math.pow(10, 6));
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

	public File getConditionsFile() {
		return new File(cacheDir + File.separator + getConditionsFileName());
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

	public String getConditionsUrl() {
		return CONDITIONS_URL_PREFIX + getConditionsFileName();		
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Site)) {
			return false;
		}
		Site other = (Site)o;
		return id.equals(other.id);
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	public boolean matches(String s) {
		if (s == null || s.length() == 0) {
			return false;
		}
		s = s.toLowerCase();
		if (nameLc.contains(s)) {
			return true;
		}
		if (regionLc.contains(s)) {
			return true;
		}
		return false;
	}
}
