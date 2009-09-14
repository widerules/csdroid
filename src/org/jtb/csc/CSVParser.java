package org.jtb.csc;

import java.util.ArrayList;

import android.util.Log;

class CSVParser {
	public static ArrayList<String> parse(String line) {
		ArrayList<String> tokens = new ArrayList<String>();
		if (line == null) {
			return tokens;
		}

		StringBuilder sb = new StringBuilder();
		boolean quotes = false;
		boolean leading = true;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			switch (c) {
			case '"':
				quotes = !quotes;
				leading = false;
				break;
			case ',':
			case '|':
				if (!quotes) {
					tokens.add(sb.toString());
					sb = new StringBuilder();
					leading = true;
				}
				break;
			case '\n':
			case '[':
			case ']':
			case '(':
			case ')':
				break;
			case ' ':
			case '\t':
				if (!leading) {
					sb.append(c);
				}
				break;
			default:
				leading = false;
				sb.append(c);
			}
			if (i == line.length() - 1) {
				tokens.add(sb.toString());
			}
		}

		return tokens;

	}
}
