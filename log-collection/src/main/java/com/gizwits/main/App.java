package com.gizwits.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.servlet.ErrorPage;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;

/**
 * Created by feel on 2017/2/1.
 */
@SpringBootApplication(scanBasePackages = {"com.gizwits"})
public class App {


    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /**
     * 错误页面需要放在Spring Boot web应用的static内容目录下，
     * 它的默认位置是：src/main/resources/static
     *
     * @return
     */
    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {

        return new EmbeddedServletContainerCustomizer() {
            @Override
            public void customize(ConfigurableEmbeddedServletContainer container) {

                ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/");
                ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/");
                ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/");
                ErrorPage error405Page = new ErrorPage(HttpStatus.METHOD_NOT_ALLOWED, "/");

                container.addErrorPages(error401Page, error404Page, error500Page, error405Page);
            }
        };
    }

    public static void main(String[] args) throws InterruptedException {

        SpringApplication.run(App.class, args);

    }

}
