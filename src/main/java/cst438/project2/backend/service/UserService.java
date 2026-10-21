package cst438.project2.backend.service;

import cst438.project2.backend.dto.user.RegisterRequest;
import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.dto.user.selfUpdateRequest;
import cst438.project2.backend.model.User;
import cst438.project2.backend.model.UserRole;
import cst438.project2.backend.repository.UserRepository;
import cst438.project2.backend.dto.user.UserRequest;
import cst438.project2.backend.exceptions.NotFoundException;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
      return userRepository.findAll(pageable).map(UserResponse::init);
    }


    @Transactional
    public User registerUser(Jwt jwt, String username) {
      // Check if user already exists, null if not
      User existing = userRepository
          .findByGoogleSubject(jwt.getSubject())
          .orElse(null);

      // If user already exists, return existing user
      if (existing != null) {
        return existing;
      }

      // If invalid username, throw error
      if (username == null || username.isBlank() || username.trim().length() > 100) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid username, must be between 1 and 100 characters long (not including leading spaces)");
      }

      String trimName = username.trim();
      existing = userRepository.findByUsername(trimName).orElse(null);
      if ( existing != null ) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
      }
      User newUser = new User();
      newUser.setUsername(trimName);
      newUser.setGoogleSubject(jwt.getSubject());
      newUser.setRole(UserRole.USER);
      return userRepository.save(newUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public User getUser(Long userId) {
      return userRepository.findById(userId)
          .orElseThrow(() -> new NotFoundException(userId, "User"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public UserResponse createUser(UserRequest request) {
      User user = new User();
      user.setUsername(request.username());
      user.setEmail(request.email());
      user.setRole(request.role());
      user.setGoogleSubject(request.subject());
      return UserResponse.init(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public User updateUser(Long userId, UserRequest patch) {
      if (patch == null || patch.role() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields: role");
      }

      User user = getUser(userId);
      user.setRole(patch.role());
      user.setUsername(patch.username());
      user.setEmail(patch.email());
      user.setGoogleSubject(patch.subject());
      return userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteUser(Long userId) {
      User user = getUser(userId);
      userRepository.delete(user);
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    @Transactional
    public void deleteMe(Authentication authentication) {
      User user = findUserFromAuthentication(authentication);
      userRepository.delete(user);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public User findUserFromAuthentication(Authentication authentication) {
      if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
        throw new IllegalArgumentException("Authentication is not a JWT");
      }
      String googleSubject = jwtAuth.getToken().getSubject();

      return userRepository.findByGoogleSubject(googleSubject).orElseThrow(
          () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
      );
    }

    @PreAuthorize("hasAnyRole('USER', 'MANAGER')")
    @Transactional
    public User updateMe(Authentication auth, selfUpdateRequest request) {
      User user = findUserFromAuthentication(auth);
      if (request.username() != null) {
        user.setUsername(request.username());
      }
      if (request.email() != null) {
        user.setEmail(request.email());
      }
      return userRepository.save(user);
    }
}
