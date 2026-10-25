
package com.vaultsphere.repository;

import com.vaultsphere.model.StoredDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository
        extends JpaRepository<StoredDocument, Long> {

    List<StoredDocument> findByOwnerUsernameOrderByUploadedAtDesc(
            String username
    );

    List<StoredDocument> findAllByOrderByUploadedAtDesc();

    long countByOwnerUsername(String username);

    Optional<StoredDocument> findByIdAndOwnerUsername(
            Long id,
            String username
    );

    boolean existsByIdAndOwnerUsername(
            Long id,
            String username
    );
}
