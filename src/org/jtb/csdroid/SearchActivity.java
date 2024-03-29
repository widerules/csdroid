package org.jtb.csdroid;

import java.util.ArrayList;
import java.util.List;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.donate.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SearchActivity extends Activity {
	private static final int SEARCHING_DIALOG = 0;
	private static final int INFO_DIALOG = 1;

	private static final int SEARCHING_DIALOG_DISMISS_WHAT = 1;
	static final int UPDATE_LIST_WHAT = 2;
	static final int INIT_WHAT = 3;
	static final int UPDATE_WHAT = 4;
	static final int RESET_WHAT = 5;
	static final int HIDE_LIST_WHAT = 16;
	static final int SHOW_LIST_WHAT = 17;

	private ProgressDialog mSearchingDialog;
	private Dialog mInfoDialog;
	private AlertDialog mListClickDialog;

	private TextView mSearchEdit;
	private List<Site> mSites = new ArrayList<Site>();
	private ListView mCSCListView;
	private SearchActivity mThis;
	private Button mSearchButton;
	private String lastSearchString = "";

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INIT_WHAT:
				init();
				break;
			case RESET_WHAT:
				mSites = null;
				lastSearchString = "";
				break;
			case UPDATE_WHAT:
				update();
				break;
			case UPDATE_LIST_WHAT:
				updateList();
				break;
			case SEARCHING_DIALOG_DISMISS_WHAT:
				if (mSearchingDialog != null && mSearchingDialog.isShowing()) {
					mSearchingDialog.hide();
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

	private void updateList() {
		if (mSites != null) {
			CSCAdapter csca = new CSCAdapter(mThis, mSites);
			mCSCListView.setAdapter(csca);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		mThis = this;
		mStaticHandler = mHandler;

		mSearchButton = (Button) findViewById(R.id.search_button);
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				update();
			}
		});
		mSearchEdit = (EditText) findViewById(R.id.search_edit);
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
	}

	private void init() {
		if (mSites == null) {
			update();
		}
	}

	private void update() {
		showDialog(SEARCHING_DIALOG);
		new Thread(new Runnable() {
			public void run() {
				try {
					String searchString = mSearchEdit.getText().toString();
					if (searchString != null) {
						searchString = searchString.trim();
					}
					if (searchString == null || searchString.length() == 0
							|| searchString.equalsIgnoreCase(lastSearchString)) {
						return;
					}
					lastSearchString = searchString;

					Message m = Message.obtain(mHandler, HIDE_LIST_WHAT);
					mHandler.sendMessage(m);

					CSCManager cscm = CSCManager.getInstance(mThis);
					mSites = cscm.getSites(TabWidgetActivity.mLocation,
							mSearchEdit.getText().toString(), getMaxCharts());
					mHandler.sendMessage(Message.obtain(mHandler,
							UPDATE_LIST_WHAT));
					m = Message.obtain(mHandler, SHOW_LIST_WHAT);
					mHandler.sendMessage(m);
				} finally {
					mHandler.sendMessage(Message.obtain(mHandler,
							SEARCHING_DIALOG_DISMISS_WHAT));
				}
			}
		}).start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SEARCHING_DIALOG: {
			if (mSearchingDialog == null) {
				mSearchingDialog = new ProgressDialog(this);
				mSearchingDialog.setMessage("Searching, please wait.");
				mSearchingDialog.setIndeterminate(true);
				mSearchingDialog.setCancelable(false);
			}
			return mSearchingDialog;
		}
		case INFO_DIALOG: {
			if (mInfoDialog == null) {
				AlertDialog.Builder builder = new InfoDialog.Builder(this);
				mInfoDialog = builder.create();
			}
			return mInfoDialog;
		}
		}
		return null;
	}

	private int getMaxCharts() {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		int d = Integer.parseInt(prefs.getString("maxCharts", "5"));
		return d;
	}
}
