package com.sweetsoft.ui.photos;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sweetsoft.data.model.Photo;
import com.sweetsoft.databinding.ItemPhotoBinding;
import com.sweetsoft.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Approved gallery photos: staggered grid, or a fixed-size horizontal strip on the home screen. */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.VH> {
    private final boolean horizontal;
    private final List<Photo> items = new ArrayList<>();

    public PhotoAdapter(boolean horizontal) {
        this.horizontal = horizontal;
    }

    public List<Photo> getItems() {
        return items;
    }

    public void set(List<Photo> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void append(List<Photo> list) {
        if (list == null || list.isEmpty()) return;
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPhotoBinding b = ItemPhotoBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        if (horizontal) {
            ViewGroup.LayoutParams lp = b.getRoot().getLayoutParams();
            lp.width = Ui.dp(parent.getContext(), 150);
            b.getRoot().setLayoutParams(lp);
        }
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Photo p = items.get(position);
        h.b.title.setText(p.title);
        ViewGroup.LayoutParams lp = h.b.image.getLayoutParams();
        if (horizontal) {
            lp.height = Ui.dp(h.itemView.getContext(), 150);
        } else {
            // keep the original aspect ratio in the staggered grid, clamped to sane bounds
            int colWidth = h.itemView.getResources().getDisplayMetrics().widthPixels / 2;
            float ratio = (p.width > 0 && p.height > 0) ? (float) p.height / p.width : 0.75f;
            ratio = Math.max(0.6f, Math.min(ratio, 1.6f));
            lp.height = Math.round(colWidth * ratio);
        }
        h.b.image.setLayoutParams(lp);
        Ui.loadImage(h.b.image, p.thumb != null ? p.thumb : p.image);
        h.itemView.setOnClickListener(v ->
                PhotoViewerActivity.start(v.getContext(), new ArrayList<>(items), h.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemPhotoBinding b;

        VH(ItemPhotoBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
