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

    Optional<AuthUser> findByPublicUsername(String username);

    Optional<AuthUser> findByEmail(String email);

    Optional<AuthUser> findByMobile(String mobile);

    @Query(value = "SELECT u FROM AuthUser u " +
            "LEFT JOIN u.instructor i " +
            "LEFT JOIN u.trainer t " +
            "LEFT JOIN InstructorBusinessProfile ibp ON ibp.instructor = i " +
            "LEFT JOIN TrainerBusinessProfile tbp ON tbp.trainer = t " +
            "WHERE (i IS NOT NULL OR t IS NOT NULL) " +
            "AND (u.email LIKE %:data% OR u.firstName LIKE %:data% OR u.lastName LIKE %:data% OR u.mobile LIKE %:data%) " +
            "AND (:businessProfileId IS NULL OR ibp.businessProfile.id = :businessProfileId OR tbp.businessProfile.id = :businessProfileId) " +
            "GROUP BY u.id ")
    Page<AuthUser> searchUser(@Param("data") String data, @Param("businessProfileId") Long businessProfileId, Pageable pageable);

    @Query(value = "SELECT new com.fitnexus.server.dto.coach.CoachDetailsResponse(a.id, a.firstName, a.lastName, a.publicUsername, a.image, " +
            "((COALESCE(t.rating * t.ratingCount, 0) + COALESCE(t.physicalClassRating * t.physicalClassRatingCount, 0) + COALESCE(i.rating * i.ratingCount, 0)) / " +
            "CASE WHEN (COALESCE(t.ratingCount, 0) + COALESCE(t.physicalClassRatingCount, 0) + COALESCE(i.ratingCount, 0) < 1) THEN 1 " +
            "ELSE (COALESCE(t.ratingCount, 0) + COALESCE(t.physicalClassRatingCount, 0) + COALESCE(i.ratingCount, 0)) END ), " +
            "COALESCE(t.ratingCount, 0) + COALESCE(t.physicalClassRatingCount, 0) + COALESCE(i.ratingCount, 0)) " +
            "FROM AuthUser a LEFT JOIN a.instructor i LEFT JOIN a.trainer t " +
            "LEFT JOIN InstructorBusinessProfile ibp ON ibp.instructor = i " +
            "JOIN TrainerBusinessProfile tbp ON tbp.trainer = t " +
            "WHERE a.status = com.fitnexus.server.enums.UserStatus.ACTIVE " +
            "AND (:gender IS NULL OR a.gender = :gender) " +
            "AND (t.status = :status OR i.status = :status) " +
            "AND (ibp IN :instructorBusinessProfiles OR tbp IN :trainerBusinessProfiles) " +
            "GROUP BY a.id " +
            "ORDER BY ((COALESCE(t.rating * t.ratingCount, 0) + COALESCE(t.physicalClassRatingCount, 0) + COALESCE(i.rating * i.ratingCount, 0)) / " +
            "CASE WHEN (COALESCE(t.ratingCount, 0) + COALESCE(t.physicalClassRatingCount, 0) + COALESCE(i.ratingCount, 0) < 1) THEN 1 " +
            "ELSE (COALESCE(t.ratingCount, 0) + COALESCE(t.physicalClassRatingCount, 0) + COALESCE(i.ratingCount, 0)) END ) DESC " +
            "")
    Page<CoachDetailsResponse> getAllByGenderAndStatusAndBusinessProfilesIn(@Param("gender") Gender gender,
                                                                            @Param("status") CoachStatus status,
                                                                            @Param("instructorBusinessProfiles")
                                                                                    List<InstructorBusinessProfile> instructorBusinessProfiles,
                                                                            @Param("trainerBusinessProfiles")
                                                                                    List<TrainerBusinessProfile> trainerBusinessProfiles,
                                                                            Pageable pageable);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByMobile(String mobile);
}
