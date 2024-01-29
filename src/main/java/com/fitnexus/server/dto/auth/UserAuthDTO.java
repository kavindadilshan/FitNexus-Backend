package com.fitnexus.server.dto.auth;

import com.fitnexus.server.enums.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;


@Getter
@Setter
@ToString(callSuper = true)
public class UserAuthDTO extends User implements UserDetails {

    private long userId;
    private UserStatus status;
    private CommonUserAuth userDetails;

    public UserAuthDTO(long userId, String username, String password, List<SimpleGrantedAuthority> authorities,
                       UserStatus status, CommonUserAuth userDetails) {
        super(username, password, authorities);
        this.userId = userId;
        this.status = status;
        this.userDetails = userDetails;
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public String getPassword() {
        return super.getPassword();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
}
