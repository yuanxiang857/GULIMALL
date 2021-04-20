package com.yuanxiang.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.yuanxiang.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.ml.job.results.Bucket;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

	//索引数据  插入更新二合一
	@Autowired
	private RestHighLevelClient client;
	@Test
	public void contextLoads() throws IOException {
		IndexRequest indexRequest = new IndexRequest("users");
		indexRequest.id("1");
		Users users = new Users("yuanxiang","Male",23);
		String jsonString = JSON.toJSONString(users);
		indexRequest.source(jsonString, XContentType.JSON);
		//执行操作
		IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
		System.out.println(index);
	}
	@Test
	public void searchDate() throws IOException{
		//1、创建检索请求
		SearchRequest searchRequest = new SearchRequest();
		//指定索引
		searchRequest.indices("bank");

		//指定DSL、检索条件
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(QueryBuilders.matchQuery("address","mill"));
		//按照年龄进行聚合
		TermsAggregationBuilder aggAgg = AggregationBuilders.terms("aggAgg").field("age").size(10);
		searchSourceBuilder.aggregation(aggAgg);
		//按照平均薪资
		AvgAggregationBuilder balanceAgg=AggregationBuilders.avg("balanceAvg").field("balance");
		searchSourceBuilder.aggregation(balanceAgg);

		System.out.println(searchSourceBuilder.toString());
		searchRequest.source(searchSourceBuilder);
		//2、执行检索
		SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
		//3、分析结果
		System.out.println(searchResponse.toString());
		SearchHits hits = searchResponse.getHits();
		SearchHit[] searchHits = hits.getHits();
		for (SearchHit hit:searchHits) {
//			hit.getIndex();hit.getType();
//			hit.getSourceAsMap();
			String string = hit.getSourceAsString();
			Account account = JSON.parseObject(string, Account.class);
			System.out.println(account);
		}
		//分析聚合的结果
		Aggregations aggregations = searchResponse.getAggregations();
		Terms aggAgg1 = aggregations.get("aggAgg");
		for (Terms.Bucket bucket:aggAgg1.getBuckets()) {
			String keyString = bucket.getKeyAsString();
			System.out.println("年龄"+keyString+"--"+bucket.getDocCount());
		}
		Avg balanceAvg = aggregations.get("balanceAvg");
		System.out.println("平均薪资"+balanceAvg.getValue());

	}

	@AllArgsConstructor
	@Data
	class Users{
		private String userName;
		private String gender;
		private Integer age;
	}

	@AllArgsConstructor
	@Data
	@ToString
	public class Account {

		private int account_number;
		private int balance;
		private String firstname;
		private String lastname;
		private int age;
		private String gender;
		private String address;
		private String employer;
		private String email;
		private String city;
		private String state;
	}

}
