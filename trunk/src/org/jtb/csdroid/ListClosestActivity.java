package org.jtb.csdroid;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Conditions;
import org.jtb.csc.Site;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ListClosestActivity extends Activity implements LocationListener {
	static final int UPDATE_LOCATION_DIALOG_SHOW_WHAT = 0;
	static final int UPDATE_LOCATION_DIALOG_DISMISS_WHAT = 1;
	static final int SERVICE_START_DIALOG_SHOW_WHAT = 2;
	static final int SERVICE_START_DIALOG_DISMISS_WHAT = 3;
	static final int SERVICE_UPDATE_DIALOG_SHOW_WHAT = 4;
	static final int SERVICE_UPDATE_DIALOG_DISMISS_WHAT = 5;
	static final int UPDATE_LIST_WHAT = 6;
	static final int UPDATE_LOCATION_WHAT = 7;
	static final int UPDATE_SERVICE_WHAT = 8;
	static final int HIDE_LIST_WHAT = 9;
	public static final int REFRESH_ERROR_SHOW_WHAT = 10;
	public static final int REFRESH_ERROR_HIDE_WHAT = 11;
	public static final int LOCATION_WAIT_DIALOG_SHOW_WHAT = 12;
	public static final int LOCATION_WAIT_DIALOG_DISMISS_WHAT = 13;

	private static final int UPDATE_LOCATION_DIALOG = 0;
	private static final int SERVICE_START_DIALOG = 1;
	private static final int SERVICE_UPDATE_DIALOG = 2;
	private static final int REFRESH_ERROR_DIALOG = 3;
	private static final int LOCATION_WAIT_DIALOG = 4;
	private static final int INFO_DIALOG = 5;
	private static final int REFRESH_DIALOG = 6;

	private static final int INFO_MENU = 0;
	private static final int REFRESH_MENU = 1;

	private ProgressDialog mUpdateLocationDialog;
	private ProgressDialog mServiceStartDialog;
	private ProgressDialog mServiceUpdateDialog;
	private AlertDialog mRefreshErrorDialog;
	private AlertDialog mRefreshDialog;
	private ProgressDialog mLocationWaitDialog = null;
	private Dialog mInfoDialog = null;

	private List<Site> mSites;
	private ListView mCSCListView;
	private TextView mNoChartsTextView;
	private ListClosestActivity mThis;
	private Timer mTimer = new Timer();
	private String mRefreshError = null;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_LIST_WHAT:
				mCSCListView.setVisibility(View.GONE);
				break;
			case UPDATE_SERVICE_WHAT:
				updateService();
				break;
			case UPDATE_LOCATION_WHAT:
				updateLocation();
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
			case SERVICE_START_DIALOG_SHOW_WHAT:
				showDialog(SERVICE_START_DIALOG);
				break;
			case SERVICE_START_DIALOG_DISMISS_WHAT:
				dismissDialog(SERVICE_START_DIALOG);
				break;
			case SERVICE_UPDATE_DIALOG_SHOW_WHAT:
				showDialog(SERVICE_UPDATE_DIALOG);
				break;
			case LOCATION_WAIT_DIALOG_SHOW_WHAT:
				showDialog(LOCATION_WAIT_DIALOG);
				break;
			case LOCATION_WAIT_DIALOG_DISMISS_WHAT:
				dismissDialog(LOCATION_WAIT_DIALOG);
				break;
			case SERVICE_UPDATE_DIALOG_DISMISS_WHAT:
				dismissDialog(SERVICE_UPDATE_DIALOG);
				break;
			case REFRESH_ERROR_SHOW_WHAT:
				closeServiceDialogs();
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

	private void closeServiceDialogs() {
		if (mServiceStartDialog != null) {
			dismissDialog(SERVICE_START_DIALOG);
		}
		if (mServiceUpdateDialog != null) {
			dismissDialog(SERVICE_UPDATE_DIALOG);
		}
	}

	private void updateList() {
		if (mSites == null || mSites.size() == 0) {
			mCSCListView.setVisibility(View.GONE);
			mNoChartsTextView.setVisibility(View.VISIBLE);
		} else {
			CSCAdapter csca = new CSCAdapter(mThis, mSites);
			mCSCListView.setAdapter(csca);
			mNoChartsTextView.setVisibility(View.GONE);
			mCSCListView.setVisibility(View.VISIBLE);
		}
	}

	public void updateLocation() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String name = lm.getBestProvider(new Criteria(), true);
		if (name == null) {
			// TODO: error dialog an exit (this.finish())?
			Log.e(getClass().getSimpleName(),
					"no best location provider returned");
		}
		// LocationProvider lp = lm.getProvider(name);
		onLocationChanged(lm.getLastKnownLocation(name));
		lm.requestLocationUpdates(name, 10 * 60 * 1000, 10 * 1000, mThis); // 10
																			// mins,
																			// 10km
	}

	public void startService() {
		new Thread(new Runnable() {
			public void run() {
				mHandler.sendMessage(Message.obtain(mHandler,
						SERVICE_START_DIALOG_SHOW_WHAT));
				mHandler.sendMessage(Message.obtain(mHandler, HIDE_LIST_WHAT));
				CSCManager.getInstance(mThis);
				mHandler.sendMessage(Message.obtain(mHandler,
						SERVICE_START_DIALOG_DISMISS_WHAT));
				mHandler.sendMessage(Message.obtain(mHandler,
						UPDATE_LOCATION_WHAT));

				/*
				 * mTimer.scheduleAtFixedRate(new TimerTask() { public void
				 * run() { mThis.updateService(); } },
				 * CSCManager.REFRESH_INTERVAL, CSCManager.REFRESH_INTERVAL);
				 */
				Intent i = new Intent(mThis, CSCService.class);
				mThis.startService(i);
			}
		}).start();
	}

	public void updateService() {
		new Thread(new Runnable() {
			public void run() {
				mHandler.sendMessage(Message.obtain(mHandler,
						SERVICE_UPDATE_DIALOG_SHOW_WHAT));
				mHandler.sendMessage(Message.obtain(mHandler, HIDE_LIST_WHAT));

				CSCManager.getInstance(mThis);

				mHandler.sendMessage(Message.obtain(mHandler,
						SERVICE_UPDATE_DIALOG_DISMISS_WHAT));
				mHandler.sendMessage(Message.obtain(mHandler,
						UPDATE_LOCATION_WHAT));
			}
		}).start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.closest);

		mThis = this;
		mStaticHandler = mHandler;

		mNoChartsTextView = (TextView) findViewById(R.id.no_charts_text);
		mCSCListView = (ListView) findViewById(R.id.csc_list);
		mCSCListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Site s = mSites.get(position);
				Intent i = new Intent(parent.getContext(), DetailActivity.class);
				i.putExtra("org.jtb.csdroid.site.id", s.getId());
				startActivity(i);
			}
		});
		/*
		 * mCSCListView.setOnItemSelectedListener(new OnItemSelectedListener() {
		 * public void onItemSelected(AdapterView<?> parent, View view, int
		 * position, long id) {
		 * 
		 * int count = parent.getChildCount(); for (int i = 0; i < count; i++) {
		 * TableLayout tl = (TableLayout)parent.getChildAt(i); Site s =
		 * mSites.get(i); Conditions cs =
		 * CSCManager.getInstance(mThis).getConditions(s); if (i == position) {
		 * // selected int color = cs.getViewRating().getSelectedColor();
		 * tl.setBackgroundColor(color); TextView text =
		 * (TextView)tl.findViewById(R.id.csc_label);
		 * text.setTextColor(Color.parseColor("#333333")); } else { int color =
		 * cs.getViewRating().getColor(); tl.setBackgroundColor(color); TextView
		 * text = (TextView)tl.findViewById(R.id.csc_label);
		 * text.setTextColor(Color.parseColor("#ffffff")); } } }
		 * 
		 * public void onNothingSelected(AdapterView<?> parent) { } });
		 */

		startService();
	}

	public void onLocationChanged(final Location location) {
		new Thread(new Runnable() {
			public void run() {
				if (location == null) {
					mHandler.sendMessage(Message.obtain(mHandler,
							LOCATION_WAIT_DIALOG_SHOW_WHAT));
					return;
				}
				if (mLocationWaitDialog != null) {
					mLocationWaitDialog = null;
					mHandler.sendMessage(Message.obtain(mHandler,
							LOCATION_WAIT_DIALOG_DISMISS_WHAT));
				}
				Message m = Message.obtain(mHandler,
						UPDATE_LOCATION_DIALOG_SHOW_WHAT);
				mHandler.sendMessage(m);

				CSCManager cscm = CSCManager.getInstance(mThis);
				mSites = cscm.getSites(location, 100 * 1000, 10); // 100km
				mHandler
						.sendMessage(Message.obtain(mHandler, UPDATE_LIST_WHAT));
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, INFO_MENU, 0, R.string.info_menu).setIcon(R.drawable.info);
		menu.add(0, REFRESH_MENU, 1, R.string.refresh_menu).setIcon(R.drawable.refresh);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case INFO_MENU:
			showDialog(INFO_DIALOG);
			return true;
		case REFRESH_MENU:
			showDialog(REFRESH_DIALOG);
			return true;
		}

		return super.onOptionsItemSelected(item);
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
		case SERVICE_START_DIALOG: {
			mServiceStartDialog = new ProgressDialog(this);
			mServiceStartDialog.setMessage("Preparing charts, please wait.");
			mServiceStartDialog.setIndeterminate(true);
			mServiceStartDialog.setCancelable(false);
			return mServiceStartDialog;
		}
		case SERVICE_UPDATE_DIALOG: {
			mServiceUpdateDialog = new ProgressDialog(this);
			mServiceUpdateDialog.setMessage("Updating charts, please wait.");
			mServiceUpdateDialog.setIndeterminate(true);
			mServiceUpdateDialog.setCancelable(false);
			return mServiceUpdateDialog;
		}
		case REFRESH_DIALOG: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Charts update automatically. Manual updates can add extra load to servers.\n\nAre you sure?");
			builder.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(REFRESH_DIALOG);
							updateService();
						}
					});
			builder.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(REFRESH_DIALOG);
						}
					});
			mRefreshErrorDialog = builder.create();
			return mRefreshErrorDialog;
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
		case INFO_DIALOG: {
			AlertDialog.Builder builder;

			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.info, null);
			layout.setMinimumHeight(180);
			layout.setMinimumWidth(240);
			builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			builder.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(INFO_DIALOG);
						}
					});
			mInfoDialog = builder.create();

			return mInfoDialog;
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