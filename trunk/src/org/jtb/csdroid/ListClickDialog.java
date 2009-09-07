package org.jtb.csdroid;

import java.util.List;

import org.jtb.csc.Site;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;

public class ListClickDialog extends AlertDialog {	
	public static class Builder extends AlertDialog.Builder {
		private List<Site> mSites;
		private int mPosition;
		
		public Builder(Context context, List<Site> sites, int position) {
			super(context);
			this.mSites = sites;
			this.mPosition = position;

			setItems(R.array.click_entries, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					Site s = mSites.get(mPosition);
					AlertDialog ad = (AlertDialog)dialog;
					switch (which) {
					case 0:
						Intent i = new Intent(ad.getContext(), DetailActivity.class);
						i.putExtra("org.jtb.csdroid.site.id", s.getId());
						ad.getContext().startActivity(i);
						break;
					case 1: 
						String uri = "geo:"+ s.getLatitude() + "," + s.getLongitude() + "?z=16";  
						ad.getContext().startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri))); 	
						break;
					case 2:
						SharedPreferences prefs = PreferenceManager
								.getDefaultSharedPreferences(ad.getContext());
						String f = prefs.getString("faves", "");
						Faves faves = new Faves(f);
						faves.getFaves().add(s.getId());
						Editor e = prefs.edit();
						e.putString("faves", faves.toString());
						e.commit();
					break;
					case 3:
						prefs = PreferenceManager
								.getDefaultSharedPreferences(ad.getContext());
						f = prefs.getString("faves", "");
						faves = new Faves(f);
						faves.getFaves().remove(s.getId());
						e = prefs.edit();
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
	}
	
	public ListClickDialog(Context context, List<Site> sites) {
		super(context); 
	}
	
	
}