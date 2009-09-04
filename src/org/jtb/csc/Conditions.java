package org.jtb.csc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.util.Log;

public class Conditions {
	private List<Condition> conditions = new ArrayList<Condition>();
	private ViewRating viewRating = null;

	public Conditions(InputStream is) {
		parse(is);
	}

	private void parse(InputStream is) {
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"),
					512);

			// title
			br.readLine();
			// version
			br.readLine();
			// UTC offset
			br.readLine();
			// blocks open
			br.readLine();
			// block column comments
			br.readLine();

			String line;
			while (!(line = br.readLine()).equals(")")) {
				Condition c = new Condition(line);
				getConditions().add(c);
			}

		} catch (IOException ioe) {
			Log.e(getClass().getSimpleName(), "error reading condition", ioe);
		}
	}

	public List<Condition> getConditions() {
		return conditions;
	}

	public ViewRating getViewRating() {
		if (viewRating != null) {
			return viewRating;
		}
		int best = 0;
		Date now = new Date();
		for (Condition c : conditions) {
			if (c.getDate().compareTo(now) <= 0) {
				continue;
			}
			if (c.getRating() > best) {
				best = c.getRating();
			}
		}
		viewRating = ViewRating.valueOf(best);
		// Log.i(getClass().getSimpleName(), "best: " + best + ", view rating: "
		// + viewRating);
		return viewRating;
	}
}
