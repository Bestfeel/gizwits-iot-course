package com.gizwits.controller;

import com.alibaba.fastjson.JSONObject;
import com.gizwits.bean.Contants;
import com.gizwits.bean.PayloadBody;
import com.gizwits.bean.ResponseMessage;
import com.gizwits.noti2.client.NotiClient;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by feel on 2017/7/12.
 */
@EnableSwagger2
@RestController
@Api(value = "api", description = "设备远程控制", tags = {"device"})
@RequestMapping(value = "/")
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private NotiClient notiClient;
    @Value("${app.product_key}")
    private String product_key;

    @ApiIgnore//使用该注解忽略这个API
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public ResponseMessage index(HttpServletRequest request) {

        return new ResponseMessage(Contants.SUCCESS_0, "欢迎使用设备日志采集系统，使用如下跳转链接", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/logMonitor");

    }


    /**
     * <code>
     * {
     * "mac": "xxxx",
     * "did": "xxx",
     * "cmd": {
     * "LED_OnOff": false,
     * "LED_Color": "紫色",
     * "Motor_Speed": 0
     * }
     * }
     * </code>
     *
     * @param cmd
     * @param request
     * @return
     */
    @CrossOrigin
    @ApiOperation(value = "设备远程控制", notes = "测试接口详细描述")
    @RequestMapping(value = "/dev/control", method = RequestMethod.POST, produces = "application/json")
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage remoteControl(@ApiParam(name = "cmd", required = true, value = "控制设备命令指令 example : <code>{\n" +
            "  \"mac\": \"xx\",\n" +
            "  \"did\": \"xx\",\n" +
            "  \"cmd\": {\n" +
            "    \"LED_OnOff\": false,\n" +
            "    \"LED_Color\": \"紫色\",\n" +
            "    \"Motor_Speed\": 1\n" +
            "  }\n" +
            "}</code> ") @RequestBody String cmd, HttpServletRequest request) {


        if (cmd != null) {

            PayloadBody map = JSONObject.parseObject(cmd, PayloadBody.class);

            if (!map.isEmpty()) {
                notiClient.sendControlMessage(product_key, map.getMac(), map.getDid(), map.getCmd());

                return new ResponseMessage(Contants.SUCCESS_0, "success", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/dev/control");
            }

        }
        return new ResponseMessage(Contants.ERROR_0, "error", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/dev/control");


    }

    @PostConstruct
    public void listenLog() {


        Thread thread = new Thread(() -> {
            //订阅(接收)推送事件消息
            String messgae = null;
            while ((messgae = notiClient.reveiceMessgae()) != null) {
                logger.info("实时接收snoti消息:{}", messgae);
            }
        });
        thread.setName("--listenLogThread--");
        thread.start();


    }
}
