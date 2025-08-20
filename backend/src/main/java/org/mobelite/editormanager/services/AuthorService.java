package org.mobelite.editormanager.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mobelite.editormanager.dto.AuthorDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.mappers.AuthorMapper;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class AuthorService {
  private final AuthorRepository authorRepository;
  private final BookRepository bookRepository;

  public AuthorDTO addAuthor(AuthorDTO request) {
    // Prevent duplicate author names
    if (authorRepository.findAuthorByName(request.getName()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Author already exists");
    }

    Author author = AuthorMapper.toEntity(request);

    // Validate books
    if (author.getBooks() != null) {
      Set<String> seenIsbns = new HashSet<>();
      for (Book book : author.getBooks()) {
        if (!seenIsbns.add(book.getIsbn())) {
          // Duplicate ISBN inside the same payload
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
            "Duplicate ISBN in author request: " + book.getIsbn());
        }

        if (bookRepository.existsByIsbn(book.getIsbn())) {
          throw new ResponseStatusException(HttpStatus.CONFLICT,
            "Book with ISBN " + book.getIsbn() + " already exists");
        }

        book.setAuthor(author);
      }
    }

    Author savedAuthor = authorRepository.save(author);
    return AuthorMapper.toDTO(savedAuthor);
  }

  public List<Author> getAllAuthors() {
    return authorRepository.findAll();
  }

  @Transactional
  public void deleteAuthor(Long authorId) {
    Author author = authorRepository.findById(authorId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
        "Author not found with id: " + authorId));

    // Delete all books of this author
    List<Book> books = bookRepository.findAll()
      .stream()
      .filter(b -> b.getAuthor().getId().equals(authorId))
      .toList();

    System.out.println("Deleting books for author " + author.getName() + ": " + books.size());
    books.forEach(bookRepository::delete);

    // Now delete the author
    authorRepository.delete(author);
    System.out.println("Author deleted: " + author.getName());
  }
}
