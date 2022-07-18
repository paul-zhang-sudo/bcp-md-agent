package com.bsi.md.agent.ot;

import com.alibaba.fastjson.JSON;
import com.huaweicloud.sdk.iot.module.DcClient;
import com.huaweicloud.sdk.iot.module.ModuleShadowNotificationCallback;
import com.huaweicloud.sdk.iot.module.PointsCallback;
import com.huaweicloud.sdk.iot.module.dto.*;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;

@Slf4j
public class AgDcDriver implements PointsCallback, ModuleShadowNotificationCallback {

    /**
     * 数采应用客户端，与边缘Hub建立MQTT连接
     */
    private DcClient dcClient;

    @PostConstruct
    void init() throws Exception {

        //打开客户端
        dcClient = DcClient.createFromEnv();
        dcClient.open();
        //设置回调，并同步模块影子
        dcClient.setPointsCallback(this);
        dcClient.startModuleShadow(this);
    }

    /**
     * 收到模块下行数采配置，消息需要缓存或持久化
     *进入边缘节点详情-》应用模块-》数采配置-》下发按钮
     */
    @Override
    public void onModuleShadowReceived(ModuleShadowNotification shadow) {
        log.info("收到ot下发的配置信息。。。");
        log.info("shadow:{}", JSON.toJSONString(shadow));
    }

    /**
     * 收到点位设置的处理
     */
    @Override
    public PointsSetRsp onPointSet(String requestId, PointsSetReq pointsSetReq) {
        return null;
    }

    /**
     * 收到点位读取的处理
     */
    @Override
    public PointsGetRsp onPointGet(String requestId, PointsGetReq pointsGetReq) {
        return null;
    }
}
