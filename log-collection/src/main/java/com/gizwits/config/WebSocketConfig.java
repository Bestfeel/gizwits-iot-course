package com.gizwits.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * Created by feel on 2017/2/1.
 * websocket 配置
 * 参考官网文档：https://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html
 */
@Configuration
@EnableWebSocketMessageBroker //开启使用STOMP协议来传输基于代理的消息
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
    /**
     * 注册STOMP协议的节点，并指定映射的UR
     *
     * @param stompEndpointRegistry
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry stompEndpointRegistry) {
        stompEndpointRegistry.addEndpoint("/endpointLogMonitor")
                .setAllowedOrigins("*")
                .withSockJS(); //注册STOMP协议节点，同时指定使用SockJS协议，允许跨域
    }

    /**
     * 实现推送功能，配置消息代理
     *
     * @param config
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/exchange"); //  消息定义主题，可开放多个消息主题
        config.setApplicationDestinationPrefixes("/app"); //全局使用的订阅前缀（客户端订阅路径上会体现出来）,和@MessageMapping 进行组合
        config.setUserDestinationPrefix("/user"); //点对点使用的订阅前缀（客户端订阅路径上会体现出来），不设置的话，默认也是/user/
    }


}
