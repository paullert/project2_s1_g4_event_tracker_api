package cst438.project2.backend.controller;

import cst438.project2.backend.dto.location.LocationCreate;
import cst438.project2.backend.dto.location.LocationPatch;
import cst438.project2.backend.dto.location.LocationResponse;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import cst438.project2.backend.service.LocationService;
import cst438.project2.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.net.URI;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

  private final LocationService locationService;
  private final UserService userService;

  public LocationController(LocationService locationService, UserService userService) {
    this.locationService = locationService;
    this.userService = userService;
  }

  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public PagedModel<LocationResponse> getLocations(
      @PageableDefault(
          size = 20,
          sort = "updatedAt",
          direction = Sort.Direction.DESC
      )
      Pageable pageable
  ) {
    return new PagedModel<>(locationService.getLocations(pageable).map(LocationResponse::init));
  }

  @PostMapping
  public ResponseEntity<LocationResponse> createLocation(
      @RequestBody LocationCreate locationReq,
      Authentication userAuth) {
    Location location = locationService.createLocation(locationReq, userAuth);
    return ResponseEntity.created(URI.create("/api/v1/locations/" + location.getLocationId()))
        .body(LocationResponse.init(location));
  }

  @GetMapping("/{locationId}")
  public LocationResponse getLocation(@PathVariable Long locationId) {
    return LocationResponse.init(locationService.getLocationById(locationId));
  } // TODO: Test get request

  @PutMapping("/{locationId}")
  public LocationResponse updateLocation(
      @PathVariable Long locationId,
      @RequestBody LocationCreate locationReq
  ) {
    Location location = locationService.getLocationById(locationId);
    location = locationService.putLocation(location, locationReq);
    return LocationResponse.init(location);
  }

  @PatchMapping("/{locationId}")
  public LocationResponse patchLocation(
      @PathVariable Long locationId,
      @RequestBody LocationPatch locationPatch
  ) {
    Location location = locationService.getLocationById(locationId);
    location = locationService.patchLocation(location, locationPatch);
    return LocationResponse.init(location);
  }

  @DeleteMapping("/{locationId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteLocation(@PathVariable Long locationId) {
    Location location = locationService.getLocationById(locationId);
    locationService.deleteLocation(location);
  }
}
