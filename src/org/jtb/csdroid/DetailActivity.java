package org.jtb.csdroid;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.donate.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
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

		new AsyncTask<String, Void, Site>() {
			@Override
			protected Site doInBackground(String... ids) {
				Site site = CSCManager.getInstance(DetailActivity.this)
						.getSite(ids[0]);
				return site;
			}

			@Override
			protected void onPostExecute(Site result) {
				mSite = result;
				loadUi();
			}
		}.execute(id);
	}

	private void loadUi() {
		final ImageView iv = (ImageView) findViewById(R.id.detail_img);
		new AsyncTask<Void, Void, Bitmap>() {
			@Override
			protected Bitmap doInBackground(Void... vs) {
				BufferedInputStream bis = null;
				try {
					bis = new BufferedInputStream(new FileInputStream(
							mSite.getDetailImageFile()), 1024);
					Bitmap bm = BitmapFactory.decodeStream(bis);
					return bm;
				} catch (FileNotFoundException e) {
					Log.e(getClass().getSimpleName(),
							"could not read detail image", e);
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
					iv.setImageBitmap(result);
					iv.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							String u = mSite.getUrl();
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
							startActivity(i);
						}
					});
				}
			}
		}.execute();		
	}
}