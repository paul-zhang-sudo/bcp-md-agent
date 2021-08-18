package com.bsi.md.agent.service;

import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.service.FwService;
import com.bsi.framework.core.utils.CollectionUtils;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.datasource.AgApiTemplate;
import com.bsi.md.agent.entity.AgDataSource;
import com.bsi.md.agent.datasource.AgJdbcTemplate;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.entity.dto.AgDataSourceDto;
import com.bsi.md.agent.repository.AgDataSourceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Transactional
@Service
@Slf4j
public class AgDataSourceService extends FwService {
    @Autowired
    private AgDataSourceRepository agDataSourceRepository;

    /**
     * 查询所有数据源配置
     * @return List<AgDataSource>
     */
    public List<AgDataSource> findAll(){
        return agDataSourceRepository.findAll();
    }

    public boolean refreshDataSource(){
        Boolean flag = true;
        int size = 0;
        try {
            List<AgDataSource> list = findAll();
            AgDatasourceContainer.clearApiataSource();
            AgDatasourceContainer.clearJdbcDataSource();
            if( CollectionUtils.isNotEmpty(list) ){
                size = list.size();
                for(AgDataSource ds:list){
                    JSONObject config = JSONObject.parseObject( ds.getConfigValue() );
                    //api类型数据源处理
                    if( AgConstant.AG_NODETYPE_API.equals( ds.getType() ) ){
                        AgApiTemplate apiTemplate = new AgApiTemplate();
                        apiTemplate.setApiUrl( config.getString("apiUrl") );
                        AgDatasourceContainer.addApiDataSource(ds.getId(),apiTemplate);
                    //数据库类型数据源处理
                    }else if( AgConstant.AG_NODETYPE_DATABASE.equals( ds.getType() ) ){
                        AgJdbcTemplate template = new AgJdbcTemplate();
                        template.setDataSource(config.getString("driverClassName"),config.getString("url"),config.getString("username"),config.getString("password"));
                        AgDatasourceContainer.addJdbcDataSource(ds.getId(),template);
                    }
                }
            }
        }catch (Exception e){
            log.error("刷新数据源报错:{}", ExceptionUtils.getFullStackTrace(e));
            flag = false;
        }
        log.info("一共初始化{}条数据源信息",size);
        return flag;
    }

    /**
     * 更新数据源
     * @param ds
     */
    public void updateDS(AgDataSourceDto ds){
        AgDataSource dataSource = new AgDataSource();
        dataSource.setId( ds.getId() );
        dataSource.setClassify( ds.getClassify() );
        dataSource.setType( ds.getType() );
        dataSource.setName( ds.getName() );
        dataSource.setConfigValue( ds.getConfigValue() );
        dataSource.setEnable( true );
        agDataSourceRepository.save(dataSource);
        //刷新缓存
        refreshDataSource();
    }
}
