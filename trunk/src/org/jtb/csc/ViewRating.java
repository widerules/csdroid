package org.jtb.csc;

import android.graphics.Color;

public enum ViewRating {
    EXCELLENT("Excellent", Color.parseColor("#009900")),
    GOOD("Good", Color.parseColor("#99ff00")), 
    FAIR("Fair", Color.parseColor("#ffff00")),
    POOR("Poor", Color.parseColor("#FF0000")),
    NONE("None", Color.parseColor("#333333"));

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
        if (rating >= 0 && rating <= 14) {
            return POOR;
        }
        if (rating >= 15 && rating <= 17) {
            return FAIR;
        }
        if (rating >= 18 && rating <= 19) {
            return GOOD;
        }
        if (rating >= 20 && rating <= 20) {
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
