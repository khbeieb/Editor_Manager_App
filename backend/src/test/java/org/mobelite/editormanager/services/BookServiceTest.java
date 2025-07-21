package org.mobelite.editormanager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mobelite.editormanager.dto.AuthorBasicDTO;
import org.mobelite.editormanager.dto.BookDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.BookRepository;

import java.time.LocalDate;
import java.util.*;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addBook_shouldSaveBook_whenIsbnNotExists() {
        // Arrange
        Author author = new Author();
        author.setId(1L);
        author.setName("Author Name");
        author.setNationality("Country");

        BookDTO inputDto = new BookDTO(null, "Title", "ISBN12345", new AuthorBasicDTO(1L, null, null), LocalDate.of(2020,1,1));

        when(bookRepository.existsByIsbn("ISBN12345")).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        Book savedBook = new Book();
        savedBook.setTitle("Title");
        savedBook.setIsbn("ISBN12345");
        savedBook.setAuthor(author);
        savedBook.setPublicationDate(LocalDate.of(2020,1,1));

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        // Act
        BookDTO result = bookService.addBook(inputDto);

        // Assert
        assertNotNull(result);
        assertEquals("Title", result.getTitle());
        assertEquals("ISBN12345", result.getIsbn());
        assertEquals(1L, result.getAuthor().getId());
        assertEquals("Author Name", result.getAuthor().getName());
        assertEquals("Country", result.getAuthor().getNationality());
        assertEquals(LocalDate.of(2020,1,1), result.getPublicationDate());

        verify(bookRepository).existsByIsbn("ISBN12345");
        verify(authorRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_shouldThrow_whenIsbnExists() {
        // Arrange
        BookDTO inputDto = new BookDTO(null, "Title", "ISBN12345", new AuthorBasicDTO(1L, null, null), LocalDate.of(2020,1,1));
        when(bookRepository.existsByIsbn("ISBN12345")).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookService.addBook(inputDto));
        assertTrue(ex.getMessage().contains("already exists"));

        verify(bookRepository).existsByIsbn("ISBN12345");
        verifyNoMoreInteractions(authorRepository, bookRepository);
    }

    @Test
    void addBook_shouldThrow_whenAuthorNotFound() {
        // Arrange
        BookDTO inputDto = new BookDTO(null, "Title", "ISBN12345", new AuthorBasicDTO(1L, null, null), LocalDate.of(2020,1,1));
        when(bookRepository.existsByIsbn("ISBN12345")).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> bookService.addBook(inputDto));
        assertTrue(ex.getMessage().contains("Author not found"));

        verify(bookRepository).existsByIsbn("ISBN12345");
        verify(authorRepository).findById(1L);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    void getByIsbn_shouldReturnBookDTO_whenFound() {
        // Arrange
        Author author = new Author();
        author.setId(1L);
        author.setName("Author Name");
        author.setNationality("Country");

        Book book = new Book();
        book.setTitle("Title");
        book.setIsbn("ISBN12345");
        book.setAuthor(author);
        book.setPublicationDate(LocalDate.of(2020,1,1));

        when(bookRepository.findByIsbn("ISBN12345")).thenReturn(Optional.of(book));

        // Act
        Optional<BookDTO> result = bookService.getByIsbn("ISBN12345");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Title", result.get().getTitle());
        assertEquals("ISBN12345", result.get().getIsbn());
        assertEquals(1L, result.get().getAuthor().getId());
        assertEquals("Author Name", result.get().getAuthor().getName());
        assertEquals("Country", result.get().getAuthor().getNationality());
        assertEquals(LocalDate.of(2020,1,1), result.get().getPublicationDate());
    }

    @Test
    void getByIsbn_shouldReturnEmpty_whenNotFound() {
        // Arrange
        when(bookRepository.findByIsbn("ISBN12345")).thenReturn(Optional.empty());

        // Act
        Optional<BookDTO> result = bookService.getByIsbn("ISBN12345");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void getBooks_shouldReturnListOfBookDTO() {
        // Arrange
        Author author1 = new Author();
        author1.setId(1L);
        author1.setName("Author One");
        author1.setNationality("Country1");

        Book book1 = new Book();
        book1.setTitle("Title1");
        book1.setIsbn("ISBN1");
        book1.setAuthor(author1);
        book1.setPublicationDate(LocalDate.of(2020,1,1));

        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("Author Two");
        author2.setNationality("Country2");

        Book book2 = new Book();
        book2.setTitle("Title2");
        book2.setIsbn("ISBN2");
        book2.setAuthor(author2);
        book2.setPublicationDate(LocalDate.of(2021,2,2));

        when(bookRepository.findAll()).thenReturn(List.of(book1, book2));

        // Act
        List<BookDTO> results = bookService.getBooks();

        // Assert
        assertEquals(2, results.size());

        assertEquals("Title1", results.get(0).getTitle());
        assertEquals("ISBN1", results.get(0).getIsbn());
        assertEquals(1L, results.get(0).getAuthor().getId());
        assertEquals("Author One", results.get(0).getAuthor().getName());

        assertEquals("Title2", results.get(1).getTitle());
        assertEquals("ISBN2", results.get(1).getIsbn());
        assertEquals(2L, results.get(1).getAuthor().getId());
        assertEquals("Author Two", results.get(1).getAuthor().getName());
    }
}