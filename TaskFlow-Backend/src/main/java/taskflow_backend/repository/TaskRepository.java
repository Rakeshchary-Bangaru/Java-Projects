package taskflow_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import taskflow_backend.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}