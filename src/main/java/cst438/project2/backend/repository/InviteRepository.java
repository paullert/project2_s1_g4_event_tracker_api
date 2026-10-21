package cst438.project2.backend.repository;
import cst438.project2.backend.model.Invite;
import java.util.List;
import java.util.Optional;

import cst438.project2.backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import cst438.project2.backend.model.InviteId;


public interface InviteRepository extends JpaRepository<Invite, InviteId> {
    //the spring data jpa can automatically implement the method
    //gets all invitation records given the userid
    Page<Invite> findByUser_UserId(Long userId, Pageable pageable);

    long countByEvent_IdAndStatus(Long eventId, String status);

    Page<Invite> findByEvent_Id(Long eventId, Pageable pageable);

    Optional<Invite> findByUser_UserIdAndEvent_Id(Long userId, Long eventId);

    boolean existsByUser_UserIdAndEvent_Id(Long userId, Long eventId);
}
