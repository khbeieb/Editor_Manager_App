package org.mobelite.editormanager.repositories;

import org.mobelite.editormanager.entities.Magazine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MagazineRepository extends JpaRepository<Magazine, Integer> {

    boolean existsMagazineByIssueNumber(int issueNumber);
    
    List<Magazine> findMagazineByIssueNumber(int issueNumber);

    List<Magazine> findByTitleContainingIgnoreCase(String title);
    
}
