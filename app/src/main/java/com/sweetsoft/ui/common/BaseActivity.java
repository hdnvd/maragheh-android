package com.sweetsoft.ui.common;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.sweetsoft.util.SystemBars;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

/** Toolbar with back arrow + cancels in-flight requests when the screen closes. */
public abstract class BaseActivity extends AppCompatActivity {
    private final List<Call<?>> calls = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        SystemBars.enable(this);
        super.onCreate(savedInstanceState);
    }

    protected void setupToolbar(Toolbar toolbar, CharSequence title) {
        SystemBars.padToolbar(toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
    }

    protected <T> Call<T> track(Call<T> call) {
        calls.add(call);
        return call;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        for (Call<?> c : calls) c.cancel();
        super.onDestroy();
    }
}
