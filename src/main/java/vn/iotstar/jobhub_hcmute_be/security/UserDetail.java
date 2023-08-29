package vn.iotstar.jobhub_hcmute_be.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.iotstar.jobhub_hcmute_be.entity.Permission;
import vn.iotstar.jobhub_hcmute_be.entity.User;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class UserDetail implements UserDetails {

    private static final long serialVersionUID = 1L;

    private User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        for(Permission permission:user.getRole().getPermissions()){
            authorities.add(new SimpleGrantedAuthority(permission.getName()));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_"+user.getRole().getName()));
        return authorities;
    }

    public String getUserId() {
        return user.getUserId();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
