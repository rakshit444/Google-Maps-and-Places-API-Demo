package com.api.uniconn.Rest;

import android.util.Log;

import com.api.uniconn.Rest.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Rakshit on 6/22/2016.
 */

public class RestClient {


    private static final String TAG = "Rest Client";
    private ApiService mApiService;
    private HttpLoggingInterceptor interceptor;
    private OkHttpClient.Builder client;



    public RestClient() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
                .create();
        interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        Log.i(TAG,"new timeout");
        client = new OkHttpClient.Builder().addInterceptor(interceptor)
                .connectTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5,TimeUnit.MINUTES)
                 .writeTimeout(5,TimeUnit.MINUTES);
        Log.i(TAG,"executed new timeout");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client.build())
                .build();

        Log.i(TAG, "setting retrofit service");

        mApiService =
                retrofit.create(ApiService.class);
        Log.i(TAG, "Done");
    }

    public ApiService getApiService() {
        return mApiService;
    }


}

