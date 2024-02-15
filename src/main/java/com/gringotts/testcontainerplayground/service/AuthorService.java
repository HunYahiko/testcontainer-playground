package com.gringotts.testcontainerplayground.service;

import com.gringotts.testcontainerplayground.web.AuthorDto;
import com.gringotts.testcontainerplayground.web.CreateAuthorData;

import java.util.List;

public interface AuthorService {
    List<AuthorDto> getAll();
}
