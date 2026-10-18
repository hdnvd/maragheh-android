package com.sweetsoft.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sweetsoft.data.model.Weather;
import com.sweetsoft.databinding.ItemWeatherDayBinding;
import com.sweetsoft.util.Fa;

import java.util.ArrayList;
import java.util.List;

public class WeatherDayAdapter extends RecyclerView.Adapter<WeatherDayAdapter.VH> {
    private final List<Weather.Day> items = new ArrayList<>();

    public void set(List<Weather.Day> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemWeatherDayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Weather.Day d = items.get(position);
        h.b.dayName.setText(d.dayName);
        h.b.dayIcon.setText(WeatherIcons.emoji(d.icon, true));
        h.b.dayMax.setText(Fa.temp(d.max) + "°");
        h.b.dayMin.setText(Fa.temp(d.min) + "°");
        h.itemView.setContentDescription(d.dayName + " " + d.text);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final ItemWeatherDayBinding b;

        VH(ItemWeatherDayBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
