package cst438.project2.backend.security;


import cst438.project2.backend.model.User;
import cst438.project2.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;


@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final UserRepository userRepository;

  public JwtAuthConverter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    String googleSubject = jwt.getSubject();

    User user = userRepository.findByGoogleSubject(googleSubject);

    if (user == null) {
      throw new BadCredentialsException("Google user is not registered");
    }

    List<GrantedAuthority> authorities = new ArrayList<>();

    if (user.getRole() != null) {
      authorities.add(
          new SimpleGrantedAuthority("ROLE_" + user.getRole())
      );
    }

    return new JwtAuthenticationToken(
        jwt,
        authorities,
        googleSubject
    );
  }
}
