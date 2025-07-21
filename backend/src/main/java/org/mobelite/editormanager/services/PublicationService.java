package org.mobelite.editormanager.services;

import lombok.AllArgsConstructor;
import org.mobelite.editormanager.dto.PublicationDTO;
import org.mobelite.editormanager.entities.Publication;
import org.mobelite.editormanager.enums.PublicationType;
import org.mobelite.editormanager.mappers.PublicationMapper;
import org.mobelite.editormanager.repositories.BookRepository;
import org.mobelite.editormanager.repositories.MagazineRepository;
import org.mobelite.editormanager.repositories.PublicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PublicationService {
    private final PublicationRepository publicationRepository;
    private final MagazineRepository magazineRepository;
    private final BookRepository bookRepository;

    public Page<PublicationDTO> getPublications(Pageable pageable) {
        Page<Publication> publications = publicationRepository.findAll(pageable);
        return publications.map(PublicationMapper::toDTO);
    }

    public List<PublicationDTO> searchByTitle(String title) {
        List<PublicationDTO> books = bookRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(PublicationMapper::toDTO)
                .toList();

        List<PublicationDTO> magazines = magazineRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(PublicationMapper::toDTO)
                .toList();

        List<PublicationDTO> results = new ArrayList<>();
        results.addAll(books);
        results.addAll(magazines);
        return results;
    }
}
