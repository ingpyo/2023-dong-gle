package org.donggle.backend.application.repository;

import org.donggle.backend.domain.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    Optional<Blog> findByName(String blogName);
}
