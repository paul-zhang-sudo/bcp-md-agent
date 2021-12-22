package com.bsi.md.agent.utils;

import com.alibaba.fastjson.JSON;
import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;

/**
 * 代理服务工具类
 */
public class AgApiProxyUtils {

    /**
     * 判断指定路径是否已经做过api代理，如果配置了代理就把返回
     * @return
     */
    public static AgIntegrationConfigVo isProxied(String path){
        Object obj = EHCacheUtil.getValue(AgConstant.AG_EHCACHE_API,path);
        return obj!=null?JSON.parseObject(obj.toString(),AgIntegrationConfigVo.class):null;
    }
}
