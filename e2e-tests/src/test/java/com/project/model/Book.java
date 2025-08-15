package com.project.model;

import lombok.Data;

@Data
public class Book {
  private int id;
  private String title;
  private String isbn;
  private AuthorBasic author;
  private String publicationDate; // Keeping this as String for easy JSON mapping in tests
}
