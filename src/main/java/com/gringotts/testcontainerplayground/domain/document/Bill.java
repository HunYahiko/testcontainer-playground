package com.gringotts.testcontainerplayground.domain.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "bills")
@Data
public class Bill {
    @Id
    private String id;
    private String bookName;
    private String authorName;
    private Double price;

    public Bill(String bookName, String authorName, Double price) {
        this.bookName = bookName;
        this.authorName = authorName;
        this.price = price;
    }
}
