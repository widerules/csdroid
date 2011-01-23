package org.jtb.csdroid;

import java.util.List;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.donate.R;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class FaveShortcutDialogBuilder extends AlertDialog.Builder {

	private TabWidgetActivity activity;
	private Prefs prefs;
	private List<Site> sites;
	
	public FaveShortcutDialogBuilder(TabWidgetActivity activity) {
		super(activity);
		this.activity = activity;
		this.prefs = new Prefs(activity);
		
		this.setTitle("Select Shortcut Site");
		
		LayoutInflater inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ListView listView = (ListView) inflater.inflate(
				R.layout.fave_dialog,
				(ViewGroup) activity.findViewById(R.id.fave_list));

		String f = prefs.getString("faves", "");
		Faves faves = new Faves(f);

		sites = CSCManager.getInstance(activity).getSites(
				TabWidgetActivity.mLocation, faves.getFaves());
		
		listView.setAdapter(new CSCAdapter(activity, sites));
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				FaveShortcutDialogBuilder.this.activity.dismissDialog(TabWidgetActivity.FAVE_SHORTCUT_DIALOG);		
				Site fave = FaveShortcutDialogBuilder.this.sites.get(position);
				FaveShortcutDialogBuilder.this.activity.saveShortcut(fave);
			}
		});
		setView(listView);
	}
}
