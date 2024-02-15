package com.gringotts.testcontainerplayground.repository;

import com.gringotts.testcontainerplayground.config.BasePostgreSQLContainer;
import com.gringotts.testcontainerplayground.domain.Author;
import com.gringotts.testcontainerplayground.domain.Book;
import com.gringotts.testcontainerplayground.service.document.BillService;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = {BookRepositoryTest.Initializer.class})
@EnableAutoConfiguration(exclude = {ElasticsearchDataAutoConfiguration.class,
        ElasticsearchRepositoriesAutoConfiguration.class})
public class BookRepositoryTest {

    @MockBean
    private BillService billService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    private Author murakami;
    private Author me;

    @ClassRule
    public static BasePostgreSQLContainer postgreSQLContainer = new BasePostgreSQLContainer("postgres:15.3")
            .withDatabaseName("integration-tests-db")
            .withUsername("admin")
            .withPassword("admin");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(applicationContext.getEnvironment());
        }
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
}