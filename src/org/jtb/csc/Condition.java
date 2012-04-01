package org.jtb.csc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import android.util.Log;

public class Condition {

	private static final Pattern CONDITION_PATTERN = Pattern
			.compile("\\(\"(.+)\",\\t(.+),\\t(.+),\\t(.+),\\t(.+),\\t(.+),\\t(.+),\\t\\),");
	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_PATTERN);
	private Date date;
	private int cloud;
	private int seeing;
	private int trans;
	private int rating;

	public Condition(String s) {
		/*
		Matcher m = CONDITION_PATTERN.matcher(s);
		if (!m.matches()) {
			return;
		}

		date = parseDate(m.group(1));
		cloud = parseValue(m.group(2), false);
		trans = parseValue(m.group(3), true);
		seeing = parseValue(m.group(4), true);
		*/
		
		ArrayList<String> tokens = CSVParser.parse(s);
		date = parseDate(tokens.get(0));
		cloud = parseValue(tokens.get(1), false);
		trans = parseValue(tokens.get(2), true);
		seeing = parseValue(tokens.get(3), true);
		
		if (seeing == -1 || cloud == -1 || trans == -1) {
			rating = -1;
		} else {
			rating = seeing + trans + cloud;
		}		
	}

	public boolean isComplete() {
		return rating != -1;
	}

	private int parseValue(String s, boolean higherBetter) {
		if (s.equals("None")) {
			return -1;
		}
		return Integer.parseInt(s);
	}

	private Date parseDate(String s) {
		try {
			Date d = DATE_FORMAT.parse(s);
			return d;
		} catch (ParseException pe) {
			Log.e(getClass().getSimpleName(), "could not parse condition date", pe);
			return null;
		}
	}

	public Date getDate() {
		return date;
	}

	public int getCloud() {
		return cloud;
	}

	public int getSeeing() {
		return seeing;
	}

	public int getTrans() {
		return trans;
	}

	@Override
	public String toString() {
		return "{ date=" + new SimpleDateFormat(DATE_PATTERN).format(date)
				+ ", cloud=" + cloud + ", seeing=" + seeing + ", trans="
				+ trans + ", rating=" + getRating() + " }";
	}

	public int getRating() {
		return rating;
	}
}
