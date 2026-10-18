package com.sweetsoft.ui.common;

import android.view.View;

import com.sweetsoft.R;
import com.sweetsoft.databinding.ViewStateBinding;

/** Loading / error / empty overlay shared by all lists and detail screens. */
public class StateView {
    private final ViewStateBinding b;

    public StateView(ViewStateBinding binding) {
        this.b = binding;
    }

    public void loading() {
        b.stateRoot.setVisibility(View.VISIBLE);
        b.stateProgress.setVisibility(View.VISIBLE);
        b.stateText.setVisibility(View.GONE);
        b.stateRetry.setVisibility(View.GONE);
    }

    public void error(String message, Runnable retry) {
        b.stateRoot.setVisibility(View.VISIBLE);
        b.stateProgress.setVisibility(View.GONE);
        b.stateText.setVisibility(View.VISIBLE);
        b.stateText.setText(message != null ? message : b.getRoot().getContext().getString(R.string.error_network));
        b.stateRetry.setVisibility(retry != null ? View.VISIBLE : View.GONE);
        b.stateRetry.setOnClickListener(v -> {
            if (retry != null) retry.run();
        });
    }

    public void empty() {
        b.stateRoot.setVisibility(View.VISIBLE);
        b.stateProgress.setVisibility(View.GONE);
        b.stateText.setVisibility(View.VISIBLE);
        b.stateText.setText(R.string.empty_list);
        b.stateRetry.setVisibility(View.GONE);
    }

    public void hide() {
        b.stateRoot.setVisibility(View.GONE);
    }
}
