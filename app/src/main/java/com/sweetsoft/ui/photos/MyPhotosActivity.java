package com.sweetsoft.ui.photos;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.Session;
import com.sweetsoft.data.model.Paged;
import com.sweetsoft.data.model.Photo;
import com.sweetsoft.databinding.ActivityListBinding;
import com.sweetsoft.databinding.ItemMyPhotoBinding;
import com.sweetsoft.ui.auth.LoginActivity;
import com.sweetsoft.ui.common.BaseActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.common.EndlessScroll;
import com.sweetsoft.ui.common.StateView;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.Ui;
import com.sweetsoft.util.SystemBars;

import java.util.ArrayList;
import java.util.List;

/** The signed-in user's submissions with their review status. */
public class MyPhotosActivity extends BaseActivity {
    private ActivityListBinding b;
    private StateView state;
    private final Adapter adapter = new Adapter();
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        b = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, getString(R.string.title_my_photos));
        SystemBars.padBottom(b.list);
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
        b.refresh.setOnRefreshListener(() -> load(1));
        state.loading();
        load(1);
    }

    private void load(int pageToLoad) {
        loading = true;
        track(ApiClient.get().myPhotos(pageToLoad)).enqueue(new Callback<Paged<Photo>>() {
            @Override
            public void onSuccess(Paged<Photo> body) {
                loading = false;
                b.refresh.setRefreshing(false);
                page = pageToLoad;
                hasMore = body != null && body.hasMore();
                List<Photo> data = body != null && body.data != null ? body.data : new ArrayList<>();
                if (pageToLoad == 1) adapter.items.clear();
                adapter.items.addAll(data);
                adapter.notifyDataSetChanged();
                if (adapter.items.isEmpty()) state.empty();
                else state.hide();
            }

            @Override
            public void onError(String message, int code) {
                loading = false;
                b.refresh.setRefreshing(false);
                if (code == 401) {
                    Session.clear();
                    startActivity(new Intent(MyPhotosActivity.this, LoginActivity.class));
                    finish();
                    return;
                }
                if (adapter.items.isEmpty()) state.error(message, () -> {
                    state.loading();
                    load(1);
                });
            }
        });
    }

    private static class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        final List<Photo> items = new ArrayList<>();

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(ItemMyPhotoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Photo p = items.get(position);
            h.b.title.setText(p.title);
            h.b.date.setText(Fa.digits(p.createdAtFa));
            Ui.loadImage(h.b.image, p.thumb != null ? p.thumb : p.image);
            int color;
            int label;
            if (Photo.APPROVED.equals(p.status)) {
                color = R.color.status_approved;
                label = R.string.status_approved;
            } else if (Photo.REJECTED.equals(p.status)) {
                color = R.color.status_rejected;
                label = R.string.status_rejected;
            } else {
                color = R.color.status_pending;
                label = R.string.status_pending;
            }
            h.b.status.setText(label);
            h.b.status.setBackgroundTintList(ContextCompat.getColorStateList(h.itemView.getContext(), color));
            boolean showReason = Photo.REJECTED.equals(p.status) && !TextUtils.isEmpty(p.rejectReason);
            h.b.reason.setVisibility(showReason ? View.VISIBLE : View.GONE);
            if (showReason) h.b.reason.setText(h.itemView.getContext().getString(R.string.reject_reason, p.rejectReason));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class VH extends RecyclerView.ViewHolder {
            final ItemMyPhotoBinding b;

            VH(ItemMyPhotoBinding b) {
                super(b.getRoot());
                this.b = b;
            }
        }
    }
}
