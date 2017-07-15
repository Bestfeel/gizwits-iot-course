package com.gizwits.analyse;

import com.gizwits.bean.RequestVoiceText;
import me.chanjar.weixin.mp.util.json.WxMpGsonBuilder;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Retrofit;

import java.io.IOException;

/**
 * Created by feel on 2017/7/14.
 */
@Component
public class HttpSemantic {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpSemantic.class);

    @Autowired
    private Retrofit newRetrofit;

    public String getVoiceSemantic(RequestVoiceText voiceText) {

        IContent iContent = newRetrofit.create(IContent.class);

        Call<ResponseBody> content = iContent.getContent(voiceText);

        try {
            String semText = content.execute().body().string();

            return semText;
        } catch (IOException e) {

            LOGGER.error("{}", e);
            e.printStackTrace();
            return null;
        }
    }


}
