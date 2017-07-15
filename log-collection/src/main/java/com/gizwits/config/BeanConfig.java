package com.gizwits.config;

import com.gizwits.noti2.client.Events;
import com.gizwits.noti2.client.NotiClient;
import com.gizwits.util.LRUCache;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by feel on 2017/2/2.
 */
@Configuration
public class BeanConfig {

    @Value("${app.product_key}")
    private String product_key;
    @Value("${app.auth_id}")
    private String auth_id;
    @Value("${app.auth_secret}")
    private String auth_secret;
    @Value("${app.subkey}")
    private String subkey;
    @Value("${app.prefetch_count}")
    private int prefetch_count;
    @Value("${semantic.api}")
    private String analyseUrl;

    @Bean
    public LRUCache lruCache() {
        return new LRUCache<String, String>(1000);
    }

    @Bean(name = "notiClient")
    public NotiClient notiClient() {

        NotiClient notiClient = NotiClient
                .build()
                .setHost("snoti.gizwits.com")
                .setPort(2017)
                .login(product_key, auth_id, auth_secret, subkey, prefetch_count, Arrays.asList(Events.ONLINE, Events.OFFLINE, Events.STATUS_KV, Events.STATUS_RAW, Events.ATTR_ALERT, Events.ATTR_FAULT));

        notiClient.doStart();

        return notiClient;
    }


    /**
     * 获取Retrofit适配器。
     *
     * @return 网络适配器
     */
    @Bean(name = "newRetrofit")
    public Retrofit newRetrofit() {

        return new Retrofit.Builder().baseUrl(analyseUrl)
                .client(getClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * 获取 OkHttpClient
     *
     * @return OkHttpClient
     */
    private OkHttpClient.Builder getClient() {

        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS);


    }

}
