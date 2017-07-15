package com.gizwits.controller;

import com.gizwits.bean.Contants;
import com.gizwits.bean.ParticipantRepository;
import com.gizwits.bean.ResponseMessage;
import com.gizwits.tail.Tailer;
import com.gizwits.tail.TailerListenerAdapter;
import com.gizwits.util.LRUCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

/**
 * Created by feel on 2017/2/1.
 */
@ApiIgnore//使用该注解忽略这个API
@Controller
@RequestMapping(value = "/logMonitor")
public class WsController {

    private static Logger logger = LoggerFactory.getLogger(WsController.class.getName());
    @Value("${logMonitor.logpath}")
    private String logpath;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;   //注入SimpMessagingTemplate 用于点对点消息发送
    @Autowired
    private ParticipantRepository participantRepository;
    @Autowired
    private LRUCache lruCache;
    private Tailer tailer;
    private Thread thread;
    private static volatile boolean runFlag = true;

    /**
     * 参考:http://docs.spring.io/spring/docs/current/spring-framework-reference/html/websocket.html#websocket-fallback-sockjs-heartbeat
     * 相关的事件：SessionConnectEvent，SessionConnectedEvent，SessionSubscribeEvent，SessionUnsubscribeEvent，SessionDisconnectEvent
     *
     * @param event
     */
    @EventListener
    private void handleSessionSubscribeEvent(SessionSubscribeEvent event) {

        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String username = headers.getUser().getName();
        Map<String, String> activeSessions = participantRepository.getActiveSessions();
        if (!activeSessions.values().contains(username) || lruCache.isEmpty()) {
            tailFile(username);
        } else if (!activeSessions.keySet().contains(headers.getSessionId())) {

            lruCache.values().forEach(log -> {
                messagingTemplate.convertAndSendToUser(username, "/exchange/logMonitor", (String) log);
            });
        }
        // We store the session as we need to be idempotent in the disconnect event processing
        participantRepository.add(headers.getSessionId(), username);

    }

    private void tailFile(final String userName) {
        if (runFlag) {
            File file = new File(logpath);
            String[] split = this.getClass().getPackage().getName().split("\\.");
            String filterName = split[0] + "." + split[1];
            if (file.exists()) {
                tailer = new Tailer(file, new TailerListenerAdapter() {
                    @Override
                    public void handle(String line) {
                        if (runFlag) {
                            messagingTemplate.convertAndSendToUser(userName, "/exchange/logMonitor", line);
                        }
                        if (line.contains(filterName)) {
                            lruCache.put(UUID.randomUUID().toString(), line);
                        }
                    }
                }, 500, true);
                thread = new Thread(tailer);
                thread.start();
            } else {
                logger.error("file is not  exists ,{}", file);
            }

        }
    }

    @EventListener
    private void handleSessionDisconnect(SessionDisconnectEvent event) {

        Optional.ofNullable(participantRepository.getParticipant(event.getSessionId()))
                .ifPresent(login -> {
                    logger.info("连接断开：" + event.getUser().getName() + "。。。" + event.getSessionId());

                    participantRepository.removeParticipant(event.getSessionId());

                    if (participantRepository.getActiveSessions().isEmpty()) {
                        this.stop();
                    }
                });
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseMessage status(HttpServletRequest request) {

        String logMonitor = (String) request.getServletContext().getAttribute("logMonitor");
        if (StringUtils.isEmpty(logMonitor)) {
            request.getServletContext().setAttribute("logMonitor", Contants.ACTIONS[0]);
            logMonitor = Contants.ACTIONS[0];
        }
        return new ResponseMessage(Contants.SUCCESS_0, "日志系统嗅探状态：" + logMonitor, request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
    }

    @RequestMapping(value = "/set/{action}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseMessage set(@PathVariable String action, HttpServletRequest request) {

        if (Arrays.asList(Contants.ACTIONS).contains(action)) {
            if (!StringUtils.isEmpty(action)) {
                request.getServletContext().setAttribute("logMonitor", action);
            }
            if (action.equalsIgnoreCase(Contants.ACTIONS[0])) {
                runFlag = true;
                Collection<String> values = participantRepository.getActiveSessions().values();

                if (!values.isEmpty()) {
                    tailFile(values.stream().findFirst().get());
                }

                return new ResponseMessage(Contants.SUCCESS_0, "日志系统嗅探开启成功", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
            } else {
                runFlag = false;
                stop();
                return new ResponseMessage(Contants.SUCCESS_0, "日志系统嗅探关闭成功", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
            }
        } else {
            return new ResponseMessage(Contants.ERROR_0, "日志系统嗅探开启失败,非法参数", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
        }

    }

    @RequestMapping("")
    public String index() {
        return "index";
    }

    /**
     * 日志监听关闭
     */
    private void stop() {
        if (tailer != null && thread.isAlive()) {
            tailer.stop();
            thread.interrupt();
            lruCache.clear();
        }
    }
}
