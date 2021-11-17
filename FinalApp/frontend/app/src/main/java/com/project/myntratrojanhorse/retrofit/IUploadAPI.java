package com.project.myntratrojanhorse.retrofit;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface IUploadAPI {
    @Multipart
    @POST("/api/acne")
    Call<String> uploadAcne(@Part MultipartBody.Part file);

    @Multipart
    @POST("/api/tone")
    Call<String> uploadTone(@Part MultipartBody.Part file);

    @Multipart
    @POST("/api/gender")
    Call<String> uploadGender(@Part MultipartBody.Part file);

    @Multipart
    @POST("/api/wrinkle")
    Call<String> uploadWrinkle(@Part MultipartBody.Part file);
}
