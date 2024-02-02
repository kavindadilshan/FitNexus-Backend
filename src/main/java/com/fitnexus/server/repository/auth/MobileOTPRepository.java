package com.fitnexus.server.repository.auth;

import com.fitnexus.server.entity.auth.MobileOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MobileOTPRepository extends JpaRepository<MobileOtp, String> {
}
