package sa.elm.demo.security.filters;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.UserStatusEnum;
import sa.elm.demo.models.security.SecurityUser;
import sa.elm.demo.service.UsersService;
import sa.elm.demo.util.JwtUtil;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  public static final String AUTHORIZATION_HEADER = "Authorization";

  private final UsersService usersService;
  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader(AUTHORIZATION_HEADER);

    if (StringUtils.isEmpty(authHeader) || (!StringUtils.isEmpty(authHeader) && !authHeader.startsWith("Bearer "))) {
      filterChain.doFilter(request, response);
      return;
    }

    String jwt = authHeader.substring(7);
    if (jwtUtil.validateToken(jwt)) {

      String idAsString = jwtUtil.extractClaim(jwt, Claims::getSubject);

      User user = usersService.findUserById(Long.valueOf(idAsString));
      SecurityUser securityUser = map(user);
      UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
          securityUser,
          null,
          securityUser.getAuthorities()
      );

      SecurityContextHolder.getContext().setAuthentication(token);
    }

    filterChain.doFilter(request, response);
  }

  public SecurityUser map(User entity) {
    return new SecurityUser(String.valueOf(entity.getId()),
        entity.getUserStatus() == UserStatusEnum.ACTIVE,
        List.of(new SimpleGrantedAuthority(entity.getUserRole().name())));
  }


}
