package com.gringotts.testcontainerplayground.repository.document;

import com.gringotts.testcontainerplayground.domain.document.Bill;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillRepository extends ElasticsearchRepository<Bill, String> {

    List<Bill> findByAuthorName(String authorName);
    List<Bill> findByBookName(String bookName);
}
