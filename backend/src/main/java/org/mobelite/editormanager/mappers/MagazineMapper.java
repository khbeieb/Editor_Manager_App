package org.mobelite.editormanager.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mobelite.editormanager.dto.AuthorBasicDTO;
import org.mobelite.editormanager.dto.MagazineDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Magazine;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MagazineMapper {
    public static MagazineDTO toDTO(Magazine magazine) {
        if (magazine == null) return null;

        List<AuthorBasicDTO> authors = magazine.getAuthors().stream()
                .map(author -> new AuthorBasicDTO(
                        author.getId(),
                        author.getName(),
                        author.getNationality()
                ))
                .toList();

        return new MagazineDTO(
                magazine.getId(),
                magazine.getIssueNumber(),
                magazine.getTitle(),
                magazine.getPublicationDate(),
                authors
        );
    }

    public static Magazine toEntity(MagazineDTO dto, List<Author> authors) {
        Magazine magazine = new Magazine();
        magazine.setIssueNumber(dto.getIssueNumber());
        magazine.setTitle(dto.getTitle());
        magazine.setPublicationDate(dto.getPublishedDate());
        magazine.setAuthors(authors);
        return magazine;
    }
}
