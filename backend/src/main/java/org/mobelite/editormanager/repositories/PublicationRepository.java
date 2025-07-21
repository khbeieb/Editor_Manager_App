package org.mobelite.editormanager.repositories;

import org.mobelite.editormanager.entities.Publication;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PublicationRepository extends JpaRepository<Publication, Long> {
}