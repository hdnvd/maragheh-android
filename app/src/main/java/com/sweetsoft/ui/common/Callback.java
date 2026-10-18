package com.sweetsoft.ui.common;

import androidx.annotation.NonNull;

import com.sweetsoft.data.ApiClient;

import retrofit2.Call;
import retrofit2.Response;

/** Retrofit callback that splits success and a single human-readable failure message. */
public abstract class Callback<T> implements retrofit2.Callback<T> {
    private static final String NETWORK_ERROR = "خطا در ارتباط با سرور. اتصال اینترنت را بررسی کنید.";

    @Override
    public final void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (call.isCanceled()) return;
        if (response.isSuccessful()) {
            onSuccess(response.body());
        } else {
            onError(ApiClient.errorMessage(response, NETWORK_ERROR), response.code());
        }
    }

    @Override
    public final void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (call.isCanceled()) return;
        onError(NETWORK_ERROR, 0);
    }

    public abstract void onSuccess(T body);

    public abstract void onError(String message, int code);
}
