package org.jtb.csdroid;

import java.util.List;
import java.util.Timer;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ClosestActivity extends Activity implements LocationListener {
	static final int UPDATE_LOCATION_DIALOG_SHOW_WHAT = 0;
	static final int UPDATE_LOCATION_DIALOG_DISMISS_WHAT = 1;
	static final int UPDATE_LIST_WHAT = 6;
	public static final int REFRESH_ERROR_SHOW_WHAT = 10;
	static final int REFRESH_ERROR_HIDE_WHAT = 11;
	static final int LOCATION_WAIT_DIALOG_SHOW_WHAT = 12;
	static final int LOCATION_WAIT_DIALOG_DISMISS_WHAT = 13;
	static final int UPDATE_WHAT = 15;
	static final int HIDE_LIST_WHAT = 16;
	static final int SHOW_LIST_WHAT = 17;

	private static final int UPDATE_LOCATION_DIALOG = 0;
	private static final int REFRESH_ERROR_DIALOG = 3;
	private static final int LOCATION_WAIT_DIALOG = 4;

	private ProgressDialog mUpdateLocationDialog;
	private AlertDialog mRefreshErrorDialog;
	private AlertDialog mListClickDialog;
	private ProgressDialog mLocationWaitDialog = null;

	private List<Site> mSites;
	private ListView mCSCListView;
	private ClosestActivity mThis;
	private Timer mTimer = new Timer();
	private String mRefreshError = null;
	private Location mLocation = null;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_LIST_WHAT:
				mCSCListView.setVisibility(View.GONE);
				break;
			case SHOW_LIST_WHAT:
				mCSCListView.setVisibility(View.VISIBLE);
				break;
			case UPDATE_WHAT:
				update();
				break;
			case UPDATE_LIST_WHAT:
				updateList();
				break;
			case UPDATE_LOCATION_DIALOG_SHOW_WHAT:
				showDialog(UPDATE_LOCATION_DIALOG);
				break;
			case UPDATE_LOCATION_DIALOG_DISMISS_WHAT:
				dismissDialog(UPDATE_LOCATION_DIALOG);
				break;
			case LOCATION_WAIT_DIALOG_SHOW_WHAT:
				showDialog(LOCATION_WAIT_DIALOG);
				break;
			case LOCATION_WAIT_DIALOG_DISMISS_WHAT:
				dismissDialog(LOCATION_WAIT_DIALOG);
				break;
			case REFRESH_ERROR_SHOW_WHAT:
				mRefreshError = (String) msg.obj;
				showDialog(REFRESH_ERROR_DIALOG);
				break;
			case REFRESH_ERROR_HIDE_WHAT:
				dismissDialog(REFRESH_ERROR_DIALOG);
				break;
			}
		}
	};
	public static Handler mStaticHandler = null;

	private void updateList() {
		if (mSites != null) {
			CSCAdapter csca = new CSCAdapter(mThis, mSites);
			mCSCListView.setAdapter(csca);
		}
	}

	public void update() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String name = lm.getBestProvider(new Criteria(), true);
		if (name == null) {
			// TODO: error dialog an exit (this.finish())?
			Log.e(getClass().getSimpleName(),
					"no best location provider returned");
		}
		// LocationProvider lp = lm.getProvider(name);
		Location l = lm.getLastKnownLocation(name);
		onLocationChanged(l);
		
		lm.requestLocationUpdates(name, 10 * 60 * 1000, 10 * 1000, mThis); 
	}

	private int getMaxCharts() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		int d = Integer.parseInt(prefs.getString("maxCharts", "5"));
		return d;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.closest);

		mThis = this;
		mStaticHandler = mHandler;

		mCSCListView = (ListView) findViewById(R.id.csc_list);
		mCSCListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				AlertDialog.Builder builder = new ListClickDialog.Builder(mThis, mSites, position);
				mListClickDialog = builder.create();
				mListClickDialog.show();
			}
		});	
	}

	public void onLocationChanged(final Location location) {
		new Thread(new Runnable() {
			public void run() {
				if (location == null) {
					mHandler.sendMessage(Message.obtain(mHandler,
							LOCATION_WAIT_DIALOG_SHOW_WHAT));
					return;
				}
				if (mLocation != null && mLocation.distanceTo(location) < 10) {
					return;
				}
				mLocation = location;
				
				if (mLocationWaitDialog != null) {
					mHandler.sendMessage(Message.obtain(mHandler,
							LOCATION_WAIT_DIALOG_DISMISS_WHAT));
					mLocationWaitDialog = null;
				}
				
				Message m = Message.obtain(mHandler,
						UPDATE_LOCATION_DIALOG_SHOW_WHAT);
				mHandler.sendMessage(m);
				m = Message.obtain(mHandler,
						HIDE_LIST_WHAT);
				mHandler.sendMessage(m);

				CSCManager cscm = CSCManager.getInstance(mThis);
				int maxCharts = getMaxCharts();
				Log.d(getClass().getSimpleName(), "getting up to " + maxCharts);
				mSites = cscm.getSites(location, maxCharts);

				mHandler
						.sendMessage(Message.obtain(mHandler, UPDATE_LIST_WHAT));
				m = Message.obtain(mHandler,
						SHOW_LIST_WHAT);
				mHandler.sendMessage(m);
				mHandler.sendMessage(Message.obtain(mHandler,
						UPDATE_LOCATION_DIALOG_DISMISS_WHAT));
			}
		}).start();
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case UPDATE_LOCATION_DIALOG: {
			mUpdateLocationDialog = new ProgressDialog(this);
			mUpdateLocationDialog
					.setMessage("Finding closest charts, please wait.");
			mUpdateLocationDialog.setIndeterminate(true);
			mUpdateLocationDialog.setCancelable(false);
			return mUpdateLocationDialog;
		}
		case REFRESH_ERROR_DIALOG: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error preparing charts");
			builder.setMessage(mRefreshError);
			builder.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(REFRESH_ERROR_DIALOG);
						}
					});
			mRefreshErrorDialog = builder.create();
			return mRefreshErrorDialog;
		}
		case LOCATION_WAIT_DIALOG: {
			mLocationWaitDialog = new ProgressDialog(this);
			mLocationWaitDialog.setIndeterminate(true);
			mLocationWaitDialog.setMessage("Waiting for location. "
					+ "You may want to enable GPS, "
					+ "WiFi, or ensure you have a strong cell signal.");
			return mLocationWaitDialog;
		}
		}
		return null;
	}
}