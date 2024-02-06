package com.fitnexus.server.repository.auth;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.auth.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    EmailVerificationToken findByToken(String token);
    EmailVerificationToken findByAuthUser(AuthUser authUser);

}
