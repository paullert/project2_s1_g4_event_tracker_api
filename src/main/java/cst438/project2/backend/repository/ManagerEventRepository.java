package cst438.project2.backend.repository;

import cst438.project2.backend.model.ManagerEvent;
import cst438.project2.backend.model.ManagerEventId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ManagerEventRepository extends JpaRepository<ManagerEvent, ManagerEventId> {
  @Modifying
  @Query("delete from ManagerEvent ownership where ownership.id.eventId = :eventId")
  void deleteForEvent(@Param("eventId") Long eventId);
}
