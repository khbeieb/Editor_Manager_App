package org.mobelite.editormanager.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@DiscriminatorValue("BOOK")
public class Book extends Publication {
    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotNull(message = "Author is required")
    @ManyToOne
    @JoinColumn(name = "author_id")
    @JsonBackReference
    private Author author;
}
