package org.mobelite.editormanager.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
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

    @PreRemove
    private void removeBookFromAuthor() {
      if (author != null) {
        author.getBooks().remove(this);
      }
    }
}
