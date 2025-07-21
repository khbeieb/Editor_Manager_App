package org.mobelite.editormanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.mobelite.editormanager.enums.PublicationType;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PublicationDTO {
    private Long id;

    private PublicationType type;

    private String title;

    private LocalDate publicationDate;

}
