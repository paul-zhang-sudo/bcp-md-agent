package com.bsi.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bsi.framework.core.utils.DateUtils;
import com.bsi.framework.core.utils.FwSpringContextUtil;
import com.bsi.framework.core.utils.StringUtils;
import com.google.common.collect.Maps;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

/**
 * mongodb操作类
 * 用来操作系统自带的mongodb
 */
public class MongoDBUtils {
    private static final MongoTemplate mongoTemplate = FwSpringContextUtil.getBean("mongoTemplate",MongoTemplate.class);
    /**
     * 批量插入 JSON 数据到指定集合。
     *
     * @param jsonString JSON 字符串
     * @param collectionName 集合名称
     * @throws IOException 如果 JSON 解析失败
     */
    public static void batchInsert(String jsonString, String collectionName) {
        List<Document> documents = JSONArray.parseArray(jsonString,Document.class);
        Date currentDate = DateUtils.now("yyyy-MM-dd HH:mm:ss.SSS");

        for (Document doc : documents) {
            doc.put("_createTime", currentDate);
            doc.put("_updateTime", currentDate);
        }
        mongoTemplate.insert(documents, collectionName);
    }

    /**
     * 批量更新 JSON 数据到指定集合。
     *
     * @param jsonString JSON 字符串
     * @param collectionName 集合名称
     * @throws IOException 如果 JSON 解析失败
     */
    public static void batchUpdate(String jsonString, String collectionName) {
        List<Document> documents = JSONArray.parseArray(jsonString,Document.class);
        Date currentDate = DateUtils.now("yyyy-MM-dd HH:mm:ss.SSS");
        List<String> ids = new ArrayList<>();
        for (Document doc : documents) {
            Object id = doc.get("_id");
            if (id != null) {
                ids.add(id.toString());
            }
        }

        if (!ids.isEmpty()) {
            Criteria criteria = new Criteria("_id").in(ids);
            Query query = new Query(criteria);

            // 构建更新操作
            Update update = new Update();
            for (Document doc : documents) {
                for (String key : doc.keySet()) {
                    if (!key.equals("_id")) {  // 排除 _id
                        update.set(key, doc.get(key));
                    }
                }
            }
            update.set("_updateTime",currentDate);
            mongoTemplate.updateMulti(query, update, collectionName);
        }
    }

    // 统计指定时间段内的 validSize 和 successSize
    public static Map<String, Object> getStats(JSONObject jsonFilter) {
        String startTime = jsonFilter.getString("startTime");
        String endTime = jsonFilter.getString("endTime");
        MatchOperation match = Aggregation.match(
                Criteria.where("timeLocal").gte(startTime).lt(endTime)
        );

        GroupOperation group = Aggregation.group()
                .sum("validSize").as("totalValidSize")
                .sum("successSize").as("totalSuccessSize");

        Aggregation aggregation = Aggregation.newAggregation(match, group);

        return Collections.unmodifiableMap(mongoTemplate.aggregate(aggregation, "task_run_log", Map.class).getMappedResults().stream()
                .findFirst()
                .orElseGet(() -> Maps.newHashMap()));
    }

    /**
     * 根据 JSON 字符串表示的过滤条件查询集合中的文档。
     *
     * @param jsonFilter JSON 字符串表示的过滤条件
     * @return 匹配的文档列表
     */
    public static List<Document> queryDocuments(JSONObject jsonFilter){
        // 解析字段和过滤条件
        String fields = jsonFilter.getString("fields");
        JSONArray filters = jsonFilter.getJSONArray("filters");

        // 构建查询条件
        Criteria criteria = buildCriteriaFromFilters(filters);

        // 创建查询对象
        Query query = new Query();
        query.addCriteria(criteria);

        // 处理 limit 参数
        Integer limit = jsonFilter.getInteger("limit");
        if (limit != null && limit > 0) {
            query.limit(limit);
        }

        // 处理排序参数
        String sortField = jsonFilter.getString("sortField");
        String sortOrder = jsonFilter.getString("sortOrder");
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (StringUtils.hasText(sortOrder) && "DESC".equalsIgnoreCase(sortOrder)) {
                direction = Sort.Direction.DESC;
            }
            query.with(Sort.by(direction, sortField));
        }

        // 处理返回字段
        if (fields != null && !fields.isEmpty()) {
            String[] fieldArray = fields.split(",");
            for (String field : fieldArray) {
                query.fields().include(field.trim());
            }
        }

        // 执行查询
        List<Document> list = mongoTemplate.find(query, Document.class, jsonFilter.getString("collectionName"));

        // 转换 ObjectId 为字符串
        return list.stream().peek(doc -> {
            Object id = doc.get("_id");
            if (id instanceof ObjectId) {
                doc.put("_id", id.toString());
            }
        }).collect(Collectors.toList());
    }

    public static List<Document> queryAndAggregate(JSONObject jsonFilter) {

        String collectionName = jsonFilter.getString("collectionName");
        String[] groupFields = jsonFilter.getString("groupFields").split(",");
        String[] returnFields = jsonFilter.getString("returnFields").split(",");

        // 解析过滤条件
        JSONArray filters = jsonFilter.getJSONArray("filters");
        Criteria criteria = buildCriteriaFromFilters(filters);

        // 创建基础分组操作
        GroupOperation groupOperation = group(groupFields);

        // 构建聚合字段
        JSONArray aggregations = jsonFilter.getJSONArray("aggregations");
        groupOperation = aggregations(aggregations, groupOperation);

        // 创建聚合管道
        Aggregation aggregation = newAggregation(
                match(criteria),
                groupOperation,
                project(returnFields).andExclude("_id")
        );

        // 执行聚合查询
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collectionName,Document.class);
        return results.getMappedResults();
    }

    private static GroupOperation aggregations(JSONArray aggregations, GroupOperation groupOperation) {
        for (int i = 0; i < aggregations.size(); i++) {
            JSONObject jsonNode = aggregations.getJSONObject(i);
            String field = jsonNode.getString("field");
            String function = jsonNode.getString("function").toLowerCase();

            switch (function) {
                case "sum":
                    groupOperation = groupOperation.sum(field).as(field + "_sum");
                    break;
                case "count":
                    groupOperation = groupOperation.count().as(field + "_count");
                    break;
                case "avg":
                    groupOperation = groupOperation.avg(field).as(field + "_avg");
                    break;
                case "min":
                    groupOperation = groupOperation.min(field).as(field + "_min");
                    break;
                case "max":
                    groupOperation = groupOperation.max(field).as(field + "_max");
                    break;
            }
        }
        return groupOperation;
    }


    /**
     * 从过滤条件列表构建 Criteria 对象。
     *
     * @param filters 过滤条件列表
     * @return Criteria 对象
     */
    private static Criteria buildCriteriaFromFilters(JSONArray filters) {
        Criteria criteria = new Criteria();

        filters.forEach(filter -> {
            JSONObject filterObj = (JSONObject) filter;
            String field = filterObj.getString("field");
            String op = filterObj.getString("op");
            Object value = filterObj.get("value");

            switch (op) {
                case "eq":
                    criteria.and(field).is(value);
                    break;
                case "ne":
                    criteria.and(field).ne(value);
                    break;
                case "gt":
                    criteria.and(field).gt(value);
                    break;
                case "gte":
                    criteria.and(field).gte(value);
                    break;
                case "lt":
                    criteria.and(field).lt(value);
                    break;
                case "lte":
                    criteria.and(field).lte(value);
                    break;
                case "in":
                    criteria.and(field).in(value);
                    break;
                case "nin":
                    criteria.and(field).nin(value);
                    break;
                case "exists":
                    criteria.and(field).exists((Boolean) value);
                    break;
                case "regex":
                    criteria.and(field).regex((String) value);
                    break;
            }
        });
        return criteria;
    }
}
