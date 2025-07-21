package org.mobelite.editormanager.mappers;

import org.mobelite.editormanager.dto.PublicationDTO;
import org.mobelite.editormanager.entities.Book;
import org.mobelite.editormanager.entities.Magazine;
import org.mobelite.editormanager.entities.Publication;
import org.mobelite.editormanager.enums.PublicationType;

public class PublicationMapper {
    public static PublicationDTO toDTO(Publication publication) {
        PublicationType type;
        if (publication instanceof Book) {
            type = PublicationType.BOOK;
        } else if (publication instanceof Magazine) {
            type = PublicationType.MAGAZINE;
        } else {
            type = PublicationType.UNKNOWN;
        }

        return new PublicationDTO(
                publication.getId(),
                type,
                publication.getTitle(),
                publication.getPublicationDate()
        );
    }
}