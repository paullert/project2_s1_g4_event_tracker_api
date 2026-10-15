package cst438.project2.backend.repository;

import cst438.project2.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;


public interface UserRepository extends JpaRepository<User, Long> {
    //the spring data jpa can automatically implement the method

  // Gets All users
  // Page<User> findAll(Pageable page); // Provided by default by JpaRepository

    // Gets 1 user given the id
    User findByUserId(Long userId);

    // TODO: Address database lack of email storage, used in Oauth2
    User findByUsername(String email);
}
