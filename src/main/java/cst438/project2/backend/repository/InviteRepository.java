package cst438.project2.backend.repository;
import cst438.project2.backend.model.Invite;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InviteRepository extends JpaRepository<Invite, Long> {
    //the spring data jpa can automatically implement the method
    //gets all invitation records given the userid
    List<Invite> findByUserId(Long userId);
}
