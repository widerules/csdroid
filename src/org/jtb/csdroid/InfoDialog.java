package org.jtb.csdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public class InfoDialog extends AlertDialog {

	public static class Builder extends AlertDialog.Builder {
		public Builder(Context context) {
			super(context);
			
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.info, null);
			layout.setMinimumHeight(180);
			layout.setMinimumWidth(240);
			setView(layout);
			setNeutralButton(R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
		}		
	}
	
	public InfoDialog(Context context) {
		super(context);
	}
}
