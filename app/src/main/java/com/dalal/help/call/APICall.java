package com.dalal.help.call;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class APICall implements Callback<NoteRes> {

    static final String BASE_URL = "https://fcm.googleapis.com/";

    public void start(NoteReq request) {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        NoteAPI notificationAPI = retrofit.create(NoteAPI.class);

        Call<NoteRes> call = notificationAPI.sendNote(request);
        call.enqueue(this);

    }


    @Override
    public void onResponse(Call<NoteRes> call, Response<NoteRes> response) {
        if (response.isSuccessful()) {
            response.body();
        } else {
            System.out.println(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<NoteRes> call, Throwable t) {
        t.printStackTrace();
    }

    OkHttpClient okHttpClient = new OkHttpClient().newBuilder().addInterceptor(new Interceptor() {
        @Override
        public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
            Request originalRequest = chain.request();
            Request.Builder builder = originalRequest.newBuilder().header("Authorization", "key=AIzaSyAxMMU7_3B_44G1yhnO3-PjhlkxTOK_8pQ");
            Request newRequest = builder.build();
            return chain.proceed(newRequest);
        }
    }).build();


    public void sendNote(String token, String title, String body) {

        NoteReq notificationRequest = new NoteReq();
        notificationRequest.setTo(token);
        notificationRequest.setPriority("normal");
        Note notification = new Note();
        notification.setTitle(title);
        notification.setBody(body);
        notificationRequest.setNotification(notification);
        this.start(notificationRequest);
    }

    public void sendNoteToGroup(String topic, String title, String body) {

        NoteReq notificationRequest = new NoteReq();
        notificationRequest.setTo("/topics/" + topic);
        notificationRequest.setPriority("normal");
        Note notification = new Note();
        notification.setTitle(title);
        notification.setBody(body);
        notificationRequest.setNotification(notification);
        this.start(notificationRequest);
    }
}
