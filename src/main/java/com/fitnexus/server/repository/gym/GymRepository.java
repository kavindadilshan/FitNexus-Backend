package com.fitnexus.server.repository.gym;

import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.businessprofile.BusinessProfileLocation;
import com.fitnexus.server.entity.gym.Gym;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {

    Optional<Gym> findByLocation(BusinessProfileLocation location);

    Optional<Gym> findByGymUniqueName(String gymName);

    @Query(value = "SELECT g.id ," +
            "(111.111 * DEGREES(ACOS(LEAST(1.0, COS(RADIANS(l.latitude))" +
            "* COS(RADIANS(:latitude))" +
            "* COS(RADIANS(l.longitude - :longitude))" +
            "+ SIN(RADIANS(l.latitude))" +
            "* SIN(RADIANS(:latitude)))))) AS distance " +
            "FROM gym g " +
            "JOIN business_profile_location l ON l.id=g.location_id " +
            "JOIN business_agreement ba ON ba.business_profile_id = l.business_profile_id " +
            "WHERE ba.status = 'ACTIVE' AND ba.exp_date > CURRENT_TIMESTAMP " +
            "AND g.name LIKE %:name% " +
            "HAVING distance<=:limit " +
            "ORDER BY g.rating DESC ",
            nativeQuery = true,
            countQuery = "SELECT COUNT(g.id) ," +
                    "(111.111 * DEGREES(ACOS(LEAST(1.0, COS(RADIANS(l.latitude))" +
                    "* COS(RADIANS(:latitude))" +
                    "* COS(RADIANS(l.longitude - :longitude))" +
                    "+ SIN(RADIANS(l.latitude))" +
                    "* SIN(RADIANS(:latitude)))))) AS distance " +
                    "FROM gym g " +
                    "JOIN business_profile_location l ON l.id=g.location_id " +
                    "JOIN business_agreement ba ON ba.business_profile_id = l.business_profile_id " +
                    "WHERE ba.status = 'ACTIVE' AND ba.exp_date > CURRENT_TIMESTAMP " +
                    "AND g.name LIKE %:name% " +
                    "HAVING distance<=:limit " +
                    "ORDER BY g.rating DESC ")
    Page<Object[]> searchActiveGyms(@Param("name") String name,
                                    @Param("longitude") double longitude,
                                    @Param("latitude") double latitude,
                                    @Param("limit") double limit,
                                    Pageable pageable);

    @Query(value = "SELECT g FROM Gym g " +
            "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = g.location.businessProfile " +
            "JOIN g.location l " +
            "WHERE ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP " +
            "AND (:country IS NULL OR l.country=:country) " +
            "GROUP BY g.id ORDER BY g.rating DESC ",
            countQuery = "SELECT COUNT(DISTINCT g.id) FROM Gym g " +
                    "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = g.location.businessProfile " +
                    "JOIN g.location l " +
                    "WHERE ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP " +
                    "AND (:country IS NULL OR l.country=:country) " +
                    "GROUP BY g.id ORDER BY g.rating DESC ")
    Page<Gym> getPopularGyms(@Param("country") String country, Pageable pageable);

    @Query(value = "SELECT g FROM Gym g " +
            "WHERE g.name LIKE %:text% OR g.description LIKE %:text% " +
            "GROUP BY g.id ORDER BY g.id DESC ",
            countQuery = "SELECT COUNT(DISTINCT g.id) FROM Gym g " +
                    "WHERE g.name LIKE %:text% OR g.description LIKE %:text% " +
                    "GROUP BY g.id ORDER BY g.id DESC ")
    Page<Gym> searchGyms(@Param("text") String text, Pageable pageable);

    @Query(value = "SELECT g FROM Gym g " +
            "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = g.location.businessProfile " +
            "WHERE ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP " +
            "AND g.location.businessProfile = :profile " +
            "AND (:country IS NULL OR g.location.country = :country )" +
            "GROUP BY g.id ORDER BY g.rating DESC ",
            countQuery = "SELECT COUNT(DISTINCT g.id) FROM Gym g " +
                    "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = g.location.businessProfile " +
                    "WHERE ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP " +
                    "AND g.location.businessProfile = :profile " +
                    "AND (:country IS NULL OR g.location.country = :country )" +
                    "GROUP BY g.id ORDER BY g.rating DESC ")
    Page<Gym> getGymByBusinessProfile(@Param("profile") BusinessProfile profile, @Param("country") String country, Pageable pageable);

    @Query(value = "SELECT g.id ," +
            "(111.111 * DEGREES(ACOS(LEAST(1.0, COS(RADIANS(l.latitude))" +
            "* COS(RADIANS(:latitude))" +
            "* COS(RADIANS(l.longitude - :longitude))" +
            "+ SIN(RADIANS(l.latitude))" +
            "* SIN(RADIANS(:latitude)))))) AS distance " +
            "FROM gym g " +
            "JOIN business_profile_location l ON l.id=g.location_id " +
            "JOIN business_profile b ON b.id=l.business_profile_id " +
            "JOIN business_agreement ba ON ba.business_profile_id = b.id " +
            "WHERE ba.status = 'ACTIVE' AND ba.exp_date > CURRENT_TIMESTAMP " +
            "AND b.id=:profileId " +
            "HAVING distance<=:limit " +
            "ORDER BY g.rating DESC ",
            nativeQuery = true,
            countQuery = "SELECT COUNT(g.id) ," +
                    "(111.111 * DEGREES(ACOS(LEAST(1.0, COS(RADIANS(l.latitude))" +
                    "* COS(RADIANS(:latitude))" +
                    "* COS(RADIANS(l.longitude - :longitude))" +
                    "+ SIN(RADIANS(l.latitude))" +
                    "* SIN(RADIANS(:latitude)))))) AS distance " +
                    "FROM gym g " +
                    "JOIN business_profile_location l ON l.id=g.location_id " +
                    "JOIN business_profile b ON b.id=l.business_profile_id " +
                    "JOIN business_agreement ba ON ba.business_profile_id = b.id " +
                    "WHERE ba.status = 'ACTIVE' AND ba.exp_date > CURRENT_TIMESTAMP " +
                    "AND b.id=:profileId " +
                    "HAVING distance<=:limit " +
                    "ORDER BY g.rating DESC ")
    Page<Object[]> getNearestGymsByBusinessProfile(@Param("profileId") long profileId,
                                                   @Param("longitude") double longitude,
                                                   @Param("latitude") double latitude,
                                                   @Param("limit") double limit,
                                                   Pageable pageable);

    @Query(value = "SELECT g FROM Gym g " +
            "WHERE g.name LIKE %:text% OR g.description LIKE %:text% " +
            "AND g.location.businessProfile = :profile " +
            "GROUP BY g.id ORDER BY g.id DESC ",
            countQuery = "SELECT COUNT(DISTINCT g.id) FROM Gym g " +
                    "WHERE g.name LIKE %:text% OR g.description LIKE %:text% " +
                    "AND g.location.businessProfile = :profile " +
                    "GROUP BY g.id ORDER BY g.id DESC ")
    Page<Gym> searchGymsByBusinessProfile(@Param("profile") BusinessProfile profile, @Param("text") String text, Pageable pageable);

    Page<Gym> getAllByLocation_BusinessProfileOrderByIdDesc(BusinessProfile businessProfile, Pageable pageable);

    List<Gym> getAllByLocation_BusinessProfileOrderByIdDesc(BusinessProfile businessProfile);

    long countByLocation_BusinessProfile(BusinessProfile businessProfile);

    @Query(value = "SELECT b FROM Gym g " +
            "JOIN FETCH BusinessProfile b ON g.location.businessProfile = b " +
            "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = b " +
            "LEFT JOIN FETCH BusinessProfileImage bi ON bi.businessProfile = b JOIN FETCH BusinessProfileLocation bl ON g.location = bl " +
            "WHERE ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP " +
            "AND (:name IS NULL OR (b.businessName LIKE %:name% )) AND bl.country = :country " +
            "GROUP BY b.id ORDER BY ba.expDate ASC ",
            countQuery = "SELECT COUNT (DISTINCT b.id) FROM Gym g " +
                    "JOIN FETCH BusinessProfile b ON g.location.businessProfile = b " +
                    "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = b " +
                    "LEFT JOIN FETCH BusinessProfileImage bi ON bi.businessProfile = b JOIN FETCH BusinessProfileLocation bl ON g.location = bl " +
                    "WHERE ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP " +
                    "AND (:name IS NULL OR (b.businessName LIKE %:name% )) AND bl.country = :country " +
                    "ORDER BY ba.expDate ASC ")
    Page<BusinessProfile> getActiveBusinessProfiles(@Param("name") String name, @Param("country") String country, Pageable pageable);
}
