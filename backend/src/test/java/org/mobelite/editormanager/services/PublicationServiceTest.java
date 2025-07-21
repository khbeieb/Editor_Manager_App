package org.mobelite.editormanager.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mobelite.editormanager.dto.PublicationDTO;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.entities.Magazine;
import org.mobelite.editormanager.entities.Publication;
import org.mobelite.editormanager.enums.PublicationType;
import org.mobelite.editormanager.repositories.BookRepository;
import org.mobelite.editormanager.repositories.MagazineRepository;
import org.mobelite.editormanager.repositories.PublicationRepository;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PublicationServiceTest {

    private PublicationRepository publicationRepository;
    private MagazineRepository magazineRepository;
    private BookRepository bookRepository;

    private PublicationService publicationService;

    @BeforeEach
    void setUp() {
        publicationRepository = mock(PublicationRepository.class);
        magazineRepository = mock(MagazineRepository.class);
        bookRepository = mock(BookRepository.class);
        publicationService = new PublicationService(publicationRepository, magazineRepository, bookRepository);
    }

    @Test
    void getPublications_shouldReturnPageOfPublications() {
        Pageable pageable = PageRequest.of(0, 5);
        Publication publication = new Book();
        publication.setTitle("Some Book");
        Page<Publication> page = new PageImpl<>(List.of(publication));

        when(publicationRepository.findAll(pageable)).thenReturn(page);

        Page<PublicationDTO> result = publicationService.getPublications(pageable);

        assertEquals(1, result.getTotalElements());
        verify(publicationRepository, times(1)).findAll(pageable);
    }

    @Test
    void searchByTitle_shouldReturnCombinedBookAndMagazineResults() {
        Book book = new Book();
        book.setTitle("Java Fundamentals");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));

        Magazine magazine = new Magazine();
        magazine.setTitle("Java Monthly");
        magazine.setPublicationDate(LocalDate.of(2021, 5, 10));

        when(bookRepository.findByTitleContainingIgnoreCase("java")).thenReturn(List.of(book));
        when(magazineRepository.findByTitleContainingIgnoreCase("java")).thenReturn(List.of(magazine));

        List<PublicationDTO> result = publicationService.searchByTitle("java");

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(p -> p.getType() == PublicationType.BOOK));
        assertTrue(result.stream().anyMatch(p -> p.getType() == PublicationType.MAGAZINE));

        verify(bookRepository).findByTitleContainingIgnoreCase("java");
        verify(magazineRepository).findByTitleContainingIgnoreCase("java");
    }
}