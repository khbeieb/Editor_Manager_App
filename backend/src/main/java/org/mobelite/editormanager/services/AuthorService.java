package org.mobelite.editormanager.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mobelite.editormanager.dto.AuthorDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.mappers.AuthorMapper;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorDTO addAuthor(AuthorDTO request) {
        if (authorRepository.findAuthorByName(request.getName()).isPresent()) {
            throw new RuntimeException("Author already exists");
        }

        Author author = AuthorMapper.toEntity(request);

        if (author.getBooks() != null) {
            for (Book book : author.getBooks()) {
                if (bookRepository.existsByIsbn(book.getIsbn())) {
                    throw new RuntimeException("Book with ISBN " + book.getIsbn() + " already exists");
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
        .orElseThrow(() -> new RuntimeException("Author not found with id: " + authorId));

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
