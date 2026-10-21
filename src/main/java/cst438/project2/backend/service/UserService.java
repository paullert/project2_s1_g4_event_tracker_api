package cst438.project2.backend.service;

import cst438.project2.backend.dto.user.UserResponse;
import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.UserRepository;
import cst438.project2.backend.dto.user.UserRequest;
import cst438.project2.backend.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
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

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(Pageable pageable) {
      return userRepository.findAll(pageable).map(UserResponse::init);
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
      return userRepository.findById(userId)
          .orElseThrow(() -> new NotFoundException(userId, "User"));
    }

    @Transactional
    public User updateUser(Long userId, UserRequest patch) {
      if (patch == null || patch.role() == null) {
        throw new IllegalArgumentException("role is required");
      }

      User user = getUser(userId);
      user.setRole(patch.role());
      return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
      User user = getUser(userId);
      userRepository.delete(user);
    }

    public User findUserFromAuthentication(Authentication authentication) {
      if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
        throw new IllegalArgumentException("Authentication is not a JWT");
      }
      String googleSubject = jwtAuth.getToken().getSubject();

      return userRepository.findByGoogleSubject(googleSubject).orElseThrow(
          () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
      );
    }
}
