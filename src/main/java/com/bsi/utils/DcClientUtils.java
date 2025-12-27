package com.bsi.utils;

import com.bsi.framework.core.utils.FwSpringContextUtil;
import com.bsi.md.agent.ot.AgDcDriver;
import com.huawei.m2m.edge.daemon.util.JacksonUtil;
import com.huaweicloud.sdk.iot.module.DcClient;
import com.huaweicloud.sdk.iot.module.dto.PointsReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * OT数采工具类
 */
public class DcClientUtils {

    private static Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");

    private static DcClient getDcClient(){
        return FwSpringContextUtil.getBean("agDcDriver",AgDcDriver.class).getDcClient();
    }

    /**
     *
     * @param points
     * @return
     */
    public static boolean reportPoints(Map<String, Object> points) {
        PointsReport report = new PointsReport();
        report.setPoints(points);
        try {
            info_log.info("需要上报的数据:{}", JacksonUtil.pojo2Json(report));
            getDcClient().pointReport(report);
            return true;
        } catch (Exception e) {
            info_log.error("数据上报出现错误 ",e);
            return false;
        }
    }
}
