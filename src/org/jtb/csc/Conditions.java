package org.jtb.csc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Conditions {
	private static Pattern UTC_OFFSET_PATTERN = Pattern
			.compile("UTC_offset = (-?[0-9]{1,2}).0");

	private ViewRating viewRating = null;

	public Conditions(InputStream is) {
		parse(is);
	}

	private void parse(InputStream is) {
		BufferedReader br = null;
		String line;
		int offset;

		try {
			br = new BufferedReader(new InputStreamReader(is, "ISO-8859-1"),
					512);

			// title
			br.readLine();
			// version
			br.readLine();
			// UTC offset
			// assume we are in same offset as chart.
			// this is probably the case, but if not
			// it only effects the rating
			line = br.readLine();
			/*
			 * Matcher m = UTC_OFFSET_PATTERN.matcher(line); if (m.matches()) {
			 * offset = Integer.parseInt(m.group(1));
			 * Log.d(getClass().getSimpleName(), "found UTC offset: " + offset);
			 * } else { offset = 0; Log.e(getClass().getSimpleName(),
			 * "no UTC offset found, line: " + line); }
			 */
			// block open
			br.readLine();
			// block column comments
			br.readLine();

			HashMap<ViewRating, Integer> bestRuns = new HashMap<ViewRating, Integer>();
			HashMap<ViewRating, Integer> currentRuns = new HashMap<ViewRating, Integer>();

			currentRuns.put(ViewRating.NONE, 0);
			currentRuns.put(ViewRating.POOR, 0);
			currentRuns.put(ViewRating.FAIR, 0);
			currentRuns.put(ViewRating.GOOD, 0);
			currentRuns.put(ViewRating.EXCELLENT, 0);

			Date now = new Date();

			while (!(line = br.readLine()).equals(")")) {
				Condition c = new Condition(line);
				if (!c.isComplete()) {
					Log.d(getClass().getSimpleName(),
							"skipping condition, incomplete data");
					continue;
				}
				if (c.getDate().compareTo(now) <= 0) {
					Log.d(getClass().getSimpleName(),
							"skipping condition, in the past");
					continue;
				}

				ViewRating thisRating = ViewRating.valueOf(c.getRating());

				for (Entry<ViewRating, Integer> es : currentRuns.entrySet()) {
					ViewRating vr = es.getKey();
					Integer run = es.getValue();

					if (thisRating.compareTo(vr) >= 0) {
						run++;
					} else {
						run = 0;
					}
					es.setValue(run);

					Integer bestRun = bestRuns.get(thisRating);
					if (bestRun == null || run > bestRun) {
						bestRuns.put(vr, run);
					}
				}
			}

			ViewRating bestRating = ViewRating.NONE;

			for (Entry<ViewRating, Integer> es : bestRuns.entrySet()) {
				if (es.getValue() >= 3) {
					if (es.getKey().compareTo(bestRating) >= 0) {
						bestRating = es.getKey();
					}
				}
			}

			viewRating = bestRating;
		} catch (IOException ioe) {
			Log.e(getClass().getSimpleName(), "error reading condition", ioe);
		}
	}

	public ViewRating getViewRating() {
		return viewRating;
	}
}
