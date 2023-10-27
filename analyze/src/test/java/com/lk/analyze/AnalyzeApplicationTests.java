package com.lk.analyze;

import cn.hutool.core.date.DateUtil;
import com.lk.analyze.model.document.Chart;
import com.mongodb.client.MongoCollection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class AnalyzeApplicationTests {
    @Resource
    MongoTemplate mongoTemplate;
    @Test
    void contextLoads() {
//        Chart chart = new Chart();
//        chart.setName("测试111");
//        chart.setChatType("1");
//        chart.setCreateTime(DateUtil.date());
//        mongoTemplate.insert(chart);
//        Query query = new Query(Criteria.where("name").is("测试"));
//        List<Chart> charts = mongoTemplate.find(query, Chart.class);
//        for(Chart c:charts) System.out.println(c.toString());
    }

}
