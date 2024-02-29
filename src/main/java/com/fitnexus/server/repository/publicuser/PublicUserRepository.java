package com.fitnexus.server.repository.publicuser;

import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.enums.UserVerificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface PublicUserRepository extends JpaRepository<PublicUser, Long> {
    PublicUser findByMobile(String mobile);

    @Query(value =
            "SELECT p FROM InstructorPackageEnroll ipe " +
                    "JOIN PublicUser p ON ipe.publicUser = p " +
                    "JOIN InstructorPackage ip ON ipe.instructorPackage = ip " +
                    "JOIN Instructor i ON ip.instructor = i " +
                    "JOIN AuthUser a ON i.authUser = a WHERE a.id=:authUserId " +
                    "GROUP BY p.id")
    List<PublicUser> getAllPublicUsersByAuthUserId(@Param("authUserId") long authUserId);

    @Query(value =
            "SELECT p.* FROM public_user p \n" +
                    "WHERE \n" +
                    "p.id NOT IN (SELECT public_user_id FROM instructor_package_enroll WHERE date_time BETWEEN :startDateTime AND :endDateTime) AND\n" +
                    "p.id NOT IN (SELECT public_user_id FROM class_session_enroll WHERE date_time BETWEEN :startDateTime AND :endDateTime) AND\n" +
                    "p.id NOT IN (SELECT public_user_id FROM physical_session_enroll WHERE date_time BETWEEN :startDateTime AND :endDateTime) AND\n" +
                    "p.id NOT IN (SELECT public_user_id FROM public_user_membership WHERE date_time BETWEEN :startDateTime AND :endDateTime)",
            nativeQuery = true)
    List<PublicUser> getAllPublicUsersNotPurchasedAnything(@Param("startDateTime") LocalDateTime startDateTime, @Param("endDateTime") LocalDateTime endDateTime);

    List<PublicUser> findAllByCreatedDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    List<PublicUser> findAllByLastSeenDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    PublicUser findByEmail(String email);

    PublicUser findBySocialMediaId(String socialId);

    PublicUser findByVerificationNoAndVerificationType(String verificationNo, UserVerificationType verificationType);

    @Query(value = "SELECT p FROM PublicUser p WHERE " +
            "p.firstName LIKE %:data% OR " +
            "p.lastName LIKE %:data% OR " +
            "CONCAT(p.firstName,' ',p.lastName) LIKE %:data% OR " +
            "p.mobile LIKE %:data% OR " +
            "p.verificationNo LIKE %:data% OR " +
            "p.email LIKE %:data% ",
            countQuery = "SELECT COUNT (p.id) FROM PublicUser p WHERE " +
                    "p.firstName LIKE %:data% OR " +
                    "p.lastName LIKE %:data% OR " +
                    "CONCAT(p.firstName,' ',p.lastName) LIKE %:data% OR " +
                    "p.mobile LIKE %:data% OR " +
                    "p.verificationNo LIKE %:data% OR " +
                    "p.email LIKE %:data% ")
    Page<PublicUser> searchPublicUser(@Param("data") String data, Pageable pageable);

    PublicUser findByStripeClientId(String stripeId);

    Optional<PublicUser> findByReferralCode(String referralCode);

    boolean existsByTwilioUserIdentity(String twilioUserIdentity);

    @Query(value = "SELECT DISTINCT p FROM PublicUser p " +
            "LEFT JOIN ClassSessionEnroll ce ON ce.publicUser=p " +
            "LEFT JOIN PhysicalSessionEnroll pe ON pe.publicUser=p " +
            "LEFT JOIN InstructorPackageEnroll ie ON ie.publicUser=p " +
            "WHERE (ce.classSession.classParent.businessProfile=:businessProfile " +
            "OR pe.physicalClassSession.physicalClass.businessProfile=:businessProfile " +
            "OR ie.instructorPackage.businessProfile=:businessProfile ) " +
            "AND (:text IS NULL " +
            "OR p.firstName LIKE %:text% " +
            "OR p.lastName LIKE %:text% " +
            "OR p.mobile LIKE %:text% " +
            "OR p.email LIKE %:text% " +
            "OR p.verificationNo LIKE %:text% ) " +
            "ORDER BY p.id ",
            countQuery = "SELECT COUNT (DISTINCT p.id) FROM PublicUser p " +
                    "LEFT JOIN ClassSessionEnroll ce ON ce.publicUser=:p " +
                    "LEFT JOIN PhysicalSessionEnroll pe ON pe.publicUser=:p " +
                    "LEFT JOIN InstructorPackageEnroll ie ON ie.publicUser=:p " +
                    "WHERE (ce.classSession.classParent.businessProfile=:businessProfile " +
                    "OR pe.physicalClassSession.physicalClass.businessProfile=:businessProfile " +
                    "OR ie.instructorPackage.businessProfile=:businessProfile ) " +
                    "AND (:text IS NULL " +
                    "OR p.firstName LIKE %:text% " +
                    "OR p.lastName LIKE %:text% " +
                    "OR p.mobile LIKE %:text% " +
                    "OR p.email LIKE %:text% " +
                    "OR p.verificationNo LIKE %:text% ) ")
    Page<PublicUser> findPublicUsersForBusinessProfile
            (@Param("businessProfile") BusinessProfile businessProfile, @Param("text") String text, Pageable pageable);

    @Query(value = "SELECT COUNT (DISTINCT p.id) FROM PublicUser p " +
            "LEFT JOIN ClassSessionEnroll ce ON ce.publicUser=p " +
            "LEFT JOIN PhysicalSessionEnroll pe ON pe.publicUser=p " +
            "LEFT JOIN InstructorPackageEnroll ie ON ie.publicUser=p " +
            "WHERE ce.classSession.classParent.businessProfile=:businessProfile " +
            "OR pe.physicalClassSession.physicalClass.businessProfile=:businessProfile " +
            "OR ie.instructorPackage.businessProfile=:businessProfile ")
    long countPublicUsersForBusinessProfile(@Param("businessProfile") BusinessProfile businessProfile);

    List<PublicUser> findPublicUsersByTimeZone(String timeZone);
}
