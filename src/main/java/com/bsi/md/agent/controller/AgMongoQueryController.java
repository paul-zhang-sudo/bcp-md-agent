package com.bsi.md.agent.controller;

import com.alibaba.fastjson.JSONObject;
import com.bsi.utils.MongoDBUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * mongodb服务
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/mongo")
public class AgMongoQueryController {
    @Autowired
    private MongoTemplate mongoTemplate;
    @PostMapping("/query")
    public List<Document> query(@RequestBody JSONObject params) {
        return MongoDBUtils.queryDocuments(params);
    }

    @PostMapping("/aggregate")
    public List<Map> aggregate(@RequestBody JSONObject params) {
        return MongoDBUtils.queryAndAggregate(params);
    }

}
