package com.geo.mybook.search.config;


import jakarta.annotation.Resource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
creator：AZERL7
createTime：15:35
*/
@Configuration
public class ElasticsearchRestHighLevelClient {
    @Resource
    private ElasticsearchProperties  elasticsearchProperties;

    private static final String COLON=":";
    private static final String HTTP="http";

    @Bean
    public RestHighLevelClient getRestHighLevelClient() {
        String address=elasticsearchProperties.getAddress();
        //按照冒号分隔
        String[] addressArray=address.split(COLON);
        //IP地址
        String host=addressArray[0];
        //端口
        int port=Integer.parseInt(addressArray[1]);
        HttpHost httpHost=new HttpHost(host,port,HTTP);
        return new RestHighLevelClient(RestClient.builder(httpHost));
    }
}
