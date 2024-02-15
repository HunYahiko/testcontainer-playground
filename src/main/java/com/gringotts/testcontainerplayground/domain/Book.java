package com.gringotts.testcontainerplayground.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "BOOKS")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "synopsis")
    private String synopsis;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    private Author author;

    public Book(String title, String synopsis, LocalDate releaseDate) {
        this.title = title;
        this.synopsis = synopsis;
        this.releaseDate = releaseDate;
    }
}
