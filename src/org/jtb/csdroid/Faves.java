package org.jtb.csdroid;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Faves {
	private Set<String> faves;

	public Faves(String favesString) {
		if (favesString != null && favesString.length() > 0) {
			faves = new HashSet<String>(Arrays.asList(favesString.split("\\|")));
		} else {
			faves = new HashSet<String>();
		}
	}

	@Override
	public String toString() {
		StringBuffer b = new StringBuffer();
		for (Iterator<String> i = faves.iterator(); i.hasNext();) {
			b.append(i.next());
			if (i.hasNext()) {
				b.append("|");
			}
		}

		return b.toString();
	}

	public Set<String> getFaves() {
		return faves;
	}
}
