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
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			switch (c) {
			case ',':
			case '|':
				tokens.add(sb.toString());
				sb = new StringBuilder();
				break;
			case '\n':
			case '[':
			case ']':
			case '(':
			case ')':
			case '"':
			case '\t':
				break;
			default:
				sb.append(c);
			}
		}
		tokens.add(sb.toString());
		return tokens;

	}
}
