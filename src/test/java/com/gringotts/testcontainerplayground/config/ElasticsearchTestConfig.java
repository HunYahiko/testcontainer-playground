package com.gringotts.testcontainerplayground.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@TestConfiguration
@EnableElasticsearchRepositories({"com.gringotts"})
public class ElasticsearchTestConfig {

    @Value("${app.elasticsearch.host}")
    private String elastisearchHost;

    @Value("${app.elasticsearch.port}")
    private int elastisearchPort;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient.builder(
                new HttpHost(elastisearchHost, elastisearchPort, "http")
        ).build();

        ElasticsearchTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);

    }
}
