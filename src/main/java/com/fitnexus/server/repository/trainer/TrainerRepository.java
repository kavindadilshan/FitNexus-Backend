package com.fitnexus.server.repository.trainer;

import com.fitnexus.server.dto.coach.CoachDetailsResponse;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.trainer.Trainer;
import com.fitnexus.server.enums.CoachVerificationType;
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
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

    Optional<Trainer> findByVerificationTypeAndVerificationNo(CoachVerificationType verificationType, String verificationNo);

    @Query(value = "SELECT t FROM Trainer t " +
            "JOIN FETCH t.trainerBusinessProfiles ib " +
            "JOIN FETCH BusinessProfile b ON ib.businessProfile = b " +
            "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = b " +
            "LEFT JOIN FETCH TrainerTypeDetail ttd ON ttd.trainer = t " +
            "LEFT JOIN FETCH TrainerType tt ON tt.id = ttd.trainerType.id " +
            "JOIN ClassSession cs ON cs.trainer = t " +
            "JOIN Class c ON cs.classParent = c " +
            "WHERE t.status = com.fitnexus.server.enums.UserStatus.ACTIVE " +
            "AND (:gender IS NULL OR t.authUser.gender =:gender) " +
            "AND ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE " +
            "AND ba.expDate > CURRENT_TIMESTAMP " +
            "AND ((:name IS NULL) OR (t.authUser.firstName LIKE %:name% OR t.authUser.lastName LIKE %:name%)) " +
            "AND (COALESCE(:types) IS NULL OR (tt.typeName IN :types)) " +
            "AND cs.dateAndTime > CURRENT_TIMESTAMP " +
            "AND (:allTrainers=TRUE  OR (EXISTS (SELECT pc.id FROM PackageClass pc WHERE pc.classParent=c))) " +
            "GROUP BY t.id ORDER BY t.rating DESC, t.id DESC ",
            countQuery = "SELECT COUNT(DISTINCT t.id) FROM Trainer t " +
                    "JOIN t.trainerBusinessProfiles ib " +
                    "JOIN BusinessProfile b ON ib.businessProfile = b " +
                    "JOIN BusinessAgreement ba ON ba.businessProfile = b " +
                    "LEFT JOIN TrainerTypeDetail ttd ON ttd.trainer = t " +
                    "LEFT JOIN TrainerType tt ON tt.id = ttd.trainerType.id " +
                    "JOIN ClassSession cs ON cs.trainer = t " +
                    "JOIN Class c ON cs.classParent = c " +
                    "WHERE t.status = com.fitnexus.server.enums.UserStatus.ACTIVE " +
                    "AND (:gender IS NULL OR t.authUser.gender =:gender) " +
                    "AND ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE " +
                    "AND ba.expDate > CURRENT_TIMESTAMP " +
                    "AND ((:name IS NULL) OR (t.authUser.firstName LIKE %:name% OR t.authUser.lastName LIKE %:name%)) " +
                    "AND (COALESCE(:types) IS NULL OR (tt.typeName IN :types)) " +
                    "AND cs.dateAndTime > CURRENT_TIMESTAMP "+
                    "AND (:allTrainers=FALSE  OR (EXISTS (SELECT pc.id FROM PackageClass pc WHERE pc.classParent=c))) ")
    Page<Trainer> getAllActiveTrainers(@Param("gender") Gender gender, @Param("name") String name,
                                       @Param("types") List<String> types, @Param("allTrainers")boolean allTrainers, Pageable pageable);

    @Query(value = "SELECT t FROM Trainer t\n" +
            "JOIN FETCH t.trainerBusinessProfiles ib\n" +
            "JOIN FETCH BusinessProfile b ON ib.businessProfile = b\n" +
            "JOIN FETCH BusinessAgreement ba ON ba.businessProfile = b\n" +
            "LEFT JOIN FETCH TrainerTypeDetail ttd ON ttd.trainer = t\n" +
            "LEFT JOIN FETCH TrainerType tt ON tt.id = ttd.trainerType.id\n" +
            "JOIN PhysicalClassSession pcs ON pcs.trainer = t\n" +
            "JOIN PhysicalClass pc ON pcs.physicalClass = pc\n" +
            "WHERE t.status = com.fitnexus.server.enums.UserStatus.ACTIVE\n" +
            "AND (:gender IS NULL OR t.authUser.gender =:gender)\n" +
            "AND (:country IS NULL OR t.authUser.country = :country)\n" +
            "AND ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP\n" +
            "AND ((:name IS NULL) OR (t.authUser.firstName LIKE %:name% OR t.authUser.lastName LIKE %:name%))\n" +
            "AND (COALESCE(:types) IS NULL OR (tt.typeName IN :types)) AND pcs.dateAndTime > CURRENT_TIMESTAMP\n" +
            "GROUP BY t.id ORDER BY t.physicalClassRating DESC, t.id DESC ",
            countQuery = "SELECT COUNT(DISTINCT t.id) FROM Trainer t\n" +
                    "JOIN t.trainerBusinessProfiles ib\n" +
                    "JOIN BusinessProfile b ON ib.businessProfile = b\n" +
                    "JOIN BusinessAgreement ba ON ba.businessProfile = b\n" +
                    "LEFT JOIN TrainerTypeDetail ttd ON ttd.trainer = t\n" +
                    "LEFT JOIN TrainerType tt ON tt.id = ttd.trainerType.id\n" +
                    "JOIN PhysicalClassSession pcs ON pcs.trainer = t\n" +
                    "JOIN PhysicalClass pc ON pcs.physicalClass = pc\n" +
                    "WHERE t.status = com.fitnexus.server.enums.UserStatus.ACTIVE\n" +
                    "AND (:gender IS NULL OR t.authUser.gender =:gender)\n" +
                    "AND (:country IS NULL OR t.authUser.country = :country)\n" +
                    "AND ba.status = com.fitnexus.server.enums.BusinessAgreementStatus.ACTIVE AND ba.expDate > CURRENT_TIMESTAMP\n" +
                    "AND ((:name IS NULL) OR (t.authUser.firstName LIKE %:name% OR t.authUser.lastName LIKE %:name%))\n" +
                    "AND (COALESCE(:types) IS NULL OR (tt.typeName IN :types)) AND pcs.dateAndTime > CURRENT_TIMESTAMP ")
    Page<Trainer> getAllActivePhysicalTrainers(@Param("gender") Gender gender, @Param("name") String name, @Param("country") String country,
                                               @Param("types") List<String> types, Pageable pageable);

    @Query(value = "SELECT new com.fitnexus.server.dto.coach.CoachDetailsResponse(a.id, a.firstName, a.lastName, a.publicUsername, a.image, " +
            "t.rating, t.ratingCount) FROM Trainer t JOIN  t.authUser a JOIN  ClassSession cs ON cs.trainer = t " +
            "JOIN  com.fitnexus.server.entity.classes.Class c ON cs.classParent = c " +
            "WHERE t.status = com.fitnexus.server.enums.UserStatus.ACTIVE " +
            "AND (:gender IS NULL OR a.gender = :gender) " +
            "AND c = :classParent GROUP BY t.id ORDER BY t.rating DESC ")
    List<CoachDetailsResponse> getTrainersByClass(@Param("gender") Gender gender, @Param("classParent") Class classParent);

    @Query(value = "SELECT new com.fitnexus.server.dto.coach.CoachDetailsResponse(a.id, a.firstName, a.lastName, a.publicUsername, a.image, " +
            "t.physicalClassRating, t.physicalClassRatingCount) FROM Trainer t JOIN  t.authUser a JOIN  PhysicalClassSession cs ON cs.trainer = t " +
            "JOIN  com.fitnexus.server.entity.classes.physical.PhysicalClass c ON cs.physicalClass = c " +
            "WHERE t.status = com.fitnexus.server.enums.UserStatus.ACTIVE " +
            "AND (:gender IS NULL OR a.gender = :gender) " +
            "AND c = :physicalClass GROUP BY t.id ORDER BY t.physicalClassRating DESC ")
    List<CoachDetailsResponse> getTrainersByPhysicalClass(@Param("gender") Gender gender, @Param("physicalClass") PhysicalClass physicalClass);

    Optional<Trainer> findTrainerByAuthUser_Id(long authUserId);

    boolean existsByVerificationTypeAndVerificationNo(CoachVerificationType type, String number);
}
