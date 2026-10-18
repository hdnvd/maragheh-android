package com.sweetsoft.data;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sweetsoft.BuildConfig;
import com.sweetsoft.data.model.ApiMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static ApiService service;
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create();

    private ApiClient() {}

    public static synchronized ApiService get() {
        if (service == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(90, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request.Builder b = chain.request().newBuilder()
                                .header("Accept", "application/json")
                                .header("X-App-Version", String.valueOf(BuildConfig.VERSION_CODE));
                        String token = Session.getToken();
                        if (token != null) b.header("Authorization", "Bearer " + token);
                        return chain.proceed(b.build());
                    })
                    .build();
            service = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(GSON))
                    .build()
                    .create(ApiService.class);
        }
        return service;
    }

    /** Extracts the first human-readable error from a Laravel error response. */
    public static String errorMessage(Response<?> response, String fallback) {
        try (ResponseBody body = response.errorBody()) {
            if (body == null) return fallback;
            ApiMessage msg = GSON.fromJson(body.charStream(), ApiMessage.class);
            if (msg != null) {
                if (msg.errors != null) {
                    for (List<String> list : msg.errors.values()) {
                        if (list != null && !list.isEmpty()) return list.get(0);
                    }
                }
                if (msg.message != null && !msg.message.isEmpty()) return msg.message;
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }
}
