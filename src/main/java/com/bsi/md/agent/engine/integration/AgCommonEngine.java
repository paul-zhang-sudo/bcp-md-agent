package com.bsi.md.agent.engine.integration;

import com.bsi.md.agent.engine.integration.input.AgInput;
import com.bsi.md.agent.engine.integration.output.AgOutput;
import com.bsi.md.agent.engine.integration.transform.AgTransform;
import lombok.Data;

/**
 * 集成引擎父类，实现简单逻辑
 */
@Data
public class AgCommonEngine implements AgIntegrationEngine {
    //输入
    protected AgInput input;
    //转换
    protected AgTransform transform;
    //输出
    protected AgOutput output;

    @Override
    public Object input(Context context) throws Exception{
        return input.read(context);
    }

    @Override
    public Object transform(Context context) throws Exception{
        return transform.transform(context);
    }

    @Override
    public Object output(Context context) throws Exception{
        return output.write(context);
    }
}