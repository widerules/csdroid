package org.jtb.csc;

public class BlockTimer {
	private long start;
	
	public BlockTimer() {
		this.start = System.currentTimeMillis();
	}
	
	public long elapsed() {
		return System.currentTimeMillis() - start;
	}
}
