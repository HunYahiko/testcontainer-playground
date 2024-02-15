package com.gringotts.testcontainerplayground.service.document;

import com.gringotts.testcontainerplayground.domain.document.Bill;

import java.util.List;

public interface BillService {

    List<Bill> getAllByBookName(String bookName);
    List<Bill> getAllByAuthorName(String authorName);
}
