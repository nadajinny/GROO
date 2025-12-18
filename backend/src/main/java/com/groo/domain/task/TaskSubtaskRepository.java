package com.groo.domain.task;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskSubtaskRepository extends JpaRepository<TaskSubtask, Long> {
    List<TaskSubtask> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}
