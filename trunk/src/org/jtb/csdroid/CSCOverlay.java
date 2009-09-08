package org.jtb.csdroid;

import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CSCOverlay extends ItemizedOverlay {
	private List<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	public CSCOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	@Override
	protected boolean onTap(int i) {
		Toast.makeText(CSCMapActivity.mThis, mOverlays.get(i).getTitle(),
				Toast.LENGTH_SHORT).show();
		return (true);
	}
}
