package com.sweetsoft.ui.posts;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sweetsoft.databinding.ItemGalleryImageBinding;
import com.sweetsoft.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Swipeable header images of a post. */
public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.VH> {
    public interface OnImageClick {
        void onClick(int position);
    }

    private final List<String> urls = new ArrayList<>();
    private final OnImageClick listener;

    public GalleryAdapter(OnImageClick listener) {
        this.listener = listener;
    }

    public void set(List<String> list) {
        urls.clear();
        if (list != null) urls.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemGalleryImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Ui.loadImage(h.b.image, urls.get(position));
        h.b.image.setOnClickListener(v -> listener.onClick(h.getBindingAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return urls.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemGalleryImageBinding b;

        VH(ItemGalleryImageBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
