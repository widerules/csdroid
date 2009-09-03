package org.jtb.csdroid;

import java.util.List;

import org.jtb.csc.Site;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CSCAdapter extends ArrayAdapter {
	private Activity context;
	private List<Site> sites;

	CSCAdapter(Activity context, List<Site> sites) {
		super(context, R.layout.csc, sites);

		this.context = context;
		this.sites = sites;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View row = inflater.inflate(R.layout.csc, null);
		Site s = sites.get(position);
		
		TextView label = (TextView) row.findViewById(R.id.csc_label);
		String name = s.getName();
		String region = s.getRegion();
		label.setText(region + " - " + name);

		ImageView summaryImg = (ImageView) row.findViewById(R.id.csc_summary_img);
		Bitmap bm = BitmapFactory.decodeFile(s.getSummaryImageFile().toString());
		summaryImg.setImageBitmap(bm);
		
		return row;
	}	
}
