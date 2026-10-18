package com.sweetsoft.ui.posts;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.model.Paged;
import com.sweetsoft.data.model.Post;
import com.sweetsoft.databinding.FragmentPostsBinding;
import com.sweetsoft.ui.MainActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.common.EndlessScroll;
import com.sweetsoft.ui.common.StateView;

import retrofit2.Call;

/** List of admin-written posts: tourist attractions or famous people of Maragheh. */
public class PostsFragment extends Fragment implements MainActivity.ScrollToTop {
    private static final String ARG_TYPE = "type";

    private FragmentPostsBinding b;
    private StateView state;
    private PostAdapter adapter;
    private String type;
    private String query;
    private Call<Paged<Post>> call;
    private int page = 1;
    private boolean hasMore = true;
    private boolean loading = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable searchRunnable = this::reload;

    public static PostsFragment newInstance(String type) {
        PostsFragment f = new PostsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        b = FragmentPostsBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        type = requireArguments().getString(ARG_TYPE, Post.TYPE_ATTRACTION);
        adapter = new PostAdapter(type, false);
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
        state.loading();
        load(1);
    }

    private void reload() {
        if (b == null) return;
        page = 1;
        hasMore = true;
        load(1);
    }

    private void load(int pageToLoad) {
        if (call != null) call.cancel();
        loading = true;
        call = ApiClient.get().posts(type, pageToLoad, query);
        call.enqueue(new Callback<Paged<Post>>() {
            @Override
            public void onSuccess(Paged<Post> body) {
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
                        reload();
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
        handler.removeCallbacks(searchRunnable);
        if (call != null) call.cancel();
        b = null;
        super.onDestroyView();
    }
}
