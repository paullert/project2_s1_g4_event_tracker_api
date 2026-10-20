package cst438.project2.backend.service;

import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    // Injects the default page size from application.properties
    @Value("${app.pagination.default-page-size}")
    private int PAGE_SIZE;

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Page<User> getUsers(int page){
      Pageable pageable = PageRequest.of(
          page,
          PAGE_SIZE,
          Sort.by("username").ascending()
      );

      return userRepository.findAll(pageable);
    }

    public User findUserFromAuthentication(Authentication authentication) {
      if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
        throw new IllegalArgumentException("Authentication is not a JWT");
      }

      String googleSubject = jwtAuth.getToken().getSubject();

      return userRepository.findByGoogleSubject(googleSubject);
    }
}
