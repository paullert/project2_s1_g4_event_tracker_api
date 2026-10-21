package cst438.project2.backend.repository;

import cst438.project2.backend.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;


public interface LocationRepository extends JpaRepository<Location, Long> {
    //the spring data jpa can automatically implement the method

  // Gets All users
  // Page<Location> findAll(Pageable page); // Provided by default by JpaRepository

    // Gets 1 location given the id
    Location findByLocationId(Long locationId);


//    // TODO: Address database lack of email storage, used in Oauth2
//    User findByUsername(String email);
}
