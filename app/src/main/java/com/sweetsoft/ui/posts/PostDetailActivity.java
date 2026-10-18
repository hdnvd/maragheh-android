package com.sweetsoft.ui.posts;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.model.Photo;
import com.sweetsoft.data.model.Post;
import com.sweetsoft.data.model.Single;
import com.sweetsoft.databinding.ActivityPostDetailBinding;
import com.sweetsoft.ui.common.BaseActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.common.StateView;
import com.sweetsoft.ui.photos.PhotoViewerActivity;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.HtmlPage;
import com.sweetsoft.util.Ui;
import com.sweetsoft.util.SystemBars;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PostDetailActivity extends BaseActivity {
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_TITLE = "title";

    private ActivityPostDetailBinding b;
    private StateView state;
    private Post post;
    private GalleryAdapter gallery;

    public static void start(Context ctx, long id, String title) {
        ctx.startActivity(new Intent(ctx, PostDetailActivity.class)
                .putExtra(EXTRA_ID, id)
                .putExtra(EXTRA_TITLE, title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "");
        SystemBars.padBottom(b.content);
        state = new StateView(b.state);
        b.title.setText(getIntent().getStringExtra(EXTRA_TITLE));
        gallery = new GalleryAdapter(this::openImage);
        b.gallery.setAdapter(gallery);
        b.gallery.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateCounter(position);
            }
        });
        load();
    }

    private void load() {
        b.content.setVisibility(View.INVISIBLE);
        state.loading();
        long id = getIntent().getLongExtra(EXTRA_ID, 0);
        track(ApiClient.get().post(id)).enqueue(new Callback<Single<Post>>() {
            @Override
            public void onSuccess(Single<Post> body) {
                if (body == null || body.data == null) {
                    state.empty();
                    return;
                }
                state.hide();
                bind(body.data);
            }

            @Override
            public void onError(String message, int code) {
                state.error(message, PostDetailActivity.this::load);
            }
        });
    }

    private List<String> images(Post p) {
        List<String> list = new ArrayList<>();
        if (!TextUtils.isEmpty(p.cover)) list.add(p.cover);
        if (p.images != null) {
            for (String url : p.images) if (!list.contains(url)) list.add(url);
        }
        return list;
    }

    private void bind(Post p) {
        post = p;
        b.content.setVisibility(View.VISIBLE);
        b.title.setText(p.title);
        b.subtitle.setText(p.subtitle);
        b.subtitle.setVisibility(TextUtils.isEmpty(p.subtitle) ? View.GONE : View.VISIBLE);
        List<String> imgs = images(p);
        gallery.set(imgs);
        if (imgs.isEmpty()) b.appBar.setExpanded(false, false);
        updateCounter(0);
        HtmlPage.render(b.body, p.bodyHtml);
        boolean hasLocation = p.latitude != null && p.longitude != null;
        b.navigate.setVisibility(hasLocation ? View.VISIBLE : View.GONE);
        b.navigate.setOnClickListener(v -> navigate(p));
        invalidateOptionsMenu();
    }

    private void updateCounter(int position) {
        int count = gallery.getItemCount();
        b.galleryCounter.setVisibility(count > 1 ? View.VISIBLE : View.GONE);
        b.galleryCounter.setText(Fa.digits((position + 1) + " / " + count));
    }

    private void openImage(int position) {
        if (post == null) return;
        ArrayList<Photo> photos = new ArrayList<>();
        for (String url : images(post)) {
            Photo ph = new Photo();
            ph.image = url;
            ph.title = post.title;
            photos.add(ph);
        }
        PhotoViewerActivity.start(this, photos, position);
    }

    /** Opens any installed map app (Neshan, Balad, Google Maps…) at the attraction. */
    private void navigate(Post p) {
        String geo = String.format(Locale.US, "geo:%f,%f?q=%f,%f(%s)",
                p.latitude, p.longitude, p.latitude, p.longitude, Uri.encode(p.title));
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(geo)));
        } catch (ActivityNotFoundException e) {
            Ui.openUrl(this, String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%f,%f",
                    p.latitude, p.longitude));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        menu.findItem(R.id.action_share).setVisible(post != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share && post != null) {
            String text = post.title + (post.summary != null ? "\n" + post.summary : "")
                    + "\n\n" + getString(R.string.share_app_text);
            Ui.shareText(this, post.title, text);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
