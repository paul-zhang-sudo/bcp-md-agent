package com.bsi.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.bsi.framework.core.utils.ExceptionUtils;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.naming.NameCoder;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import com.thoughtworks.xstream.io.xml.Dom4JXmlWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.OutputFormat;

import javax.xml.stream.XMLOutputFactory;
import java.util.*;
import java.util.Map.Entry;

/**
 * xml工具类
 * @author fish
 */
@Slf4j
public class XmlUtils {

    private static XStream xmlStream = new XStream(new Dom4JDriver(new XmlFriendlyNameCoder("_","_","_")));

    static {
        xmlStream.registerConverter( new MapEntryConverter());
    }

    /**
     * 对象转xml文本
     * @param obj
     * @return
     */
    public static String toXml(Object obj){
        return xmlStream.toXML( obj );
    }

    /**
     * json文件转换成xml文本
     * @param json
     * @return
     */
    public static String jsonArray2Xml(String json){
        Object obj = JSON.parseArray(json);
        return xmlStream.toXML( obj );
    }


    /**
     * json文件转换成xml文本
     * @param json
     * @return
     */
    public static String json2Xml(String json){
        Object obj = JSON.parse(json,Feature.OrderedField);
        return xmlStream.toXML( obj );
    }

    /**
     * json文件转换成xml文本
     * @param json
     * @param interceptNode
     * @return
     */
    public static String json2Xml(String json,String interceptNode){
        String result = "";
        try{
            String xml = json2Xml(json);
            Document doc = DocumentHelper.parseText(xml);
            result = doc.getRootElement().element("map").element(interceptNode).asXML();
            result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+result;
        }catch (DocumentException e){
            log.info("xml解析失败:{}", ExceptionUtils.getFullStackTrace(e));
        }
        return result;
    }

    /**
     * xml文本转换成json文本
     * @param xml
     * @return
     */
    public static String xml2json(String xml){
        return JSON.toJSONString(xmlStream.fromXML(xml) );
    }

    /**
     * xml文本转换成json文本
     * @param xml
     * @return
     */
    public static String xml2json(String xml,String rootNode){
        xmlStream.alias(rootNode,Map.class);
        return xml2json(xml);
    }

    /**
     * converter类
     */
    static class MapEntryConverter implements Converter{
        public MapEntryConverter(){
            super();
        }

        @Override
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            map2xml(value,writer,context);
        }
        /**
         * map转换成xml
         * @param value
         * @param writer
         * @param context
         */
        private void map2xml(Object value, HierarchicalStreamWriter writer,
                               MarshallingContext context) {
            Class cls = value.getClass();
            if(List.class.isAssignableFrom(cls)){
                List<Object> list = (List<Object>) value;
                for (Object v : list) {
                    write(writer,"item",v,context);
                }
            }else{
                Map<String,Object> map = (Map<String, Object>) value;
                Iterator it = map.entrySet().iterator();
                while( it.hasNext() ){
                    Map.Entry<String, Object> entry = (Map.Entry<String, Object>) it.next();
                    write(writer,entry.getKey(),entry.getValue(),context);
                }
            }
        }

        /**
         * 节点输出
         * @param writer
         * @param k
         * @param v
         * @param context
         */
        private void write(HierarchicalStreamWriter writer,Object k,Object v,MarshallingContext context){
            if(k.toString().endsWith("-attr")){
                Map<String,Object> attr = (Map<String,Object>)v;
                attr.forEach((key,value)->writer.underlyingWriter().addAttribute(key,value.toString()));
            }else{
                writer.startNode(k.toString());
                if (v instanceof String) {
                    writer.setValue((String) v);
                } else {
                    map2xml(v, writer, context);
                }
                writer.endNode();
            }
        }

        protected Object populateMap(HierarchicalStreamReader reader,
                                     UnmarshallingContext context) {
            boolean mapFlag = true;
            Map<String, Object> map = new LinkedHashMap<>();
            List<Object> list = new ArrayList<>();
            //如果有属性，则作为子元素 key_attr 输出
            if( reader.getAttributeCount()>0 ){
                Map<String, Object> attrMap = new LinkedHashMap<>();
                for(int i=0;i<reader.getAttributeCount();i++){
                    attrMap.put(reader.getAttributeName(i),reader.getAttribute(i));
                }
                map.put(reader.getNodeName()+"-attr",attrMap);
            }
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String key = reader.getNodeName();
                Object value = null;
                if (reader.hasMoreChildren()) {
                    value = populateMap(reader, context);
                } else {
                    value = reader.getValue();
                }
                if (mapFlag) {
                    if (map.containsKey(key)) {
                        mapFlag = false;
                        Iterator<Entry<String, Object>> iter = map.entrySet()
                                .iterator();
                        while (iter.hasNext())
                            list.add(iter.next().getValue());
                        list.add(value);
                    } else {
                        map.put(key, value);
                    }
                } else {
                    list.add(value);
                }
                reader.moveUp();
            }
            if (mapFlag)
                return map;
            else
                return list;
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return populateMap(reader,context);
        }

        @Override
        public boolean canConvert(Class aClass) {
            return AbstractMap.class.isAssignableFrom(aClass)||AbstractList.class.isAssignableFrom(aClass);
        }
    }
    public static  void main(String[] ars) throws Exception{

        String xml = "<ufinterface billtype=\"supplier\" filename=\"supplier17ca11026ee0000zhenyun.xml\" isexchange=\"Y\" replace=\"Y\" roottag=\"sendresult\" sender=\"zhenyun\" successful=\"N\">\n" +
                "    <sendresult>\n" +
                "        <billpk>\n" +
                "        </billpk>\n" +
                "        <bdocid>17ca11035e5000000000000000000000supplier102480</bdocid>\n" +
                "        <filename>supplier17ca11026ee0000zhenyun.xml</filename>\n" +
                "        <resultcode>32001</resultcode>\n" +
                "        <resultdescription>单据  17ca11035e5000000000000000000000supplier102480  开始处理...\n" +
                "单据  17ca11035e5000000000000000000000supplier102480  处理错误:业务插件处理错误:插件类=nc.bs.bd.pfxx.plugin.SupplierPfxxPlugin,异常信息:下列字段值已存在，不允许重复，请检查：\n" +
                "[客商编码: CO00010673]</resultdescription>\n" +
                "        <content></content>\n" +
                "    </sendresult>\n" +
                "</ufinterface>";
        xmlStream.alias("ufinterface",Map.class);
        log.info( XmlUtils.xml2json(xml) );

//        String json="{\"ufinterface\":{\"ufinterface-attr\":{\"account\":\"004\",\"billtype\":\"supplier\",\"filename\":\"\",\"groupcode\":\"00\",\"isexchange\":\"Y\",\"replace\":\"Y\",\"roottag\":\"\",\"sender\":\"zhenyun\"},\"bill\":{\"bill-attr\":{\"id\":\"1001ZZ1000000003RRFK\"},\"billhead\":{\"pk$group\":\"00\",\"pk$org\":\"00\",\"code\":\"0911\",\"name\":\"测试1021\",\"shortname\":\"1021\",\"ename\":\"a\",\"mnecode\":\"a\",\"trade\":\"\",\"pk$supplier$main\":\"\",\"supprop\":\"0\",\"pk$areacl\":\"\",\"pk$supplierclass\":\"503\",\"iscustomer\":\"N\",\"corcustomer\":\"\",\"isfreecust\":\"N\",\"isoutcheck\":\"N\",\"pk$financeorg\":\"\",\"taxpayerid\":\"\",\"registerfund\":\"0.00000000\",\"legalbody\":\"\",\"ecotypesincevfive\":\"\",\"pk$suptaxes\":\"\",\"zipcode\":\"\\n            \",\"url\":\"a\",\"memo\":\"\\n            \",\"suplinkman\":[{\"linkmanvo\":{\"code\":\"a\",\"name\":\"a\",\"sex\":\"0\",\"vjob\":\"a\",\"birthday\":\"2012-05-25 10:26:11\",\"phone\":\"a\",\"cell\":\"a\",\"fax\":\"a\",\"email\":\"a\",\"webaddress\":\"a\",\"address\":\"a\",\"postcode\":\"a\",\"memo\":\"a\"},\"pk$linkman\":\"\",\"isdefault\":\"N\"},{\"linkmanvo\":{\"code\":\"a\",\"name\":\"a\",\"sex\":\"0\",\"vjob\":\"a\",\"birthday\":\"2012-05-25 10:26:11\",\"phone\":\"a\",\"cell\":\"a\",\"fax\":\"a\",\"email\":\"a\",\"webaddress\":\"a\",\"address\":\"a\",\"postcode\":\"a\",\"memo\":\"a\"},\"pk$linkman\":\"\",\"isdefault\":\"N\"}],\"tel1\":\"\",\"tel2\":\"\",\"tel3\":\"\",\"fax1\":\"\",\"fax2\":\"\",\"email\":\"\",\"pk$country\":\"CN\",\"pk$timezone\":\"P0800\",\"pk$format\":\"ZH-CN\",\"enablestate\":\"2\",\"corpaddress\":{\"code\":\"a\",\"country\":\"CN\",\"detailinfo\":\"a\",\"postcode\":\"a\",\"province\":\"\",\"status\":\"0\",\"vsection\":\"\"}}}}}\n";
//        System.out.println( json2Xml(json,"ufinterface") );

//        List<AgDataSource> list = new ArrayList<>();
//        AgDataSource ds = new AgDataSource();
//        ds.setConfigValue("config");
//        ds.setName("fish");
//
//        AgDataSource ds1 = new AgDataSource();
//        ds1.setConfigValue("config");
//        ds1.setName("fish");
//        list.add(ds);
//        list.add(ds1);
//        String json = JSON.toJSONString(list);
//        System.out.println( toXml(ds) );
//        System.out.println( json );
//        System.out.println( json2Xml(json) );

//        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
//                "\n" +
//                "<com.alibaba.fastjson.JSONArray>\n" +
//                "  <list>\n" +
//
//                "    <tt>\n" +
//                "      <name>fish</name>\n" +
//                "      <configValue>config</configValue>\n" +
//                "    </tt>\n" +
//                "  </list>\n" +
//                "</com.alibaba.fastjson.JSONArray>";
//        log.info( xml2json( xml ) );
    }
}
