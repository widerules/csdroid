package org.jtb.csdroid;

import org.jtb.csc.CSCManager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

public class TabWidgetActivity extends TabActivity {
	static final int SERVICE_START_DIALOG_SHOW_WHAT = 0;
	static final int SERVICE_START_DIALOG_DISMISS_WHAT = 1;
	public static final int SERVICE_START_WHAT = 2;

	private static final int INFO_DIALOG = 0;
	private static final int REFRESH_DIALOG = 1;
	private static final int SERVICE_START_DIALOG = 2;

	private static final int INFO_MENU = 0;
	private static final int REFRESH_MENU = 1;
	private static final int PREFS_MENU = 2;

	private static final int PREFS_REQUEST = 0;

	private TabHost mTabHost;
	private View mSearchView;
	private View mClosestView;
	private TabWidgetActivity mThis;

	private Dialog mInfoDialog = null;
	private AlertDialog mRefreshDialog = null;
	private ProgressDialog mServiceStartDialog;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SERVICE_START_WHAT:
				startService();
				break;
			case SERVICE_START_DIALOG_SHOW_WHAT:
				showDialog(SERVICE_START_DIALOG);
				break;
			case SERVICE_START_DIALOG_DISMISS_WHAT:
				dismissDialog(SERVICE_START_DIALOG);
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
		
		mSearchView = findViewById(R.id.search);
		mClosestView = findViewById(R.id.closest);

		mTabHost = getTabHost();
		mTabHost.addTab(mTabHost.newTabSpec("closest").setIndicator("Closest",
				getResources().getDrawable(R.drawable.location)).setContent(
				new Intent(this, ClosestActivity.class)));
		mTabHost.addTab(mTabHost.newTabSpec("search").setIndicator("Search",
				getResources().getDrawable(R.drawable.search)).setContent(
				new Intent(this, SearchActivity.class)));

		String currentTab = getCurrentTab();
		setTabsVisible(currentTab);

		mTabHost.setCurrentTabByTag(currentTab);
		mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				setCurrentTab(tabId);
				setTabsVisible(tabId);
			}
		});
		
		startService();
	}

	private void setCurrentTab(String tabId) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mThis.getBaseContext());
		String currentTab = mTabHost.getCurrentTabTag();
		Editor e = prefs.edit();
		e.putString("currentTab", currentTab);
		e.commit();
	}

	private String getCurrentTab() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(mThis.getBaseContext());
		String currentTab = prefs.getString("currentTab", "closest");
		return currentTab;
	}

	private void setTabsVisible(String tabId) {
		if (tabId.equals("closest")) {
			mClosestView.setVisibility(View.VISIBLE);
			mSearchView.setVisibility(View.GONE);
		} else if (tabId.equals("search")) {
			mClosestView.setVisibility(View.GONE);
			mSearchView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, INFO_MENU, 0, R.string.info_menu).setIcon(R.drawable.info);
		menu.add(0, REFRESH_MENU, 1, R.string.refresh_menu).setIcon(
				R.drawable.refresh);
		menu.add(0, PREFS_MENU, 2, R.string.prefs_menu).setIcon(
				R.drawable.prefs);
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
			if (resultCode == PrefsActivity.CHANGED_RESULT) {
				updateActivities();
			}
			break;
		}
	}

	private void updateActivities() {
		Handler h = ClosestActivity.mStaticHandler;
		if (h != null) {
			h.sendMessage(ClosestActivity.mStaticHandler
					.obtainMessage(ClosestActivity.UPDATE_WHAT));
		}
		h = SearchActivity.mStaticHandler;
		if (h != null) {
			h.sendMessage(SearchActivity.mStaticHandler
					.obtainMessage(SearchActivity.UPDATE_WHAT));
		}
	}

	public void startService() {
		new Thread(new Runnable() {
			public void run() {
				mHandler.sendMessage(Message.obtain(mHandler,
						SERVICE_START_DIALOG_SHOW_WHAT));
				CSCManager.getInstance(mThis);
				mHandler.sendMessage(Message.obtain(mHandler,
						SERVICE_START_DIALOG_DISMISS_WHAT));

				CSCManager.getInstance(mThis);
				updateActivities();
				
				Intent i = new Intent(mThis, CSCService.class);
				mThis.startService(i);
			}
		}).start();
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case REFRESH_DIALOG: {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder
					.setMessage("Charts update automatically. Manual updates can add extra load to servers.\n\nAre you sure?");
			builder.setPositiveButton(R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(REFRESH_DIALOG);
							CSCManager.getInstance(mThis).clearCache();
							startService();
						}
					});
			builder.setNegativeButton(R.string.no,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dismissDialog(REFRESH_DIALOG);
						}
					});
			mRefreshDialog = builder.create();
			return mRefreshDialog;
		}
		case INFO_DIALOG: {
			AlertDialog.Builder builder = new InfoDialog.Builder(this);
			mInfoDialog = builder.create();
			return mInfoDialog;
		}
		case SERVICE_START_DIALOG: {
			mServiceStartDialog = new ProgressDialog(this);
			mServiceStartDialog.setMessage("Preparing charts, please wait.");
			mServiceStartDialog.setIndeterminate(true);
			mServiceStartDialog.setCancelable(false);
			return mServiceStartDialog;
		}
		}
		return null;
	}
}
