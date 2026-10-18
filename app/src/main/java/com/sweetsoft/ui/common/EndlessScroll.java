package com.sweetsoft.ui.common;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

/** Calls {@link #onLoadMore()} when the list is scrolled near its end. */
public abstract class EndlessScroll extends RecyclerView.OnScrollListener {
    private static final int THRESHOLD = 4;

    @Override
    public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
        if (dy <= 0) return;
        RecyclerView.LayoutManager lm = rv.getLayoutManager();
        int last = 0;
        if (lm instanceof LinearLayoutManager) {
            last = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
        } else if (lm instanceof StaggeredGridLayoutManager) {
            int[] pos = ((StaggeredGridLayoutManager) lm).findLastVisibleItemPositions(null);
            for (int p : pos) last = Math.max(last, p);
        }
        if (lm != null && last >= lm.getItemCount() - THRESHOLD) onLoadMore();
    }

    public abstract void onLoadMore();
}
