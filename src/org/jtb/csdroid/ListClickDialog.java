package org.jtb.csdroid;

import java.util.ArrayList;
import java.util.List;

import org.jtb.csc.Site;
import org.jtb.csdroid.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ListClickDialog extends AlertDialog {
	public static class Builder extends AlertDialog.Builder {
		private List<Site> mSites;
		private int mPosition;
		private Context mContext;
		private boolean fave = false;

		public Builder(Context context, List<Site> sites, int position) {
			super(context);
			this.mSites = sites;
			this.mPosition = position;
			this.mContext = context;
			setFave();

			setItems(getListItems(), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Site s = mSites.get(mPosition);
					AlertDialog ad = (AlertDialog) dialog;
					switch (which) {
					case 0:
						Intent i = new Intent(ad.getContext(),
								DetailActivity.class);
						i.putExtra("org.jtb.csdroid.site.id", s.getId());
						ad.getContext().startActivity(i);
						break;
					case 1:
						i = new Intent(ad.getContext(), CSCMapActivity.class);
						i.putExtra("org.jtb.csdroid.site.id", s.getId());
						ad.getContext().startActivity(i);

						// String uri = "geo:"+ s.getLatitude() + "," +
						// s.getLongitude() + "?z=16";
						// ad.getContext().startActivity(new
						// Intent(android.content.Intent.ACTION_VIEW,
						// Uri.parse(uri)));
						break;
					case 2:
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(ad.getContext());
						String f = prefs.getString("faves", "");
						Faves faves = new Faves(f);

						if (fave) {
							faves.getFaves().remove(s.getId());
						} else {
							faves.getFaves().add(s.getId());
						}

						Editor e = prefs.edit();
						e.putString("faves", faves.toString());
						e.commit();

						break;
					}
				}
			});
			setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
		}

		private void setFave() {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			String f = prefs.getString("faves", "");
			Faves faves = new Faves(f);
			Site site = mSites.get(mPosition);
			if (faves.getFaves().contains(site.getId())) {
				fave = true;
			}
		}

		private String[] getListItems() {
			ArrayList<String> items = new ArrayList<String>();
			items.add("Details");
			items.add("Map");
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(mContext);
			String f = prefs.getString("faves", "");
			if (fave) {
				items.add("Remove From Favorities");
			} else {
				items.add("Add To Favorities");
			}

			return items.toArray(new String[0]);
		}
	}

	public ListClickDialog(Context context, List<Site> sites) {
		super(context);
	}

}
