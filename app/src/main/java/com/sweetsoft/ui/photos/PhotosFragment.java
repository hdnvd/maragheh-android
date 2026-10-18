package com.sweetsoft.ui.photos;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.Session;
import com.sweetsoft.data.model.Paged;
import com.sweetsoft.data.model.Photo;
import com.sweetsoft.databinding.FragmentPhotosBinding;
import com.sweetsoft.ui.MainActivity;
import com.sweetsoft.ui.auth.LoginActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.common.EndlessScroll;
import com.sweetsoft.ui.common.StateView;

import retrofit2.Call;

public class PhotosFragment extends Fragment implements MainActivity.ScrollToTop {
    private FragmentPhotosBinding b;
    private StateView state;
    private final PhotoAdapter adapter = new PhotoAdapter(false);
    private Call<Paged<Photo>> call;
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        b = FragmentPhotosBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        state = new StateView(b.state);
        StaggeredGridLayoutManager lm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        lm.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        b.list.setLayoutManager(lm);
        b.list.setAdapter(adapter);
        b.list.addOnScrollListener(new EndlessScroll() {
            @Override
            public void onLoadMore() {
                if (!loading && hasMore) load(page + 1);
            }
        });
        b.list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView rv, int dx, int dy) {
                if (dy > 8) b.fabUpload.shrink();
                else if (dy < -8) b.fabUpload.extend();
            }
        });
        b.refresh.setColorSchemeResources(R.color.brand_red);
        b.refresh.setOnChildScrollUpCallback((p, c) -> b.list.canScrollVertically(-1));
        b.refresh.setOnRefreshListener(() -> load(1));
        b.fabUpload.setOnClickListener(v -> {
            Class<?> target = Session.isLoggedIn() ? UploadPhotoActivity.class : LoginActivity.class;
            Intent i = new Intent(requireContext(), target);
            if (!Session.isLoggedIn()) i.putExtra(LoginActivity.EXTRA_NEXT_UPLOAD, true);
            startActivity(i);
        });
        state.loading();
        load(1);
    }

    private void load(int pageToLoad) {
        if (call != null) call.cancel();
        loading = true;
        call = ApiClient.get().photos(pageToLoad);
        call.enqueue(new Callback<Paged<Photo>>() {
            @Override
            public void onSuccess(Paged<Photo> body) {
                loading = false;
                if (b == null) return;
                b.refresh.setRefreshing(false);
                page = pageToLoad;
                hasMore = body != null && body.hasMore();
                if (pageToLoad == 1) adapter.set(body != null ? body.data : null);
                else adapter.append(body != null ? body.data : null);
                if (adapter.getItemCount() == 0) state.empty();
                else state.hide();
            }

            @Override
            public void onError(String message, int code) {
                loading = false;
                if (b == null) return;
                b.refresh.setRefreshing(false);
                if (pageToLoad == 1 && adapter.getItemCount() == 0) {
                    state.error(message, () -> {
                        state.loading();
                        load(1);
                    });
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
        if (call != null) call.cancel();
        b = null;
        super.onDestroyView();
    }
}
