package org.jtb.csc;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jtb.csdroid.ClosestActivity;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Message;
import android.util.Log;

public class CSCManager {
	public static long REFRESH_INTERVAL = 3 * 60 * 60 * 1000; // 3 hours

	private static final String SITE_URL = "http://cleardarksky.com/t/chart_prop00.txt";
	private static final String SITE_LOCATION_URL = "http://cleardarksky.com/t/chart_keys00.txt";

	private static CSCManager manager;

	private Context context;
	private Map<String, Site> siteMap = null;
	private Map<String, Conditions> conditionsMap = new HashMap<String, Conditions>();
	private File cacheDir;
	private File siteFile;
	private File siteLocationFile;

	private CSCManager(Context context) {
		this.context = context;
		// cacheDir = context.getCacheDir();
		cacheDir = context.getDir("csc", Context.MODE_PRIVATE);
		Log.d(getClass().getSimpleName(), "using cache dir: " + cacheDir);
		siteFile = new File(cacheDir.toString() + "/" + "chart_prop00.txt");
		siteLocationFile = new File(cacheDir.toString() + "/"
				+ "chart_keys00.txt");
	}

	public synchronized static CSCManager getInstance(Context context) {
		if (manager == null) {
			manager = new CSCManager(context);
		}

		manager.refresh();
		return manager;
	}

	public synchronized void refresh() {
		//
		// only refresh if time stamp doesn't exist or
		// it was created beyond refresh interval
		//
		// why not just rely on service to call refresh?
		// the reason is that the service can be ejected from memory
		// in that case, we can to avoid re-fetching all of the
		// data
		//

		// 
		// there are three possible states,
		// 1. no data, or out of date on disk
		// 2. data on disk, but not in memory
		// 3. data in memory (and disk)

		Date d = getLastRefresh();
		if (d == null
				|| (new Date().getTime() > d.getTime() + REFRESH_INTERVAL)) {
			// case 1: no or out of date data on disk
			Log.d(getClass().getSimpleName(), "full refresh");
			clearCache();

			try {
				readUrl(SITE_URL, siteFile, 16384);
				readUrl(SITE_LOCATION_URL, siteLocationFile, 16384);
				loadSites();
			} catch (Throwable t) {
				// TODO: move to tabwidget
				ClosestActivity.mStaticHandler.sendMessage(Message
						.obtain(ClosestActivity.mStaticHandler,
								ClosestActivity.REFRESH_ERROR_SHOW_WHAT, t
										.getMessage()));
				siteMap = new HashMap<String, Site>();
			}
		} else if (siteMap == null) {
			// case 2: data on disk, but not in memory
			Log.d(getClass().getSimpleName(), "partial refresh");
			loadSites();
		}

		// case 3: data in memory
		Log.d(getClass().getSimpleName(), "no refresh");
	}

	private Date getLastRefresh() {
		if (!siteFile.exists()) {
			return null;
		}
		long d = siteFile.lastModified();
		return new Date(d);
	}

	private void readConditions(List<Site> sites) {
		for (Site s : sites) {
			readConditions(s);
		}
	}

	private void readConditions(Site s) {
		File f = s.getConditionsFile();
		if (!f.exists()) {
			String url = s.getConditionsUrl();
			readUrl(url, f, 512);
		}
	}

	private void loadSites() {
		siteMap = new HashMap<String, Site>();

		InputStream is = null;
		BufferedReader br = null;

		try {
			is = new FileInputStream(siteFile);
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"),
					16384);

			String line;
			while ((line = br.readLine()) != null) {
				Site s = new Site(cacheDir, line);
				if (s.getId() != null) {
					siteMap.put(s.getId(), s);
				}
			}
			Log.d(this.getClass().getSimpleName(), "read "
					+ siteMap.keySet().size() + " sites");
		} catch (IOException ioe) {
			Log.e(getClass().getSimpleName(), "error loading sites file", ioe);
			return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ioe) {
				Log.e(getClass().getSimpleName(), "error closing sites file",
						ioe);
			}
		}

		loadSiteLocations();
	}

	private void loadSiteLocations() {
		InputStream is = null;
		BufferedReader br = null;

		try {
			is = new FileInputStream(siteLocationFile);
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"),
					16384);

			String line;
			while ((line = br.readLine()) != null) {
				SiteLocation sl = new SiteLocation(line);
				if (!sl.isLocatable()) {
					Log.d(this.getClass().getSimpleName(),
							"no site location found for site id: \""
									+ sl.getId() + "\", removing");
					siteMap.remove(sl.getId());
				} else {
					Site s = siteMap.get(sl.getId());
					if (s == null) {
						Log.d(this.getClass().getSimpleName(),
								"no site found for site location id: \""
										+ sl.getId() + "\", ignoring");
					} else {
						s.setSiteLocation(sl);
					}
				}
			}
		} catch (IOException ioe) {
			Log.e(getClass().getSimpleName(),
					"error loading site locations file", ioe);
			return;
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ioe) {
				Log.e(getClass().getSimpleName(),
						"error closing site locations file", ioe);
			}
		}
	}

	private void readUrl(String url, File f, int bufferSize) {
		Log.d(getClass().getSimpleName(), "reading URL: " + url
				+ " into file: " + f);

		InputStream is = null;
		OutputStream os = null;

		try {
			URL u = new URL(url);
			HttpURLConnection uc = (HttpURLConnection) u.openConnection();
			uc.setReadTimeout(10000);

			if (uc.getResponseCode() != 200) {
				Log.w(getClass().getSimpleName(), "error reading URL: " + u
						+ ", response code: " + uc.getResponseCode());
				return;
			}
			is = new BufferedInputStream(uc.getInputStream(), bufferSize);
			if (f.exists()) {
				f.delete();
			}
			f.createNewFile();
			os = new BufferedOutputStream(new FileOutputStream(f), bufferSize);

			byte[] buffer = new byte[bufferSize];
			int count;
			while ((count = is.read(buffer, 0, bufferSize)) != -1) {
				os.write(buffer, 0, count);
			}
		} catch (Throwable t) {
			Log.w(getClass().getSimpleName(), t);
			return;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException ioe) {
				Log.e(getClass().getSimpleName(), "error closing file", ioe);
			}
		}
	}

	public synchronized void clearCache() {
		siteMap = null;
		conditionsMap = new HashMap<String, Conditions>();

		if (cacheDir.exists()) {
			File[] files = cacheDir.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();
			}
		}
	}

	public synchronized List<Site> getSites(Location location, int maxSites) {
		LocationManager lm = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		String name = lm.getBestProvider(new Criteria(), true);

		List<Site> sites = new ArrayList<Site>();
		for (Site s : siteMap.values()) {
			Location l = new Location(name);
			l.setLatitude(s.getLatitude());
			l.setLongitude(s.getLongitude());
			float dt = location.distanceTo(l);
			s.setDistance(dt);
			sites.add(s);
		}
		Collections.sort(sites, new Site.DistanceComparator<Site>());
		sites = sites.subList(0, Math.min(sites.size(), maxSites));

		readSummaryImages(sites);
		readConditions(sites);
		getConditions(sites);

		return sites;
	}

	public synchronized List<Site> getSites(String s, int maxSites) {
		List<Site> sites = new ArrayList<Site>();
		for (Site site : siteMap.values()) {
			if (site.matches(s)) {
				sites.add(site);
			}
			if (sites.size() == maxSites) {
				break;
			}
		}
		Collections.sort(sites, new Site.DistanceComparator<Site>());
		sites = sites.subList(0, Math.min(sites.size(), maxSites));

		readSummaryImages(sites);
		readConditions(sites);
		getConditions(sites);

		return sites;
	}

	public synchronized List<Site> getSites(Collection<String> siteIds) {
		List<Site> sites = new ArrayList<Site>();
		for (String id : siteIds) {
			Site s = siteMap.get(id);
			if (s == null) {
				Log.w(getClass().getSimpleName(), "no site found for id: " + id);
				continue;
			}
			sites.add(s);
		}
		Collections.sort(sites, new Site.DistanceComparator<Site>());

		readSummaryImages(sites);
		readConditions(sites);
		getConditions(sites);

		return sites;
	}

	public void getConditions(List<Site> sites) {
		for (Site s : sites) {
			getConditions(s);
		}
	}

	public synchronized Conditions getConditions(Site s) {
		InputStream is = null;
		Conditions cs = conditionsMap.get(s.getId());
		if (cs != null) {
			return cs;
		}
		try {
			is = new FileInputStream(s.getConditionsFile());
			cs = new Conditions(is);
			conditionsMap.put(s.getId(), cs);
			return cs;
		} catch (FileNotFoundException e) {
			Log.w(getClass().getSimpleName(),
					"could not read conditions file for site: " + s, e);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(getClass().getSimpleName(), "could not close stream",
							e);
				}
			}
		}
	}

	public synchronized Site getSite(String id) {
		Site s = siteMap.get(id);
		if (s == null) {
			return null;
		}
		readDetailImage(s);
		return s;
	}

	private void readSummaryImages(List<Site> sites) {
		for (Site s : sites) {
			File f = s.getSummaryImageFile();
			if (!f.exists()) {
				String u = s.getSummaryImageUrl();
				readUrl(u, f, 512);
			}
		}
	}

	private void readDetailImage(Site s) {
		String u = s.getDetailImageUrl();
		File f = s.getDetailImageFile();
		if (!f.exists()) {
			readUrl(u, f, 4096);
		}
	}
}
