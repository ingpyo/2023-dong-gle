package org.donggle.backend.application.repository;

import org.donggle.backend.domain.writing.Block;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {
    @EntityGraph(attributePaths = {"content"})
    List<Block> findAllByWritingId(final Long writingId);

    @Override
    @EntityGraph(attributePaths = {"content"})
    Optional<Block> findById(final Long id);
}
