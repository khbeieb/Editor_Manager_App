package org.mobelite.editormanager.services;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mobelite.editormanager.dto.BookDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.mappers.BookMapper;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class BookService {
  private final BookRepository bookRepository;
  private final AuthorRepository authorRepository;

  public BookDTO addBook(BookDTO bookDTO) {
    if (bookRepository.existsByIsbn(bookDTO.getIsbn())) {
      throw new RuntimeException("Book with isbn " + bookDTO.getIsbn() + " already exists");
    }

    Author author = authorRepository.findById(bookDTO.getAuthor().getId())
      .orElseThrow(() -> new RuntimeException("Author not found with id: " + bookDTO.getAuthor().getId()));

    Book savedBook = bookRepository.save(BookMapper.toEntity(bookDTO, author));

    return BookMapper.toDTO(savedBook);
  }

  public Optional<BookDTO> getByIsbn(String isbn) {
    return bookRepository.findByIsbn(isbn).map(BookMapper::toDTO);
  }

  public List<BookDTO> getBooks() {
    return bookRepository.findAll().stream()
      .map(BookMapper::toDTO)
      .collect(Collectors.toList());
  }

  @Transactional
  public boolean deleteBook(Long id) {
    log.info("Attempting to delete book with ID: {}", id);
    if (bookRepository.existsById(id)) {
      bookRepository.deleteById(id);
      log.info("Successfully deleted book with ID: {}", id);
      return true;
    }
    log.warn("Book with ID {} not found for deletion", id);
    return false;
  }

  @Transactional
  public boolean deleteBookByIsbn(String isbn) {
    log.info("Attempting to delete book with ISBN: {}", isbn);
    Optional<Book> book = bookRepository.findByIsbn(isbn);
    if (book.isPresent()) {
      bookRepository.delete(book.get());
      log.info("Successfully deleted book with ISBN: {}", isbn);
      return true;
    }
    log.warn("Book with ISBN {} not found for deletion", isbn);
    return false;
  }


}
