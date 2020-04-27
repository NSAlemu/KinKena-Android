package com.example.ethiopianmusicapp.Objects;

import android.graphics.Color;

public class Utility {
    public static int manipulateColor(int color, float whiteAmount) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * 0.6f);
        int g = Math.round(Color.green(color) * 0.6f);
        int b = Math.round(Color.blue(color) * 0.6f);
        return Color.argb(a,
                Math.min(r,255),
                Math.min(g,255),
                Math.min(b,255));
    }
}
