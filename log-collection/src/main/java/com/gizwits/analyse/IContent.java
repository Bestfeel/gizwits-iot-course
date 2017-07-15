package com.gizwits.analyse;

import com.gizwits.bean.RequestVoiceText;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IContent {
    @Headers({"Content-Type: application/json",
            "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36"
    })
    @POST("/semantic_api/analyse")
    Call<ResponseBody> getContent(@Body RequestVoiceText body);

}