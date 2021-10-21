package com.bsi.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * 过滤掉不想要的节点
 */
@Slf4j
public class AgentXStream extends XStream {
    public AgentXStream() {
       super();
    }

    public AgentXStream(HierarchicalStreamDriver hierarchicalStreamDriver) {
        super(hierarchicalStreamDriver);
    }
    @Override
    protected MapperWrapper wrapMapper(MapperWrapper next) {
        return new MapperWrapper(next) {
            @Override
            public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") Class definedIn, String fieldName) {
                log.info(fieldName);
                // 不能识别的节点，掠过。
                if (definedIn == Object.class) {
                    return false;
                }

                // 节点名称为fileName的掠过
                if (fieldName.equals("map")) {
                    return false;
                }
                return super.shouldSerializeMember(definedIn, fieldName);
            }
        };
    }

}