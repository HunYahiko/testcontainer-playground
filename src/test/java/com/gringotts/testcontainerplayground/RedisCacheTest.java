package com.gringotts.testcontainerplayground;

import com.gringotts.testcontainerplayground.domain.Author;
import com.gringotts.testcontainerplayground.domain.Book;
import com.gringotts.testcontainerplayground.repository.AuthorRepository;
import com.gringotts.testcontainerplayground.service.AuthorService;
import com.gringotts.testcontainerplayground.service.document.BillService;
import com.gringotts.testcontainerplayground.web.AuthorDto;
import com.redis.testcontainers.RedisContainer;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@EnableCaching
@ContextConfiguration
@RunWith(SpringRunner.class)
@EnableAutoConfiguration(exclude = {ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRepositoriesAutoConfiguration.class})
public class RedisCacheTest {

    @MockBean
    private BillService billService;

    @MockBean
    private AuthorRepository authorRepository;

    @Autowired
    private AuthorService authorService;

//    @ClassRule
//    public static final RedisContainer REDIS_CONTAINER = new RedisContainer(DockerImageName.parse("redis:7.2.1-alpine"))
//            .withExposedPorts(6379);

    @ClassRule
    public static final GenericContainer REDIS_CONTAINER_GENERIC = new GenericContainer(DockerImageName.parse("redis:7.2.1-alpine"))
            .withExposedPorts(6379).dependsOn();

//    @DynamicPropertySource
//    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
//        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());
//    }

    @DynamicPropertySource
    private static void registerRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER_GENERIC::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER_GENERIC.getMappedPort(6379).toString());
    }

    @Before
    public void before() {
        final var murakami = new Author("Haruki Murakami", LocalDate.of(1949, 1, 12));
        final var kafkaOnTheShore = new Book("Kafka On The Shore",
                                             "Something about the book",
                                             LocalDate.of(2002, 9, 12));
        final var norwegianWood = new Book("Norwegian Wood",
                                           "Something about the book",
                                           LocalDate.of(1987, 1, 1));
        murakami.addBook(kafkaOnTheShore);
        murakami.addBook(norwegianWood);
        final var me = new Author("Stanislav Sănduță", LocalDate.of(1997, 6, 16));

        when(authorRepository.findAll()).thenReturn(List.of(murakami, me));
    }

    @Test
    public void givenRedisContainerConfiguredWithDynamicProperties_whenCheckingRunningStatus_thenStatusIsRunning() {
        assertThat(REDIS_CONTAINER_GENERIC.isRunning()).isTrue();
    }

    @Test
    public void getAll_whenCalledTwice_databaseIsHitOnlyOnce() {
        final List<AuthorDto> returnedNonCachedResults = authorService.getAll();

        assertThat(returnedNonCachedResults).isNotNull().isNotEmpty().hasSize(2);

        final List<AuthorDto> returnedCachedResults = authorService.getAll();

        assertThat(returnedCachedResults).isNotNull().isNotEmpty().hasSize(2);
        verify(authorRepository, times(1)).findAll();
    }
}
