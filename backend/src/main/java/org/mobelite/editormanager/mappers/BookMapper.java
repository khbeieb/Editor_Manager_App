package org.mobelite.editormanager.mappers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.mobelite.editormanager.dto.AuthorBasicDTO;
import org.mobelite.editormanager.dto.BookDTO;
import org.mobelite.editormanager.entities.Author;
import org.mobelite.editormanager.entities.Book;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookMapper {
    public static BookDTO toDTO(Book book) {
        if (book == null || book.getAuthor() == null) {
            return null;
        }

        Author author = book.getAuthor();

        return new BookDTO(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                new AuthorBasicDTO(
                        author.getId(),
                        author.getName(),
                        author.getNationality()
                ),
                book.getPublicationDate()
        );
    }

    public static Book toEntity(BookDTO dto, Author author) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setIsbn(dto.getIsbn());
        book.setPublicationDate(dto.getPublicationDate());
        book.setAuthor(author);
        return book;
    }
}
