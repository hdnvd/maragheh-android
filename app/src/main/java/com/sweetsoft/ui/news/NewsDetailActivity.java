package com.sweetsoft.ui.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.model.News;
import com.sweetsoft.data.model.Single;
import com.sweetsoft.databinding.ActivityNewsDetailBinding;
import com.sweetsoft.ui.common.BaseActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.ui.common.StateView;
import com.sweetsoft.util.Fa;
import com.sweetsoft.util.HtmlPage;
import com.sweetsoft.util.Ui;
import com.sweetsoft.util.SystemBars;

public class NewsDetailActivity extends BaseActivity {
    private static final String EXTRA_ID = "id";
    private static final String EXTRA_TITLE = "title";

    private ActivityNewsDetailBinding b;
    private StateView state;
    private News news;

    public static void start(Context ctx, long id, String title) {
        ctx.startActivity(new Intent(ctx, NewsDetailActivity.class)
                .putExtra(EXTRA_ID, id)
                .putExtra(EXTRA_TITLE, title));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityNewsDetailBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "");
        SystemBars.padBottom(b.content);
        state = new StateView(b.state);
        b.title.setText(getIntent().getStringExtra(EXTRA_TITLE));
        load();
    }

    private void load() {
        b.content.setVisibility(View.INVISIBLE);
        state.loading();
        long id = getIntent().getLongExtra(EXTRA_ID, 0);
        track(ApiClient.get().newsDetail(id)).enqueue(new Callback<Single<News>>() {
            @Override
            public void onSuccess(Single<News> body) {
                if (body == null || body.data == null) {
                    state.empty();
                    return;
                }
                state.hide();
                bind(body.data);
            }

            @Override
            public void onError(String message, int code) {
                state.error(message, NewsDetailActivity.this::load);
            }
        });
    }

    private void bind(News n) {
        news = n;
        b.content.setVisibility(View.VISIBLE);
        b.title.setText(n.title);
        b.source.setText(n.source != null ? n.source.name : "");
        b.source.setVisibility(n.source != null ? View.VISIBLE : View.GONE);
        b.date.setText(Fa.digits(n.publishedAtFa));
        b.summary.setText(n.summary);
        b.summary.setVisibility(TextUtils.isEmpty(n.summary) ? View.GONE : View.VISIBLE);
        if (!TextUtils.isEmpty(n.image)) {
            Ui.loadImage(b.image, n.image);
        } else {
            b.appBar.setExpanded(false, false);
        }
        HtmlPage.render(b.body, n.bodyHtml);
        b.openSource.setVisibility(TextUtils.isEmpty(n.url) ? View.GONE : View.VISIBLE);
        b.openSource.setOnClickListener(v -> Ui.openUrl(this, n.url));
        invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        menu.findItem(R.id.action_share).setVisible(news != null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_share && news != null) {
            String text = news.title + (news.url != null ? "\n" + news.url : "")
                    + "\n\n" + getString(R.string.share_app_text);
            Ui.shareText(this, news.title, text);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
