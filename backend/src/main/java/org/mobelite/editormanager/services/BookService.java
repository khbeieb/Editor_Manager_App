package org.mobelite.editormanager.services;

import lombok.AllArgsConstructor;
import org.mobelite.editormanager.dto.AuthorBasicDTO;
import org.mobelite.editormanager.dto.BookDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.mappers.BookMapper;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
}
