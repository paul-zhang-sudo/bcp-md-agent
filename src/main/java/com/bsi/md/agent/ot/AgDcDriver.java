package com.bsi.md.agent.ot;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.md.agent.entity.dto.AgConfigDto;
import com.bsi.md.agent.entity.dto.AgDataSourceDto;
import com.bsi.md.agent.service.AgConfigService;
import com.bsi.md.agent.service.AgDataSourceService;
import com.huaweicloud.sdk.iot.module.DcClient;
import com.huaweicloud.sdk.iot.module.ModuleShadowNotificationCallback;
import com.huaweicloud.sdk.iot.module.PointsCallback;
import com.huaweicloud.sdk.iot.module.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;

@Slf4j
@Component
public class AgDcDriver implements PointsCallback, ModuleShadowNotificationCallback {

    /**
     * 数采应用客户端，与边缘Hub建立MQTT连接
     */
    private DcClient dcClient;

    private AgConfigService agConfigService;
    private AgDataSourceService agDataSourceService;
    @PostConstruct
    void init() throws Exception {
        log.info("初始化dcClient");
        //打开客户端
        try {
            dcClient = DcClient.createFromEnv();
            dcClient.open();
            //设置回调，并同步模块影子
            dcClient.setPointsCallback(this);
            dcClient.startModuleShadow(this);
        }catch (Exception ignored) {
        }
    }

    /**
     * 收到模块下行数采配置，消息需要缓存或持久化
     *进入边缘节点详情-》应用模块-》数采配置-》下发按钮
     */
    @Override
    public void onModuleShadowReceived(ModuleShadowNotification shadow) {
        log.info("收到ot下发的配置信息。。。");
        log.info("shadow:{}", JSON.toJSONString(shadow));

        JSONObject obj = JSON.parseObject( JSON.toJSONString(shadow) );
        JSONObject defaultValue = obj.getJSONObject("properties").getJSONObject("default_values");
        //初始化数据源
        JSONObject otParam = obj.getJSONObject("properties").getJSONObject("connection_info");
        log.info("otParam:{}",otParam.toJSONString());
        JSONArray dsArr = defaultValue.getJSONArray("bcp_ds");
        for(int i=0;i<dsArr.size();i++){
            AgDataSourceDto dto = JSON.parseObject(dsArr.getString(i),AgDataSourceDto.class);
            log.info("dsDto:{}",JSON.toJSONString(dto));
            JSONObject cfv = JSON.parseObject(dto.getConfigValue());
            if(cfv.size()>0){
                log.info("进行属性替换");
                cfv.forEach((k,v)->{
                    if("globalParams".equals(k)){
                        return;
                    }
                    cfv.put(k,otParam.getOrDefault(dto.getId()+"_"+k,v));
                });
            }
            JSONArray globalParams = cfv.getJSONArray("globalParams");
            if(globalParams!=null && globalParams.size()>0){
                for(int j=0;j<globalParams.size();j++){
                    JSONObject glParam = globalParams.getJSONObject(j);
                    glParam.put("value",otParam.getOrDefault(dto.getId()+"_"+glParam.get("key"),glParam.get("value")));
                }
            }
            dto.setConfigValue(cfv.toJSONString());
            log.info("要更新的数据源:{}",JSON.toJSONString(dto));
            //刷新数据源
            agDataSourceService.updateDS(dto);
        }
        //初始化配置
        AgConfigDto cfn = JSON.parseObject(defaultValue.getString("bcp_conf"),AgConfigDto.class);
        //配置中有些配置参数需要从点位中取值 TODO
        if(cfn!=null){
            agConfigService.updateConfig( cfn );
        }

        //根据前缀把数据源拆分出来
//        Map<String,JSONObject> dsMap = new HashMap<>();
//        ds.forEach((k,v)->{
//            if(k.indexOf("script")>=0){
//                return;
//            }
//            String key = k.substring(0,k.indexOf("_"));
//            JSONObject tmp = dsMap.get(key);
//            if(tmp==null){
//                tmp = new JSONObject();
//                dsMap.put(k,tmp);
//            }
//            tmp.put(k.replace(key,""),v);
//        });
//        String[] excludeProperties = {"id", "name","type","classify","delFlag"};
//        PropertyPreFilters filters = new PropertyPreFilters();
//        PropertyPreFilters.MySimplePropertyPreFilter excludeFilter = filters.addFilter();
//        excludeFilter.addExcludes(excludeProperties);
        //组装、刷新数据源
//        dsMap.values().forEach(a->{
//            AgDataSourceDto dto = new AgDataSourceDto();
//            dto.setId(a.getString("id"));
//            dto.setName(a.getString("name"));
//            dto.setType(a.getString("type"));
//            dto.setClassify( a.getString("classify") );
//            dto.setDelFlag(a.getBoolean("delFlag")==null?false:a.getBoolean("delFlag"));
//            dto.setConfigValue(JSON.toJSONString(obj,excludeFilter));
//            //刷新数据源
//            agDataSourceService.updateDS(dto);
//        });

        //组装、刷新config
//        JSONObject conf = obj.getJSONObject("points");
//        conf.forEach((k,v)->{
//            JSONObject val = (JSONObject) v;
//            AgConfigDto dto = new AgConfigDto();
//            dto.setId(val.getString("id"));
//            dto.setName(val.getString("name"));
//            dto.setConfigValue(val.getString("configValue"));
//            agConfigService.updateConfig(dto);
//        });
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

    private String generateCron(int minute){
        String cron = "0 %s * * * ?";
        return String.format(cron,minute<60?("0/"+minute):"0/59");
    }
}
