package cst438.project2.backend.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import cst438.project2.backend.model.Event;


public interface EventRepository extends JpaRepository<Event, Long> {
    // TODO: Implement the methods
}
