package com.example.androidbarberstaffapp.Retrofit;



import com.example.androidbarberstaffapp.Model.FCMResponse;
import com.example.androidbarberstaffapp.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({"Content-Type:application/json", "Authorization:key=AAAA9XJ58xc:APA91bHCugbPsxGHVvzyNEUa-D4UvwZDJb11JCRVaPzpm3WIPSDCHNtRF3oa7NAru-IVQ1VvKjlINDgMXlbIm6LG9cTFzlLKM0y1YnJ96p33fxKrt_AyLhroblOlSvAI6ZziWD7EiPKn"})
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
