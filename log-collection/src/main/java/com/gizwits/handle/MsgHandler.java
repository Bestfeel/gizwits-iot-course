package com.gizwits.handle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gizwits.analyse.HttpSemantic;
import com.gizwits.bean.DeviceInfo;
import com.gizwits.bean.RequestVoiceText;
import com.gizwits.noti2.client.NotiClient;
import me.chanjar.weixin.common.api.WxConsts;
import me.chanjar.weixin.common.exception.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import me.chanjar.weixin.mp.util.json.WxMpGsonBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

@Component
public class MsgHandler extends AbstractHandler {

    @Autowired
    private HttpSemantic httpSemantic;
    @Autowired
    private NotiClient notiClient;
    @Value("${app.product_key}")
    private String product_key;
    @Value("${app.did}")
    private String did;
    @Value("${app.mac}")
    private String mac;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService weixinService,
                                    WxSessionManager sessionManager) {

        if (!wxMessage.getMsgType().equals(WxConsts.XML_MSG_EVENT)) {
            //TODO 可以选择将消息保存到本地
        }

        //当用户输入关键词如“你好”，“客服”等，并且有客服在线时，把消息转发给在线客服
        try {
            if (StringUtils.startsWithAny(wxMessage.getContent(), "你好", "客服")
                    && weixinService.getKefuService().kfOnlineList()
                    .getKfOnlineList().size() > 0) {
                return WxMpXmlOutMessage.TRANSFER_CUSTOMER_SERVICE()
                        .fromUser(wxMessage.getToUser())
                        .toUser(wxMessage.getFromUser()).build();
            }
        } catch (WxErrorException e) {
            e.printStackTrace();
        }

        String content = "";
        //TODO 组装回复消息
        if (wxMessage.getMsgType().equals(WxConsts.XML_MSG_VOICE)) {
            content = wxMessage.getRecognition();
        } else if (wxMessage.getMsgType().equals(WxConsts.XML_MSG_TEXT)) {
            content = wxMessage.getContent();
        } else {

            content = WxMpGsonBuilder.create().toJson(wxMessage);
        }


        if (StringUtils.isNotEmpty(content)) {


            try {

                RequestVoiceText requestVoiceText = new RequestVoiceText();

                requestVoiceText.setText(content);
                DeviceInfo deviceInfo = new DeviceInfo();

                deviceInfo.setPk(product_key);
                deviceInfo.setDid(did);
                requestVoiceText.setDevices(Arrays.asList(deviceInfo));


                String voiceSemantic = httpSemantic.getVoiceSemantic(requestVoiceText);

                if (StringUtils.isNotEmpty(voiceSemantic)) {


                    JSONObject jsonObject = JSONObject.parseObject(voiceSemantic);

                    JSONArray object = jsonObject.getJSONArray("object");

                    if (!object.isEmpty()) {

                        Map cmd = object.getJSONObject(0).getObject("attr", Map.class);

                        notiClient.sendControlMessage(product_key, mac, did, cmd);
                    }

                    return new TextBuilder().build(jsonObject.getString("reply_text"), wxMessage, weixinService);
                }
            } catch (Exception e) {
                logger.error("{}", e);
            }
        }

        return new TextBuilder().build("没听懂你说什么", wxMessage, weixinService);


    }

}