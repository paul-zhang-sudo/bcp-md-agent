package com.bsi.md.agent.engine.plugins;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.*;
import com.bsi.md.agent.constant.AgConstant;
import com.bsi.md.agent.email.AgEmailEntity;
import com.bsi.md.agent.email.AgEmailService;
import com.bsi.md.agent.engine.integration.Context;
import com.bsi.md.agent.entity.dto.AgHttpResult;
import com.bsi.utils.HttpUtils;
import com.bsi.utils.SHA256Utils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 任务异常告警插件
 */
@Slf4j
@Component
public class AgTaskErrorDataWarnPlugin implements AgAfterOutputPlugin{

    private static final Logger info_log = LoggerFactory.getLogger("TASK_INFO_LOG");
    private static final Pattern dynamicLimitCount = Pattern.compile("\\$\\{([A-Za-z0-9]+)\\}");

    @Override
    public void handlerMsg(Context context) {
        String warnMethodId = context.getWarnMethodId();
        String taskId = context.getTaskId();
        String errorData = context.getTaskErrorData();
        //告警方式为空或者异常数据为空，则不需要继续执行
        if(StringUtils.isNullOrBlank(warnMethodId) || StringUtils.isNullOrBlank(errorData)){
            return;
        }
        Object cnf = EHCacheUtil.getValue(AgConstant.AG_EHCACHE_WARN,warnMethodId);
        String warnConf = cnf==null?"":cnf.toString();
        //告警配置不存在
        if(StringUtils.isNullOrBlank(warnConf)){
            return;
        }

        JSONObject warn = JSON.parseObject(warnConf);
        String type = warn.getString("type");
        JSONObject warnObj = JSON.parseObject(warn.getString("configValue"));
        //飞书
        if( "feishu".equals(type) ){
            String webhookUrl = warnObj.getString("webhookUrl");
            String secretkey = warnObj.getString("secretkey");
            String template = warnObj.getString("template");

            try {
                JSONObject msg = new JSONObject();
                long timestamp = Instant.now().getEpochSecond(); //当前秒
                msg.put("timestamp",timestamp+"");
                msg.put("sign", SHA256Utils.genSign(secretkey,timestamp));
                msg.put("msg_type","text");
                JSONObject textMsg = new JSONObject();
                String text = "<at user_id='all'>所有人</at>"+setContentParams(template,errorData);
                //飞书自定义机器人只支持20kb的数据，如果超过20kb则进行截断
                if( text.length()>20*1000 ){
                    text = text.substring(0,20*1000)+"[内容过长已截断...]";
                }
                textMsg.put("text",text);
                msg.put("content",textMsg);
                AgHttpResult res = HttpUtils.post(webhookUrl,null,msg.toJSONString());
                log.info("调用飞书消息推送接口code:{},msg:{}",res.getCode(),res.getResult());
            }catch (Exception e){
                log.error("send msg error:{}", ExceptionUtils.getFullStackTrace(e));
            }
        //邮件
        }else if( "email".equals(type) ){
            AgEmailService agEmailService = FwSpringContextUtil.getBean("agEmailService",AgEmailService.class);

            JavaMailSenderImpl javaMailSenderImpl = FwSpringContextUtil.getBean(JavaMailSenderImpl.class);
            javaMailSenderImpl.setHost(warnObj.getString("host"));
            javaMailSenderImpl.setUsername(warnObj.getString("userName"));
            javaMailSenderImpl.setPassword(warnObj.getString("password"));
            Properties properties = new Properties();
            properties.put("mail.smtp.auth", "true");
            properties.put("mail.smtp.timeout", "60000");
            properties.put("bcp.mail.from", warnObj.getString("userName"));
            javaMailSenderImpl.setJavaMailProperties(properties);

            AgEmailEntity email = new AgEmailEntity();
            email.setSubject(warnObj.getString("title"));
            email.setReceiver(warnObj.getString("receiver"));
            String content = warnObj.getString("content");
            email.setContent( setContentParams(content,errorData) );

            //发送邮件
            agEmailService.sendEmail(email);
        }
    }

    /**
     * 修改内容中的参数
     * @param content
     */
    private String setContentParams(String content,String errorData){
        List<String> list = getKeyListByContent(content);
        JSONObject obj = JSON.parseObject(errorData);
        if(CollectionUtils.isNotEmpty(list)){
            for (String k : list) {
                content = content.replaceAll("\\$\\{"+k+"\\}",obj.getString(k));
            }

        }
        return content;
    }

    /**
     * 按照动态内容的参数出现顺序,将参数放到List中
     *
     * @param content
     * @return
     */
    private List<String> getKeyListByContent(String content) {
        Set<String> paramSet = new LinkedHashSet<>();
        Matcher m = dynamicLimitCount.matcher(content);
        while (m.find()) {
            paramSet.add(m.group(1));
        }
        return new ArrayList<>(paramSet);
    }
}
