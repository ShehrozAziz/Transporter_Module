package com.example.transporter_module;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/sendToJavaApp")
    Call<ServerResponse> sendData(@Body ClientData clientData);
    @GET("/sendToJavaApp")
    Call<ServerResponse> getMessage();
}

