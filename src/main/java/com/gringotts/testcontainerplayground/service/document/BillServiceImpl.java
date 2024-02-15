package com.gringotts.testcontainerplayground.service.document;

import com.gringotts.testcontainerplayground.domain.document.Bill;
import com.gringotts.testcontainerplayground.repository.document.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    @Override
    public List<Bill> getAllByBookName(String bookName) {
        return billRepository.findByBookName(bookName);
    }

    @Override
    public List<Bill> getAllByAuthorName(String authorName) {
        return billRepository.findByAuthorName(authorName);
    }
}
