package com.fitnexus.server.service.auth;

import com.fitnexus.server.entity.auth.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface UserService extends UserDetailsService {

    int createVerificationToken(AuthUser user);

    HttpStatus checkVerificationToken(String token);

    void resendEmailToken(String existingToken);
}
