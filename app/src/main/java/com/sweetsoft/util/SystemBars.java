package com.sweetsoft.util;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Edge-to-edge on every Android version (Android 15+ enforces it anyway):
 * the red toolbar runs under a transparent status bar and content stays clear of the system bars.
 */
public final class SystemBars {
    private SystemBars() {}

    public static void enable(ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT));
    }

    /** Toolbar grows by the status-bar height and pads its content below it. */
    public static void padToolbar(View toolbar) {
        int base = actionBarSize(toolbar);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.statusBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(v.getPaddingLeft(), bars.top, v.getPaddingRight(), v.getPaddingBottom());
            ViewGroup.LayoutParams lp = v.getLayoutParams();
            if (lp.height != base + bars.top) {
                lp.height = base + bars.top;
                v.setLayoutParams(lp);
            }
            return insets;
        });
    }

    /** Keeps scrolling content / bottom bars above the gesture or navigation bar. */
    public static void padBottom(View view) {
        int initial = view.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(view, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), initial + bars.bottom);
            return insets;
        });
    }

    private static int actionBarSize(View v) {
        TypedArray a = v.getContext().obtainStyledAttributes(new int[]{androidx.appcompat.R.attr.actionBarSize});
        int size = a.getDimensionPixelSize(0, 0);
        a.recycle();
        return size;
    }
}
