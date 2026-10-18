package com.sweetsoft.ui.photos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sweetsoft.R;
import com.sweetsoft.data.ApiClient;
import com.sweetsoft.data.Session;
import com.sweetsoft.data.model.Photo;
import com.sweetsoft.data.model.Single;
import com.sweetsoft.databinding.ActivityUploadBinding;
import com.sweetsoft.ui.auth.LoginActivity;
import com.sweetsoft.ui.common.BaseActivity;
import com.sweetsoft.ui.common.Callback;
import com.sweetsoft.util.ImageCompressor;
import com.sweetsoft.util.Ui;
import com.sweetsoft.util.SystemBars;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class UploadPhotoActivity extends BaseActivity {
    private ActivityUploadBinding b;
    private Uri picked;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private final ActivityResultLauncher<PickVisualMediaRequest> picker =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri == null) return;
                picked = uri;
                b.pickHint.setVisibility(View.GONE);
                b.preview.setVisibility(View.VISIBLE);
                Glide.with(this).load(uri).centerCrop().into(b.preview);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!Session.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class).putExtra(LoginActivity.EXTRA_NEXT_UPLOAD, true));
            finish();
            return;
        }
        b = ActivityUploadBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, getString(R.string.title_upload));
        SystemBars.padBottom(b.getRoot());
        b.pickCard.setOnClickListener(v -> picker.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build()));
        b.submit.setOnClickListener(v -> submit());
    }

    private void submit() {
        String title = b.title.getText() != null ? b.title.getText().toString().trim() : "";
        String desc = b.description.getText() != null ? b.description.getText().toString().trim() : "";
        if (picked == null) {
            Ui.snack(b.getRoot(), getString(R.string.photo_required));
            return;
        }
        if (title.isEmpty()) {
            b.titleLayout.setError(getString(R.string.title_required));
            return;
        }
        b.titleLayout.setError(null);
        setBusy(true);
        Uri uri = picked;
        io.execute(() -> {
            try {
                File file = ImageCompressor.compress(this, uri);
                main.post(() -> upload(file, title, desc));
            } catch (Exception e) {
                main.post(() -> {
                    setBusy(false);
                    Ui.snack(b.getRoot(), getString(R.string.photo_required));
                });
            }
        });
    }

    private void upload(File file, String title, String desc) {
        MediaType text = MediaType.get("text/plain");
        MultipartBody.Part image = MultipartBody.Part.createFormData("image", file.getName(),
                RequestBody.create(file, MediaType.get("image/jpeg")));
        track(ApiClient.get().uploadPhoto(image, RequestBody.create(title, text), RequestBody.create(desc, text)))
                .enqueue(new Callback<Single<Photo>>() {
                    @Override
                    public void onSuccess(Single<Photo> body) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                        setBusy(false);
                        new MaterialAlertDialogBuilder(UploadPhotoActivity.this)
                                .setMessage(R.string.upload_success)
                                .setPositiveButton(R.string.my_photos, (d, w) -> {
                                    startActivity(new Intent(UploadPhotoActivity.this, MyPhotosActivity.class));
                                    finish();
                                })
                                .setOnCancelListener(d -> finish())
                                .setNegativeButton(android.R.string.ok, (d, w) -> finish())
                                .show();
                    }

                    @Override
                    public void onError(String message, int code) {
                        //noinspection ResultOfMethodCallIgnored
                        file.delete();
                        setBusy(false);
                        if (code == 401) {
                            Session.clear();
                            startActivity(new Intent(UploadPhotoActivity.this, LoginActivity.class)
                                    .putExtra(LoginActivity.EXTRA_NEXT_UPLOAD, true));
                            finish();
                            return;
                        }
                        Ui.snack(b.getRoot(), message);
                    }
                });
    }

    private void setBusy(boolean busy) {
        b.progress.setIndeterminate(true);
        b.progress.setVisibility(busy ? View.VISIBLE : View.INVISIBLE);
        b.submit.setEnabled(!busy);
        b.submit.setText(busy ? R.string.uploading : R.string.upload);
        b.pickCard.setEnabled(!busy);
    }

    @Override
    protected void onDestroy() {
        io.shutdownNow();
        super.onDestroy();
    }
}
