package com.sweetsoft.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.snackbar.Snackbar;
import com.sweetsoft.R;

public final class Ui {
    private Ui() {}

    public static void loadImage(ImageView view, String url) {
        Glide.with(view)
                .load(url)
                .placeholder(R.color.surface_variant)
                .error(R.color.surface_variant)
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .centerCrop()
                .into(view);
    }

    public static void snack(View anchor, CharSequence text) {
        Snackbar.make(anchor, text, Snackbar.LENGTH_LONG).show();
    }

    public static void openUrl(Context ctx, String url) {
        try {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static void shareText(Activity activity, String subject, String text) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, subject);
        i.putExtra(Intent.EXTRA_TEXT, text);
        activity.startActivity(Intent.createChooser(i, activity.getString(R.string.share)));
    }

    public static int dp(Context ctx, int dp) {
        return Math.round(dp * ctx.getResources().getDisplayMetrics().density);
    }
}
