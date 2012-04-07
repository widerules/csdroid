package org.jtb.csdroid;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Conditions;
import org.jtb.csc.Site;
import org.jtb.csc.ViewRating;
import org.jtb.csdroid.donate.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CSCAdapter extends ArrayAdapter<Site> {
	private Activity context;
	private List<Site> sites;
	private Units units;
	private LayoutInflater inflater;

	CSCAdapter(Activity context, List<Site> sites) {
		super(context, R.layout.csc, sites);

		this.context = context;
		this.sites = sites;
		Prefs prefs = new Prefs(context);
		this.units = prefs.getUnits();
		this.inflater = context.getLayoutInflater();
	}

	public View getView(int position, View convertView, final ViewGroup parent) {
		View view;
		if (convertView != null) {
			view = convertView;
		} else {
			view = inflater.inflate(R.layout.csc, null);
		}
		final Site s = sites.get(position);

		ViewRating vr = ViewRating.NONE;
		CSCManager cscm = CSCManager.getInstance(context);
		Conditions cs = cscm.getConditions(s);
		if (cs != null) {
			vr = cs.getViewRating();
		}

		View ratingView = view.findViewById(R.id.csc_rating);
		ratingView.setBackgroundColor(vr.getColor());

		TextView label = (TextView) view.findViewById(R.id.csc_label);
		String name = s.getName();
		String region = s.getRegion();
		label.setText(region + " - " + name);

		TextView ratingLabel = (TextView) view
				.findViewById(R.id.csc_rating_label);
		String r = vr.toDisplayString() + " conditions";
		ratingLabel.setText(r);

		TextView distance = (TextView) view.findViewById(R.id.csc_distance);
		if (s.isLocatable()) {
			float d = s.getDistance();
			if (units == Units.METRIC) {
				distance.setText((int) (d / 1000) + " km");
			} else {
				distance.setText((int) (d / 1000 * Units.MILES_MUTLIPLIER)
						+ " miles");
			}
		} else {
			distance.setText("unknown location");
		}

		// load summary image

		final ImageView summaryImg = (ImageView) view
				.findViewById(R.id.csc_summary_img);

		new AsyncTask<Void, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Void... vs) {
				BufferedInputStream bis = null;
				try {
					bis = new BufferedInputStream(new FileInputStream(
							s.getSummaryImageFile()), 512);
					Bitmap bm = BitmapFactory.decodeStream(bis);
					return bm;
				} catch (FileNotFoundException e) {
					Log.e(getClass().getSimpleName(),
							"could not read summary image", e);
					return null;
				} finally {
					if (bis != null) {
						try {
							bis.close();
						} catch (IOException e) {
						}
					}
				}
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				if (result != null) {
					summaryImg.setImageBitmap(result);
				}
			}
		}.execute();

		return view;
	}
}
