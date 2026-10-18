package com.sweetsoft.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.Session;
import com.sweetsoft.data.model.Post;
import com.sweetsoft.databinding.ActivityMainBinding;
import com.sweetsoft.ui.about.AboutActivity;
import com.sweetsoft.ui.auth.LoginActivity;
import com.sweetsoft.ui.home.HomeFragment;
import com.sweetsoft.ui.news.NewsFragment;
import com.sweetsoft.ui.photos.MyPhotosActivity;
import com.sweetsoft.ui.photos.PhotosFragment;
import com.sweetsoft.ui.posts.PostsFragment;
import com.sweetsoft.util.AppLocale;
import com.sweetsoft.util.SystemBars;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String KEY_TAB = "tab";
    private ActivityMainBinding b;
    private @IdRes int currentTab = R.id.nav_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen.installSplashScreen(this);
        SystemBars.enable(this);
        super.onCreate(savedInstanceState);
        // Earlier test builds registered "fa" as Android's per-app language, which makes Gboard switch to
        // Persian on every launch. Persian is now applied in-app (AppLocale); clear the old system setting.
        if (!AppCompatDelegate.getApplicationLocales().isEmpty()) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList());
        }
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        SystemBars.padToolbar(b.toolbar);
        setSupportActionBar(b.toolbar);

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getInt(KEY_TAB, R.id.nav_home);
        }
        b.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item.getItemId());
            return true;
        });
        b.bottomNav.setOnItemReselectedListener(item -> {
            Fragment f = getSupportFragmentManager().findFragmentByTag(tag(item.getItemId()));
            if (f instanceof ScrollToTop) ((ScrollToTop) f).scrollToTop();
        });
        b.bottomNav.setSelectedItemId(currentTab);
        showTab(currentTab);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(AppLocale.wrap(base));
    }

    /** Used by the home screen's "see all" buttons. */
    public void selectTab(@IdRes int id) {
        b.bottomNav.setSelectedItemId(id);
    }

    private void showTab(@IdRes int id) {
        currentTab = id;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction().setReorderingAllowed(true);
        for (Fragment f : fm.getFragments()) {
            if (!tag(id).equals(f.getTag())) tx.hide(f);
        }
        Fragment target = fm.findFragmentByTag(tag(id));
        if (target == null) {
            tx.add(R.id.container, create(id), tag(id));
        } else {
            tx.show(target);
        }
        tx.commit();
        b.toolbar.setTitle(title(id));
    }

    private static String tag(int id) {
        return "tab_" + id;
    }

    private Fragment create(int id) {
        if (id == R.id.nav_news) return new NewsFragment();
        if (id == R.id.nav_attractions) return PostsFragment.newInstance(Post.TYPE_ATTRACTION);
        if (id == R.id.nav_celebrities) return PostsFragment.newInstance(Post.TYPE_CELEBRITY);
        if (id == R.id.nav_photos) return new PhotosFragment();
        return new HomeFragment();
    }

    private String title(int id) {
        if (id == R.id.nav_news) return getString(R.string.title_news);
        if (id == R.id.nav_attractions) return getString(R.string.title_attractions);
        if (id == R.id.nav_celebrities) return getString(R.string.title_celebrities);
        if (id == R.id.nav_photos) return getString(R.string.title_photos);
        return getString(R.string.app_name);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TAB, currentTab);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_account) {
            showAccount();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAccount() {
        if (!Session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }
        String[] options = {getString(R.string.my_photos), getString(R.string.logout)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(Session.getName() + "\n" + Session.getPhone())
                .setItems(options, (d, which) -> {
                    if (which == 0) {
                        startActivity(new Intent(this, MyPhotosActivity.class));
                    } else {
                        confirmLogout();
                    }
                })
                .show();
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.logout, (d, w) -> {
                    ApiClient.get().logout().enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {}

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {}
                    });
                    Session.clear();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    /** Implemented by tabs that can scroll back to the top when re-selected. */
    public interface ScrollToTop {
        void scrollToTop();
    }
}
