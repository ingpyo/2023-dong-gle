package org.donggle.backend.application.repository;

import org.donggle.backend.domain.BlogWriting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogWritingRepository extends JpaRepository<BlogWriting, Long> {
    List<BlogWriting> findByWritingId(Long writingId);
}
