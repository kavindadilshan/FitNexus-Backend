package com.fitnexus.server.repository.auth;

import com.fitnexus.server.dto.coach.CoachDetailsResponse;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.instructor.InstructorBusinessProfile;
import com.fitnexus.server.entity.trainer.TrainerBusinessProfile;
import com.fitnexus.server.enums.CoachStatus;
import com.fitnexus.server.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByUsername(String username);

}
