package cst438.project2.backend.repository;


import cst438.project2.backend.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


public interface EventRepository extends JpaRepository<Event, Long> {
    @Query(value = "select coalesce(arc_event_size, 0) from archived_events where arc_event_id = :eventId",
        nativeQuery = true)
    Optional<Long> findArchivedAttendeeCount(@Param("eventId") Long eventId);
}
