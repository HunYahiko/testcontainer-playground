package com.gringotts.testcontainerplayground.web;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateAuthorData {
    private String name;
    private LocalDate dateOfBirth;
}
