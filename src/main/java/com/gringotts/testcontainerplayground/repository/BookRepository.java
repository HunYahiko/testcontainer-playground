package com.gringotts.testcontainerplayground.repository;

import com.gringotts.testcontainerplayground.domain.Author;
import com.gringotts.testcontainerplayground.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findAllByAuthor(Author author);
}
