package org.jtb.csdroid;

import java.io.IOException;
import java.util.List;

import org.jtb.csc.CSCLocation;
import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.donate.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;

public class TabWidgetActivity extends TabActivity {
	// static final int SERVICE_START_DIALOG_SHOW_WHAT = 0;
	private final int SERVICE_START_DIALOG_DISMISS_WHAT = 1;
	public static final int REFRESH_ERROR_SHOW_WHAT = 2;
	static final int REFRESH_ERROR_HIDE_WHAT = 3;
	static final int FAVE_SHORTCUT_SHOW_WHAT = 4;

	private static final int INFO_DIALOG = 0;
	private static final int REFRESH_DIALOG = 1;
	private static final int SERVICE_START_DIALOG = 2;
	private static final int ADDRESS_DIALOG = 3;
	private static final int LOCATION_ERROR_DIALOG = 4;
	private static final int GEOCODE_ERROR_DIALOG = 5;
	private static final int REFRESH_ERROR_DIALOG = 6;
	static final int FAVE_SHORTCUT_DIALOG = 7;

	private static final int INFO_MENU = 0;
	private static final int ADDRESS_MENU = 1;
	private static final int REFRESH_MENU = 2;
	private static final int PREFS_MENU = 3;
	private static final int MY_LOCATION_MENU = 4;

	private static final int PREFS_REQUEST = 0;

	static CSCLocation mLocation = new CSCLocation();

	private TabHost mTabHost;
	private TabWidgetActivity mThis;

	private Dialog mInfoDialog = null;
	private AlertDialog mRefreshDialog = null;
	private ProgressDialog mServiceStartDialog;
	private AlertDialog mAddressDialog;
	private AlertDialog mGeocodeErrorDialog;
	private AlertDialog mLocationErrorDialog;
	private AlertDialog mRefreshErrorDialog;
	Dialog mFaveShortcutDialog = null;

	private Prefs mPrefs;
	private String mRefreshError = null;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SERVICE_START_DIALOG_DISMISS_WHAT:
				if (mServiceStartDialog.isShowing()) {
					mServiceStartDialog.hide();
				}
				break;
			case REFRESH_ERROR_SHOW_WHAT:
				mRefreshError = (String) msg.obj;
				showDialog(REFRESH_ERROR_DIALOG);
				break;
			case REFRESH_ERROR_HIDE_WHAT:
				if (mRefreshErrorDialog.isShowing()) {
					dismissDialog(REFRESH_ERROR_DIALOG);
				}
				break;
			case FAVE_SHORTCUT_SHOW_WHAT:
				showDialog(FAVE_SHORTCUT_DIALOG);
				break;
			}
		}
	};
	public static Handler mStaticHandler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tabs);

		mThis = this;
		mStaticHandler = mHandler;
		mPrefs = new Prefs(this);

		mTabHost = getTabHost();

		TabHost.TabSpec closestTab = mTabHost.newTabSpec("closest")
				.setIndicator(
						"Closest",
						getResources().getDrawable(
								android.R.drawable.ic_menu_compass));
		TabHost.TabSpec searchTab = mTabHost.newTabSpec("search").setIndicator(
				"Search",
				getResources().getDrawable(android.R.drawable.ic_menu_search));
		TabHost.TabSpec favesTab = mTabHost.newTabSpec("faves").setIndicator(
				"Favorites",
				getResources().getDrawable(android.R.drawable.ic_menu_edit));

		closestTab.setContent(new Intent(this, ClosestActivity.class));
		searchTab.setContent(new Intent(this, SearchActivity.class));
		favesTab.setContent(new Intent(this, FavesActivity.class));

		mTabHost.addTab(closestTab);
		mTabHost.addTab(searchTab);
		mTabHost.addTab(favesTab);

		String currentTab = mPrefs.getCurrentTab();
		mTabHost.setCurrentTabByTag(currentTab);

		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				mPrefs.setCurrentTab(mTabHost, tabId);
				initActivity();
			}
		});

	}

	@Override
	public void onResume() {
		super.onResume();
		startService();
	}

	private boolean setLocationByAddress(String addr) {
		Geocoder gc = new Geocoder(this);
		List<Address> addrs;
		try {
			addrs = gc.getFromLocationName(addr, 1);
			if (addrs.size() == 0) {
				Log.w(getClass().getSimpleName(), "could not geocode address: "
						+ addr);
				return false;
			}
			Address a = addrs.get(0);

			TabWidgetActivity.mLocation.setLatitude(a.getLatitude());
			TabWidgetActivity.mLocation.setLongitude(a.getLongitude());
			return true;
		} catch (IOException e) {
			Log.w(getClass().getSimpleName(), "could not geocode address: "
					+ addr, e);
			return false;
		}
	}

	private boolean setLocation() {
		String address = mPrefs.getAddress();
		if (address != null && address.length() > 0) {
			if (setLocationByAddress(address)) {
				return true;
			}
		}
		if (!setLocationByProvider()) {
			showDialog(LOCATION_ERROR_DIALOG);
			return false;
		}
		return true;
	}

	private boolean setLocationByProvider() {
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		String name = lm.getBestProvider(new Criteria(), true);
		if (name == null) {
			Log.w(getClass().getSimpleName(), "no location provider returned");
			return false;
		}

		// LocationProvider lp = lm.getProvider(name);

		Location l = lm.getLastKnownLocation(name);
		if (l == null) {
			Log.w(getClass().getSimpleName(), "no last location");
			return false;
		}

		TabWidgetActivity.mLocation.setLatitude(l.getLatitude());
		TabWidgetActivity.mLocation.setLongitude(l.getLongitude());
		return true;
	}

	private void initActivity() {
		String tabId = mPrefs.getCurrentTab();

		if (tabId.equals("closest")) {
			if (setLocation()) {
				Handler h = ClosestActivity.mStaticHandler;
				if (h != null) {
					h.sendMessage(ClosestActivity.mStaticHandler
							.obtainMessage(ClosestActivity.INIT_WHAT));
				}
			}
		} else if (tabId.equals("search")) {
			Handler h = SearchActivity.mStaticHandler;
			if (h != null) {
				h.sendMessage(SearchActivity.mStaticHandler
						.obtainMessage(SearchActivity.INIT_WHAT));
			}
		} else if (tabId.equals("faves")) {
			Handler h = FavesActivity.mStaticHandler;
			if (h != null) {
				h.sendMessage(FavesActivity.mStaticHandler
						.obtainMessage(FavesActivity.INIT_WHAT));
			}
		}
	}

	private void updateActivity() {
		String tabId = mPrefs.getCurrentTab();

		if (tabId.equals("closest")) {
			if (setLocation()) {
				Handler h = ClosestActivity.mStaticHandler;
				if (h != null) {
					h.sendMessage(ClosestActivity.mStaticHandler
							.obtainMessage(ClosestActivity.UPDATE_WHAT));
				}
			}
		} else if (tabId.equals("search")) {
			Handler h = SearchActivity.mStaticHandler;
			if (h != null) {
				h.sendMessage(SearchActivity.mStaticHandler
						.obtainMessage(SearchActivity.UPDATE_WHAT));
			}
		} else if (tabId.equals("faves")) {
			Handler h = FavesActivity.mStaticHandler;
			if (h != null) {
				h.sendMessage(FavesActivity.mStaticHandler
						.obtainMessage(FavesActivity.UPDATE_WHAT));
			}
		}
	}

	private void resetActivities() {
		Handler h = ClosestActivity.mStaticHandler;
		if (h != null) {
			h.sendMessage(ClosestActivity.mStaticHandler
					.obtainMessage(ClosestActivity.RESET_WHAT));
		}
		h = SearchActivity.mStaticHandler;
		if (h != null) {
			h.sendMessage(SearchActivity.mStaticHandler
					.obtainMessage(SearchActivity.RESET_WHAT));
		}
		h = FavesActivity.mStaticHandler;
		if (h != null) {
			h.sendMessage(FavesActivity.mStaticHandler
					.obtainMessage(FavesActivity.RESET_WHAT));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, INFO_MENU, 0, R.string.info_menu).setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0, MY_LOCATION_MENU, 1, R.string.my_location_menu).setIcon(
				android.R.drawable.ic_menu_compass);
		menu.add(0, ADDRESS_MENU, 2, R.string.address_menu).setIcon(
				android.R.drawable.ic_menu_send);
		menu.add(0, REFRESH_MENU, 3, R.string.refresh_menu).setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(0, PREFS_MENU, 4, R.string.prefs_menu).setIcon(
				android.R.drawable.ic_menu_preferences);
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
		case ADDRESS_MENU:
			showDialog(ADDRESS_DIALOG);
			return true;
		case MY_LOCATION_MENU:
			if (setLocationByProvider()) {
				mPrefs.setAddress("");
				resetActivities();
				updateActivity();
				mTabHost.setCurrentTabByTag("closest");
			} else {
				showDialog(LOCATION_ERROR_DIALOG);
			}
			return true;
		case PREFS_MENU:
			Intent prefsActivity = new Intent(getBaseContext(),
					PrefsActivity.class);
			startActivityForResult(prefsActivity, PREFS_REQUEST);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PREFS_REQUEST:
			/*
			 * if (resultCode == PrefsActivity.CHANGED_RESULT) {
			 * resetActivities(); updateActivity(); }
			 */
			resetActivities();
			updateActivity();

			break;
		}
	}

	public void startService() {
		showDialog(SERVICE_START_DIALOG);
		new Thread(new Runnable() {
			public void run() {
				Looper.prepare();
				try {
					CSCManager.getInstance(mThis);
				} finally {
					mHandler.sendMessage(Message.obtain(mHandler,
							SERVICE_START_DIALOG_DISMISS_WHAT));
				}

				String action = getIntent().getAction();
				String id = null;
				Bundle extras = getIntent().getExtras();
				if (extras != null) {
					id = extras.getString("org.jtb.csdroid.site.id");
				}

				// request to create shortcut?
				if (action != null
						&& action
								.equals("android.intent.action.CREATE_SHORTCUT")) {
					mHandler.sendEmptyMessage(FAVE_SHORTCUT_SHOW_WHAT);
					// request to open details?
				} else if (id != null) {
					Intent i = new Intent(TabWidgetActivity.this,
							DetailActivity.class);
					i.putExtra("org.jtb.csdroid.site.id", id);
					startActivity(i);
					finish();
					// request to open application
				} else {
					resetActivities();
					initActivity();
				}
			}
		}).start();
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case REFRESH_DIALOG: {
			if (mRefreshDialog == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("Charts update automatically. Manual updates can add extra load to servers.\n\nAre you sure?");
				builder.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dismissDialog(REFRESH_DIALOG);
								CSCManager.getInstance(mThis).clearCache();
								startService();
							}
						});
				builder.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dismissDialog(REFRESH_DIALOG);
							}
						});
				mRefreshDialog = builder.create();
			}
			return mRefreshDialog;
		}
		case INFO_DIALOG: {
			AlertDialog.Builder builder = new InfoDialog.Builder(this);
			mInfoDialog = builder.create();
			return mInfoDialog;
		}
		case SERVICE_START_DIALOG: {
			if (mServiceStartDialog == null) {
				mServiceStartDialog = new ProgressDialog(this);
				mServiceStartDialog
						.setMessage("Preparing charts, please wait.");
				mServiceStartDialog.setIndeterminate(true);
				mServiceStartDialog.setCancelable(false);
			}
			return mServiceStartDialog;
		}
		case ADDRESS_DIALOG: {
			LayoutInflater factory = LayoutInflater.from(this);
			final View zipView = factory.inflate(R.layout.address_dialog, null);
			final EditText zipEdit = (EditText) zipView
					.findViewById(R.id.address);
			mAddressDialog = new AlertDialog.Builder(this)
					.setTitle("Enter Address / Zip Code")
					.setView(zipView)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									String address = zipEdit.getText()
											.toString();
									dismissDialog(ADDRESS_DIALOG);
									if (setLocationByAddress(address)) {
										mPrefs.setAddress(address);
										resetActivities();
										updateActivity();
										mTabHost.setCurrentTabByTag("closest");
									} else {
										showDialog(GEOCODE_ERROR_DIALOG);
									}
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dismissDialog(ADDRESS_DIALOG);
								}
							}).create();
			return mAddressDialog;
		}
		case LOCATION_ERROR_DIALOG: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Warning");
			builder.setMessage("Could not determine your location. Enable network or GPS location services, or use Menu>Go to Address.");
			builder.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(LOCATION_ERROR_DIALOG);
						}
					});
			mLocationErrorDialog = builder.create();
			return mLocationErrorDialog;
		}
		case GEOCODE_ERROR_DIALOG: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Could not find location for that address / zip.");
			builder.setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(GEOCODE_ERROR_DIALOG);
						}
					});
			mGeocodeErrorDialog = builder.create();
			return mGeocodeErrorDialog;
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
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			mRefreshErrorDialog = builder.create();
			return mRefreshErrorDialog;
		}
		case FAVE_SHORTCUT_DIALOG: {
			if (mFaveShortcutDialog == null) {
				AlertDialog.Builder builder = new FaveShortcutDialogBuilder(
						this);
				mFaveShortcutDialog = builder.create();
			}
			return mFaveShortcutDialog;
		}
		}
		return null;
	}

	void saveShortcut(Site site) {
		Intent shortcutIntent = new Intent(this, TabWidgetActivity.class);
		// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		// TODO: get side ID and add it
		shortcutIntent.putExtra("org.jtb.csdroid.site.id", site.getId());

		Intent intent = new Intent();
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, site.getName());
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(this, R.drawable.icon));
		intent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

		setResult(RESULT_OK, intent);
		finish();
	}

}
