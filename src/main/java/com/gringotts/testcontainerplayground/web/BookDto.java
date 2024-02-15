package com.gringotts.testcontainerplayground.web;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookDto {
    private Long id;
    private String title;
    private String synopsis;
    private LocalDate releaseDate;
}
