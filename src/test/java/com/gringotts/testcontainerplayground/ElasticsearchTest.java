package com.gringotts.testcontainerplayground;

import com.gringotts.testcontainerplayground.config.ElasticsearchTestConfig;
import com.gringotts.testcontainerplayground.domain.document.Bill;
import com.gringotts.testcontainerplayground.repository.document.BillRepository;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@Import(ElasticsearchTestConfig.class)
public class ElasticsearchTest {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    @ClassRule
    public static final ElasticsearchContainer ELASTICSEARCH_CONTAINER =
            new ElasticsearchContainer(DockerImageName.parse("elasticsearch:7.17.3")
                                                      .asCompatibleSubstituteFor(
                                                              "docker.elastic.co/elasticsearch/elasticsearch"))
                    .withExposedPorts(9200, 9300)
                    .withEnv("cluster.name", "elasticsearch")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", Boolean.FALSE.toString());

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("app.elasticsearch.host", ELASTICSEARCH_CONTAINER::getHost);
        registry.add("app.elasticsearch.port", () -> ELASTICSEARCH_CONTAINER.getMappedPort(9200).toString());
    }

    @Before
    public void setUp() throws Exception {
        recreateIndex();
        final Bill firstBill = new Bill("To Kill A Mockingbird", "Harper Lee", 24.99);
        final Bill secondBill = new Bill("To Kill A Mockingbird", "Harper Lee", 24.99);
        final Bill thirdBill = new Bill("Kafka On The Shore", "Haruki Murakami", 49.98);
        final Bill fourthBill = new Bill("Norwegian Wood", "Haruki Murakami", 33.97);
        billRepository.saveAll(List.of(firstBill, secondBill, thirdBill, fourthBill));
    }

    @Test
    public void findByBookName_whenInvoked_returnsExpectedResult() {
        final List<Bill> bills = billRepository.findByBookName("To Kill A Mockingbird");

        assertThat(bills).isNotNull()
                         .isNotEmpty()
                         .hasSize(2)
                         .allMatch(bill -> bill.getBookName().equals("To Kill A Mockingbird"));
    }

    @Test
    public void findByAuthorName_whenInvoked_returnsExpectedResult() {
        final List<Bill> bills = billRepository.findByAuthorName("Haruki Murakami");

        assertThat(bills).isNotNull()
                         .isNotEmpty()
                         .hasSize(2)
                         .allMatch(bill -> bill.getAuthorName().equals("Haruki Murakami"));
    }

    private void recreateIndex() {
        if (elasticsearchTemplate.indexOps(Bill.class).exists()) {
            elasticsearchTemplate.indexOps(Bill.class).delete();
            elasticsearchTemplate.indexOps(Bill.class).create();
        }
    }
}
