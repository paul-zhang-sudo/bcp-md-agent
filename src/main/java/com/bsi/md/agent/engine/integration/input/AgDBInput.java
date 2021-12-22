package com.bsi.md.agent.engine.integration.input;

import com.bsi.md.agent.datasource.AgApiTemplate;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import com.bsi.md.agent.entity.vo.AgIntegrationConfigVo;
import com.bsi.md.agent.utils.AgTokenUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库输入节点
 * @author fish
 */
@Slf4j
public class AgDBInput extends AgCommonInput{

    /**
     * 读取数据
     * @param context
     * @return
     */
    @Override
    public Object read(Context context) throws Exception{
       return super.read(context);
    }
}
