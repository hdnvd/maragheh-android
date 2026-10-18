package com.sweetsoft.ui.posts;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.sweetsoft.data.model.Post;
import com.sweetsoft.databinding.ItemCelebrityBinding;
import com.sweetsoft.databinding.ItemCelebrityHBinding;
import com.sweetsoft.databinding.ItemPostBinding;
import com.sweetsoft.databinding.ItemPostHBinding;
import com.sweetsoft.util.Ui;

import java.util.ArrayList;
import java.util.List;

/** Attractions and celebrities, as a full list or a compact horizontal strip. */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.VH> {
    private final String type;
    private final boolean horizontal;
    private final List<Post> items = new ArrayList<>();

    public PostAdapter(String type, boolean horizontal) {
        this.type = type;
        this.horizontal = horizontal;
    }

    public void set(List<Post> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    public void append(List<Post> list) {
        if (list == null || list.isEmpty()) return;
        int start = items.size();
        items.addAll(list);
        notifyItemRangeInserted(start, list.size());
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        boolean celeb = Post.TYPE_CELEBRITY.equals(type);
        ViewBinding b;
        if (horizontal) {
            b = celeb ? ItemCelebrityHBinding.inflate(inf, parent, false) : ItemPostHBinding.inflate(inf, parent, false);
        } else {
            b = celeb ? ItemCelebrityBinding.inflate(inf, parent, false) : ItemPostBinding.inflate(inf, parent, false);
        }
        return new VH(b);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Post p = items.get(position);
        h.title.setText(p.title);
        if (h.subtitle != null) {
            h.subtitle.setText(p.subtitle);
            h.subtitle.setVisibility(TextUtils.isEmpty(p.subtitle) ? View.GONE : View.VISIBLE);
        }
        if (h.summary != null) {
            h.summary.setText(p.summary);
            h.summary.setVisibility(TextUtils.isEmpty(p.summary) ? View.GONE : View.VISIBLE);
        }
        Ui.loadImage(h.image, p.cover);
        h.itemView.setOnClickListener(v -> PostDetailActivity.start(v.getContext(), p.id, p.title));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView title;
        final TextView subtitle;
        final TextView summary;

        VH(ViewBinding b) {
            super(b.getRoot());
            if (b instanceof ItemPostBinding) {
                ItemPostBinding x = (ItemPostBinding) b;
                image = x.image; title = x.title; subtitle = x.subtitle; summary = x.summary;
            } else if (b instanceof ItemPostHBinding) {
                ItemPostHBinding x = (ItemPostHBinding) b;
                image = x.image; title = x.title; subtitle = x.subtitle; summary = null;
            } else if (b instanceof ItemCelebrityBinding) {
                ItemCelebrityBinding x = (ItemCelebrityBinding) b;
                image = x.image; title = x.title; subtitle = x.subtitle; summary = x.summary;
            } else {
                ItemCelebrityHBinding x = (ItemCelebrityHBinding) b;
                image = x.image; title = x.title; subtitle = null; summary = null;
            }
        }
    }
}
