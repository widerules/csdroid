package org.jtb.csdroid;

import java.util.List;

import org.jtb.csc.CSCManager;
import org.jtb.csc.Site;
import org.jtb.csdroid.donate.R;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.ZoomControls;

public class CSCMapActivity extends MapActivity {
	static CSCMapActivity mThis;

	private Site mSite;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mThis = this;
		
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
		
		MapView mapView = (MapView) findViewById(R.id.mapview);

		mapView.setBuiltInZoomControls(true);
		
		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.point);
		CSCOverlay itemizedOverlay = new CSCOverlay(drawable);	
		
		GeoPoint point = new GeoPoint(mSite.getLatitudeE6(), mSite.getLongitudeE6());
		OverlayItem overlayitem = new OverlayItem(point, mSite.getName(), "");		
		itemizedOverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedOverlay);		
		
		mapView.getController().setZoom(12);
		mapView.getController().animateTo(point);
	}
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

}
