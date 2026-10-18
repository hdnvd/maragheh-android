package com.sweetsoft.ui.photos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.sweetsoft.R;
import com.sweetsoft.data.model.Photo;
import com.sweetsoft.databinding.ActivityPhotoViewerBinding;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.SystemBars;

import java.util.ArrayList;
import java.util.List;

/** Full-screen swipeable, zoomable photo viewer. */
public class PhotoViewerActivity extends AppCompatActivity {
    private static final String EXTRA_PHOTOS = "photos";
    private static final String EXTRA_POSITION = "position";
    /** Keep the Intent well below Binder's 1MB limit. */
    private static final int MAX_ITEMS = 60;

    private ActivityPhotoViewerBinding b;
    private List<Photo> photos;
    private boolean chromeVisible = true;

    public static void start(Context ctx, ArrayList<Photo> photos, int position) {
        int from = Math.max(0, position - MAX_ITEMS / 2);
        int to = Math.min(photos.size(), from + MAX_ITEMS);
        ArrayList<Photo> window = new ArrayList<>(photos.subList(from, to));
        ctx.startActivity(new Intent(ctx, PhotoViewerActivity.class)
                .putExtra(EXTRA_PHOTOS, window)
                .putExtra(EXTRA_POSITION, position - from));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SystemBars.enable(this);
        super.onCreate(savedInstanceState);
        b = ActivityPhotoViewerBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        SystemBars.padToolbar(b.toolbar);
        SystemBars.padBottom(b.captionBox);
        b.toolbar.setNavigationOnClickListener(v -> finish());

        photos = (List<Photo>) getIntent().getSerializableExtra(EXTRA_PHOTOS);
        if (photos == null) photos = new ArrayList<>();
        b.pager.setAdapter(new Pages());
        b.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                bindCaption(position);
            }
        });
        int pos = getIntent().getIntExtra(EXTRA_POSITION, 0);
        b.pager.setCurrentItem(pos, false);
        bindCaption(pos);
    }

    private void bindCaption(int position) {
        if (position < 0 || position >= photos.size()) return;
        Photo p = photos.get(position);
        b.toolbar.setTitle(photos.size() > 1 ? Fa.digits((position + 1) + " / " + photos.size()) : "");
        b.title.setText(p.title);
        b.title.setVisibility(TextUtils.isEmpty(p.title) ? View.GONE : View.VISIBLE);
        b.description.setText(p.description);
        b.description.setVisibility(TextUtils.isEmpty(p.description) ? View.GONE : View.VISIBLE);
        String meta = "";
        if (!TextUtils.isEmpty(p.sender)) meta = getString(R.string.sent_by, p.sender);
        if (!TextUtils.isEmpty(p.createdAtFa)) meta += (meta.isEmpty() ? "" : " • ") + Fa.digits(p.createdAtFa);
        b.meta.setText(meta);
        b.meta.setVisibility(meta.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void toggleChrome() {
        chromeVisible = !chromeVisible;
        float alpha = chromeVisible ? 1f : 0f;
        b.toolbar.animate().alpha(alpha).setDuration(150).start();
        b.captionBox.animate().alpha(alpha).setDuration(150).start();
    }

    private class Pages extends RecyclerView.Adapter<Pages.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ZoomImageView v = new ZoomImageView(parent.getContext());
            v.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            v.setOnClickListener(x -> toggleChrome());
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            Photo p = photos.get(position);
            Glide.with(h.image)
                    .load(p.image)
                    .thumbnail(Glide.with(h.image).load(p.thumb))
                    .into(h.image);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class VH extends RecyclerView.ViewHolder {
            final ZoomImageView image;

            VH(ZoomImageView v) {
                super(v);
                image = v;
            }
        }
    }
}
