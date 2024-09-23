package sa.elm.demo.models.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

public class SecurityUser extends User {


  public SecurityUser(String id, boolean enabled, Collection<? extends GrantedAuthority> authorities) {
    super(id, "", true, true, true, true, authorities);
  }

  public Long getId() {
    return Long.valueOf(this.getUsername());
  }

}
