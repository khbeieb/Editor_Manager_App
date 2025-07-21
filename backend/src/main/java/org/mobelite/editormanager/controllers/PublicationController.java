package org.mobelite.editormanager.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.mobelite.editormanager.dto.ApiResponse;
import org.mobelite.editormanager.dto.BookDTO;
import org.mobelite.editormanager.dto.MagazineDTO;
import org.mobelite.editormanager.dto.PublicationDTO;
import org.mobelite.editormanager.services.BookService;
import org.mobelite.editormanager.services.MagazineService;
import org.mobelite.editormanager.services.PublicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/publications")
@RequiredArgsConstructor
@Tag(name = "Publications", description = "Manage Publications")
public class PublicationController {
    private final MagazineService magazineService;
    private final BookService bookService;
    private final PublicationService publicationService;

    @Operation(summary = "Get paginated Publications (Books + Magazines)")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PublicationDTO>>> getPublications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PublicationDTO> publicationDTOs = publicationService.getPublications(pageable);

        ApiResponse<Page<PublicationDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Publications fetched successfully",
                publicationDTOs,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all Publications grouped by Books and Magazines")
    @GetMapping("/grouped")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllPublications() {
        List<BookDTO> books = bookService.getBooks();
        List<MagazineDTO> magazines = magazineService.getAllMagazines();

        Map<String, Object> groupedPublications = new HashMap<>();
        groupedPublications.put("books", books);
        groupedPublications.put("magazines", magazines);

        ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Publications fetched successfully",
                groupedPublications,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search publications by title")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<PublicationDTO>>> searchPublications(@RequestParam String title) {
        List<PublicationDTO> results = publicationService.searchByTitle(title);

        ApiResponse<List<PublicationDTO>> response = new ApiResponse<>(
                HttpStatus.OK.value(),
                "Found " + results.size() + " publications matching title: " + title,
                results,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }
}