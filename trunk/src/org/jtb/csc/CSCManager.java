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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jtb.csdroid.ClosestActivity;
import org.jtb.csdroid.TabWidgetActivity;

import android.content.Context;
import android.location.Location;
import android.os.Message;
import android.util.Log;

public class CSCManager {
	public static long REFRESH_INTERVAL = 3 * 60 * 60 * 1000; // 3 hours

	private static final String SITE_URL = "http://cleardarksky.com/t/chart_prop00.txt";
	private static final String SITE_LOCATION_URL = "http://cleardarksky.com/t/chart_keys00.txt";

	private static CSCManager manager;

	private Map<String, Site> siteMap = null;
	private Map<String, Conditions> conditionsMap = new HashMap<String, Conditions>();

	private File cacheDir;
	private File siteFile;
	private File siteLocationFile;

	private CSCManager(Context context) {
		// cacheDir = context.getCacheDir();
		cacheDir = context.getDir("csc", Context.MODE_PRIVATE);
		Log.d("csdroid", "using cache dir: " + cacheDir);
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

	private synchronized void refresh() {
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

		try {

			Date d = getLastRefresh();
			if (d == null
					|| (new Date().getTime() > d.getTime() + REFRESH_INTERVAL)) {
				// case 1: no or out of date data on disk
				Log.d("csdroid", "full refresh");
				clearCache();

				readUrl(SITE_URL, siteFile, 16384);
				readUrl(SITE_LOCATION_URL, siteLocationFile, 16384);
				loadSites();

			} else if (siteMap == null) {
				// case 2: data on disk, but not in memory
				Log.d("csdroid", "partial refresh");
				loadSites();
			}
		} catch (Throwable t) {
			Log.e("csdroid", "error refreshing", t);
			TabWidgetActivity.mStaticHandler.sendMessage(Message.obtain(
					ClosestActivity.mStaticHandler,
					TabWidgetActivity.REFRESH_ERROR_SHOW_WHAT, t.getMessage()));
			siteMap = new HashMap<String, Site>();
		}
		// case 3: data in memory
		Log.d("csdroid", "no refresh");
	}

	private Date getLastRefresh() {
		if (!siteFile.exists()) {
			return null;
		}
		long d = siteFile.lastModified();
		return new Date(d);
	}

	private void readConditions(List<Site> sites) throws IOException {
		for (Site s : sites) {
			readConditions(s);
		}
	}

	private void readConditions(Site s) throws IOException {
		File f = s.getConditionsFile();
		if (!f.exists()) {
			String url = s.getConditionsUrl();
			readUrl(url, f, 512);
		}
	}

	private void loadSites() throws IOException {
		siteMap = new HashMap<String, Site>();

		InputStream is = null;
		BufferedReader br = null;

		try {
			BlockTimer bt = new BlockTimer();
			is = new FileInputStream(siteFile);
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"),
					16384);

			String line;
			Site s;
			while ((line = br.readLine()) != null) {
				try {
					s = new Site(cacheDir, line);					
					if (s.getId() != null) {
						siteMap.put(s.getId(), s);
					}
				} catch (IndexOutOfBoundsException e) {
					Log.w("csdroid",
							"csc manager, could not parse site, line: " + line);
				}
			}
			Log.d("csdroid", "csc manager, read " + siteMap.keySet().size()
					+ " sitesm elasped time: " + bt.elapsed() + "ms");
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ioe) {
				Log.e("csdroid", "error closing sites file", ioe);
			}
		}

		loadSiteLocations();
	}

	private void loadSiteLocations() throws IOException {
		InputStream is = null;
		BufferedReader br = null;

		try {
			BlockTimer bt = new BlockTimer();

			is = new FileInputStream(siteLocationFile);
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"),
					16384);

			String line;
			SiteLocation sl;
			Site s;
			while ((line = br.readLine()) != null) {
				sl = new SiteLocation(line);
				s = siteMap.get(sl.getId());
				if (s == null) {
					Log.d("csdroid", "no site found for site location id: \""
							+ sl.getId() + "\", ignoring");

				} else if (sl.isLocatable()) {
					s.setSiteLocation(sl);
				}
			}
			Log.d("csdroid", "took: " + bt.elapsed()
					+ "ms to load site locations");
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ioe) {
				Log.e("csdroid", "error closing site locations file", ioe);
			}
		}
	}

	private void readUrl(String url, File f, int bufferSize) throws IOException {
		Log.d("csdroid", "reading URL: " + url + " into file: " + f);

		InputStream is = null;
		OutputStream os = null;

		try {
			URL u = new URL(url);
			HttpURLConnection uc = (HttpURLConnection) u.openConnection();
			uc.setRequestProperty(
					"User-agent",
					"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30)");
			uc.setReadTimeout(10000);

			if (uc.getResponseCode() != 200) {
				Log.w("csdroid", "error reading URL: " + u
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
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException ioe) {
				Log.e("csdroid", "error closing file", ioe);
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

	private void setDistance(CSCLocation l, Site s) {
		if (l == null) {
			return;
		}
		if (!s.isLocatable()) {
			return;
		}
		float[] results = new float[3];
		Location.distanceBetween(l.getLatitude(), l.getLongitude(),
				s.getLatitude(), s.getLongitude(), results);
		s.setDistance(results[0]);
	}

	public synchronized List<Site> getSites(CSCLocation location, int maxSites) {
		List<Site> sites = new ArrayList<Site>();
		for (Site s : siteMap.values()) {
			setDistance(location, s);
			sites.add(s);
		}
		Collections.sort(sites, new Site.DistanceComparator<Site>());
		sites = sites.subList(0, Math.min(sites.size(), maxSites));

		try {
			readSummaryImages(sites);
			readConditions(sites);
			BlockTimer bt = new BlockTimer();
			getConditions(sites);
			Log.d(getClass().getSimpleName(), "took: " + bt.elapsed()
					+ "ms to get conditions");
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "error getting sites", t);
			TabWidgetActivity.mStaticHandler.sendMessage(Message.obtain(
					ClosestActivity.mStaticHandler,
					TabWidgetActivity.REFRESH_ERROR_SHOW_WHAT, t.getMessage()));

		}

		return sites;
	}

	public synchronized List<Site> getSites(CSCLocation location, String s,
			int maxSites) {
		List<Site> sites = new ArrayList<Site>();
		for (Site site : siteMap.values()) {
			if (site.matches(s)) {
				setDistance(location, site);
				sites.add(site);
			}
		}
		Collections.sort(sites, new Site.DistanceComparator<Site>());
		sites = sites.subList(0, Math.min(sites.size(), maxSites));

		try {
			readSummaryImages(sites);
			readConditions(sites);
			getConditions(sites);
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "error getting sites", t);
			TabWidgetActivity.mStaticHandler.sendMessage(Message.obtain(
					ClosestActivity.mStaticHandler,
					TabWidgetActivity.REFRESH_ERROR_SHOW_WHAT, t.getMessage()));

		}
		return sites;
	}

	public synchronized List<Site> getSites(Collection<String> siteIds) {
		return getSites(null, siteIds);
	}

	public synchronized List<Site> getSites(CSCLocation location,
			Collection<String> siteIds) {
		List<Site> sites = new ArrayList<Site>();
		Site s;
		for (String id : siteIds) {
			s = siteMap.get(id);
			if (s == null) {
				Log.w(getClass().getSimpleName(), "no site found for id: " + id);
				continue;
			}

			setDistance(location, s);
			sites.add(s);
		}
		Collections.sort(sites, new Site.DistanceComparator<Site>());

		try {
			readSummaryImages(sites);
			readConditions(sites);
			getConditions(sites);
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "error getting sites", t);
			TabWidgetActivity.mStaticHandler.sendMessage(Message.obtain(
					ClosestActivity.mStaticHandler,
					TabWidgetActivity.REFRESH_ERROR_SHOW_WHAT, t.getMessage()));
		}
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
			Log.e(getClass().getSimpleName(), "error getting conditions", e);
			TabWidgetActivity.mStaticHandler.sendMessage(Message.obtain(
					ClosestActivity.mStaticHandler,
					TabWidgetActivity.REFRESH_ERROR_SHOW_WHAT, e.getMessage()));
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
		try {
			readDetailImage(s);
		} catch (Throwable t) {
			Log.e(getClass().getSimpleName(), "error getting site", t);
			TabWidgetActivity.mStaticHandler.sendMessage(Message.obtain(
					ClosestActivity.mStaticHandler,
					TabWidgetActivity.REFRESH_ERROR_SHOW_WHAT, t.getMessage()));
		}
		return s;
	}

	private void readSummaryImages(List<Site> sites) throws IOException {
		File f;
		for (Site s : sites) {
			f = s.getSummaryImageFile();
			if (!f.exists()) {
				String u = s.getSummaryImageUrl();
				readUrl(u, f, 512);
			}
		}
	}

	private void readDetailImage(Site s) throws IOException {
		String u = s.getDetailImageUrl();
		File f = s.getDetailImageFile();
		if (!f.exists()) {
			readUrl(u, f, 4096);
		}
	}
}
