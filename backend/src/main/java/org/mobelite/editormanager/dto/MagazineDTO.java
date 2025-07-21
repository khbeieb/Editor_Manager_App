package org.mobelite.editormanager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class MagazineDTO {
    private Long id;
    private int issueNumber;
    private String title;
    private LocalDate publishedDate;
    private List<AuthorBasicDTO> authors;
}
