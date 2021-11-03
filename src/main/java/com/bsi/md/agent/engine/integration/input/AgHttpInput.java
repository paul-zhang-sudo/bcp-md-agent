package com.bsi.md.agent.engine.integration.input;

import com.bsi.md.agent.datasource.AgApiTemplate;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.utils.AgTokenUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * http输入节点
 * @author fish
 */
@Slf4j
public class AgHttpInput extends AgCommonInput{

    /**
     * 读取数据
     * @param context
     * @return
     */
    @Override
    public Object read(Context context) {
        setToken(context);
       return super.read(context);
    }

    /**
     * token设置
     * @param context
     */
    public void setToken(Context context){
        //获取token放入context
        AgIntegrationConfigVo config = (AgIntegrationConfigVo) context.get("config");
        String dataSourceId = config.getInputNode().getString("dataSource");
        AgApiTemplate apiTemplate =  AgDatasourceContainer.getApiDataSource( dataSourceId );
        if( "token".equals( apiTemplate.getAuthType() ) ){
            context.put("inputToken",AgTokenUtils.getToken(apiTemplate));
        }
    }
}
