package cst438.project2.backend.service;

import cst438.project2.backend.dto.location.LocationCreateRequest;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.LocationRepository;
import cst438.project2.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class LocationService {


  private final LocationRepository locationRepository;
  private final UserService userService;

  public LocationService(LocationRepository locationRepository, UserService userService) {
    this.locationRepository = locationRepository;
    this.userService = userService;
  }

  public Page<Location> getLocations(Pageable pageable) {
    if (pageable.getPageSize() <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    if (pageable.getPageNumber() < 0) {
      throw new IllegalArgumentException("Page must be greater than or equal to 0");
    }

    System.out.println(pageable.getSort()); // TODO: remove

    return locationRepository.findAll(pageable);
  }

  public Location createLocation(LocationCreateRequest locationReq, Authentication userAuth) {
    User creator = userService.findUserFromAuthentication(userAuth);
    Location location = new Location(
        locationReq.locationName(),
        locationReq.address(),
        locationReq.locationDescription(),
        creator
    );
    return locationRepository.save(location);
  }
}
