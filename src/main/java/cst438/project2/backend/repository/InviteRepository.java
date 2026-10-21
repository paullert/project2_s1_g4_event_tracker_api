package cst438.project2.backend.repository;
import cst438.project2.backend.model.Invite;
import java.util.List;
import java.util.Optional;

import cst438.project2.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InviteRepository extends JpaRepository<Invite, Long> {
    //the spring data jpa can automatically implement the method
    //gets all invitation records given the userid
    Page<Invite> findByUserId(Long userId, Pageable pageable);

    long countByEventIdAndStatus(Long eventId, String status);

    Page<Invite> findByEventId(Long eventId, Pageable pageable);

    Invite findByEventIdAndUserId(Long userId, Long eventId);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);
}
