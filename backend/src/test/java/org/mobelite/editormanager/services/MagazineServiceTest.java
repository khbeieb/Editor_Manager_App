package org.mobelite.editormanager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mobelite.editormanager.dto.AuthorBasicDTO;
import org.mobelite.editormanager.dto.MagazineDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Magazine;
import org.mobelite.editormanager.mappers.MagazineMapper;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.MagazineRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class MagazineServiceTest {

    @Mock
    private MagazineRepository magazineRepository;

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private MagazineService magazineService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addMagazine_shouldSaveAndReturnMagazineDTO_whenIssueNumberNotExists() {
        // Arrange
        AuthorBasicDTO authorDto = new AuthorBasicDTO(1L, "Author One", "CountryA");
        MagazineDTO request = new MagazineDTO(null, 101, "Monthly Tech", LocalDate.of(2025, 7, 1), List.of(authorDto));

        Author author = new Author();
        author.setId(1L);
        author.setName("Author One");
        author.setNationality("CountryA");

        when(magazineRepository.existsMagazineByIssueNumber(101)).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));

        Magazine savedMagazine = MagazineMapper.toEntity(request, List.of(author));

        when(magazineRepository.save(any(Magazine.class))).thenReturn(savedMagazine);

        // Act
        MagazineDTO result = magazineService.addMagazine(request);

        // Assert
        assertNotNull(result);
        assertEquals(101, result.getIssueNumber());
        assertEquals("Monthly Tech", result.getTitle());
        assertEquals(1, result.getAuthors().size());
        assertEquals("Author One", result.getAuthors().get(0).getName());

        verify(magazineRepository).existsMagazineByIssueNumber(101);
        verify(authorRepository).findById(1L);
        verify(magazineRepository).save(any(Magazine.class));
    }

    @Test
    void addMagazine_shouldThrow_whenIssueNumberExists() {
        // Arrange
        MagazineDTO request = new MagazineDTO(null,101, "Monthly Tech", LocalDate.of(2025, 7, 1), List.of());

        when(magazineRepository.existsMagazineByIssueNumber(101)).thenReturn(true);

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> magazineService.addMagazine(request));
        assertTrue(ex.getMessage().contains("already exists"));

        verify(magazineRepository).existsMagazineByIssueNumber(101);
        verifyNoMoreInteractions(authorRepository, magazineRepository);
    }

    @Test
    void addMagazine_shouldThrow_whenAuthorNotFound() {
        // Arrange
        AuthorBasicDTO authorDto = new AuthorBasicDTO(1L, null, null);
        MagazineDTO request = new MagazineDTO(null,101, "Monthly Tech", LocalDate.of(2025, 7, 1), List.of(authorDto));

        when(magazineRepository.existsMagazineByIssueNumber(101)).thenReturn(false);
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class, () -> magazineService.addMagazine(request));
        assertTrue(ex.getMessage().contains("Author not found"));

        verify(magazineRepository).existsMagazineByIssueNumber(101);
        verify(authorRepository).findById(1L);
        verifyNoMoreInteractions(magazineRepository);
    }

    @Test
    void getAllMagazines_shouldReturnListOfMagazineDTO() {
        // Arrange
        Author author1 = new Author();
        author1.setId(1L);
        author1.setName("Author One");
        author1.setNationality("CountryA");

        Author author2 = new Author();
        author2.setId(2L);
        author2.setName("Author Two");
        author2.setNationality("CountryB");

        Magazine mag1 = new Magazine();
        mag1.setIssueNumber(101);
        mag1.setTitle("Monthly Tech");
        mag1.setPublicationDate(LocalDate.of(2025, 7, 1));
        mag1.setAuthors(List.of(author1));

        Magazine mag2 = new Magazine();
        mag2.setIssueNumber(102);
        mag2.setTitle("Weekly News");
        mag2.setPublicationDate(LocalDate.of(2025, 7, 5));
        mag2.setAuthors(List.of(author1, author2));

        when(magazineRepository.findAll()).thenReturn(List.of(mag1, mag2));

        // Act
        List<MagazineDTO> results = magazineService.getAllMagazines();

        // Assert
        assertEquals(2, results.size());

        MagazineDTO result1 = results.get(0);
        assertEquals(101, result1.getIssueNumber());
        assertEquals("Monthly Tech", result1.getTitle());
        assertEquals(1, result1.getAuthors().size());
        assertEquals("Author One", result1.getAuthors().get(0).getName());

        MagazineDTO result2 = results.get(1);
        assertEquals(102, result2.getIssueNumber());
        assertEquals("Weekly News", result2.getTitle());
        assertEquals(2, result2.getAuthors().size());
        assertEquals("Author One", result2.getAuthors().get(0).getName());
        assertEquals("Author Two", result2.getAuthors().get(1).getName());

        verify(magazineRepository).findAll();
    }
}