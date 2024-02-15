package com.gringotts.testcontainerplayground;

import com.gringotts.testcontainerplayground.config.ElasticsearchTestConfig;
import com.gringotts.testcontainerplayground.domain.Author;
import com.gringotts.testcontainerplayground.domain.Book;
import com.gringotts.testcontainerplayground.domain.document.Bill;
import com.gringotts.testcontainerplayground.repository.AuthorRepository;
import com.gringotts.testcontainerplayground.repository.BookRepository;
import com.gringotts.testcontainerplayground.repository.document.BillRepository;
import com.gringotts.testcontainerplayground.service.document.BillService;
import com.redis.testcontainers.RedisContainer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers = {TestcontainerDockerComposeTest.Initializer.class})
@Import(ElasticsearchTestConfig.class)
public class TestcontainerDockerComposeTest {

    private static final int REDIS_PORT = 6379;
    private static final int POSTGRES_PORT = 5432;
    private static final int ELASTICSEARCH_PORT = 9200;

    @MockBean
    private BillService billService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchTemplate;

    private Author murakami;
    private Author me;

    @ClassRule
    public static ComposeContainer environment =
            new ComposeContainer(new File("src/test/resources/elasticsearch-redis-postgresql.yml"))
                    .withOptions("--compatibility")
                    .withExposedService()
                    .withExposedService("redis-1", REDIS_PORT, Wait.forHealthcheck())
                    .withExposedService("elasticsearch-1", ELASTICSEARCH_PORT, Wait.forHealthcheck())
                    .withExposedService("postgres-1", POSTGRES_PORT, Wait.forHealthcheck());

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            Optional<ContainerState> postgresState = environment.getContainerByServiceName("postgres-1");
            PostgreSQLContainer postgres = postgresState.map(c -> (PostgreSQLContainer)c).orElseThrow();
            TestPropertyValues.of(
                    "spring.datasource.username=" + postgres.getUsername(),
                    "spring.datasource.password=" + postgres.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        Optional<ContainerState> redisState = environment.getContainerByServiceName("redis-1");
        RedisContainer redis = redisState.map(c -> (RedisContainer)c).orElseThrow();
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(REDIS_PORT).toString());
        Optional<ContainerState> elasticsearchState = environment.getContainerByServiceName("elasticsearch-1");
        ElasticsearchContainer elasticsearch = elasticsearchState.map(c -> (ElasticsearchContainer)c).orElseThrow();
        registry.add("app.elasticsearch.host", elasticsearch::getHost);
        registry.add("app.elasticsearch.port", () -> elasticsearch.getMappedPort(ELASTICSEARCH_PORT).toString());
    }

    @Before
    public void before() {
        murakami = new Author("Haruki Murakami", LocalDate.of(1949, 1, 12));
        final var kafkaOnTheShore = new Book("Kafka On The Shore",
                                             "Something about the book",
                                             LocalDate.of(2002, 9, 12));
        final var norwegianWood = new Book("Norwegian Wood",
                                           "Something about the book",
                                           LocalDate.of(1987, 1, 1));
        murakami.addBook(kafkaOnTheShore);
        murakami.addBook(norwegianWood);
        authorRepository.save(murakami);

        me = new Author("Stanislav Sănduță", LocalDate.of(1997, 6, 16));
        authorRepository.save(me);

        recreateIndex();
        final Bill firstBill = new Bill("To Kill A Mockingbird", "Harper Lee", 24.99);
        final Bill secondBill = new Bill("To Kill A Mockingbird", "Harper Lee", 24.99);
        final Bill thirdBill = new Bill("Kafka On The Shore", "Haruki Murakami", 49.98);
        final Bill fourthBill = new Bill("Norwegian Wood", "Haruki Murakami", 33.97);
        billRepository.saveAll(List.of(firstBill, secondBill, thirdBill, fourthBill));
    }

    private void recreateIndex() {
        if (elasticsearchTemplate.indexOps(Bill.class).exists()) {
            elasticsearchTemplate.indexOps(Bill.class).delete();
            elasticsearchTemplate.indexOps(Bill.class).create();
        }
    }

    @Test
    public void findAllByAuthor_whenInvoked_returnsExpectedResults() {
        final List<Book> returnedBooks = bookRepository.findAllByAuthor(murakami);

        assertThat(returnedBooks).isNotNull()
                                 .isNotEmpty()
                                 .hasSize(2);
    }

    @Test
    public void findAllByAuthor_whenAuthorHasNoBooks_returnsEmptyList() {
        final List<Book> returnedBooks = bookRepository.findAllByAuthor(me);

        assertThat(returnedBooks).isEmpty();
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
}
