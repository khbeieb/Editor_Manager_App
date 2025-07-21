package org.mobelite.editormanager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.mobelite.editormanager.dto.ApiResponse;
import org.mobelite.editormanager.dto.AuthorDTO;
import org.mobelite.editormanager.mappers.AuthorMapper;
import org.mobelite.editormanager.services.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
@Tag(name = "Authors", description = "Manage Authors")
public class AuthorController {

    private final AuthorService authorService;

    @Operation(summary = "Add a new Author")
    @PostMapping
    public ResponseEntity<ApiResponse<AuthorDTO>> addAuthor(@Valid @RequestBody AuthorDTO author) {
        AuthorDTO savedAuthor = authorService.addAuthor(author);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>(
                        HttpStatus.CREATED.value(),
                        "Author created successfully",
                        savedAuthor,
                        LocalDateTime.now()
                )
        );
    }

    @Operation(summary = "Get all Authors")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AuthorDTO>>> getAuthors() {
        List<AuthorDTO> authors = authorService.getAllAuthors()
                .stream()
                .map(AuthorMapper::toDTO)
                .toList();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Authors fetched successfully",
                        authors,
                        LocalDateTime.now()
                )
        );
    }
}