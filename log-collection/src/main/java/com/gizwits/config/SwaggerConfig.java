package com.gizwits.config;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static com.google.common.base.Predicates.not;
import static com.google.common.base.Predicates.or;
import static springfox.documentation.builders.PathSelectors.regex;
import static springfox.documentation.builders.RequestHandlerSelectors.withClassAnnotation;

/**
 * link: http://localhost:8080/swagger-ui.html
 * https://springfox.github.io/springfox/docs/snapshot/
 * https://github.com/swagger-api/swagger-core/wiki/Annotations-1.5.X
 */
@Configuration
public class SwaggerConfig {
    @Bean
    public Docket merchantStoreApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .genericModelSubstitutes(DeferredResult.class)
                .useDefaultResponseMessages(false)
                .forCodeGeneration(true)
                .pathMapping("/")// base，最终调用接口后会和paths拼接在一起
                .select()
                .apis(not(withClassAnnotation(ApiIgnore.class))) //SwaggerIngore的注解的controller将会被忽略
                .paths(paths())
                .paths(not(regex("/error")))
                .build()
                .apiInfo(testApiInfo());

    }

    private Predicate<String> paths() {
        return or(regex("/.*"));
    }

    private ApiInfo testApiInfo() {
        ApiInfo apiInfo = new ApiInfo("设备远程控制",//大标题
                "设备远程控制",//小标题
                "1.0",//版本
                "http://www.gizwits.com/",
                "fye@gizwits.com",//作者
                "The Apache License, Version 2.0",//链接显示文字
                "www.gizwits.com"//网站链接
        );
        return apiInfo;
    }

}