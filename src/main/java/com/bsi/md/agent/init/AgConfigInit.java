package com.bsi.md.agent.init;

import com.bsi.md.agent.service.AgDataSourceService;
import com.bsi.md.agent.service.AgJobService;
import com.bsi.md.agent.service.AgWarnMethodService;
import com.bsi.md.agent.utils.IoTEdgeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


/**
 * 配置初始化
 * @author fish
 */
@Component
@Order(1)
@Slf4j
public class AgConfigInit implements ApplicationRunner {
	@Autowired
	private AgJobService agJobService;

	@Autowired
	private AgDataSourceService agDataSourceService;

	@Autowired
	private AgWarnMethodService agWarnMethodService;
	/**
	 * 初始化计划任务
	 * @param args
	 */
	@Override
	public void run(ApplicationArguments args) {
		log.info("=======================开始初始化数据源和集成配置信息=======================");
		try {
			IoTEdgeUtil.getDriverClient();
			log.info("=======================初始化IoTEdge服务=======================");
		}catch (Exception ignored){

		}

		//1、初始化数据源
		boolean flag1 = agDataSourceService.refreshDataSource();
        //2、初始化定时任务和实时接口
		boolean flag2 = agJobService.refreshJob();
		//3、初始化告警配置
		boolean flag3 = agWarnMethodService.refreshWarnMethod();

		if( !flag1 || !flag2 || !flag3){
			log.error("初始化配置失败，系统退出。。。。。。。");
			System.exit(0);
		}
		log.info("=======================初始化数据源和集成配置信息完毕=======================");
	}
}
