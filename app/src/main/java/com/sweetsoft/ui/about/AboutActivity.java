package com.sweetsoft.ui.about;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.sweetsoft.BuildConfig;
import com.sweetsoft.R;
import com.sweetsoft.databinding.ActivityAboutBinding;
import com.sweetsoft.ui.common.BaseActivity;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.Ui;
import com.sweetsoft.util.SystemBars;

public class AboutActivity extends BaseActivity {
    private static final String BAZAAR_PAGE = "https://cafebazaar.ir/app/" + BuildConfig.APPLICATION_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAboutBinding b = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, getString(R.string.title_about));
        SystemBars.padBottom(b.getRoot());
        b.version.setText(getString(R.string.about_version, Fa.digits(BuildConfig.VERSION_NAME)));
        b.rate.setOnClickListener(v -> rate());
        b.share.setOnClickListener(v ->
                Ui.shareText(this, getString(R.string.app_name), getString(R.string.share_app_text)));
    }

    /** Opens Cafebazaar's rating screen, falling back to the web page. */
    private void rate() {
        Intent i = new Intent(Intent.ACTION_EDIT, Uri.parse("bazaar://details?id=" + BuildConfig.APPLICATION_ID));
        i.setPackage("com.farsitel.bazaar");
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Ui.openUrl(this, BAZAAR_PAGE);
        }
    }
}
