package com.sweetsoft.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.model.HomeData;
import com.sweetsoft.data.model.Post;
import com.sweetsoft.data.model.Weather;
import com.sweetsoft.databinding.FragmentHomeBinding;
import com.sweetsoft.databinding.ViewSectionHeaderBinding;
import com.sweetsoft.ui.MainActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.common.StateView;
import com.sweetsoft.ui.news.NewsAdapter;
import com.sweetsoft.ui.photos.PhotoAdapter;
import com.sweetsoft.ui.posts.PostAdapter;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.Jalali;

import retrofit2.Call;

public class HomeFragment extends Fragment implements MainActivity.ScrollToTop {
    private FragmentHomeBinding b;
    private StateView state;
    private Call<HomeData> call;
    private final WeatherDayAdapter forecast = new WeatherDayAdapter();
    private final NewsAdapter news = new NewsAdapter();
    private final PostAdapter attractions = new PostAdapter(Post.TYPE_ATTRACTION, true);
    private final PostAdapter celebrities = new PostAdapter(Post.TYPE_CELEBRITY, true);
    private final PhotoAdapter photos = new PhotoAdapter(true);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle s) {
        b = FragmentHomeBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        state = new StateView(b.state);
        b.todayDate.setText(Jalali.today().format());
        b.forecast.setAdapter(forecast);
        b.newsList.setAdapter(news);
        b.attractionsList.setAdapter(attractions);
        b.celebritiesList.setAdapter(celebrities);
        b.photosList.setAdapter(photos);

        header(b.headerNews, R.string.latest_news, R.id.nav_news);
        header(b.headerAttractions, R.string.title_attractions, R.id.nav_attractions);
        header(b.headerCelebrities, R.string.title_celebrities, R.id.nav_celebrities);
        header(b.headerPhotos, R.string.latest_photos, R.id.nav_photos);

        b.refresh.setColorSchemeResources(R.color.brand_red);
        b.refresh.setOnChildScrollUpCallback((parent, child) -> b.content.canScrollVertically(-1));
        b.refresh.setOnRefreshListener(this::load);
        b.content.setVisibility(View.INVISIBLE);
        state.loading();
        load();
    }

    private void header(ViewSectionHeaderBinding h, int title, int tab) {
        h.sectionTitle.setText(title);
        h.sectionMore.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) ((MainActivity) getActivity()).selectTab(tab);
        });
    }

    private void load() {
        if (call != null) call.cancel();
        call = ApiClient.get().home();
        call.enqueue(new Callback<HomeData>() {
            @Override
            public void onSuccess(HomeData data) {
                if (b == null) return;
                b.refresh.setRefreshing(false);
                state.hide();
                b.content.setVisibility(View.VISIBLE);
                bind(data);
            }

            @Override
            public void onError(String message, int code) {
                if (b == null) return;
                b.refresh.setRefreshing(false);
                if (b.content.getVisibility() != View.VISIBLE) {
                    state.error(message, () -> {
                        state.loading();
                        load();
                    });
                }
            }
        });
    }

    private void bind(HomeData d) {
        bindWeather(d.weather);
        news.set(d.news);
        attractions.set(d.attractions);
        celebrities.set(d.celebrities);
        photos.set(d.photos);
        setSection(b.headerNews, b.newsList, d.news != null && !d.news.isEmpty());
        setSection(b.headerAttractions, b.attractionsList, d.attractions != null && !d.attractions.isEmpty());
        setSection(b.headerCelebrities, b.celebritiesList, d.celebrities != null && !d.celebrities.isEmpty());
        setSection(b.headerPhotos, b.photosList, d.photos != null && !d.photos.isEmpty());
    }

    private void setSection(ViewSectionHeaderBinding header, View list, boolean visible) {
        header.getRoot().setVisibility(visible ? View.VISIBLE : View.GONE);
        list.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void bindWeather(Weather w) {
        if (w == null || w.current == null) {
            b.weatherCard.setVisibility(View.GONE);
            return;
        }
        b.weatherCard.setVisibility(View.VISIBLE);
        Weather.Current c = w.current;
        b.temp.setText(Fa.temp(c.temp) + "°");
        b.weatherText.setText(c.text);
        b.weatherIcon.setText(WeatherIcons.emoji(c.icon, c.isDay));
        b.feelsLike.setText(getString(R.string.weather_feels_like, Fa.temp(c.feelsLike)));
        b.humidity.setText("💧 " + getString(R.string.weather_humidity, Fa.digits(c.humidity)));
        b.wind.setText("🌬️ " + getString(R.string.weather_wind, Fa.temp(c.wind)));
        if (w.sunrise != null && w.sunset != null) {
            b.sun.setVisibility(View.VISIBLE);
            b.sun.setText("🌅 " + Fa.digits(w.sunrise) + "  🌇 " + Fa.digits(w.sunset));
        } else {
            b.sun.setVisibility(View.GONE);
        }
        forecast.set(w.daily);
    }

    @Override
    public void scrollToTop() {
        if (b != null) b.content.smoothScrollTo(0, 0);
    }

    @Override
    public void onDestroyView() {
        if (call != null) call.cancel();
        b = null;
        super.onDestroyView();
    }
}
