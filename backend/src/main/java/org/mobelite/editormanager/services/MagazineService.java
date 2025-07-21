package org.mobelite.editormanager.services;

import lombok.AllArgsConstructor;
import org.mobelite.editormanager.dto.AuthorBasicDTO;
import org.mobelite.editormanager.dto.MagazineDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Magazine;
import org.mobelite.editormanager.entities.Publication;
import org.mobelite.editormanager.mappers.MagazineMapper;
import org.mobelite.editormanager.repositories.AuthorRepository;
import org.mobelite.editormanager.repositories.MagazineRepository;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@AllArgsConstructor
public class MagazineService {
    private final MagazineRepository magazineRepository;
    private final AuthorRepository authorRepository;

    public MagazineDTO addMagazine(MagazineDTO request) {
        if (magazineRepository.existsMagazineByIssueNumber(request.getIssueNumber())) {
            throw new RuntimeException("Magazine with issue number " + request.getIssueNumber() + " already exists");
        }

        // Validate and fetch complete Author entities from DB
        List<Author> realAuthors = request.getAuthors().stream()
                .map(author -> authorRepository.findById(author.getId())
                        .orElseThrow(() -> new RuntimeException("Author not found with ID: " + author.getId())))
                .toList();

        Magazine magazine = MagazineMapper.toEntity(request, realAuthors);

        Magazine savedMagazine = magazineRepository.save(magazine);

        return MagazineMapper.toDTO(savedMagazine);
    }

    public List<MagazineDTO> getAllMagazines() {
        return magazineRepository.findAll().stream()
                .map(MagazineMapper::toDTO)
                .toList();
    }
}
