package com.sweetsoft.ui.news;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.chip.Chip;
import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.model.News;
import com.sweetsoft.data.model.Paged;
import com.sweetsoft.data.model.Single;
import com.sweetsoft.data.model.Source;
import com.sweetsoft.databinding.FragmentNewsBinding;
import com.sweetsoft.ui.MainActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.common.EndlessScroll;
import com.sweetsoft.ui.common.StateView;
import com.sweetsoft.util.Jalali;

import java.util.List;

import retrofit2.Call;

public class NewsFragment extends Fragment implements MainActivity.ScrollToTop {
    private FragmentNewsBinding b;
    private StateView state;
    private final NewsAdapter adapter = new NewsAdapter();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::reload;
    private Call<Paged<News>> call;

    // filters
    private String query;
    private Jalali from;
    private Jalali to;
    private Long sourceId;
    private Jalali customFrom;
    private Jalali customTo;
    private int lastDateChip = R.id.chip_all;

    // paging
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        b = FragmentNewsBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        state = new StateView(b.state);
        b.list.setAdapter(adapter);
        b.list.addOnScrollListener(new EndlessScroll() {
            @Override
            public void onLoadMore() {
                if (!loading && hasMore) load(page + 1);
            }
        });
        b.refresh.setColorSchemeResources(R.color.brand_red);
        b.refresh.setOnChildScrollUpCallback((p, c) -> b.list.canScrollVertically(-1));
        b.refresh.setOnRefreshListener(this::reload);

        b.search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int c, int d) {}
            @Override public void onTextChanged(CharSequence s, int a, int c, int d) {}

            @Override
            public void afterTextChanged(Editable s) {
                String q = s.toString().trim();
                query = q.isEmpty() ? null : q;
                handler.removeCallbacks(searchRunnable);
                handler.postDelayed(searchRunnable, 450);
            }
        });
        b.search.setOnEditorActionListener((v, actionId, e) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                handler.removeCallbacks(searchRunnable);
                reload();
                return true;
            }
            return false;
        });

        b.dateChips.setOnCheckedStateChangeListener((group, ids) -> {
            if (ids.isEmpty()) return;
            int id = ids.get(0);
            Jalali today = Jalali.today();
            if (id == R.id.chip_custom) return; // handled by click
            lastDateChip = id;
            customFrom = customTo = null;
            b.chipCustom.setText(R.string.filter_custom);
            if (id == R.id.chip_today) {
                from = today;
                to = today;
            } else if (id == R.id.chip_week) {
                from = today.minusDays(7);
                to = today;
            } else if (id == R.id.chip_month) {
                from = today.minusDays(30);
                to = today;
            } else {
                from = to = null;
            }
            reload();
        });
        b.chipCustom.setOnClickListener(v -> JalaliRangeDialog.show(requireContext(), customFrom, customTo,
                new JalaliRangeDialog.Listener() {
                    @Override
                    public void onRange(Jalali f, Jalali t) {
                        customFrom = f;
                        customTo = t;
                        from = f;
                        to = t;
                        lastDateChip = R.id.chip_custom;
                        b.chipCustom.setText(f.format() + " تا " + t.format());
                        b.chipCustom.setChecked(true);
                        reload();
                    }

                    @Override
                    public void onCancel() {
                        if (b != null) b.dateChips.check(lastDateChip);
                    }
                }));

        loadSources();
        state.loading();
        load(1);
    }

    private void loadSources() {
        ApiClient.get().sources().enqueue(new Callback<Single<List<Source>>>() {
            @Override
            public void onSuccess(Single<List<Source>> body) {
                if (b == null || body == null || body.data == null || body.data.size() < 2) return;
                b.sourceScroll.setVisibility(View.VISIBLE);
                b.sourceChips.removeAllViews();
                b.sourceChips.addView(sourceChip(getString(R.string.filter_all_sources), null, true));
                for (Source s : body.data) b.sourceChips.addView(sourceChip(s.name, s.id, false));
            }

            @Override
            public void onError(String message, int code) {
                // sources filter is optional
            }
        });
    }

    private Chip sourceChip(String text, Long id, boolean checked) {
        Chip chip = (Chip) getLayoutInflater().inflate(R.layout.view_source_chip, b.sourceChips, false);
        chip.setText(text);
        chip.setId(View.generateViewId());
        chip.setChecked(checked);
        chip.setOnClickListener(v -> {
            sourceId = id;
            reload();
        });
        return chip;
    }

    private void reload() {
        if (b == null) return;
        page = 1;
        hasMore = true;
        if (adapter.getItemCount() == 0) state.loading();
        load(1);
    }

    private void load(int pageToLoad) {
        if (call != null) call.cancel();
        loading = true;
        call = ApiClient.get().news(pageToLoad, query,
                from != null ? from.toIsoGregorian() : null,
                to != null ? to.toIsoGregorian() : null,
                sourceId);
        call.enqueue(new Callback<Paged<News>>() {
            @Override
            public void onSuccess(Paged<News> body) {
                loading = false;
                if (b == null) return;
                b.refresh.setRefreshing(false);
                page = pageToLoad;
                hasMore = body != null && body.hasMore();
                List<News> data = body != null ? body.data : null;
                if (pageToLoad == 1) {
                    adapter.set(data);
                    b.list.scrollToPosition(0);
                } else {
                    adapter.append(data);
                }
                if (adapter.getItemCount() == 0) state.empty();
                else state.hide();
            }

            @Override
            public void onError(String message, int code) {
                loading = false;
                if (b == null) return;
                b.refresh.setRefreshing(false);
                if (pageToLoad == 1) {
                    adapter.set(null);
                    state.error(message, NewsFragment.this::reload);
                }
            }
        });
    }

    @Override
    public void scrollToTop() {
        if (b != null) b.list.smoothScrollToPosition(0);
    }

    @Override
    public void onDestroyView() {
        handler.removeCallbacks(searchRunnable);
        if (call != null) call.cancel();
        b = null;
        super.onDestroyView();
    }
}
