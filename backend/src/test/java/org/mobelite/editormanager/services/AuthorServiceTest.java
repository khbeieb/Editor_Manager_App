package org.mobelite.editormanager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mobelite.editormanager.dto.AuthorDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.BookRepository;

import java.time.LocalDate;
import java.util.*;

public class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private AuthorService authorService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addAuthor_shouldSaveAndReturnAuthorDTO_whenAuthorDoesNotExistAndBooksAreNull() {
        // Arrange
        AuthorDTO request = new AuthorDTO(
            null,
                "Jane Austen",
                LocalDate.of(1775, 12, 16),
                "British",
                null
        );

        when(authorRepository.findAuthorByName("Jane Austen")).thenReturn(Optional.empty());

        Author savedAuthor = new Author();
        savedAuthor.setId(1L);
        savedAuthor.setName("Jane Austen");
        savedAuthor.setBirthDate(LocalDate.of(1775, 12, 16));
        savedAuthor.setNationality("British");
        savedAuthor.setBooks(new ArrayList<>());

        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        // Act
        AuthorDTO result = authorService.addAuthor(request);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Austen", result.getName());
        assertEquals("British", result.getNationality());

        verify(authorRepository).findAuthorByName("Jane Austen");
        verify(authorRepository).save(any(Author.class));
        verifyNoInteractions(bookRepository);
    }

    @Test
    void addAuthor_shouldThrowException_whenAuthorAlreadyExists() {
        // Arrange
        AuthorDTO request = new AuthorDTO(null,"Jane Austen", LocalDate.of(1775, 12, 16), "British", null);
        when(authorRepository.findAuthorByName("Jane Austen")).thenReturn(Optional.of(new Author()));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authorService.addAuthor(request));
        assertEquals("Author already exists", exception.getMessage());

        verify(authorRepository).findAuthorByName("Jane Austen");
        verifyNoMoreInteractions(authorRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void addAuthor_shouldThrowException_whenBookWithSameIsbnAlreadyExists() {
        // Arrange
        Book book = new Book();
        book.setIsbn("1234567890");

        AuthorDTO request = new AuthorDTO(
                null,
                "New Author",
                LocalDate.of(2000, 1, 1),
                "Nowhere",
                List.of(book)
        );

        when(authorRepository.findAuthorByName("New Author")).thenReturn(Optional.empty());
        when(bookRepository.existsByIsbn("1234567890")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authorService.addAuthor(request));
        assertEquals("Book with ISBN 1234567890 already exists", exception.getMessage());

        verify(authorRepository).findAuthorByName("New Author");
        verify(bookRepository).existsByIsbn("1234567890");
        verifyNoMoreInteractions(authorRepository, bookRepository);
    }

    @Test
    void addAuthor_shouldSetAuthorOnBooks_whenBooksArePresent() {
        // Arrange
        Book book1 = new Book();
        book1.setIsbn("ISBN-1");

        Book book2 = new Book();
        book2.setIsbn("ISBN-2");

        List<Book> books = List.of(book1, book2);

        AuthorDTO request = new AuthorDTO(
                null,
                "Author With Books",
                LocalDate.of(1990, 1, 1),
                "Exampleland",
                books
        );

        when(authorRepository.findAuthorByName("Author With Books")).thenReturn(Optional.empty());
        when(bookRepository.existsByIsbn("ISBN-1")).thenReturn(false);
        when(bookRepository.existsByIsbn("ISBN-2")).thenReturn(false);

        ArgumentCaptor<Author> authorCaptor = ArgumentCaptor.forClass(Author.class);

        Author savedAuthor = new Author();
        savedAuthor.setId(99L);
        savedAuthor.setName("Author With Books");
        savedAuthor.setBirthDate(LocalDate.of(1990, 1, 1));
        savedAuthor.setNationality("Exampleland");
        savedAuthor.setBooks(books);

        when(authorRepository.save(any(Author.class))).thenReturn(savedAuthor);

        // Act
        AuthorDTO result = authorService.addAuthor(request);

        // Assert
        assertNotNull(result);
        assertEquals("Author With Books", result.getName());
        assertEquals(2, result.getBooks().size());

        verify(authorRepository).save(authorCaptor.capture());
        Author captured = authorCaptor.getValue();
        assertEquals(captured, book1.getAuthor());
        assertEquals(captured, book2.getAuthor());

        verify(bookRepository).existsByIsbn("ISBN-1");
        verify(bookRepository).existsByIsbn("ISBN-2");
    }

    @Test
    void getAllAuthors_shouldReturnListOfAuthors() {
        // Arrange
        List<Author> authors = List.of(new Author(), new Author());
        when(authorRepository.findAll()).thenReturn(authors);

        // Act
        List<Author> result = authorService.getAllAuthors();

        // Assert
        assertEquals(2, result.size());
        verify(authorRepository).findAll();
    }
}