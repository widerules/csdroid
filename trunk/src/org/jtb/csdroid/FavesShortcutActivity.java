package org.jtb.csdroid;

import java.util.List;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.donate.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class FavesShortcutActivity extends Activity {
	private Prefs prefs;
	private List<Site> sites;
	private ListView faveList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		prefs = new Prefs(this);
		String f = prefs.getString("faves", "");
		Faves faves = new Faves(f);

		if (faves.getFaves().isEmpty()) {
			setContentView(R.layout.fave_dialog_empty);
		} else {

			setContentView(R.layout.fave_dialog);

			faveList = (ListView) this.findViewById(R.id.fave_list);

			sites = CSCManager.getInstance(this).getSites(
					TabWidgetActivity.mLocation, faves.getFaves());

			faveList.setAdapter(new CSCAdapter(this, sites));
			faveList.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v,
						int position, long id) {
					Site fave = sites.get(position);
					saveShortcut(fave);
					finish();
				}
			});
		}
	}

	private void saveShortcut(Site site) {
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
