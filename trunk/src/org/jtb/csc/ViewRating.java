package org.jtb.csc;

import android.graphics.Color;

public enum ViewRating {
    EXCELLENT(4, Color.parseColor("#009900"), Color.parseColor("#ffffff")),
    GOOD(3, Color.parseColor("#99ff00"), Color.parseColor("#ffffff")), 
    FAIR(2, Color.parseColor("#ffff00"), Color.parseColor("#ffffff")),
    POOR(1, Color.parseColor("#FF0000"), Color.parseColor("#ffffff")),
    NONE(0, Color.parseColor("#333333"), Color.parseColor("#ffffff"));

    private int i;
    private int color;
    private int selectedColor;
    
    private ViewRating(int i, int color, int selectedColor) {
        this.i = i;
        this.color = color;
        this.selectedColor = selectedColor;
    }

    public static ViewRating valueOf(int rating) {
        if (rating == 0) {
            return NONE;
        }
        if (rating >= 1 && rating <= 15) {
            return POOR;
        }
        if (rating >= 16 && rating <= 18) {
            return FAIR;
        }
        if (rating >= 19 && rating <= 19) {
            return GOOD;
        }
        if (rating >= 20 && rating <= 20) {
            return EXCELLENT;
        }
        throw new AssertionError("invalid rating value: " + rating);
    }

    public int toInt() {
        return i;
    }

    public int getColor() {
    	return color;
    }

	public int getSelectedColor() {
		return selectedColor;
	}
    
    /*
    @Override
    public int compareTo(Object o) {
        ViewRating other = (ViewRating)o;
        if (i == other.i) {
            return 0;
        }
        if (i > other.i) {
            return 1;
        }
        return -1;
    }
     */
}
