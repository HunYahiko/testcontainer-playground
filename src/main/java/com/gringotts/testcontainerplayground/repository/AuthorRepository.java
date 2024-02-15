package com.gringotts.testcontainerplayground.repository;

import com.gringotts.testcontainerplayground.domain.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
