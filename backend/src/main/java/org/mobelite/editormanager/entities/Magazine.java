package org.mobelite.editormanager.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@DiscriminatorValue("MAGAZINE")
public class Magazine extends Publication{

    @Min(value = 1, message = "Issue number must be at least 1")
    private int issueNumber;

    @NotEmpty(message = "At least one author is required")
    @Valid
    @ManyToMany
    @JoinTable(
            name = "magazine_author",
            joinColumns = @JoinColumn(name = "magazine_id"),
            inverseJoinColumns = @JoinColumn(name = "author_id")
    )
    private List<Author> authors;
}
