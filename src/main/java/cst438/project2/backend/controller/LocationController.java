package cst438.project2.backend.controller;

import cst438.project2.backend.dto.CreationResponse;
import cst438.project2.backend.dto.location.LocationCreateRequest;
import cst438.project2.backend.dto.location.LocationResponse;
import cst438.project2.backend.model.Location;
import cst438.project2.backend.model.User;
import cst438.project2.backend.service.LocationService;
import cst438.project2.backend.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
  public Page<LocationResponse> getLocations(
      @PageableDefault(
          size = 20,
          sort = "updatedAt",
          direction = Sort.Direction.DESC
      )
      Pageable pageable
  ) {
    return locationService.getLocations(pageable).map(LocationResponse::init);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreationResponse<LocationResponse> createLocation(@RequestBody LocationCreateRequest locationReq, Authentication userAuth) {
    User creator = userService.findUserFromAuthentication(userAuth);
    Location location = locationService.createLocation(locationReq, userAuth);
    return new CreationResponse<>(
        "Successful",
        "Location Created",
        String.format("/api/v1/locations/%s", location.getLocationId()),
        LocationResponse.init(location)
    );
  }
}