package com.gringotts.testcontainerplayground.service;

import com.gringotts.testcontainerplayground.domain.Author;
import com.gringotts.testcontainerplayground.domain.Book;
import com.gringotts.testcontainerplayground.repository.AuthorRepository;
import com.gringotts.testcontainerplayground.web.AuthorDto;
import com.gringotts.testcontainerplayground.web.BookDto;
import com.gringotts.testcontainerplayground.web.CreateAuthorData;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "authorCache")
    public List<AuthorDto> getAll() {
        return authorRepository.findAll().stream()
                               .map(this::toDto)
                               .collect(Collectors.toList());
    }

    private AuthorDto toDto(Author author) {
        final var dto = new AuthorDto();
        dto.setId(author.getId());
        dto.setName(author.getName());
        dto.setDateOfBirth(author.getDateOfBirth());
        dto.setBooks(author.getBooks().stream().map(this::toDto).collect(Collectors.toList()));
        return dto;
    }

    private BookDto toDto(Book book) {
        final var dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setSynopsis(book.getSynopsis());
        dto.setReleaseDate(book.getReleaseDate());
        return dto;
    }
}
