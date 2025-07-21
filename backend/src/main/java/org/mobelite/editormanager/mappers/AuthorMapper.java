package org.mobelite.editormanager.mappers;

import org.mobelite.editormanager.dto.AuthorDTO;
import org.mobelite.editormanager.entities.Author;

public class AuthorMapper {
    public static AuthorDTO toDTO(Author entity) {
        return new AuthorDTO(
                entity.getId(),
                entity.getName(),
                entity.getBirthDate(),
                entity.getNationality(),
                entity.getBooks()
        );
    }

    public static Author toEntity(AuthorDTO dto) {
        Author author = new Author();
        author.setName(dto.getName());
        author.setBirthDate(dto.getBirthDate());
        author.setNationality(dto.getNationality());
        author.setBooks(dto.getBooks());
        return author;
    }
}
