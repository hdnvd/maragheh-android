package com.sweetsoft.ui.news;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sweetsoft.data.model.News;
import com.sweetsoft.databinding.ItemNewsBinding;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.VH> {
    private final List<News> items = new ArrayList<>();

    public void set(List<News> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void append(List<News> list) {
        if (list == null || list.isEmpty()) return;
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemNewsBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        News n = items.get(position);
        h.b.title.setText(n.title);
        h.b.time.setText(Fa.timeAgo(n.publishedAt, n.publishedAtFa));
        if (n.source != null) {
            h.b.source.setVisibility(View.VISIBLE);
            h.b.source.setText(n.source.name);
        } else {
            h.b.source.setVisibility(View.GONE);
        }
        Ui.loadImage(h.b.image, n.image);
        h.b.getRoot().setOnClickListener(v -> NewsDetailActivity.start(v.getContext(), n.id, n.title));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemNewsBinding b;

        VH(ItemNewsBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
