package com.bsi.utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.Dom4JDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class XmlUtil_V2 {

    private static final XStream xmlStream = new XStream(new Dom4JDriver(new XmlFriendlyNameCoder("_", "_")));

    static {
        xmlStream.registerConverter(new MapEntryConverter());
    }

    /**
     * 对象转xml文本
     *
     * @param obj
     * @return
     */
    public static String toXml(Object obj) {
        return xmlStream.toXML(obj);
    }

    /**
     * json文件转换成xml文本
     *
     * @param json
     * @return
     */
    public static String jsonArray2Xml(String json) {
        Object obj = JSON.parseArray(json);
        return xmlStream.toXML(obj);
    }


    /**
     * json文件转换成xml文本
     *
     * @param json
     * @return
     */
    public static String json2Xml(String json) {
        Object obj = JSON.parse(json, Feature.OrderedField);
        return xmlStream.toXML(obj);
    }

    /**
     * json文件转换成xml文本
     *
     * @param json
     * @param interceptNode
     * @return
     */
    public static String json2Xml(String json, String interceptNode) {
        String result = "";
        try {
            String xml = json2Xml(json);
            Document doc = DocumentHelper.parseText(xml);
            result = doc.getRootElement().element("map").element(interceptNode).asXML();
            result = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + result;
        } catch (DocumentException e) {
            log.info("xml解析失败:{}", e.fillInStackTrace());
        }
        return result;
    }

    /**
     * xml文本转换成json文本
     *
     * @param xml
     * @return
     */
    public static String xml2json(String xml) {
        return JSON.toJSONString(xmlStream.fromXML(xml));
    }

    /**
     * xml文本转换成json文本
     *
     * @param xml
     * @return
     */
    public static String xml2json(String xml, String rootNode) {
        xmlStream.alias(rootNode, Map.class);
        return xml2json(xml);
    }

    /**
     * 根据xml路径获取xml节点下的文本
     *
     * @param xml
     * @param path
     * @return
     */
    public static String getTextByPath(String xml, String path) {
        Node n = null;
        try {
            Document document = DocumentHelper.parseText(xml);
            n = document.selectSingleNode(path);
        } catch (Exception e) {
            log.error("获取xml的节点文本报错:{}", e.fillInStackTrace());
        }
        return n == null ? "" : n.getText();
    }

    /**
     * converter类
     */
    static class MapEntryConverter implements Converter {
        public MapEntryConverter() {
            super();
        }

        @Override
        public void marshal(Object value, HierarchicalStreamWriter writer, MarshallingContext context) {
            map2xml(value, writer, context, null);
        }

        /**
         * map转换成xml
         *
         * @param value
         * @param writer
         * @param context
         */
        private void map2xml(Object value, HierarchicalStreamWriter writer,
                             MarshallingContext context, String prefix) {
            Class cls = value.getClass();
            if (List.class.isAssignableFrom(cls)) {
                List<Object> list = (List<Object>) value;
                for (Object v : list) {
                    write(writer, prefix, v, context);
                }
            } else {
                Map<String, Object> map = (Map<String, Object>) value;
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    write(writer, entry.getKey(), entry.getValue(), context);
                }
            }
        }

        /**
         * 节点输出
         *
         * @param writer
         * @param k
         * @param v
         * @param context
         */
        private void write(HierarchicalStreamWriter writer, Object k, Object v, MarshallingContext context) {
            if (k.toString().endsWith("-attr")) {
                Map<String, Object> attr = (Map<String, Object>) v;
                attr.forEach((key, value) -> writer.underlyingWriter().addAttribute(key, value.toString()));
            } else {
                if (!List.class.isAssignableFrom(v.getClass())) {
                    writer.startNode(k.toString());
                    if (v instanceof String) {
                        writer.setValue((String) v);
                    } else {
                        map2xml(v, writer, context, (String) k);
                    }
                    writer.endNode();
                } else {
                    map2xml(v, writer, context, (String) k);
                }
            }
        }

        protected Object populateMap(HierarchicalStreamReader reader,
                                     UnmarshallingContext context) {
            boolean mapFlag = true;
            Map<String, Object> map = new LinkedHashMap<>();
            //如果有属性，则作为子元素 key_attr 输出
            if (reader.getAttributeCount() > 0) {
                Map<String, Object> attrMap = new LinkedHashMap<>();
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    attrMap.put(reader.getAttributeName(i), reader.getAttribute(i));
                }
                map.put(reader.getNodeName() + "-attr", attrMap);
            }
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                String key = reader.getNodeName();
                Object value = null;
                if (reader.hasMoreChildren() || reader.getAttributeCount() > 0) {
                    value = populateMap(reader, context);
                } else {
                    value = reader.getValue();
                }
                if (map.containsKey(key)) {
                    Object obj = map.get(key);
                    if (obj instanceof ArrayList) {
                        ArrayList array = (ArrayList) obj;
                        array.add(value);
                    } else {
                        ArrayList array = new ArrayList();
                        array.add(obj);
                        array.add(value);
                        map.put(key, array);
                    }
                } else {
                    map.put(key, value);
                }
                reader.moveUp();
            }
            return map;
        }

        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            return populateMap(reader, context);
        }


        @Override
        public boolean canConvert(Class aClass) {
            return AbstractMap.class.isAssignableFrom(aClass) || AbstractList.class.isAssignableFrom(aClass);
        }
    }

}
