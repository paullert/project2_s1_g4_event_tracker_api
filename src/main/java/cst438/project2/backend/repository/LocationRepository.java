package cst438.project2.backend.repository;

import cst438.project2.backend.model.Invite;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LocationRepository extends JpaRepository<Location, Long> {
    //the spring data jpa can automatically implement the method

  // Gets All users
  // Page<Location> findAll(Pageable page); // Provided by default by JpaRepository

    // Gets 1 location given the id
    Location findByLocationId(Long locationId);

    List<Invite> findByEventId(Long eventId);

    Optional<Invite> findByIdAndUserId(Long inviteId, Long userId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
