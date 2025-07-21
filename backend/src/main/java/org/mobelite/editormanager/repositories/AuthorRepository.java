package org.mobelite.editormanager.repositories;

import org.mobelite.editormanager.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Optional<Author> findAuthorByName(String name);
}
