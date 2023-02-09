package com.bsi.md.agent.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.service.FwService;
import com.bsi.framework.core.utils.CollectionUtils;
import com.bsi.framework.core.utils.EHCacheUtil;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.bsi.framework.core.utils.StringUtils;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.datasource.AgApiTemplate;
import com.bsi.md.agent.datasource.AgApiUpTemplate;
import com.bsi.md.agent.datasource.AgDatasourceContainer;
import com.bsi.md.agent.datasource.AgJdbcTemplate;
import com.bsi.md.agent.datasource.AgKafkaTemplate;
import com.bsi.md.agent.datasource.AgSapRFCTemplate;
import com.bsi.md.agent.entity.AgDataSource;
import com.bsi.md.agent.entity.dto.AgDataSourceDto;
import com.bsi.md.agent.ot.AgDcDriver;
import com.bsi.md.agent.repository.AgDataSourceRepository;
import com.bsi.md.agent.utils.AgJasyptUtils;
import com.bsi.utils.DecryptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Transactional
@Service
@Slf4j
public class AgDataSourceService extends FwService {
    @Autowired
    private AgDataSourceRepository agDataSourceRepository;

    @Autowired
    private AgDcDriver agDcDriver;

    /**
     * 查询所有数据源配置
     * @return List<AgDataSource>
     */
    public List<AgDataSource> findAll(){
        return agDataSourceRepository.findAll();
    }

    public boolean refreshDataSource(){
        boolean flag = true;
        int size = 0;
        try {
            List<AgDataSource> list = findAll();
            //清空数据源
            AgDatasourceContainer.clearAllDataSource();
            //清空密钥
            EHCacheUtil.removeAllEhcache(EHCacheUtil.APIAUTH_CACHE);
            if( CollectionUtils.isNotEmpty(list) ){
                size = list.size();
                for(AgDataSource ds:list){
                    JSONObject config = JSONObject.parseObject( ds.getConfigValue() );
                    //解密在云端加密的数据//会多一些属性，待修改
                    JSONObject obj = new JSONObject();
                    config.forEach((k,v)->{
                        if(k.startsWith("secret_")){
                            obj.put(k.replace("secret_",""), DecryptUtils.decryptFromHWCloud(v.toString()));
                            //config.remove(k);
                        }else {
                            obj.put(k,v);
                        }
                    });
                    config = obj;
                    //处理数据源属性
                    JSONArray global = config.getJSONArray("globalParams");
                    if(global!=null && global.size()>0){
                        JSONObject prop = new JSONObject();
                        for(int i=0;i<global.size();i++){
                            JSONObject o = global.getJSONObject(i);
                            String value = o.getString("value");
                            if(o.getBooleanValue("secret")){
                                //如果是ot下发的配置则使用华为加密算法解密
                                if(StringUtils.hasText( o.getString("source") ) && "ot".equals(o.getString("source"))){
                                    value = agDcDriver.getDcClient().decryptDataFromCloud(value);
                                }else {
                                    value = AgJasyptUtils.decode(AgJasyptUtils.PWD,value);
                                }
                            }
                            prop.put( o.getString("key"),value );
                        }
                        AgDatasourceContainer.setDSProperties(ds.getId(),prop);
                    }

                    //api类型数据源处理
                    if( AgConstant.AG_NODETYPE_API.equals( ds.getType() ) ){
                        AgApiTemplate apiTemplate = new AgApiTemplate();
                        apiTemplate.setApiUrl( config.getString("url") );
                        AgDatasourceContainer.addApiDataSource(ds.getId(),apiTemplate);
                    //数据库类型数据源处理
                    }else if( AgConstant.AG_NODETYPE_DATABASE.equals( ds.getType() ) ){
                        AgJdbcTemplate template = new AgJdbcTemplate();

                        template.setDataSource(config.getString("driverClassName"),config.getString("url"),config.getString("username"),config.getString("password"));
                        AgDatasourceContainer.addJdbcDataSource(ds.getId(),template);
                    //api上报类型数据源处理
                    }else if( AgConstant.AG_NODETYPE_APIUP.equals( ds.getType() ) ){
                        AgApiUpTemplate apiUpTemplate = new AgApiUpTemplate();
                        apiUpTemplate.setAk(config.getString("ak"));
                        apiUpTemplate.setSk(config.getString("sk"));
                        //初始化数据源
                        AgDatasourceContainer.addApiUpDataSource( ds.getId(),apiUpTemplate );
                        //初始化密钥
                        EHCacheUtil.setApiSecret(apiUpTemplate.getAk(),apiUpTemplate.getSk());
                    //sapRfc类型
                    }else if( AgConstant.AG_NODETYPE_SAPRFC.equals( ds.getType() ) ){
                        Map<String,String> map = Collections.emptyMap();
                        String otherParam = config.getString("otherParam");
                        if(StringUtils.hasText(otherParam)){
                            map = JSONObject.parseObject(otherParam,Map.class);
                        }
                        AgSapRFCTemplate sapRFCTemplate = new AgSapRFCTemplate(ds.getId(),config.getString("serverIp"),
                                config.getString("serverNo"),config.getString("clientNo"),config.getString("userName"),
                                config.getString("password"),map);
                        AgDatasourceContainer.addSapRfcDataSource( ds.getId(), sapRFCTemplate);

                    //MQ类型
                    }else if( AgConstant.AG_NODETYPE_MQ.equals( ds.getType() ) ){
                        Map<String,String> map = Collections.emptyMap();
                        String otherParam = config.getString("otherParam");
                        if(StringUtils.hasText(otherParam)){
                            map = JSONObject.parseObject(otherParam,Map.class);
                        }
                        //String servers, String groupId, String autoCommit, String autoCommitInterval,String autoOffset ,String keyDecode, String classDecode
                        AgKafkaTemplate kafkaTemplate = new AgKafkaTemplate(config.getString("servers"),config.getString("groupId"),(String) config.getOrDefault("autoCommit","true"),
                                "2000","latest","org.apache.kafka.common.serialization.StringDeserializer","org.apache.kafka.common.serialization.StringDeserializer",map);
                        AgDatasourceContainer.addKafkaDataSource(ds.getId(),kafkaTemplate);
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
        if(ds.getDelFlag()!=null&&ds.getDelFlag()){
            agDataSourceRepository.deleteById(ds.getId());
        }else {
            AgDataSource dataSource = new AgDataSource();
            dataSource.setId( ds.getId() );
            dataSource.setClassify( ds.getClassify() );
            dataSource.setType( ds.getType() );
            dataSource.setName( ds.getName() );
            dataSource.setConfigValue( ds.getConfigValue() );
            dataSource.setEnable( true );
            agDataSourceRepository.save(dataSource);
        }
        //刷新缓存
        refreshDataSource();
    }
}
