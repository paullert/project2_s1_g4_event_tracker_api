package cst438.project2.backend.service;

import cst438.project2.backend.dto.location.LocationCreate;
import cst438.project2.backend.dto.location.LocationPatch;
import cst438.project2.backend.exceptions.BadRequestException;
import cst438.project2.backend.exceptions.NotFoundException;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.LocationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocationService {


  private final LocationRepository locationRepository;
  private final UserService userService;

  public LocationService(LocationRepository locationRepository, UserService userService) {
    this.locationRepository = locationRepository;
    this.userService = userService;
  }


  @Transactional(readOnly = true)
  public Location getLocationById(Long locationId) {
    return locationRepository.findById(locationId).orElseThrow(
        () -> new NotFoundException(locationId, "Location")
    );
  }

  @Transactional(readOnly = true)
  public Page<Location> getLocations(Pageable pageable) {
    if (pageable.getPageSize() <= 0) {
      throw new IllegalArgumentException("Size must be greater than 0");
    }
    if (pageable.getPageNumber() < 0) {
      throw new IllegalArgumentException("Page must be greater than or equal to 0");
    }

    return locationRepository.findAll(pageable);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public Location createLocation(LocationCreate locationReq, Authentication userAuth) {
    if (locationReq.locationName() == null || locationReq.address() == null) {
      throw new BadRequestException("Missing required fields: locationName, address");
    }
    User creator = userService.findUserFromAuthentication(userAuth);
    Location location = new Location(
        null,
        locationReq.locationName(),
        locationReq.address(),
        locationReq.locationDescription(),
        creator,
        null,
        null
    );
    return locationRepository.save(location);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public Location patchLocation(Location location, LocationPatch reqBody) {
    if (reqBody.locationName() == null && reqBody.address() == null && reqBody.locationDescription() == null) {
      throw new BadRequestException("Provide at least one location field to change");
    }
    if (reqBody.locationName() != null) {
      location.setLocationName(reqBody.locationName());
    }
    if (reqBody.address() != null) {
      location.setAddress(reqBody.address());
    }
    if (reqBody.locationDescription() != null) {
      location.setLocationDescription(reqBody.locationDescription());
    }

    return locationRepository.save(location);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public Location putLocation(Location location, LocationCreate reqBody) {
    if (reqBody.locationName() == null || reqBody.address() == null) {
      throw new BadRequestException("Missing 1 or more required fields: locationName, address");
    }
    location.setAddress(reqBody.address());
    location.setLocationDescription(reqBody.locationDescription());
    location.setLocationName(reqBody.locationName());
    return locationRepository.save(location);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  public void deleteLocation(Location location) {
    locationRepository.delete(location);
  }
}
