package org.mobelite.editormanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.mobelite.editormanager.entities.Book;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class AuthorDTO {
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;

    @NotBlank(message = "Nationality is required")
    private String nationality;

    private List<Book> books;
}
