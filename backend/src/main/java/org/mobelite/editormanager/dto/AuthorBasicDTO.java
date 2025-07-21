package org.mobelite.editormanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public class AuthorBasicDTO {
    @NonNull
    private Long id;

    private String name;

    private String nationality;
}
