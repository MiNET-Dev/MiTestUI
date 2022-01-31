package com.minet.mitestui;

import android.content.Context;
import android.util.Log;

import androidx.fragment.app.DialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class APIStore {

    HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

    OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://192.168.100.72/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public interface MiEngine {
        @Headers({
                "Content-Type: application/json"
        })
        @POST("MiEngine/assets/api/AP.php")
        Call<ResponseBody> Login(@Body TechnicianLoginModel data);
    }

    public void Login(String username, String pin, Callback<ResponseBody> callback) {
        try {
            MiEngine MiEngine = retrofit.create(MiEngine.class);

            Call<ResponseBody> call = MiEngine.Login(new TechnicianLoginModel(username, pin));

            interceptor.level(HttpLoggingInterceptor.Level.BODY);

            call.enqueue(callback);
        } catch (Exception ex) {
            //
        }
    }

}
