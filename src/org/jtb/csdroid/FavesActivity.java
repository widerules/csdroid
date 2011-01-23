package org.jtb.csdroid;

import java.util.List;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.donate.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FavesActivity extends Activity implements
		OnSharedPreferenceChangeListener {
	private static final int UPDATE_DIALOG = 0;

	static final int INIT_WHAT = 0;
	static final int UPDATE_DIALOG_SHOW_WHAT = 1;
	static final int UPDATE_DIALOG_DISMISS_WHAT = 2;
	static final int UPDATE_LIST_WHAT = 4;
	static final int HIDE_LIST_WHAT = 16;
	static final int SHOW_LIST_WHAT = 17;
	static final int UPDATE_WHAT = 18;
	static final int RESET_WHAT = 19;

	private List<Site> mSites;
	private ListView mCSCListView;
	private FavesActivity mThis;

	private AlertDialog mListClickDialog;
	private ProgressDialog mUpdateDialog;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
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
			case UPDATE_DIALOG_SHOW_WHAT:
				showDialog(UPDATE_DIALOG);
				break;
			case UPDATE_DIALOG_DISMISS_WHAT:
				if (mUpdateDialog != null && mUpdateDialog.isShowing()) {
					mUpdateDialog.hide();
				}
				break;
			case HIDE_LIST_WHAT:
				mCSCListView.setVisibility(View.GONE);
				break;
			case SHOW_LIST_WHAT:
				mCSCListView.setVisibility(View.VISIBLE);
				break;
			}
		}
	};
	public static Handler mStaticHandler = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.faves);

		mThis = this;
		mStaticHandler = mHandler;

		mCSCListView = (ListView) findViewById(R.id.csc_list);
		mCSCListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				AlertDialog.Builder builder = new ListClickDialog.Builder(
						mThis, mSites, position);
				mListClickDialog = builder.create();
				mListClickDialog.show();
			}
		});

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	private void updateList() {
		if (mSites != null) {
			CSCAdapter csca = new CSCAdapter(mThis, mSites);
			mCSCListView.setAdapter(csca);
		}
	}

	private void init() {
		if (mSites == null) {
			update();
		}
	}

	private void update() {
		new Thread(new Runnable() {
			public void run() {
				Message m = Message.obtain(mHandler, UPDATE_DIALOG_SHOW_WHAT);
				mHandler.sendMessage(m);
				m = Message.obtain(mHandler, HIDE_LIST_WHAT);
				mHandler.sendMessage(m);

				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getBaseContext());
				String f = prefs.getString("faves", "");
				Faves faves = new Faves(f);

				mSites = CSCManager.getInstance(getBaseContext()).getSites(
						TabWidgetActivity.mLocation, faves.getFaves());
				m = Message.obtain(mHandler, UPDATE_LIST_WHAT);
				mHandler.sendMessage(m);

				m = Message.obtain(mHandler, SHOW_LIST_WHAT);
				mHandler.sendMessage(m);
				m = Message.obtain(mHandler, UPDATE_DIALOG_DISMISS_WHAT);
				mHandler.sendMessage(m);

			}
		}).start();
	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case UPDATE_DIALOG: {
			if (mUpdateDialog == null) {
				mUpdateDialog = new ProgressDialog(this);
				mUpdateDialog.setMessage("Loading favorites, please wait.");
				mUpdateDialog.setIndeterminate(true);
				mUpdateDialog.setCancelable(false);
			}
			return mUpdateDialog;
		}
		}
		return null;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("faves")) {
			mSites = null;
			update();
		}
	}
}
