package org.jtb.csdroid;

import java.util.List;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Conditions;
import org.jtb.csc.Site;
import org.jtb.csc.ViewRating;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
		View view = inflater.inflate(R.layout.csc, null);
		Site s = sites.get(position);
	
		ViewRating vr = ViewRating.NONE;
		CSCManager cscm = CSCManager.getInstance(context);
		Conditions cs = cscm.getConditions(s);
		if (cs != null) {
			vr = cs.getViewRating();
		}
		
		View ratingView = view.findViewById(R.id.csc_rating);
		ratingView.setBackgroundColor(vr.getColor());
		
		
		/*
		TableRow r = (TableRow)table.findViewById(R.id.csc_text_row);
		r.setBackgroundColor(vr.getColor());
		r = (TableRow)table.findViewById(R.id.csc_image_row);
		r.setBackgroundColor(vr.getColor());
		*/
		
		TextView label = (TextView) view.findViewById(R.id.csc_label);
		String name = s.getName();
		String region = s.getRegion();
		label.setText(region + " - " + name);

		ImageView summaryImg = (ImageView) view.findViewById(R.id.csc_summary_img);
		Bitmap bm = BitmapFactory.decodeFile(s.getSummaryImageFile().toString());
		summaryImg.setImageBitmap(bm);
		
		return view;
	}	
}
