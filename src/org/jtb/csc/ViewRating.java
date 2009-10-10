package org.jtb.csc;

import android.graphics.Color;

public enum ViewRating {
    NONE("None", Color.parseColor("#333333")),
    POOR("Poor", Color.parseColor("#FF0000")),
    FAIR("Fair", Color.parseColor("#ffff00")),
    GOOD("Good", Color.parseColor("#99ff00")), 
    EXCELLENT("Excellent", Color.parseColor("#009900"));

    private int color;
    private String displayString;
    
    private ViewRating(String displayString, int color) {
    	this.displayString = displayString;
        this.color = color;
    }

    public static ViewRating valueOf(int rating) {
        if (rating == -1) {
            return NONE;
        }
        if (rating >= 0 && rating <= 10) {
            return POOR;
        }
        if (rating >= 11 && rating <= 15) {
            return FAIR;
        }
        if (rating >= 16 && rating <= 18) {
            return GOOD;
        }
        if (rating >= 19 && rating <= 20) {
            return EXCELLENT;
        }
        throw new AssertionError("invalid rating value: " + rating);
    }

    public int getColor() {
    	return color;
    }
    
    public String toDisplayString() {
    	return displayString;
    }
}
