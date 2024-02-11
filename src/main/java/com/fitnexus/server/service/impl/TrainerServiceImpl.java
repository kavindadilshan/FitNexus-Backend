package com.fitnexus.server.service.impl;

import com.fitnexus.server.constant.FitNexusConstants;
import com.fitnexus.server.dto.classes.ClassForTrainerDTO;
import com.fitnexus.server.dto.coach.CoachDetailsResponse;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.dto.trainer.*;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.ClassTrainer;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.classes.physical.PhysicalClassTrainer;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.trainer.*;
import com.fitnexus.server.enums.ClassMethod;
import com.fitnexus.server.enums.CoachStatus;
import com.fitnexus.server.enums.Gender;
import com.fitnexus.server.repository.businessprofile.BusinessProfileRepository;
import com.fitnexus.server.repository.classes.ClassRepository;
import com.fitnexus.server.repository.classes.ClassSessionRepository;
import com.fitnexus.server.repository.classes.ClassTrainerRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassSessionRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassTrainerRepository;
import com.fitnexus.server.repository.publicuser.PublicUserRepository;
import com.fitnexus.server.repository.trainer.*;
import com.fitnexus.server.service.ClassSessionService;
import com.fitnexus.server.service.TrainerService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.*;
import static org.apache.logging.log4j.util.Chars.SPACE;

@Slf4j
@RequiredArgsConstructor
@Service
public class TrainerServiceImpl implements TrainerService {

    private final BusinessProfileRepository businessProfileRepository;
    private final TrainerBusinessProfileRepository trainerBusinessProfileRepository;
    private final ClassRepository classRepository;
    private final TrainerRepository trainerRepository;
    private final PublicUserRepository publicUserRepository;
    private final ClassTrainerRepository classTrainerRepository;
    private final TrainerRatingRepository trainerRatingRepository;
    private final ClassSessionRepository classSessionRepository;
    private final PhysicalClassRepository physicalClassRepository;

    @Autowired
    private ClassSessionService classSessionService;
    private final TrainerTypeRepository trainerTypeRepository;
    private final PhysicalTrainerRatingRepository physicalTrainerRatingRepository;
    private final PhysicalClassTrainerRepository physicalClassTrainerRepository;
    private final PhysicalClassSessionRepository physicalClassSessionRepository;

    @Override
    public List<TrainerNameIdDTO> getTrainersForBusinessProfile(long businessProfileId) {
        BusinessProfile profile = businessProfileRepository.findById(businessProfileId).orElseThrow(() ->
                new CustomServiceException(FitNexusConstants.NotFoundConstants.NO_BUSINESS_PROFILE_FOUND));
        return trainerBusinessProfileRepository.findTrainerBusinessProfilesByBusinessProfileAndTrainer_Status(profile, CoachStatus.ACTIVE).stream().map(
                tbp -> new TrainerNameIdDTO(tbp.getTrainer().getId(), tbp.getTrainer().getAuthUser().getFirstName() + SPACE +
                        tbp.getTrainer().getAuthUser().getLastName(), tbp.getTrainer().getStatus()))
                .collect(Collectors.toList());
    }


    /**
     * This can use to get active trainers page response.
     *
     * @param gender   the gender of the coach
     * @param pageable the pageable request.
     * @param name     the search value
     * @param types    the trainer types.
     * @return the trainers page list.
     */
    @Override
    public Page<TrainerDetailDTO> getAllActiveTrainers(Gender gender, String name, List<String> types, boolean allTrainers, Pageable pageable) {
        Page<Trainer> trainerPage = trainerRepository.getAllActiveTrainers(gender, name, types, allTrainers,pageable);
        return trainerPage.map(trainer -> getTrainerDetail(trainer, ClassMethod.ONLINE));
    }

    @Override
    public Page<TrainerDetailDTO> getAllActivePhysicalTrainers(Gender gender, String name, String country,
                                                               List<String> types, Pageable pageable) {
        Page<Trainer> trainerPage = trainerRepository.getAllActivePhysicalTrainers(gender, name, country, types, pageable);
        return trainerPage.map(trainer -> getTrainerDetail(trainer, ClassMethod.PHYSICAL));
    }

    /**
     * This can use to get active trainers by given class.
     *
     * @param gender the gender of the coach
     * @return the trainers page list.
     */
    @Override
    public List<CoachDetailsResponse> getAllActiveTrainersByClass(Gender gender, long classId) {
        Class trainer = classRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        return trainerRepository.getTrainersByClass(gender, trainer);
    }

    /**
     * This can use to get active trainers by given physical class.
     *
     * @param gender the gender of the coach
     * @return the trainers page list.
     */
    @Override
    public List<CoachDetailsResponse> getAllActiveTrainersByPhysicalClass(Gender gender, long classId) {
        PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        return trainerRepository.getTrainersByPhysicalClass(gender, physicalClass);
    }

    /**
     * @param trainerId the trainer wants to get
     * @return the trainer details with sessions.
     */
    @Override
    public TrainerSingleDTO getTrainer(long trainerId, LocalDateTime dateTime, String token) {
        Trainer trainer = trainerRepository.findById(trainerId)
                .orElseThrow(() -> new CustomServiceException(FitNexusConstants.NotFoundConstants.NO_TRAINER_FOUND));
        return getTrainerSingleDetail(trainer, dateTime, token);
    }



    @Override
    public TrainerCoachDTO getTrainerForCoachApp(Trainer t) {
        return new TrainerCoachDTO(t.getId(),
                t.getAuthUser().getFirstName() + SPACE + t.getAuthUser().getLastName(),
                t.getAuthUser().getImage(),
                t.getRating(),
                t.getRatingCount(),
                t.getPhysicalClassRating(),
                t.getPhysicalClassRatingCount());
    }

    @Override
    public Page<PublicUserReviewsResponse> getTrainerRatingsByUser(long trainerId, Pageable pageable) {
        Trainer trainer = trainerRepository.findById(trainerId).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND));
        return trainerRatingRepository.getTrainerRatingByTrainer(trainer, pageable);
    }

    @Override
    public Page<PublicUserReviewsResponse> getPhysicalTrainerRatingsByUser(long trainerId, Pageable pageable) {
        Trainer trainer = trainerRepository.findById(trainerId).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND));
        return physicalTrainerRatingRepository.getPhysicalTrainerRatingByTrainer(trainer, pageable);
    }

    @Override
    public Page<TrainerNameIdDTO> getTrainersForClass(long classId, Pageable pageable) {
        Class c = classRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        Page<ClassTrainer> classTrainersPage = classTrainerRepository.findClassTrainersByClassParent(c, pageRequest);
        List<TrainerNameIdDTO> trainerList = classTrainersPage.getContent().stream().map(ct -> new TrainerNameIdDTO(ct.getTrainer().getId(),
                ct.getTrainer().getAuthUser().getFirstName() + SPACE + ct.getTrainer().getAuthUser().getLastName(), ct.getTrainer().getStatus())).collect(Collectors.toList());
        return new PageImpl<>(trainerList, pageable, classTrainersPage.getTotalElements());
    }

    @Override
    public Page<TrainerRatingDTO> getTrainerRatings(long id, Pageable pageable) {
        Optional<Trainer> optionalTrainer = trainerRepository.findTrainerByAuthUser_Id(id);
        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateTime"));
            Page<TrainerRating> ratingPage = trainerRatingRepository.findTrainerRatingsByTrainer(trainer, pageRequest);
            List<TrainerRatingDTO> ratingList = ratingPage.getContent().stream().map(rating -> new TrainerRatingDTO(rating.getTrainerRatingId(), rating.getPublicUser().getId(),
                    rating.getPublicUser().getFirstName() + SPACE + rating.getPublicUser().getLastName(), rating.getRating(), rating.getComment(), rating.getDateTime()))
                    .collect(Collectors.toList());
            return new PageImpl<>(ratingList, pageable, ratingPage.getTotalElements());
        } else return null;
    }

    @Override
    public Page<TrainerRatingDTO> getPhysicalTrainerRatings(long id, Pageable pageable) {
        Optional<Trainer> optionalTrainer = trainerRepository.findTrainerByAuthUser_Id(id);
        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateTime"));
            Page<PhysicalTrainerRating> ratingPage = physicalTrainerRatingRepository.findPhysicalTrainerRatingsByTrainer(trainer, pageRequest);
            List<TrainerRatingDTO> ratingList = ratingPage.getContent().stream().map(rating -> new TrainerRatingDTO(rating.getTrainerRatingId(), rating.getPublicUser().getId(),
                    rating.getPublicUser().getFirstName() + SPACE + rating.getPublicUser().getLastName(), rating.getRating(), rating.getComment(), rating.getDateTime()))
                    .collect(Collectors.toList());
            return new PageImpl<>(ratingList, pageable, ratingPage.getTotalElements());
        } else return null;
    }

    @Override
    public Page<ClassForTrainerDTO> getClassesForTrainer(long id, Pageable pageable) {
        Optional<Trainer> optionalTrainer = trainerRepository.findTrainerByAuthUser_Id(id);
        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            Page<ClassTrainer> classTrainers = classTrainerRepository.findClassTrainersByTrainerOrderById(trainer, pageable);
            return classTrainers.map(classTrainer -> new ClassForTrainerDTO(
                    classTrainer.getClassParent().getId(),
                    classTrainer.getClassParent().getName(),
                    classTrainer.getClassParent().getClassType().getTypeName(),
                    classSessionRepository.countClassSessionsByClassParent(classTrainer.getClassParent())));
        } else return null;
    }

    @Override
    public Page<ClassForTrainerDTO> getPhysicalClassesForTrainer(long id, Pageable pageable) {
        Optional<Trainer> optionalTrainer = trainerRepository.findTrainerByAuthUser_Id(id);
        if (optionalTrainer.isPresent()) {
            Trainer trainer = optionalTrainer.get();
            Page<PhysicalClassTrainer> classTrainers = physicalClassTrainerRepository.findPhysicalClassTrainersByTrainerOrderById(trainer, pageable);
            return classTrainers.map(classTrainer -> {
                PhysicalClass physicalClass = classTrainer.getPhysicalClass();
                return new ClassForTrainerDTO(
                        physicalClass.getId(),
                        physicalClass.getName(),
                        physicalClass.getClassType().getTypeName(),
                        physicalClassSessionRepository.countPhysicalClassSessionsByPhysicalClass(physicalClass));
            });
        } else return null;
    }



    private TrainerDetailDTO getTrainerDetail(Trainer t, ClassMethod method) {
        AuthUser a = t.getAuthUser();

        double rating = t.getRating();
        long ratingCount = t.getRatingCount();

        if (method.equals(ClassMethod.PHYSICAL)) {
            rating = t.getPhysicalClassRating();
            ratingCount = t.getPhysicalClassRatingCount();
        }

        String dateDifferenceString = null;
        long dateDifference;
        long classCount;
        long onlineClassesPerWeek;
        long physicalClassesPerWeek;

        //online classes per week
        dateDifferenceString = classSessionRepository.getDateDifference(t.getId());
        dateDifference = dateDifferenceString == null ? 0 : Long.parseLong(dateDifferenceString);
        classCount = classSessionRepository.countClassSessionsByTrainer(t);
        onlineClassesPerWeek =
                dateDifference > 7 ?
                        (classCount / (dateDifference / 7) > 0 ?
                                classCount / (dateDifference / 7)
                                : 1)
                        :
                        classCount == 0 ?
                                1
                                : classCount;

        //physical classes per week
        dateDifferenceString = physicalClassSessionRepository.getDateDifference(t.getId());
        dateDifference = dateDifferenceString == null ? 0 : Long.parseLong(dateDifferenceString);
        classCount = physicalClassSessionRepository.countPhysicalClassSessionsByTrainer(t);
        physicalClassesPerWeek =
                dateDifference > 7 ?
                        (classCount / (dateDifference / 7) > 0 ?
                                classCount / (dateDifference / 7)
                                : 1)
                        :
                        classCount == 0 ?
                                1
                                : classCount;

        //get business profile
        List<TrainerBusinessProfile> trainerBusinessProfiles = t.getTrainerBusinessProfiles();
        String businessProfileName = (trainerBusinessProfiles != null && trainerBusinessProfiles.size() > 0) ?
                trainerBusinessProfiles.get(0).getBusinessProfile().getBusinessName() : "";

        return TrainerDetailDTO.builder()
                .id(t.getId())
                .email(a.getEmail())
                .mobile(a.getMobile())
                .firstName(a.getFirstName())
                .lastName(a.getLastName())
                .publicUsername(a.getPublicUsername())
                .country(a.getCountry())
                .timeZone(a.getTimeZone())
                .addressLine1(a.getAddressLine1())
                .addressLine2(a.getAddressLine2())
                .city(a.getCity())
                .province(a.getProvince())
                .postalCode(a.getPostalCode())
                .image(a.getImage())
                .description(a.getDescription())
                .userId(a.getId())
                .ratingCount(ratingCount)
                .rating(rating)
                .classTypes(t.getTrainerTypeDetails().stream().map(trainerTypeDetail -> trainerTypeDetail.getTrainerType().getTypeName()).collect(Collectors.toList()))
                .classesPerWeek(onlineClassesPerWeek)
                .physicalClassesPerWeek(physicalClassesPerWeek)
                .businessProfileName(businessProfileName)
                .build();
    }

    private TrainerSingleDTO getTrainerSingleDetail(Trainer t, LocalDateTime dateTime, String token) {
        AuthUser a = t.getAuthUser();
        return TrainerSingleDTO.builder().id(t.getId()).rating(t.getRating()).email(a.getEmail()).mobile(a.getMobile())
                .firstName(a.getFirstName()).lastName(a.getLastName()).publicUsername(a.getPublicUsername()).country(a.getCountry()).timeZone(a.getTimeZone())
                .addressLine1(a.getAddressLine1()).addressLine2(a.getAddressLine2()).city(a.getCity()).province(a.getProvince())
                .postalCode(a.getPostalCode()).image(a.getImage()).description(a.getDescription())
                .classSessions(classSessionService.getSessionsOfTrainer(t, dateTime, token)).ratingCount(t.getRatingCount())
                .physicalCLassRating(t.getPhysicalClassRating()).physicalClassRatingCount(t.getPhysicalClassRatingCount())
                .build();
    }

    @Override
    public void createTrainerType(String type) {
        trainerTypeRepository.save(new TrainerType(type));
    }
}
