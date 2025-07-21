package org.mobelite.editormanager.services;

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
}
