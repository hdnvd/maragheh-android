package com.sweetsoft.data;

import com.sweetsoft.data.model.AuthResponse;
import com.sweetsoft.data.model.HomeData;
import com.sweetsoft.data.model.News;
import com.sweetsoft.data.model.OtpResponse;
import com.sweetsoft.data.model.Paged;
import com.sweetsoft.data.model.Photo;
import com.sweetsoft.data.model.Post;
import com.sweetsoft.data.model.Single;
import com.sweetsoft.data.model.Source;
import com.sweetsoft.data.model.User;
import com.sweetsoft.data.model.Weather;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("home")
    Call<HomeData> home();

    @GET("weather")
    Call<Weather> weather();

    @GET("news")
    Call<Paged<News>> news(@Query("page") int page,
                           @Query("q") String query,
                           @Query("from") String fromDate,
                           @Query("to") String toDate,
                           @Query("source_id") Long sourceId);

    @GET("news/{id}")
    Call<Single<News>> newsDetail(@Path("id") long id);

    @GET("sources")
    Call<Single<List<Source>>> sources();

    @GET("posts")
    Call<Paged<Post>> posts(@Query("type") String type, @Query("page") int page, @Query("q") String query);

    @GET("posts/{id}")
    Call<Single<Post>> post(@Path("id") long id);

    @GET("photos")
    Call<Paged<Photo>> photos(@Query("page") int page);

    // --- auth ---
    @FormUrlEncoded
    @POST("auth/otp/request")
    Call<OtpResponse> requestOtp(@Field("phone") String phone);

    @FormUrlEncoded
    @POST("auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Field("phone") String phone, @Field("code") String code, @Field("name") String name);

    @POST("auth/logout")
    Call<Void> logout();

    @GET("me")
    Call<Single<User>> me();

    // --- photo submissions (auth) ---
    @GET("me/photos")
    Call<Paged<Photo>> myPhotos(@Query("page") int page);

    @Multipart
    @POST("me/photos")
    Call<Single<Photo>> uploadPhoto(@Part MultipartBody.Part image,
                                    @Part("title") RequestBody title,
                                    @Part("description") RequestBody description);
}
