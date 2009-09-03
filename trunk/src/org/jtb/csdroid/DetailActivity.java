package org.jtb.csdroid;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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
			//TODO: error
			return;
		}
		
		ImageView iv = (ImageView) findViewById(R.id.detail_img);
		Bitmap bm = BitmapFactory.decodeFile(mSite.getDetailImageFile().toString());
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