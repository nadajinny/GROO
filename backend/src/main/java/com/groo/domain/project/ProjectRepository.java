package com.groo.domain.project;

import com.groo.domain.project.ProjectStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByGroupIdOrderByCreatedAtDesc(Long groupId);

    @Query("""
            SELECT p FROM Project p
            WHERE p.group.id = :groupId
            AND (:status IS NULL OR p.status = :status)
            AND (
                :keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            """)
    Page<Project> searchProjects(
            @Param("groupId") Long groupId,
            @Param("status") ProjectStatus status,
            @Param("keyword") String keyword,
            Pageable pageable);

    long countByStatus(ProjectStatus status);
}
