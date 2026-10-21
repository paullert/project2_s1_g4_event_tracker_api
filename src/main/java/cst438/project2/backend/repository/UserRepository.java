package cst438.project2.backend.repository;

import cst438.project2.backend.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {

    /** Finds the account represented by the subject in a Google JWT. */
    Optional<User> findByGoogleSubject(String googleSubject);

    /** Finds a user by their username/email value. */
    Optional<User> findByUsername(String username);
}
