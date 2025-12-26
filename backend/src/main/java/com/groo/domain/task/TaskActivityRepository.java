package com.groo.domain.task;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskActivityRepository extends JpaRepository<TaskActivity, Long> {
    List<TaskActivity> findTop50ByTaskIdOrderByCreatedAtDesc(Long taskId);
}
