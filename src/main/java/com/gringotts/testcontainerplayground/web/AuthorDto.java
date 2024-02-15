package com.gringotts.testcontainerplayground.web;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AuthorDto {
    private Long id;
    private String name;
    private LocalDate dateOfBirth;
    private List<BookDto> books;
}
