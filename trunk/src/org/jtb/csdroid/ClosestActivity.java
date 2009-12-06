package org.jtb.csdroid;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import org.jtb.csc.CSCLocation;
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
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
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

public class ClosestActivity extends Activity {
	static final int UPDATE_LOCATION_DIALOG_SHOW_WHAT = 0;
	static final int UPDATE_LOCATION_DIALOG_DISMISS_WHAT = 1;
	static final int UPDATE_LIST_WHAT = 6;
	public static final int REFRESH_ERROR_SHOW_WHAT = 10;
	static final int REFRESH_ERROR_HIDE_WHAT = 11;
	static final int UNKNOWN_LOCATION_DIALOG_SHOW_WHAT = 12;
	static final int UNKNOWN_LOCATION_DIALOG_DISMISS_WHAT = 13;
	static final int INIT_WHAT = 15;
	static final int HIDE_LIST_WHAT = 16;
	static final int SHOW_LIST_WHAT = 17;
	static final int UPDATE_WHAT = 18;
	static final int RESET_WHAT = 19;

	private static final int UPDATE_LOCATION_DIALOG = 0;
	private static final int REFRESH_ERROR_DIALOG = 3;
	private static final int UNKNOWN_LOCATION_DIALOG = 4;

	private ProgressDialog mUpdateLocationDialog;
	private AlertDialog mRefreshErrorDialog;
	private AlertDialog mListClickDialog;
	private AlertDialog mUnknownLocationDialog = null;

	private List<Site> mSites;
	private ListView mCSCListView;
	private ClosestActivity mThis;
	private Timer mTimer = new Timer();
	private String mRefreshError = null;
		
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
			case INIT_WHAT:
				init();
				break;
			case RESET_WHAT:
				mSites = null;
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
			case UNKNOWN_LOCATION_DIALOG_SHOW_WHAT:
				showDialog(UNKNOWN_LOCATION_DIALOG);
				break;
			case UNKNOWN_LOCATION_DIALOG_DISMISS_WHAT:
				dismissDialog(UNKNOWN_LOCATION_DIALOG);
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
	
	private void init() {
		if (mSites == null) {
			update();
		}
	}	

	private void update() {
		new Thread(new Runnable() {
			public void run() {
				Message m = Message.obtain(mHandler,
						UPDATE_LOCATION_DIALOG_SHOW_WHAT);
				mHandler.sendMessage(m);
				m = Message.obtain(mHandler,
						HIDE_LIST_WHAT);
				mHandler.sendMessage(m);

				CSCManager cscm = CSCManager.getInstance(mThis);
				int maxCharts = getMaxCharts();
				Log.d(getClass().getSimpleName(), "getting up to " + maxCharts);
				mSites = cscm.getSites(TabWidgetActivity.mLocation, maxCharts);

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
		case UNKNOWN_LOCATION_DIALOG: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Cannot determine your location. You may want to enable GPS and / or network based location services, or use Menu>Go to Zip");
			builder.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(UNKNOWN_LOCATION_DIALOG);
						}
					});
			mUnknownLocationDialog = builder.create();
			return mUnknownLocationDialog;
		}
		}
		return null;
	}
}