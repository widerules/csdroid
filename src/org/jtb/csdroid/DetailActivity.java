package org.jtb.csdroid;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class DetailActivity extends Activity {
	private Site mSite;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail);
		
		String id = savedInstanceState != null ? (String) savedInstanceState
				.get("org.jtb.csdriod.site.id") : null;
		if (id == null) {
			Bundle extras = getIntent().getExtras();
			id = extras != null ? (String) extras
					.get("org.jtb.csdroid.site.id") : null;
		}		
		
		mSite = CSCManager.getInstance(this).getSite(id);
		if (mSite == null) {
			Log.e(getClass().getSimpleName(), "could not get site");
			return;
		}
		
		ImageView iv = (ImageView) findViewById(R.id.detail_img);
		BufferedInputStream bis;
		try {
			bis = new BufferedInputStream(new FileInputStream(mSite.getDetailImageFile()), 1024);
		} catch (FileNotFoundException e) {
			Log.e(getClass().getSimpleName(), "could not read detail image", e);
			return;
		}		
		Bitmap bm = BitmapFactory.decodeStream(bis);
		iv.setImageBitmap(bm);	
		iv.setOnClickListener(new View.OnClickListener() {		
			public void onClick(View v) {
				String u = mSite.getUrl();
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
				startActivity(i);
			}
		});
	}
}