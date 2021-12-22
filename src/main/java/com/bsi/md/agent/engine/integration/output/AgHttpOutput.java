package com.bsi.md.agent.engine.integration.output;

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
public class AgHttpOutput extends AgCommonOutput{

    /**
     * 写入数据
     * @param context
     * @return
     */
    @Override
    public Object write(Context context) throws Exception{
        setToken(context);
       return super.write(context);
    }

    /**
     * token设置
     * @param context
     */
    private void setToken(Context context){
        //获取token放入context
        AgIntegrationConfigVo config = (AgIntegrationConfigVo) context.get("config");
        String dataSourceId = config.getInputNode().getString("dataSource");
        AgApiTemplate apiTemplate =  AgDatasourceContainer.getApiDataSource( dataSourceId );
        if( "token".equals( apiTemplate.getAuthType() ) ){
            context.put("outputToken",AgTokenUtils.getToken(apiTemplate));
        }
    }
}
