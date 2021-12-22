package com.bsi.md.agent.engine.integration.output;

import com.bsi.md.agent.engine.integration.Context;
import lombok.extern.slf4j.Slf4j;

/**
 * 数据库输出节点
 * @author fish
 */
@Slf4j
public class AgDBOutput extends AgCommonOutput{

    /**
     * 写入数据
     * @param context
     * @return Object
     */
    @Override
    public Object write(Context context) throws Exception{
       return super.write(context);
    }
}
