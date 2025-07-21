package org.mobelite.editormanager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mobelite.editormanager.dto.ApiResponse;
import org.mobelite.editormanager.dto.BookDTO;
import org.mobelite.editormanager.services.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
@Tag(name = "Books", description = "Manage Books")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Add a new Book")
    @PostMapping
    public ResponseEntity<ApiResponse<BookDTO>> createBook(@Valid @RequestBody BookDTO bookDTO) {
        log.info("Received BookDTO: {}", bookDTO);
        BookDTO savedBook = bookService.addBook(bookDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(HttpStatus.CREATED.value(), "Book created successfully", savedBook, LocalDateTime.now())
        );
    }

    @Operation(summary = "Get all Books")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookDTO>>> getBooks() {
        List<BookDTO> books = bookService.getBooks();
        return ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Books fetched successfully", books, LocalDateTime.now())
        );
    }

    @Operation(summary = "Get book by isbn")
    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<ApiResponse<BookDTO>> getByIsbn(@PathVariable String isbn) {
        Optional<BookDTO> book = bookService.getByIsbn(isbn);
        return book.map(b -> ResponseEntity.ok(
                        new ApiResponse<>(HttpStatus.OK.value(), "Book fetched successfully", b, LocalDateTime.now())
                ))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "Book not found with ISBN " + isbn, null, LocalDateTime.now())
                ));
    }
}
