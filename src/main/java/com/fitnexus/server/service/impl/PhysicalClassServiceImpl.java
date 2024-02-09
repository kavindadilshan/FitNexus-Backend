package com.fitnexus.server.service.impl;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.constant.FitNexusConstants;
import com.fitnexus.server.dto.businessprofile.BusinessProfileLocationDTO;
import com.fitnexus.server.dto.classes.*;
import com.fitnexus.server.dto.classsession.*;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.membership.MembershipDTO;
import com.fitnexus.server.dto.publicuser.PublicUserDiscountDTO;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.dto.trainer.TrainerNameIdDTO;
import com.fitnexus.server.dto.trainer.TrainerRateDTO;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.businessprofile.*;
import com.fitnexus.server.entity.classes.ClassType;
import com.fitnexus.server.entity.classes.physical.*;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.publicuser.PublicUserDiscounts;
import com.fitnexus.server.entity.trainer.Trainer;
import com.fitnexus.server.entity.trainer.TrainerBusinessProfile;
import com.fitnexus.server.enums.*;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.businessprofile.*;
import com.fitnexus.server.repository.classes.ClassSessionRepository;
import com.fitnexus.server.repository.classes.ClassTypeRepository;
import com.fitnexus.server.repository.classes.physical.*;
import com.fitnexus.server.repository.publicuser.PublicUserDiscountRepository;
import com.fitnexus.server.repository.publicuser.PublicUserMembershipRepository;
import com.fitnexus.server.repository.publicuser.PublicUserRepository;
import com.fitnexus.server.repository.trainer.TrainerBusinessProfileRepository;
import com.fitnexus.server.repository.trainer.TrainerRepository;
import com.fitnexus.server.service.*;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.FileHandler;
import com.fitnexus.server.util.UsernameGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.LockAcquisitionException;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.DuplicatedConstants.CLASS_NAME_ALREADY_EXISTS;
import static com.fitnexus.server.constant.FitNexusConstants.DuplicatedConstants.SESSION_ALREADY_EXISTS;
import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.PatternConstants.REGEX;
import static com.fitnexus.server.enums.NotificationType.PHYSICAL_SESSION_MULTIPLE;
import static com.fitnexus.server.util.FileHandler.CLASS_FOLDER;
import static org.apache.logging.log4j.util.Chars.SPACE;

@Slf4j
@RequiredArgsConstructor
@Service
public class PhysicalClassServiceImpl implements PhysicalClassService {

    private final PhysicalClassRepository physicalClassRepository;
    private final AuthUserRepository authUserRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessAgreementRepository businessAgreementRepository;
    private final ClassTypeRepository classTypeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainerBusinessProfileRepository trainerBusinessProfileRepository;
    private final PhysicalClassTrainerRepository physicalClassTrainerRepository;
    private final PhysicalClassImageRepository physicalClassImageRepository;
    private final PhysicalClassSessionRepository physicalClassSessionRepository;
    private final PhysicalClassRatingRepository physicalClassRatingRepository;
    private final BusinessProfileLocationRepository businessProfileLocationRepository;
    private final PhysicalSessionEnrollRepository physicalSessionEnrollRepository;
    private final PublicUserRepository publicUserRepository;
    private final PublicUserDiscountRepository publicUserDiscountRepository;
    private final BusinessProfileClassTypeRepository businessProfileClassTypeRepository;
    private final PublicUserMembershipRepository publicUserMembershipRepository;
    private static final double LOCATION_LIMIT = 50.00;
    @Autowired
    private TrainerService trainerService;
    private final LanguageService languageService;
    private final CoachNotificationService coachNotificationService;
    private final CommonUserService commonUserService;
    private final BusinessProfileService businessProfileService;
    private final PublicUserService publicUserService;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private final LocationFacilityRepository locationFacilityRepository;
    private final FileHandler fileHandler;
    private final ModelMapper modelMapper;
    private final LocationService locationService;
    private final FacilityService facilityService;
    @Autowired
    private MembershipService membershipService;
    private final PromoCodeManagementService promoCodeManagementService;

    private final ClassSessionRepository classSessionRepository;
    private final UsernameGeneratorUtil usernameGeneratorUtil;

    /**
     * get a new physical class entity from getNewPhysicalClass() method and
     * create class trainers and
     * saves given images using manageImages() method
     *
     * @param dto              details of the creating class
     * @param creatingUsername who creates the class
     */
    @Override
    @Transactional
    public void createPhysicalClass(ClassDTO dto, String creatingUsername) {
        PhysicalClass physicalClass = getNewPhysicalClass(dto, creatingUsername);
        BusinessProfile businessProfile = physicalClass.getBusinessProfile();

        List<Long> trainerIdList = dto.getTrainerIdList();
        if (trainerIdList.isEmpty()) throw new CustomServiceException("Trainer ID list cannot be empty!");
        List<PhysicalClassTrainer> classTrainers = new ArrayList<>();
        for (long trainerId : trainerIdList) {
            Trainer trainer = trainerRepository.findById(trainerId).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND));
            TrainerBusinessProfile trainerBusinessProfile =
                    trainerBusinessProfileRepository.findTrainerBusinessProfileByBusinessProfileAndTrainer(businessProfile, trainer);
            if (trainerBusinessProfile == null)
                throw new CustomServiceException("Trainer (" + trainer.getAuthUser().getFirstName() + SPACE + trainer.getAuthUser().getLastName() +
                        ") is not in the given business profile");

            PhysicalClassTrainer classTrainer = new PhysicalClassTrainer();
            classTrainer.setPhysicalClass(physicalClass);
            classTrainer.setTrainer(trainer);
            classTrainers.add(classTrainer);
        }

        ClassType classType = physicalClass.getClassType();
        Optional<BusinessProfileClassType> profileClassTypeOptional =
                businessProfileClassTypeRepository.findBusinessProfileClassTypeByBusinessProfileAndClassType(businessProfile, classType);
        if (!profileClassTypeOptional.isPresent()) {
            businessProfileClassTypeRepository.save(new BusinessProfileClassType(businessProfile, classType));
        }

        physicalClass = physicalClassRepository.save(physicalClass);
        log.info("Save physical class - " + physicalClass);
        classTrainers = physicalClassTrainerRepository.saveAll(classTrainers);
        log.info("Save physical class trainers - " + classTrainers);
        manageImages(physicalClass, dto.getImages());

        usernameGeneratorUtil.setPhysicalClassUniqueName(physicalClass);

    }

    private void saveFacilities(List<Long> facilityIdList, BusinessProfileLocation location) {
        if (facilityIdList != null && facilityIdList.size() > 0) {
            List<Facility> facilityList = facilityService.getFacilityList(facilityIdList);
            List<LocationFacility> locationFacilities = facilityList.stream().map(facility -> new LocationFacility(facility, location)).collect(Collectors.toList());
            locationFacilityRepository.saveAll(locationFacilities);
        }
    }

    private PhysicalClass getNewPhysicalClass(ClassDTO dto, String creatingUsername) {

        Optional<PhysicalClass> physicalClassOptional = physicalClassRepository.findPhysicalClassByName(dto.getName());
        if (physicalClassOptional.isPresent()) throw new CustomServiceException(CLASS_NAME_ALREADY_EXISTS);

        AuthUser authUser = authUserRepository.findByUsername(creatingUsername).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        BusinessProfile businessProfile = businessProfileRepository.findById(dto.getBusinessProfileId()).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));

        BusinessAgreement businessAgreement = businessAgreementRepository.findBusinessAgreementByBusinessProfileAndStatus
                (businessProfile, BusinessAgreementStatus.ACTIVE);
        if (businessAgreement == null)
            throw new CustomServiceException(FitNexusConstants.ErrorConstants.BUSINESS_PROFILE_EXPIRED);

        ClassType classType = classTypeRepository.findById(dto.getClassTypeId()).orElseThrow(() -> new CustomServiceException(NO_CLASS_TYPE_FOUND));

        String profileImageName = dto.getName().replaceAll(REGEX, "") + "ProfileImage" + UUID.randomUUID();
        String imageURL = fileHandler.saveBase64File(dto.getProfileImage(), profileImageName, CLASS_FOLDER);
        PhysicalClass physicalClass = new PhysicalClass();
        physicalClass.setName(dto.getName());
        physicalClass.setDescription(dto.getDescription());
        physicalClass.setHowToPrepare(dto.getHowToPrepare());
        physicalClass.setProfileImage(imageURL);
        physicalClass.setBusinessProfile(businessProfile);
        physicalClass.setClassType(classType);
        physicalClass.setCreatedBy(authUser);
        physicalClass.setCalorieBurnOut(dto.getCalorieBurnOut());
        physicalClass.setFirstSessionFree(dto.isFirstSessionFree());
        physicalClass.setRating(0);

        physicalClass.setYoutubeUrl(dto.getYoutubeUrl());

        return physicalClass;
    }

    private void manageImages(PhysicalClass physicalClass, List<String> imageList) {

        List<PhysicalClassImage> classImages = imageList.stream().map(
                s -> (s.startsWith("https://") || s.startsWith("http://")) ? new PhysicalClassImage(s, physicalClass) :
                        new PhysicalClassImage(fileHandler.saveBase64File(s, "physical-" + physicalClass.getName().replaceAll(REGEX, "") + UUID.randomUUID(), CLASS_FOLDER), physicalClass)
        ).collect(Collectors.toList());

        if (physicalClass.getPhysicalClassImages() != null) {
            physicalClassImageRepository.deleteAll(physicalClass.getPhysicalClassImages());
        }
        physicalClassImageRepository.saveAll(classImages);
    }

    /**
     * finds the physical class entity for given id and updates entity according to given details
     *
     * @param dto              details of the updating class
     * @param updatingUsername who updates the class
     */
    @Override
    @Transactional
    public void updatePhysicalClass(ClassDTO dto, String updatingUsername) {
        PhysicalClass physicalClass = physicalClassRepository.findById(dto.getClassId()).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        physicalClass.setName(dto.getName());
        physicalClass.setDescription(dto.getDescription());
        physicalClass.setCalorieBurnOut(dto.getCalorieBurnOut());
        physicalClass.setHowToPrepare(dto.getHowToPrepare());
        physicalClass.setFirstSessionFree(dto.isFirstSessionFree());

        physicalClass.setYoutubeUrl(dto.getYoutubeUrl());

        physicalClass.setProfileImage((dto.getProfileImage().startsWith("https://") || dto.getProfileImage().startsWith("http://"))
                ? dto.getProfileImage() :
                fileHandler.saveBase64File(dto.getProfileImage(), dto.getName().replaceAll(REGEX, "") + "ProfileImage" + UUID.randomUUID(), CLASS_FOLDER));

        ClassType classType = classTypeRepository.findById(dto.getClassTypeId()).orElseThrow(() -> new CustomServiceException(NO_CLASS_TYPE_FOUND));
        physicalClass.setClassType(classType);
        physicalClass.setUpdatedBy(authUserRepository.findByUsername(updatingUsername).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND)));

        BusinessProfile businessProfile = physicalClass.getBusinessProfile();
        Optional<BusinessProfileClassType> profileClassTypeOptional =
                businessProfileClassTypeRepository.findBusinessProfileClassTypeByBusinessProfileAndClassType(businessProfile, classType);
        if (!profileClassTypeOptional.isPresent()) {
            businessProfileClassTypeRepository.save(new BusinessProfileClassType(businessProfile, classType));
        }

        physicalClass = physicalClassRepository.save(physicalClass);
        log.info("Update physical class- " + physicalClass);
        manageTrainers(physicalClass, dto.getTrainerIdList());
        manageImages(physicalClass, dto.getImages());
    }

    @Override
    public void updateClassVisible(long id, boolean visible) {
        PhysicalClass physicalClass = physicalClassRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        physicalClass.setVisible(visible);
        physicalClassRepository.save(physicalClass);
    }

    private void manageTrainers(PhysicalClass physicalClass, List<Long> newIdList) {

        List<Long> existingIdList = physicalClass.getPhysicalClassTrainers().stream().map(classTrainer -> classTrainer.getTrainer().getId()).collect(Collectors.toList());
        List<Long> existingIdListAnother = physicalClass.getPhysicalClassTrainers().stream().map(classTrainer -> classTrainer.getTrainer().getId()).collect(Collectors.toList());
        existingIdList.removeAll(newIdList);
        newIdList.removeAll(existingIdListAnother);

        //removing
        if (!existingIdList.isEmpty()) {
            for (long id : existingIdList) {
                Trainer trainer = trainerRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND));
                List<PhysicalClassSession> sessions = physicalClassSessionRepository.findPhysicalClassSessionsByPhysicalClassAndTrainerAndStatus(physicalClass, trainer, ClassSessionStatus.PENDING);
                if (sessions != null && sessions.size() > 0)
                    throw new CustomServiceException("Can not remove the trainer.Trainer (" + trainer.getAuthUser().getFirstName() + SPACE + trainer.getAuthUser().getLastName() +
                            ") has pending sessions under this class.");
                PhysicalClassTrainer classTrainer = physicalClassTrainerRepository.findPhysicalClassTrainerByPhysicalClassIdAndTrainerId(physicalClass.getId(), id);
                physicalClassTrainerRepository.delete(classTrainer);
                log.info("Update class - removing trainer id list - " + existingIdList);
            }
        }
        //creating
        if (!newIdList.isEmpty()) {
            for (long id : newIdList) {
                Trainer trainer = trainerRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND));
                PhysicalClassTrainer classTrainer = new PhysicalClassTrainer();
                classTrainer.setPhysicalClass(physicalClass);
                classTrainer.setTrainer(trainer);
                physicalClassTrainerRepository.save(classTrainer);
            }
            log.info("Update class - new trainer id list - " + newIdList);
        }
    }

    private ClassDTO mapPhysicalClass(PhysicalClass c) {
        BusinessAgreement agreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(c.getBusinessProfile());
        return new ClassDTO(
                c.getId(),
                c.getName(),
                c.getClassUniqueName(),
                null,
                c.getCalorieBurnOut(),
                c.isFirstSessionFree(),
                c.getClassType().getId(),
                c.getClassType().getTypeName(),
                c.getHowToPrepare(),
                c.getDescription(),
                c.getProfileImage(),
                getPhysicalClassImages(c.getPhysicalClassImages()),
                c.getRating(),
                c.getRatingCount(),
                c.getBusinessProfile().getId(),
                c.getBusinessProfile().getBusinessName(),
                agreement.getPackageDetail().getPaymentModel(),
                c.getCreatedBy().getFirstName() + SPACE + c.getCreatedBy().getLastName(),
                c.getCreatedDate(),
                c.getUpdatedBy() == null ? null : c.getUpdatedBy().getFirstName() + SPACE + c.getUpdatedBy().getLastName(),
                c.getUpdatedDate(),
                c.getPhysicalClassSessions().size(),
                c.getPhysicalClassTrainers().stream().map(ct -> ct.getTrainer().getId()).collect(Collectors.toList()),
                c.getPhysicalClassTrainers().stream().map(this::getSinglePhysicalTrainerDetail).collect(Collectors.toList()),
                getTrainersForBusinessProfile(c.getBusinessProfile()),
                c.isVisible(),
                c.getYoutubeUrl()
        );
    }

    private Map<String, Object> getSinglePhysicalTrainerDetail(PhysicalClassTrainer ct) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", ct.getTrainer().getId());
        detail.put("name", ct.getTrainer().getAuthUser().getFirstName() + SPACE + ct.getTrainer().getAuthUser().getLastName());
        detail.put("gender", ct.getTrainer().getAuthUser().getGender());
        detail.put("ratings", ct.getTrainer().getRating());
        detail.put("ratingCount", ct.getTrainer().getRatingCount());
        return detail;
    }

    private List<TrainerNameIdDTO> getTrainersForBusinessProfile(BusinessProfile businessProfile) {
        return trainerBusinessProfileRepository.findTrainerBusinessProfilesByBusinessProfileAndTrainer_Status(businessProfile, CoachStatus.ACTIVE).stream().map(tb ->
                new TrainerNameIdDTO(tb.getTrainer().getId(), tb.getTrainer().getAuthUser().getFirstName() + SPACE + tb.getTrainer().getAuthUser().getLastName(), tb.getTrainer().getStatus())).
                collect(Collectors.toList());
    }

    private List<String> getPhysicalClassImages(List<PhysicalClassImage> classImages) {
        return classImages.stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList());
    }

    /**
     * finds the business profile for given id and
     * get all the classes of it and
     * crates a map object with class id and class name and
     * added them to a list
     *
     * @param businessProfileId business profile's ID of wanted classes
     * @return created list
     */
    @Override
    public List<ClassNameIdDTO> getAllPhysicalClassesForBusinessProfile(long businessProfileId) {
        log.info("getAllPhysicalClassesForBusinessProfile : businessProfileId - {}", businessProfileId);
        return physicalClassRepository.findPhysicalClassesByBusinessProfileId(businessProfileId).stream().map(
                c -> new ClassNameIdDTO(c.getId(), c.getName(), null)).
                collect(Collectors.toList());
    }

    @Override
    public Page<ClassDTO> getAllPhysicalClassesByProfile(long businessProfileId, Pageable pageable) {
        log.info("getAllPhysicalClassesByProfile : businessProfileId - {}", businessProfileId);
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "updatedDate"));
        Page<PhysicalClass> classPage = physicalClassRepository.getAllByBusinessProfileOrderByCreatedDateDesc(
                businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND)), pageRequest);
        List<ClassDTO> result = classPage.getContent().stream().map(this::mapPhysicalClass).collect(Collectors.toList());
        pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return new PageImpl<>(result, pageRequest, classPage.getTotalElements());
    }

    /**
     * finds all the classes according to pageable request
     *
     * @param pageable pageable request
     * @return page of classes
     */
    @Override
    public Page<ClassDTO> getAllPhysicalClasses(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "updatedDate"));
        Page<PhysicalClass> classPage = physicalClassRepository.findAll(pageRequest);
        List<ClassDTO> result = classPage.getContent().stream().map(this::mapPhysicalClass).collect(Collectors.toList());
        pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return new PageImpl<>(result, pageRequest, classPage.getTotalElements());
    }

    @Override
    public List<PhysicalClassDTO> getAllPhysicalClassesAll() {
        List<PhysicalClass> physicalClasses = physicalClassRepository.findAll();
        return modelMapper.map(physicalClasses, new TypeToken<List<PhysicalClassDTO>>() {
        }.getType());
    }

    @Override
    public Page<ClassDTO> searchPhysicalClass(String data, long businessProfileId, Pageable pageable, String username) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));

//        Page<BigInteger> classPage;
//        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
//        UserRole userRole = commonUserService.getRole(authUser);
//
//        if (userRole.equals(UserRole.SUPER_ADMIN))
//            classPage = classRepository.searchClass(data, category.toString(), pageRequest);
//        else {
//            BusinessProfile businessProfile = authUser.getBusinessProfileManager().getBusinessProfile();
//            classPage = classRepository.searchClassForProfile(data, category.toString(), businessProfile.getId(), pageable);
//        }

        if (!businessProfileRepository.existsById(businessProfileId))
            throw new CustomServiceException(NO_BUSINESS_PROFILE_FOUND);

        Page<BigInteger> classPage = physicalClassRepository.searchPhysicalClassForProfile(data, businessProfileId, pageRequest);

        List<ClassDTO> result = classPage.getContent().stream().map(id -> getPhysicalClassById(id.longValue())).collect(Collectors.toList());
        pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return new PageImpl<>(result, pageRequest, classPage.getTotalElements());
    }

    @Override
    public List<ClassDTO> searchPhysicalClassByName(String name, String username) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole role = commonUserService.getRole(authUser);

        List<PhysicalClass> classList = new ArrayList<>();

        if (role.equals(UserRole.BUSINESS_PROFILE_MANAGER))
            classList = physicalClassRepository.searchPhysicalCLassByNameAndProfile(name, authUser.getBusinessProfileManager().getBusinessProfile());
        if (role.equals(UserRole.SUPER_ADMIN)) classList = physicalClassRepository.searchPhysicalCLassByName(name);

        return classList.stream().map(this::mapPhysicalClass).collect(Collectors.toList());
    }

    /**
     * finds the relevant class entity for given id and creates a ClassDTO object with that data
     *
     * @param id id od searching class
     * @return created ClassDTO
     */
    @Override
    public ClassDTO getPhysicalClassById(long id) {
        return mapPhysicalClass(physicalClassRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND)));
    }

    @Override
    public Page<ClassRatingDTO> getPhysicalClassRatings(long classId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateTime"));
        Page<PhysicalClassRating> ratingPage = physicalClassRatingRepository.findPhysicalClassRatingsByPhysicalClassId(classId, pageRequest);
        List<ClassRatingDTO> ratingList = ratingPage.getContent().stream().map(r -> new ClassRatingDTO(r.getId(), r.getPublicUser().getId(),
                r.getPublicUser().getFirstName() + SPACE + r.getPublicUser().getLastName(), r.getRating(),
                r.getComment(), r.getDateTime())).collect(Collectors.toList());
        return new PageImpl<>(ratingList, pageable, ratingPage.getTotalElements());
    }

    //session

    /**
     * finds class entity for given class id - if not found -> throws an exception
     * finds trainer entity for given trainer id - if not found -> throws an exception
     * searches if a session entity is found for given (class, dateTime and trainer) - if found -> throws an exception
     * finds the parent class's business profile and checks if it is expired or if it will expire before the sessions's dateTime
     * if one of above conditions is true -> throws an exception
     * <p>
     * if no exceptions thrown :)
     * creates a ClassSession entity and assign data to it and saves it
     * then saves the given images
     *
     * @param sessionDTO details of creating session
     */
    @Override
    @Transactional
    public void createPhysicalSession(SessionCreateDTO sessionDTO) {
        PhysicalClass physicalClass = physicalClassRepository.findById(sessionDTO.getClassId()).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        Trainer trainer = trainerRepository.findById(sessionDTO.getTrainerId()).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND));

        BusinessAgreement businessAgreement = businessAgreementRepository.findBusinessAgreementByBusinessProfileAndStatus
                (physicalClass.getBusinessProfile(), BusinessAgreementStatus.ACTIVE);
        if (businessAgreement == null)
            throw new CustomServiceException(FitNexusConstants.ErrorConstants.BUSINESS_PROFILE_EXPIRED);

        BusinessProfileLocation location = businessProfileLocationRepository.findById(sessionDTO.getLocationId()).orElseThrow(() -> new CustomServiceException(NO_LOCATION_FOUND));

        List<PhysicalClassSession> createdSessions = new ArrayList<>();
        List<LocalDateTime> dateTimeList = sessionDTO.getDateTimeList();
        if (dateTimeList != null && dateTimeList.size() > 0) {
            for (LocalDateTime dateTime : dateTimeList) {

                if (LocalDateTime.now().isAfter(dateTime) || LocalDateTime.now().isEqual(dateTime))
                    throw new CustomServiceException("Date and time must be after current date and time");
                Optional<PhysicalClassSession> sessionOptional = physicalClassSessionRepository.findPhysicalClassSessionByPhysicalClassAndDateTimeAndTrainer(physicalClass, dateTime, trainer);
                if (sessionOptional.isPresent()) throw new CustomServiceException(SESSION_ALREADY_EXISTS);

                if (businessAgreement.getExpDate().isBefore(dateTime))
                    throw new CustomServiceException("The business profile expires before the session's date time.");

                PhysicalClassSession classSession = new PhysicalClassSession();
                classSession.setMaxJoiners(sessionDTO.getMaxJoiners());
                classSession.setDuration(sessionDTO.getDuration());
                classSession.setDescription(sessionDTO.getDescription());
                classSession.setStatus(ClassSessionStatus.PENDING);
                classSession.setPrice(BigDecimal.valueOf(sessionDTO.getPrice()));
                classSession.setGender(sessionDTO.getGender());
                classSession.setDateAndTime(dateTime);
                classSession.setPhysicalClass(physicalClass);
                classSession.setLanguage(languageService.getLanguage(sessionDTO.getLanguage()));
                classSession.setTrainer(trainer);
                classSession.setName(physicalClass.getName() + "-" + dateTime.toString().substring(0, 16) + "(GMT)");
                classSession.setBusinessProfileLocation(location);
                classSession.setAllowCashPayment(sessionDTO.isAllowCashPayment());
                classSession = physicalClassSessionRepository.save(classSession);
                log.info("Save physical class session - " + classSession);
                createdSessions.add(classSession);
            }

            //send notifications to coach
            AuthUser authUser = trainer.getAuthUser();
            String timeZone = authUser.getTimeZone();
            //create notification message
            if (createdSessions.size() == 1) {
                PhysicalClassSession session = createdSessions.get(0);
                String message = PHYSICAL_CLASS_SESSION_ASSIGNED_DESC
                        .replace("{firstName}", authUser.getFirstName())
                        .replace("{className}", physicalClass.getName())
                        .replace("{dateTime}", CustomGenerator.getDateTimeToGivenZone(session.getDateAndTime(),
                                timeZone) + " (" + timeZone + ")");
                coachNotificationService.sendPhysicalSessionNotification(session, CLASS_SESSION_ASSIGNED, message, false, CoachNotificationType.ASSIGN);
            } else {
                coachNotificationService.sendNotificationForMultipleSessionAssign
                        (physicalClass.getId(), physicalClass.getProfileImage(), physicalClass.getName(), trainer,
                                dateTimeList, timeZone, PHYSICAL_CLASS_SESSION_LIST_ASSIGNED_DESC, PHYSICAL_SESSION_MULTIPLE);
                for (PhysicalClassSession session : createdSessions) {
                    coachNotificationService.sendSessionStartBeforeNotification(authUser, physicalClass.getName(), session.getDateAndTime());
                }
            }
            saveFacilities(sessionDTO.getFacilityIdList(), location);
        } else {
            throw new CustomServiceException("Date time list is required");
        }
    }

    /**
     * finds the class session entity for given id and updates details
     * this method is for admin
     *
     * @param dto details of updating session
     */
    @Override
    @Transactional
    public void updatePhysicalSession(SessionDTO dto) {
        if (dto.getDateTime() == null) throw new CustomServiceException("DateTime can not be null");

        PhysicalClassSession session = physicalClassSessionRepository.findById(dto.getSessionId()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));

        BusinessProfileLocation location = businessProfileLocationRepository.findById(dto.getLocationId()).orElseThrow(() -> new CustomServiceException(NO_LOCATION_FOUND));

        session.setName(dto.getSessionName());
        session.setMaxJoiners(dto.getMaxJoiners());
        session.setDuration(dto.getDuration());
        session.setDescription(dto.getDescription());
        session.setPrice(BigDecimal.valueOf(dto.getPrice()));
        session.setGender(dto.getGender());
        session.setBusinessProfileLocation(location);
        session.setLanguage(languageService.getLanguage(dto.getLanguage()));
        session.setTrainer(trainerRepository.findById(dto.getTrainerId()).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND)));
        session.setAllowCashPayment(dto.isAllowCashPayment());
        physicalClassSessionRepository.save(session);
        log.info("Update physical class session - " + session);

        List<LocationFacility> locationFacilities = location.getLocationFacilities();
        if (locationFacilities != null && locationFacilities.size() > 0) {
            locationFacilityRepository.deleteAll(locationFacilities);
        }
        saveFacilities(dto.getFacilityIdList(), location);

        if (!session.getDateAndTime().equals(dto.getDateTime()))
            reschedulePhysicalSession(session.getId(), dto.getDateTime());
    }

    /**
     * finds the class session entity for given id and updates details
     * this method is for coach
     * updates only description, howToPrepare, dateTime
     *
     * @param dto details of updating session
     */
    @Override
    @Transactional
    public void updatePhysicalSessionByCoach(SessionUpdateDTO dto) {
        PhysicalClassSession session = physicalClassSessionRepository.findById(dto.getSessionId()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));

        session.setDescription(dto.getDescription());
        PhysicalClass physicalClass = session.getPhysicalClass();
        physicalClass.setHowToPrepare(dto.getHowToPrepare());
        physicalClassRepository.save(physicalClass);
        session = physicalClassSessionRepository.save(session);
        log.info("Update physical class session by coach - " + session);

        if (!session.getDateAndTime().equals(dto.getDateTime()))
            reschedulePhysicalSession(session.getId(), dto.getDateTime());
    }

    /**
     * Finds the relevant session entity for given id and
     * creates a SessionCoachDTO with details that want to show in coach app
     *
     * @param id id of searching session
     * @return created SessionCoachDTO object
     */
    @Override
    public SessionCoachDTO getPhysicalSessionByIdForCoach(long id) {
        return getSessionCoachDTO(physicalClassSessionRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND)));
    }

    private SessionCoachDTO getSessionCoachDTO(PhysicalClassSession s) {
        SessionCoachDTO sessionCoachDTO = new SessionCoachDTO(
                s.getId(),
                s.getName(),
                s.getPhysicalClass().getId(),
                s.getPhysicalClass().getName(),
                s.getPhysicalClass().getRating(),
                s.getPhysicalClass().getRatingCount(),
                s.getDateAndTime(),
                s.getDateAndTime().plusMinutes(s.getDuration()),
                s.getPhysicalClass().getDescription(),
                s.getDescription(),
                s.getPhysicalClass().getHowToPrepare(),
                s.getPhysicalSessionEnrolls() == null ? 0 : s.getPhysicalSessionEnrolls().size(),
                s.getPhysicalClass().getProfileImage(),
                s.getPhysicalClass().getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList()),
                getStudentsForSession(s),
                s.getPhysicalSessionEnrolls().stream().map(this::getSessionEnrollDTO).collect(Collectors.toList()),
                mapLocation(s.getBusinessProfileLocation()),
                s.getStatus());

        return sessionCoachDTO;
    }

    private List<SessionStudentDTO> getStudentsForSession(PhysicalClassSession s) {
        return s.getPhysicalSessionEnrolls().stream().map(e -> new SessionStudentDTO(e.getPublicUser().getId(),
                e.getPublicUser().getFirstName() + SPACE + e.getPublicUser().getLastName(), getAge(e.getPublicUser()),
                e.getPublicUser().getGender(), e.getPublicUser().getImage())).collect(Collectors.toList());
    }

    private String getAge(PublicUser publicUser) {
        if (publicUser.getDateOfBirth() == null) {
            return "";
        } else {
            return LocalDate.now().compareTo(publicUser.getDateOfBirth()) + " Years Old";
        }
    }

    /**
     * Finds the session entity for given id and
     * checks it's status
     * if status is PENDING and if the session has no enrollments, cancel the session
     * otherwise throws an exception mentioning relevant reason
     *
     * @param sessionId id of cancelling session
     */
    @Override
    @Transactional
    public void cancelPhysicalSession(long sessionId) {
        PhysicalClassSession session = physicalClassSessionRepository.findById(sessionId).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
        switch (session.getStatus()) {
            case CANCELLED:
                throw new CustomServiceException("This session is already canceled");
            case FINISHED:
                throw new CustomServiceException("This session is finished");
            case ONGOING:
                throw new CustomServiceException("This session can't cancel since it is ongoing");
            case PENDING:
                if (session.getPhysicalSessionEnrolls() != null && session.getPhysicalSessionEnrolls().size() > 0)
                    throw new CustomServiceException("This session has enrollments.Can't cancel session");

                //update database
                session.setStatus(ClassSessionStatus.CANCELLED);
                physicalClassSessionRepository.save(session);

                //send notification to coach
                AuthUser authUser = session.getTrainer().getAuthUser();
                LocalDateTime dateTimeForCoach = session.getDateAndTime();
                String timeZone = authUser.getTimeZone();
                String message = PHYSICAL_CLASS_SESSION_CANCELED_DESC
                        .replace("{firstName}", authUser.getFirstName())
                        .replace("{class}", session.getPhysicalClass().getName())
                        .replace("{dateTime}",
                                CustomGenerator.getDateTimeToGivenZone(dateTimeForCoach, timeZone)
//                                + " (" + timeZone + ")"
                        );
                coachNotificationService.sendPhysicalSessionNotification(session, PHYSICAL_CLASS_SESSION_CANCELED, message, true, CoachNotificationType.CANCEL);
        }
    }

    /**
     * Finds the session entity for given id and
     * checks it's status
     * if PENDING changes the date and time of the session and
     * inform enrolled public users
     *
     * @param classSessionId id of rescheduling session
     * @param newDateTime    new date and time
     */
    @Override
    @Transactional
    public void reschedulePhysicalSession(long classSessionId, LocalDateTime newDateTime) {

        if (LocalDateTime.now().isAfter(newDateTime) || LocalDateTime.now().isEqual(newDateTime))
            throw new CustomServiceException("Date and time must be after current date and time");

        PhysicalClassSession session = physicalClassSessionRepository.findById(classSessionId).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
        LocalDateTime oldDateTime = session.getDateAndTime();
        switch (session.getStatus()) {
            case CANCELLED:
                throw new CustomServiceException("This session is canceled");
            case FINISHED:
                throw new CustomServiceException("This session is finished");
            case ONGOING:
                throw new CustomServiceException("This session can't update since it is ongoing");
            case PENDING:

                //update database
                session.setDateAndTime(newDateTime);
                session.setName(session.getPhysicalClass().getName() + "-" + newDateTime.toString().substring(0, 16) + "(GMT)");
                session = physicalClassSessionRepository.save(session);
                log.info("Rescheduled physical class session - " + session);

                //send notifications to public users
                List<PublicUser> publicUsers = session.getPhysicalSessionEnrolls().stream().map(PhysicalSessionEnroll::getPublicUser).collect(Collectors.toList());
                for (PublicUser publicUser : publicUsers) {
                    //create notification message
                    String timeZone = publicUser.getTimeZone();
                    String message = "Hi " + publicUser.getFirstName() + ", " + session.getPhysicalClass().getName()
                            + " which is scheduled on " + CustomGenerator.getDateTimeToGivenZone(oldDateTime, timeZone)
//                            + " (" + timeZone + ")"
                            + " has been rescheduled to " + CustomGenerator.getDateTimeToGivenZone(newDateTime, timeZone)
//                            + " (" + timeZone + ")"
                            ;
                    publicUserService.savePhysicalSessionNotification(
                            session,
                            Collections.singletonList(publicUser),
                            PHYSICAL_CLASS_SESSION_RESCHEDULED.replace(
                                    "{className}",
                                    session.getPhysicalClass().getName()
                            ),
                            message,
                            true
                    );
                }

                //send notifications to coach
                AuthUser authUser = session.getTrainer().getAuthUser();
                String timeZone = authUser.getTimeZone();
                //create notification message
                String message = PHYSICAL_CLASS_SESSION_RESCHEDULED_DESC
                        .replace("{firstName}", authUser.getFirstName())
                        .replace("{className}", session.getPhysicalClass().getName())
                        .replace("{oldDateTime}",
                                CustomGenerator.getDateTimeToGivenZone(oldDateTime, timeZone)
//                                        + " (" + timeZone + ")"
                        )
                        .replace("{newDateTime}",
                                CustomGenerator.getDateTimeToGivenZone(newDateTime, timeZone)
//                                        + " (" + timeZone + ")"
                        );
                coachNotificationService.sendPhysicalSessionNotification(
                        session,
                        PHYSICAL_CLASS_SESSION_RESCHEDULED.replace(
                                "{className}",
                                session.getPhysicalClass().getName()),
                        message,
                        true,
                        CoachNotificationType.RESCHEDULE
                );
        }
    }

    /**
     * Finds all the sessions according to given pageable request and
     * creates a list of SessionDTO from extracted data
     *
     * @param pageable pageable request
     * @return page of SessionDTO object
     */
    @Override
    public Page<SessionDTO> getAllPhysicalSessions(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateAndTime"));
        Page<PhysicalClassSession> sessionPage = physicalClassSessionRepository.findAll(pageRequest);
        List<SessionDTO> sessions = sessionPage.getContent().stream().map(this::mapPhysicalSession).collect(Collectors.toList());
        pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateTime"));
        return new PageImpl<>(sessions, pageRequest, sessionPage.getTotalElements());
    }

    /**
     * @param date     the date to filter
     * @param pageable the page request
     * @return
     */
    @Override
    public Page<SessionDTO> getSessionsByDate(LocalDate date, Pageable pageable) {
        return physicalClassSessionRepository.getSessionsByDate(date, pageable).map(this::mapPhysicalSession);
    }

    private SessionDTO mapPhysicalSession(PhysicalClassSession c) {
        return new SessionDTO(c.getId(), c.getName(), c.getStatus(), c.getMaxJoiners(), c.getDuration(), c.getDescription(), c.getPrice().doubleValue(), c.getGender(),
                c.getDateAndTime(), c.getPhysicalClass().getId(), c.getPhysicalClass().getName(), c.getPhysicalClass().getProfileImage(),
                c.getPhysicalClass().getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList()), null,
                c.getPhysicalClass().getBusinessProfile().getId(), c.getPhysicalClass().getBusinessProfile().getBusinessName(), c.getTrainer().getId(),
                c.getTrainer().getAuthUser().getFirstName() + SPACE + c.getTrainer().getAuthUser().getLastName(), null, c.getLanguage() != null ? c.getLanguage().getLanguageName() : null,
                getTrainersForClass(c), physicalSessionEnrollRepository.countAllByPhysicalClassSession(c), c.getBusinessProfileLocation().getId(), mapLocation(c.getBusinessProfileLocation()),
                null, facilityService.getFacilityDTOListFromLocationFacilityList(c.getBusinessProfileLocation().getLocationFacilities()), c.isAllowCashPayment());
    }

    private BusinessProfileLocationDTO mapLocation(BusinessProfileLocation l) {
        return new BusinessProfileLocationDTO(l.getId(), l.getName(), l.getType(), l.getCountry(), l.getTimeZone(),
                l.getAddressLine1(), l.getAddressLine2(), l.getLongitude(), l.getLatitude(), l.getCity(), l.getProvince(), l.getPostalCode());
    }

    private List<TrainerNameIdDTO> getTrainersForClass(PhysicalClassSession c) {
        return c.getPhysicalClass().getPhysicalClassTrainers().stream().map(ct -> new TrainerNameIdDTO(ct.getTrainer().getId(),
                ct.getTrainer().getAuthUser().getFirstName() + SPACE + ct.getTrainer().getAuthUser().getLastName(), ct.getTrainer().getStatus())).collect(Collectors.toList());
    }

    /**
     * Finds the class entity for given id and
     * gets all the sessions of that class according to given pageable request and
     * creates a list of SessionDTO from extracted data
     *
     * @param classId  id of the class that sessions are belong to
     * @param dateType
     * @param pageable pageable request
     * @return page of SessionDTO object
     */
    @Override
    public Page<SessionDTO> getAllPhysicalSessionsByClass(long classId, SessionGetDateType dateType, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateAndTime"));
        PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        Page<PhysicalClassSession> sessionPage;
        if (dateType == null || dateType == SessionGetDateType.UPCOMING)
            sessionPage = physicalClassSessionRepository.getSessionsForPhysicalClass(null, physicalClass, null, pageRequest);
        else
            sessionPage = physicalClassSessionRepository.getPastSessionsForPhysicalClass(null, physicalClass, null, pageRequest);
        List<SessionDTO> sessions = sessionPage.getContent().stream().map(this::mapPhysicalSession).collect(Collectors.toList());
        pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateTime"));
        return new PageImpl<>(sessions, pageRequest, sessionPage.getTotalElements());
    }

    /**
     * Finds all the sessions according to given pageable request and
     * which contains given text in name or description or howToPrepare...
     *
     * @param data     searching text content
     * @param pageable pageable request
     * @return page of SessionDTO obejcts
     */
    @Override
    public Page<SessionDTO> searchPhysicalSession(String data, long classId, Pageable pageable, String username) {
        PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole userRole = commonUserService.getRole(authUser);

        if (userRole.equals(UserRole.SUPER_ADMIN)) {
            Page<PhysicalClassSession> sessionPage = physicalClassSessionRepository.getSessionsForPhysicalClass
                    (data, physicalClass, null, pageable);
            return sessionPage.map(this::mapPhysicalSession);
        } else {
            BusinessProfile businessProfile = authUser.getBusinessProfileManager().getBusinessProfile();
            Page<PhysicalClassSession> sessionPage = physicalClassSessionRepository.getSessionsForPhysicalClass
                    (data, physicalClass, businessProfile, pageable);
            return sessionPage.map(this::mapPhysicalSession);
        }
    }

    /**
     * Finds the session entity for given id and
     * creates a SessionDTO object from data in the entity
     *
     * @param id id of the searching session
     * @return created SessionDTO object
     */
    @Override
    public SessionDTO getPhysicalSessionById(long id) {
        return mapPhysicalSession(physicalClassSessionRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND)));
    }

    @Override
    public List<TrainerNameIdDTO> getTrainersForPhysicalClass(long id) {
        return physicalClassRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND)).getPhysicalClassTrainers().stream().
                map(ct -> new TrainerNameIdDTO(ct.getTrainer().getId(),
                        ct.getTrainer().getAuthUser().getFirstName() + SPACE + ct.getTrainer().getAuthUser().getLastName(),
                        ct.getTrainer().getStatus())).collect(Collectors.toList());
    }

    @Override
    public Page<SessionEnrollDTO> getPhysicalSessionEnrollments(long sessionId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateTime"));
        Page<PhysicalSessionEnroll> enrollPage = physicalSessionEnrollRepository.findPhysicalSessionEnrollsByPhysicalClassSessionId(sessionId, pageRequest);
        List<SessionEnrollDTO> enrollList = enrollPage.getContent().stream().map(this::getSessionEnrollDTO).collect(Collectors.toList());

        return new PageImpl<>(enrollList, pageable, enrollPage.getTotalElements());
    }

    private SessionEnrollDTO getSessionEnrollDTO(PhysicalSessionEnroll enroll) {
        SessionEnrollDTO sessionEnrollDTO = new SessionEnrollDTO();
        sessionEnrollDTO.setEnrollId(enroll.getId());
        PublicUser publicUser = enroll.getPublicUser();
        sessionEnrollDTO.setPublicUserId(publicUser.getId());
        sessionEnrollDTO.setPublicUserProfilePicture(publicUser.getImage());
        sessionEnrollDTO.setGender(publicUser.getGender());
        sessionEnrollDTO.setPublicUserName(publicUser.getFirstName() + " " + publicUser.getLastName());
        sessionEnrollDTO.setAge(publicUser.getDateOfBirth() == null ? 0 : LocalDate.now().compareTo(publicUser.getDateOfBirth()));
        sessionEnrollDTO.setCountry(publicUser.getCountry());
        sessionEnrollDTO.setDateTime(enroll.getDateTime());
        AuthUser collectedBy = enroll.getCollectedBy();
        sessionEnrollDTO.setCollectedBy(collectedBy != null ? collectedBy.getFirstName() + SPACE + collectedBy.getLastName() : null);
        SessionEnrollStatus status = enroll.getStatus();
        sessionEnrollDTO.setPaymentStatus(status);
        if (status.equals(SessionEnrollStatus.BOOKED)) {
            sessionEnrollDTO.setPaidAmount(enroll.getPaidAmount().doubleValue());
            sessionEnrollDTO.setDueAmount(0);
        } else if (status.equals(SessionEnrollStatus.PENDING)) {
            sessionEnrollDTO.setPaidAmount(0);
            sessionEnrollDTO.setDueAmount(enroll.getPaidAmount().doubleValue());
        }
        setPaymentMethod(enroll.getPaymentId(), sessionEnrollDTO);

        if (sessionEnrollDTO.getPaymentStatus() == SessionEnrollStatus.BOOKED && enroll.getPaymentId().startsWith("MEMBERSHIP_")) {
            sessionEnrollDTO.setEnrollStatus(ResponseSessionEnrollStatus.MEMBERSHIP_BOOKED);
        } else if (sessionEnrollDTO.getPaymentStatus() == SessionEnrollStatus.BOOKED && enroll.getPaymentId().startsWith("FIRST_FREE_")) {
            sessionEnrollDTO.setEnrollStatus(ResponseSessionEnrollStatus.FREE);
        } else if (sessionEnrollDTO.getPaymentStatus() == SessionEnrollStatus.PENDING && enroll.getPaymentId().startsWith("MEMBERSHIP_")) {
            sessionEnrollDTO.setEnrollStatus(ResponseSessionEnrollStatus.MEMBERSHIP_PENDING);
        } else if (sessionEnrollDTO.getPaymentStatus() == SessionEnrollStatus.BOOKED) {
            sessionEnrollDTO.setEnrollStatus(ResponseSessionEnrollStatus.SESSION_BOOKED);
        } else if (sessionEnrollDTO.getPaymentStatus() == SessionEnrollStatus.PENDING) {
            sessionEnrollDTO.setEnrollStatus(ResponseSessionEnrollStatus.SESSION_PENDING);
        } else if (sessionEnrollDTO.getPaymentStatus() == SessionEnrollStatus.FREE) {
            sessionEnrollDTO.setEnrollStatus(ResponseSessionEnrollStatus.FREE);
        }

        return sessionEnrollDTO;
    }

    /**
     * This can use to get active classes order by rating DESC.
     *
     * @param pageable the pageable request.
     * @return List of classes.
     * @throws CustomServiceException if something went wrong.
     */
    @Override
    public Page<ClassListDTO> getActivePhysicalClasses(Pageable pageable, String country, String token) {
        Page<PhysicalClass> allActiveClasses = physicalClassRepository.getAllActiveOrderedDesc(LocalDateTime.now().minusHours(18), country, pageable);
        return getClassDetailsPage(allActiveClasses, token);
    }

    /**
     * This can use to get active classes order by rating DESC. - OPEN
     *
     * @param pageable the pageable request.
     * @return List of classes.
     * @throws CustomServiceException if something went wrong.
     */
    @Override
    public Page<ClassListDTO> getActivePhysicalClassesOpen(Pageable pageable) {
        Page<PhysicalClass> allActiveClasses = physicalClassRepository.getAllActiveOrderedDesc(LocalDateTime.now().minusHours(18), null, pageable);
        return allActiveClasses.map(physicalClass -> getClassDetailsByClass(physicalClass, null));
    }


    private Page<ClassListDTO> getClassDetailsPage(Page<PhysicalClass> classes, String token) {
        PublicUser finalPublicUser = getPublicUserByToken(token);

        Page<ClassListDTO> map = classes.map(physicalClass -> {
            ClassListDTO classDetailsByClass = getClassDetailsByClass(physicalClass, finalPublicUser);
            ClassDetailsDTO classDetailsDTO = modelMapper.map(classDetailsByClass, ClassDetailsDTO.class);
            classDetailsDTO.setStartingFrom(physicalClassSessionRepository.getLowestSessionPriceFromPhysicalClass(physicalClass.getId()));
            return classDetailsDTO;
        });

        return map;
    }

    private ClassListDTO getClassListWithImagesAndUpcoming(PhysicalClass physicalClass) {
        ClassListDTO classListDTO = modelMapper.map(physicalClass, ClassListDTO.class);
        List<String> images = physicalClass.getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList());
        classListDTO.setImages(images);
        long upcomingSessionCount = physicalClassSessionRepository.countUpcomingPhysicalSessionsByClass(physicalClass, LocalDateTime.now());
        if (upcomingSessionCount > 0) classListDTO.setSessionsUpcoming(true);
        else classListDTO.setSessionsUpcoming(false);
        return classListDTO;
    }

    private ClassListDTO getClassDetailsByClass(PhysicalClass physicalClass, PublicUser publicUser) {
        ClassListDTO classListDTO = getClassListWithImagesAndUpcoming(physicalClass);
        classListDTO = getAvgPerWeekAndLastPrice(physicalClass, classListDTO, publicUser);
        ClassDetailsDTO classDetailsDTO = modelMapper.map(classListDTO, ClassDetailsDTO.class);
        classDetailsDTO.setStartingFrom(physicalClassSessionRepository.getLowestSessionPriceFromPhysicalClass(physicalClass.getId()));
        return classDetailsDTO;
    }

    private ClassListDTO getAvgPerWeekAndLastPrice(PhysicalClass physicalClass, ClassListDTO classListDTO, PublicUser publicUser) {
        boolean firstSessionFree = false;
        PhysicalClassSession lastSessionOfClass = physicalClassSessionRepository.findTopByPhysicalClassOrderByDateAndTimeDesc(physicalClass);
        if (lastSessionOfClass == null) return classListDTO;
        if (publicUser != null) {
            SessionButtonStatus buttonStatus = getButtonStatusByClass(physicalClass, publicUser);
            classListDTO.setButtonStatus(buttonStatus);
            if (buttonStatus.equals(SessionButtonStatus.FIRST_FREE)) {
                firstSessionFree = true;
            } else if (buttonStatus.equals(SessionButtonStatus.DISCOUNT)) {
                PublicUserDiscounts userDiscount = publicUserDiscountRepository
                        .findTopByPublicUserAndExpDateAfterAndCategoryNotOrderByExpDateAsc(publicUser, LocalDateTime.now(), DiscountCategory.PROMO_CODE);
                classListDTO.setDiscountMaxAmount(userDiscount.getMaxDiscount());
                classListDTO.setDiscountPercentage(userDiscount.getPercentage());
                classListDTO.setDiscountDescription(userDiscount.getDescription());
            }
            classListDTO.setFirstSessionFree(firstSessionFree);
        }
        classListDTO.setLastSessionPrice(lastSessionOfClass.getPrice());
        long differenceInDays = Duration.between(LocalDateTime.now(), lastSessionOfClass.getDateAndTime()).toDays();
        long sessionCount = physicalClassSessionRepository.countAllByPhysicalClassAndDateAndDateAndTimeAfter(physicalClass);
        if (sessionCount <= 0) return classListDTO;
        if (differenceInDays <= 7) {
            classListDTO.setAverageSessionsPerWeek(sessionCount);
            return classListDTO;
        }
        long avgSessionsCount = sessionCount / (differenceInDays / 7);
        classListDTO.setAverageSessionsPerWeek(avgSessionsCount == 0 ? 1 : avgSessionsCount);

//        LocalDateTime startDateTime = lastSessionOfClass.getDateAndTime().minusDays(7);
//        long avgSessionsCount = classSessionRepository.countClassSessionsByClassParentAndDateAndTimeBetween(classEntity, startDateTime, lastSessionOfClass.getDateAndTime());
//        classListDTO.setAverageSessionsPerWeek(avgSessionsCount == 0 ? 1 : avgSessionsCount);
//
//        ClassSession firstSessionOfClass = classSessionRepository.findTopByClassParentOrderByDateAndTimeAsc(classEntity);
//        long duration = Duration.between(firstSessionOfClass.getDateAndTime(), lastSessionOfClass.getDateAndTime()).toDays();
//        long totalSessionCount = classSessionRepository.countClassSessionsByClassParent(classEntity);
//        long averageSessionCount = 0;
//        if (duration <= 7) averageSessionCount = totalSessionCount;
//        else {
//
//        }
        return classListDTO;
    }

    private SessionButtonStatus getButtonStatusByClass(PhysicalClass physicalClass, PublicUser publicUser) {
        // checks public user is null to apply changes if this call from public user or not(not means an admin or coach).
        if (publicUser != null) {
            if (physicalClass.isFirstSessionFree()
                    && physicalSessionEnrollRepository.countAllByPublicUserAndPhysicalClassSessionPhysicalClass(publicUser, physicalClass) <= 0)
                return SessionButtonStatus.FIRST_FREE;
            else {
                PublicUserDiscounts discounts = publicUserDiscountRepository.findTopByPublicUserAndExpDateAfterAndCategoryNotOrderByExpDateAsc(publicUser, LocalDateTime.now(), DiscountCategory.PROMO_CODE);
                if (discounts != null) return SessionButtonStatus.DISCOUNT;
                else return SessionButtonStatus.PAY;
            }
        }
        return SessionButtonStatus.PAY;
    }

    private PublicUser getPublicUserByToken(String token) {
        PublicUser publicUser = null;
        if (token != null) {
            long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
            publicUser = publicUserRepository.findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        }
        return publicUser;
    }

    /**
     * Get pending class sessions after current time.
     *
     * @param pageable      the page request from client.
     * @param startDateTime the date to filter.
     * @return the sessions query results page.
     */
    @Override
    public Page<ClassSessionListResponse> getPhysicalSessionsForHome(Gender gender, String name, List<Long> classTypeIds,
                                                                     LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                                     double longitude, double latitude, String country,
                                                                     Pageable pageable, String token, String timeZone) {

        return getClassSessionsDtoPage(physicalClassSessionRepository.getUpcomingPhysicalSessionsByDate
                (longitude, latitude, LOCATION_LIMIT, classTypeIds, startDateTime, endDateTime, name, gender.toString(), country,
                        CustomUserAuthenticator.getPublicUserIdFromToken(token), pageable), token, startDateTime);
    }

    private Page<ClassSessionListResponse> getClassSessionsDtoPage(Page<Object[]> sessionObjects, String token, LocalDateTime dateTime) {
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        return sessionObjects.map(object -> getSessionResponseFromObject(object, publicUser, dateTime));
    }

    private ClassSessionListResponse getSessionResponseFromObject(Object[] object, PublicUser publicUser, LocalDateTime dateTime) {
        BigInteger id = (BigInteger) object[0];
        double distance = (double) object[1];
        PhysicalClassSession physicalSession = physicalClassSessionRepository.findById(id.longValue()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
        ClassSessionListResponse classSessionResponse = getClassSessionResponse(physicalSession, publicUser, dateTime);
        classSessionResponse.setDistance(distance);
        membershipService.setPhysicalClassMembershipDetails(classSessionResponse, null, physicalSession.getPhysicalClass(), publicUser);
        return classSessionResponse;
    }

    @Override
    public ClassSessionListResponse getClassSessionResponse(PhysicalClassSession c, PublicUser publicUser, LocalDateTime dateTime) {
        PhysicalClass physicalClass = c.getPhysicalClass();
        SessionButtonStatus buttonStatus = getButtonStatusBySession(c, physicalClass, publicUser, dateTime);
        BusinessProfileLocation businessHeadOffice = businessProfileLocationRepository
                .findBusinessProfileLocationByTypeAndBusinessProfile(BusinessProfileLocationType.HEAD_OFFICE, physicalClass.getBusinessProfile());

        PublicUserDiscountDTO discountDetails = new PublicUserDiscountDTO();
        if (buttonStatus.equals(SessionButtonStatus.DISCOUNT)) {
            discountDetails = getDiscountDetails(publicUser);
        }

        return new ClassSessionListResponse(c.getId(), c.getName(), c.getDuration(),
                c.getDescription(), c.getDateAndTime(), c.getDateAndTime().plusMinutes(c.getDuration()), null, c.getPrice(), c.getGender(),
                c.getLanguage() != null ? c.getLanguage().getLanguageName() : null, c.getMaxJoiners(),
                getAvailableCountBySession(c), c.getStatus(),
                c.getTrainer().getAuthUser().getFirstName(), c.getTrainer().getAuthUser().getLastName(),
                physicalClass.getId(), physicalClass.getName(), physicalClass.getClassUniqueName(), physicalClass.getProfileImage(),
                physicalClass.getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList()), physicalClass.getClassType().getTypeName(),
                physicalClass.getRating(), physicalClass.getRatingCount(), getAvgPerWeek(physicalClass), physicalClass.getCalorieBurnOut(), null,
                buttonStatus, discountDetails.getMaxDiscount(), discountDetails.getPercentage(), discountDetails.getDescription(),
                businessHeadOffice != null ? businessHeadOffice.getCountry() : null, mapLocation(c.getBusinessProfileLocation()), 0.0);
    }

    private ClassSessionListResponse getClassSessionResponseOpen(PhysicalClassSession c) {
        PhysicalClass physicalClass = c.getPhysicalClass();

        BusinessProfileLocation businessHeadOffice = businessProfileLocationRepository
                .findBusinessProfileLocationByTypeAndBusinessProfile(BusinessProfileLocationType.HEAD_OFFICE, physicalClass.getBusinessProfile());

        PublicUserDiscountDTO discountDetails = new PublicUserDiscountDTO();

        return new ClassSessionListResponse(c.getId(), c.getName(), c.getDuration(),
                c.getDescription(), c.getDateAndTime(), c.getDateAndTime().plusMinutes(c.getDuration()), null, c.getPrice(), c.getGender(),
                c.getLanguage() != null ? c.getLanguage().getLanguageName() : null, c.getMaxJoiners(),
                getAvailableCountBySession(c), c.getStatus(),
                c.getTrainer().getAuthUser().getFirstName(), c.getTrainer().getAuthUser().getLastName(),
                physicalClass.getId(), physicalClass.getName(), physicalClass.getClassUniqueName(), physicalClass.getProfileImage(),
                physicalClass.getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList()), physicalClass.getClassType().getTypeName(),
                physicalClass.getRating(), physicalClass.getRatingCount(), getAvgPerWeek(physicalClass), physicalClass.getCalorieBurnOut(), null,
                null, discountDetails.getMaxDiscount(), discountDetails.getPercentage(), discountDetails.getDescription(),
                businessHeadOffice != null ? businessHeadOffice.getCountry() : null, mapLocation(c.getBusinessProfileLocation()), 0.0);
    }

    private long getAvgPerWeek(PhysicalClass physicalClass) {
        PhysicalClassSession lastSessionOfClass = physicalClassSessionRepository.findTopByPhysicalClassOrderByDateAndTimeDesc(physicalClass);
        if (lastSessionOfClass != null) {
            LocalDateTime dateAndTime = lastSessionOfClass.getDateAndTime();
            long avgSessionsCount = physicalClassSessionRepository.
                    countPhysicalClassSessionsByPhysicalClassAndDateAndTimeBetween
                            (physicalClass, dateAndTime.minusDays(7), dateAndTime);
            return avgSessionsCount == 0 ? 1 : avgSessionsCount;
        }
        return 0;
    }

    private PublicUserDiscountDTO getDiscountDetails(PublicUser publicUser) {
        PublicUserDiscounts userDiscount = publicUserDiscountRepository
                .findTopByPublicUserAndExpDateAfterAndCategoryNotOrderByExpDateAsc(publicUser, LocalDateTime.now(), DiscountCategory.PROMO_CODE);
        PublicUserDiscountDTO dto = new PublicUserDiscountDTO();
        dto.setMaxDiscount(userDiscount.getMaxDiscount());
        dto.setPercentage(userDiscount.getPercentage());
        dto.setDescription(userDiscount.getDescription());
        return dto;
    }

    @Override
    public SessionButtonStatus getButtonStatusBySession(PhysicalClassSession physicalClassSession, PhysicalClass physicalClass, PublicUser publicUser, LocalDateTime dateTime) {
        // checks public user is null to apply changes if this call from public user or not(not means an admin or coach).
        if (publicUser != null) {
            PhysicalSessionEnroll sessionEnrollByUser = physicalSessionEnrollRepository.findByPhysicalClassSessionAndPublicUser(physicalClassSession, publicUser);
            if (sessionEnrollByUser != null) {
                if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.PENDING) {
                    String stripePaymentId = sessionEnrollByUser.getPaymentId();
                    if (stripePaymentId.startsWith("CASH_")) return SessionButtonStatus.PENDING_PAYMENT;
                    else return SessionButtonStatus.PENDING_PURCHASE;
                }
                if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.BOOKED)
                    return SessionButtonStatus.PURCHASED;
            } else if (getAvailableCountBySession(physicalClassSession) <= 0) {
                return SessionButtonStatus.FULL;
            }
            SessionButtonStatus sessionButtonStatus = firstSessionFreeForUser(physicalClassSession, physicalClass, publicUser, dateTime);
            if (sessionButtonStatus != null && sessionButtonStatus.equals(SessionButtonStatus.FIRST_FREE))
                return sessionButtonStatus;
            else {
                PublicUserDiscounts discounts = publicUserDiscountRepository.findTopByPublicUserAndExpDateAfterAndCategoryNotOrderByExpDateAsc(publicUser, LocalDateTime.now(), DiscountCategory.PROMO_CODE);
                if (discounts != null)
                    return SessionButtonStatus.DISCOUNT;
            }
        }
        return SessionButtonStatus.PAY;
    }

    @Override
    public SessionButtonStatus firstSessionFreeForUser(PhysicalClassSession physicalClassSession, PhysicalClass physicalClass, PublicUser publicUser, LocalDateTime dateTime) {
        if (physicalClass.isFirstSessionFree()) {
            long classEnrollCount = physicalSessionEnrollRepository.countAllByPublicUserAndPhysicalClassSessionPhysicalClass(publicUser, physicalClass);
            if (classEnrollCount <= 0) {
                return SessionButtonStatus.FIRST_FREE;
            }
        }
        return null;
    }

    @Override
    public int getAvailableCountBySession(PhysicalClassSession physicalClassSession) {
        return physicalClassSession.getMaxJoiners() - getEnrolledCountBySession(physicalClassSession);
    }

    @Override
    public int getEnrolledCountBySession(PhysicalClassSession physicalClassSession) {
        return (int) physicalSessionEnrollRepository.countAllByPhysicalSessionAndStatusAndTimeBetween(physicalClassSession,
                LocalDateTime.now().minusMinutes(ENROLLED_CHECK_MINUTES));
    }

    /**
     * This can use to get class details of user UI.
     *
     * @param classId the class id to get
     * @return ClassDetailsDTO
     * @throws CustomServiceException if something not found.
     */
    @Override
    public ClassListDTO getPhysicalClassDetails(long classId, LocalDateTime dateTime, double longitude, double latitude, String token) {
        PublicUser publicUser = getPublicUserByToken(token);
        PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
//        List<MembershipDTO> memberships = membershipService.getMembershipsByPhysicalClass(physicalClass, publicUser);
        ClassDetailsDTO classDetailsDTO = ClassDetailsDTO.builder()
                .id(classId)
                .name(physicalClass.getName())
                .rating(physicalClass.getRating())
                .howToPrepare(physicalClass.getHowToPrepare())
                .description(physicalClass.getDescription())
                .images(physicalClass.getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList()))
                .ratingCount(physicalClass.getRatingCount())
                .profileImage(physicalClass.getProfileImage())
                .classType(modelMapper.map(physicalClass.getClassType(), ClassTypeDTO.class))
                .businessProfile(businessProfileService.getBusinessProfileResponse(physicalClass.getBusinessProfile()))
                .classSessions(getUpcomingPhysicalSessionsListLimit(physicalClass, dateTime, longitude, latitude, PageRequest.of(0, 3), token))
                .startingFrom(physicalClassSessionRepository.getLowestSessionPriceFromPhysicalClass(physicalClass.getId()))
                .youtubeUrl(physicalClass.getYoutubeUrl())
                .build();
        membershipService.setPhysicalClassMembershipDetails(null, classDetailsDTO, physicalClass, publicUser);
        return getAvgPerWeekAndLastPrice(physicalClass, classDetailsDTO, publicUser);
    }

    /**
     * This can use to get class details of user UI. - OPEN
     *
     * @param classId the class id to get
     * @return ClassDetailsDTO
     * @throws CustomServiceException if something not found.
     */
    @Override
    public ClassListDTO getPhysicalClassDetailsOpen(long classId) {
        PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        List<MembershipDTO> memberships = membershipService.getMembershipsByPhysicalClass(physicalClass, null);
        ClassDetailsDTO classDetailsDTO = ClassDetailsDTO.builder().id(classId).name(physicalClass.getName()).rating(physicalClass.getRating())
                .howToPrepare(physicalClass.getHowToPrepare()).description(physicalClass.getDescription())
                .images(physicalClass.getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList()))
                .ratingCount(physicalClass.getRatingCount()).memberships(memberships)
                .profileImage(physicalClass.getProfileImage()).classType(modelMapper.map(physicalClass.getClassType(), ClassTypeDTO.class))
                .businessProfile(businessProfileService.getBusinessProfileResponse(physicalClass.getBusinessProfile()))
                .classSessions(getUpcomingPhysicalSessionsListLimitOpen(physicalClass, LocalDateTime.now()))
                .startingFrom(physicalClassSessionRepository.getLowestSessionPriceFromPhysicalClass(physicalClass.getId()))
                .youtubeUrl(physicalClass.getYoutubeUrl())
                .build();
        return getAvgPerWeekAndLastPrice(physicalClass, classDetailsDTO, null);
    }

    @Override
    public ClassListDTO getPhysicalClassDetailsOpenByClassName(String className) {

        Optional<PhysicalClass> optionalPhysicalClassByClassUniqueName = physicalClassRepository.findPhysicalClassByClassUniqueName(className);
        if (!optionalPhysicalClassByClassUniqueName.isPresent()) {
            throw new CustomServiceException(NO_CLASS_FOUND);
        }
        PhysicalClass physicalClass = optionalPhysicalClassByClassUniqueName.get();
        return getPhysicalClassDetailsOpen(physicalClass.getId());
    }

    /**
     * Get pending class sessions by class limited. -OPEN
     *
     * @param physicalClass the class entity to get sessions.
     * @return the sessions response results list.
     */
    @Override
    public List<ClassSessionListResponse> getUpcomingPhysicalSessionsListLimitOpen(PhysicalClass physicalClass, LocalDateTime dateTime) {
        return physicalClassSessionRepository.getUpcomingPhysicalSessionsListByClassLimit(physicalClass, dateTime, PageRequest.of(0, 3))
                .stream().map(this::getClassSessionResponseOpen).collect(Collectors.toList());
    }

    /**
     * Get pending class sessions by class.
     *
     * @param physicalClass the class entity to get sessions.
     * @return the sessions response results list.
     */
    @Override
    public Page<ClassSessionListResponse> getUpcomingPhysicalSessionsList(
            PhysicalClass physicalClass,
            LocalDateTime dateTime,
            String token,
            double longitude,
            double latitude,
            Pageable pageable
    ) {
        Gender gender = publicUserRepository.getOne(CustomUserAuthenticator.getPublicUserIdFromToken(token)).getGender();
        return getClassSessionsDtoPage(
                physicalClassSessionRepository.getUpcomingPhysicalSessionsListByClass(
                        physicalClass.getId(),
                        dateTime,
                        longitude,
                        latitude,
                        gender.toString(),
                        pageable
                ),
                token,
                dateTime
        );
    }

    /**
     * Get pending class sessions by class limited.
     *
     * @param physicalClass the class entity to get sessions.
     * @return the sessions response results list.
     */
    @Override
    public List<ClassSessionListResponse> getUpcomingPhysicalSessionsListLimit(
            PhysicalClass physicalClass,
            LocalDateTime dateTime,
            double longitude,
            double latitude,
            Pageable pageable,
            String token
    ) {
        Gender gender = publicUserRepository.getOne(CustomUserAuthenticator.getPublicUserIdFromToken(token)).getGender();
        return getPhysicalSessionsDtoList(
                physicalClassSessionRepository.getUpcomingPhysicalSessionsListByClassLimit(
                        physicalClass.getId(),
                        ClassSessionStatus.PENDING.toString(),
                        dateTime,
                        longitude,
                        latitude,
                        gender.toString(),
                        pageable
                ),
                token,
                dateTime
        );
    }

    @Override
    public List<ClassSessionListResponse> getPhysicalSessionsDtoList(List<Object[]> sessionObjectList, String token, LocalDateTime dateTime) {
        PublicUser publicUser = null;
        if (token != null) {
            long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
            publicUser = publicUserRepository.findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        }
        PublicUser finalPublicUser = publicUser;

        List<ClassSessionListResponse> classSessionListResponses = new ArrayList<>();
        for (Object[] object : sessionObjectList) {
            BigInteger id = (BigInteger) object[0];
            double distance = (double) object[1];
            PhysicalClassSession physicalSession = physicalClassSessionRepository.findById(id.longValue()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
            ClassSessionListResponse classSessionResponse = getClassSessionResponse(physicalSession, finalPublicUser, dateTime);
            classSessionResponse.setDistance(distance);
            membershipService.setPhysicalClassMembershipDetails(classSessionResponse, null, physicalSession.getPhysicalClass(), publicUser);
            classSessionListResponses.add(classSessionResponse);
        }
        Collections.sort(classSessionListResponses);
        return classSessionListResponses;
    }

    /**
     * Get class session by id.
     *
     * @param sessionId the class session id to get session.
     * @return the sessions response result.
     */
    @Override
    public ClassSessionSingleResponse getPhysicalSession(long sessionId, String token, LocalDateTime dateTime, double longitude, double latitude) {
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        PhysicalClassSession c = physicalClassSessionRepository.findById(sessionId).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
        AuthUser a = c.getTrainer().getAuthUser();
        PhysicalClass pc = c.getPhysicalClass();
        SessionButtonStatus buttonStatus = getButtonStatusBySession(c, pc, publicUser, dateTime);

        PublicUserDiscountDTO discountDetails = new PublicUserDiscountDTO();
        if (buttonStatus.equals(SessionButtonStatus.DISCOUNT)) {
            discountDetails = getDiscountDetails(publicUser);
        }

        BusinessProfileLocation businessProfileLocation = c.getBusinessProfileLocation();
        ClassSessionSingleResponse sessionResponse = ClassSessionSingleResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .duration(c.getDuration())
                .description(c.getDescription())
                .dateAndTime(c.getDateAndTime())
                .classImage(pc.getProfileImage())
                .endDateAndTime(c.getDateAndTime().plusMinutes(c.getDuration()))
                .price(c.getPrice())
                .gender(c.getGender())
                .trainerFirstName(a.getFirstName())
                .trainerLastName(a.getLastName())
                .className(pc.getName())
                .classTypeName(pc.getClassType().getTypeName())
                .trainerDescription(a.getDescription())
                .classRating(pc.getRating())
                .ratingCount(pc.getPhysicalClassRatings().size())
                .trainerRating(c.getTrainer().getRating())
                .trainerImage(a.getImage()).trainerRatingCount(c.getTrainer().getTrainerRatings().size())
                .trainerId(c.getTrainer().getId()).trainerUserId(a.getId()).sessionStatus(c.getStatus())
                .howToPrepare(pc.getHowToPrepare()).classDescription(pc.getDescription()).classId(pc.getId())
                .language(c.getLanguage() != null ? c.getLanguage().getLanguageName() : null)
                .buttonStatus(buttonStatus).discountMaxAmount(discountDetails.getMaxDiscount()).discountPercentage(discountDetails.getPercentage())
                .discountDescription(discountDetails.getDescription()).maxJoiners(c.getMaxJoiners()).availableCount(getAvailableCountBySession(c))
                .location(mapLocation(businessProfileLocation))
                .facilities(facilityService.getFacilityDTOListFromLocationFacilityList(businessProfileLocation.getLocationFacilities()))
                .distance(locationService.getDistance(longitude, businessProfileLocation.getLongitude(), latitude, businessProfileLocation.getLatitude()))
                .allowCashPayment(c.isAllowCashPayment())
                .youtubeUrl(pc.getYoutubeUrl())
                .build();

        membershipService.setPhysicalClassMembershipDetails(sessionResponse, null, pc, publicUser);
        return sessionResponse;
    }

    @Override
    public ClassSessionSingleResponse getPhysicalSessionByClassNameAndDate(String className, LocalDateTime dateTime, String token, LocalDateTime timeZoneDateTime, double longitude, double latitude) {
        Optional<PhysicalClass> physicalClassByClassUniqueName = physicalClassRepository.findPhysicalClassByClassUniqueName(className);
        if (!physicalClassByClassUniqueName.isPresent()) {
            throw new CustomServiceException(NO_CLASS_FOUND);
        }
        PhysicalClass physicalClass = physicalClassByClassUniqueName.get();
        Optional<PhysicalClassSession> optionalPhysicalClassSession = physicalClassSessionRepository.findClassSessionByClassParentAndDateAndTime(physicalClass, dateTime);
        if (!optionalPhysicalClassSession.isPresent()) {
            throw new CustomServiceException(NO_SESSION_FOUND);
        }
        PhysicalClassSession physicalClassSession = optionalPhysicalClassSession.get();
        return getPhysicalSession(physicalClassSession.getId(), token, timeZoneDateTime, longitude, latitude);
    }

    /**
     * Get pending class sessions by class.
     *
     * @param classId the class entity id to get sessions.
     * @return the sessions response results list.
     */
    @Override
    public Page<ClassSessionListResponse> getSessionsListByPhysicalClassId(
            long classId,
            LocalDateTime dateTime,
            String token,
            double longitude,
            double latitude,
            Pageable pageable
    ) {
        PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        return getUpcomingPhysicalSessionsList(physicalClass, dateTime, token, longitude, latitude, pageable);
    }

    /**
     * This can use to gt physical classes of given trainer.
     *
     * @param trainerUserId the trainer to get physical classes
     * @return the physical classes list
     */
    @Override
    public List<ClassListDTO> getPhysicalClassesByTrainer(long trainerUserId, String token) {
        Trainer trainer = authUserRepository.findById(trainerUserId).orElseThrow(() -> new CustomServiceException(NO_TRAINER_FOUND)).getTrainer();
        if (trainer == null) throw new CustomServiceException(NO_TRAINER_FOUND);
        PublicUser publicUser = getPublicUserByToken(token);
        return physicalClassRepository.getPhysicalClassesByTrainer(trainer, publicUser.getCountry()).stream().map(
                physicalClass -> getClassDetailsByClass(physicalClass, publicUser)).collect(Collectors.toList());
    }

    /**
     * Get physical classes by business profile
     *
     * @param businessProfileId the business profile id.
     * @return the physical sessions query results page.
     */
    @Override
    public Page<ClassListDTO> getPhysicalClassesByBusinessProfile(long businessProfileId, String country, Pageable pageable, String token) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId)
                .orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<PhysicalClass> classesByProfile = physicalClassSessionRepository.getPhysicalClassesByBusinessProfileAndCountry(businessProfile, country, pageable);
        return getClassDetailsPage(classesByProfile, token);
    }

    /**
     * This can use to set new rating from public user to physical class. this also updates the business profile rating.
     *
     * @param rateDTO the rating details DTO.
     */
    @Override
    @Transactional
    public void ratePhysicalClass(ClassRateDTO rateDTO, int count) {
        try {
            if (rateDTO.getRating() < 0 || rateDTO.getRating() > 5)
                throw new CustomServiceException("Invalid rating amount");
            PublicUser publicUser = publicUserRepository
                    .findById(rateDTO.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            PhysicalClass physicalClass = physicalClassRepository.findById(rateDTO.getClassId()).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
            List<PhysicalClassRating> ratingByUserForThisClass = physicalClassRatingRepository.findByPublicUserAndPhysicalClass(publicUser, physicalClass);
            if (ratingByUserForThisClass.size() > 0) {
                updateRating(rateDTO, ratingByUserForThisClass.get(0), physicalClass);
            } else {
                newRating(rateDTO, publicUser, physicalClass);
            }
        } catch (LockAcquisitionException | CannotAcquireLockException de) {
            // re-tries up-to 3 times if transaction deadlock found.
            if (count > 3) return;
            ratePhysicalClass(rateDTO, count + 1);
        }
    }

    private void updateRating(ClassRateDTO rateDTO, PhysicalClassRating rating, PhysicalClass physicalClass) {

        rating.setRating(rateDTO.getRating());
        rating.setComment(rateDTO.getComment());
        rating = physicalClassRatingRepository.save(rating);
        log.info("Update physical class rating - " + rating);

        // set class rating
        double newClassRating = ((physicalClass.getRating() * physicalClass.getRatingCount() - physicalClass.getRating())
                + rateDTO.getRating()) / (physicalClass.getRatingCount());
        physicalClass.setRating(CustomGenerator.round(newClassRating, RATE_DECIMAL_PLACES));
        physicalClassRatingRepository.save(rating);
        log.info("New physical class rating - " + newClassRating);

        // set business profile rating
        BusinessProfile businessProfile = physicalClass.getBusinessProfile();
        double newBusinessRating = ((businessProfile.getRating() * businessProfile.getRatingCount() - businessProfile.getRating())
                + physicalClass.getRating()) / (businessProfile.getRatingCount());
        businessProfile.setRating(CustomGenerator.round(newBusinessRating, RATE_DECIMAL_PLACES));
        businessProfileRepository.save(businessProfile);
        log.info("New business profile rating - " + newClassRating);
    }

    private void newRating(ClassRateDTO rateDTO, PublicUser publicUser, PhysicalClass physicalClass) {
        PhysicalClassRating rating = new PhysicalClassRating();
        rating.setComment(rateDTO.getComment());
        rating.setRating(rateDTO.getRating());
        rating.setPublicUser(publicUser);
        rating.setPhysicalClass(physicalClass);
        rating = physicalClassRatingRepository.save(rating);
        log.info("Save physical class rating - " + rating);

        // set class rating
        double newClassRating = (physicalClass.getRating() * physicalClass.getRatingCount() + rateDTO.getRating())
                / (physicalClass.getRatingCount() + 1);
        physicalClass.setRating(CustomGenerator.round(newClassRating, RATE_DECIMAL_PLACES));
        physicalClass.setRatingCount(physicalClass.getRatingCount() + 1);
        physicalClassRepository.save(physicalClass);
        log.info("New physical class rating - " + newClassRating);


        // set business profile rating
        BusinessProfile businessProfile = physicalClass.getBusinessProfile();
        double newBusinessRating = (businessProfile.getRating() * businessProfile.getRatingCount() + physicalClass.getRating())
                / (businessProfile.getRatingCount() + 1);
        businessProfile.setRating(CustomGenerator.round(newBusinessRating, RATE_DECIMAL_PLACES));
        businessProfile.setRatingCount(businessProfile.getRatingCount() + 1);
        businessProfileRepository.save(businessProfile);
        log.info("New business profile rating - " + newClassRating);
    }

    /**
     * @param publicUserId    the public user id
     * @param physicalClassId physical class id to check
     * @return the rating details if presents, or null if not.
     */
    @Override
    public ClassRateDTO getRateForPhysicalClassByUser(long publicUserId, long physicalClassId) {
        PublicUser publicUser = publicUserRepository
                .findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        PhysicalClass physicalClass = physicalClassRepository.findById(physicalClassId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        List<PhysicalClassRating> ratingByUserForThisClass = physicalClassRatingRepository.findByPublicUserAndPhysicalClass(publicUser, physicalClass);
        if (ratingByUserForThisClass.size() <= 0) return null;
        PhysicalClassRating c = ratingByUserForThisClass.get(0);
        return ClassRateDTO.builder().classId(physicalClassId).comment(c.getComment()).rating(c.getRating())
                .userId(c.getPublicUser().getId()).build();
    }

    /**
     * @param physicalClassId the physical class id
     * @param pageable        the pageable request
     * @return the ratings page
     */
    @Override
    public Page<PublicUserReviewsResponse> getPhysicalClassRatingsByUser(long physicalClassId, Pageable pageable) {
        PhysicalClass physicalClass = physicalClassRepository.findById(physicalClassId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        return physicalClassRatingRepository.getPhysicalClassRatingsByPhysicalClass(physicalClass, pageable);
    }

    /**
     * This can use to rate a physical class and a trainer by given physical session.
     *
     * @param rateDTO the rate details for trainer and physical class.
     */
    @Override
    @Transactional
    public void ratePhysicalClassAndTrainer(ClassAndTrainerRateDTO rateDTO, int count) {
        try {
            PhysicalClassSession physicalClassSession = physicalClassSessionRepository.findById(rateDTO.getSessionId())
                    .orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));

            ratePhysicalClass(ClassRateDTO.builder().classId(physicalClassSession.getPhysicalClass().getId())
                    .userId(rateDTO.getUserId()).rating(rateDTO.getClassRating()).comment(rateDTO.getClassComment()).build(), 0);

            trainerService.rateTrainer(TrainerRateDTO.builder().trainerId(physicalClassSession.getTrainer().getId()).
                    rating(rateDTO.getTrainerRating()).comment(rateDTO.getTrainerComment()).userId(rateDTO.getUserId()).build(), 0);
        } catch (LockAcquisitionException | CannotAcquireLockException de) {
            // re-tries up-to 3 times if transaction deadlock found.
            if (count > 3) return;
            ratePhysicalClassAndTrainer(rateDTO, count + 1);
        }
    }

    @Override
    public BigDecimal getUserDiscountForPhysicalSession(PublicUser publicUser, PhysicalClassSession physiSession) {
//        PublicUserDiscounts userDiscount = publicUserDiscountRepository
//                .findTopByPublicUserAndExpDateAfterOrderByExpDateAsc(publicUser, LocalDateTime.now());
        PublicUserDiscounts userDiscount = publicUserDiscountRepository
                .findTopByPublicUserAndExpDateAfterAndCategoryNotOrderByExpDateAsc(publicUser, LocalDateTime.now(), DiscountCategory.PROMO_CODE);
        if (userDiscount == null) return BigDecimal.ZERO;
        BigDecimal discountAmount = physiSession.getPrice()
                .multiply(new BigDecimal(Double.toString(userDiscount.getPercentage()))).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
        return discountAmount.compareTo(userDiscount.getMaxDiscount()) > 0 ? userDiscount.getMaxDiscount() : discountAmount;
    }

    @Override
    public BigDecimal getUserPromoDiscountForSession(PublicUser publicUser, PhysicalClassSession classSession, long discountId) {
        //check if discount is exists
        Optional<PublicUserDiscounts> optionalDiscount = publicUserDiscountRepository.findById(discountId);
        if (!optionalDiscount.isPresent()) throw new CustomServiceException("discount not found for id: " + discountId);
        PublicUserDiscounts userDiscount = optionalDiscount.get();

        /*check if discount is expired*/
        if (userDiscount.getExpDate().isBefore(LocalDateTime.now()))
            throw new CustomServiceException("Discount is expired");

        PhysicalClass classParent = classSession.getPhysicalClass();

        //check if discount is applicable -> if not applicable: send error
        boolean isDiscountApplicable = promoCodeManagementService.isPromoDiscountApplicable(
                publicUser,
                userDiscount,
                PromoCodeServiceCategory.FITNESS_CLASS,
                classParent.getId()
        );
        if (!isDiscountApplicable) throw new CustomServiceException("discount is not applicable");

        //calc discount amount
        BigDecimal discountAmount = classSession.getPrice()
                .multiply(new BigDecimal(Double.toString(userDiscount.getPercentage()))).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
        return discountAmount.compareTo(userDiscount.getMaxDiscount()) > 0 ? userDiscount.getMaxDiscount() : discountAmount;
    }

    @Override
    public ClassCoachDTO getPhysicalClassByIdForCoach(long id) {
        PhysicalClass c = physicalClassRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        return new ClassCoachDTO(c.getId(), c.getName(), c.getDescription(), c.getHowToPrepare(), c.getProfileImage(),
                getPhysicalClassImages(c.getPhysicalClassImages()), c.getRating(), c.getRatingCount(),
                c.getCalorieBurnOut(), getAvgPerWeekAndLastPrice(c, new ClassListDTO(), null).getAverageSessionsPerWeek(),
                getStartsPriceOfPhysicalClass(c),
                c.getPhysicalClassTrainers().stream().map(classTrainer -> trainerService.getTrainerForCoachApp(classTrainer.getTrainer())).sorted().collect(Collectors.toList()),
                getUpcomingSessionsForPhysicalClass(c), c.getBusinessProfile().getId(), c.getBusinessProfile().getBusinessName(), c.getBusinessProfile().getRating(),
                c.getBusinessProfile().getRatingCount(), c.getBusinessProfile().getProfileImage());
    }

    private double getStartsPriceOfPhysicalClass(PhysicalClass physicalClass) {
        PhysicalClassSession session = physicalClassSessionRepository.findTopByPhysicalClassOrderByPriceAsc(physicalClass);
        if (session != null) return session.getPrice().doubleValue();
        else return 0;
    }

    private List<SessionDateTimeDTO> getUpcomingSessionsForPhysicalClass(PhysicalClass physicalClass) {
        PageRequest pageRequest = PageRequest.of(0, 6, Sort.by(Sort.Direction.ASC, "dateAndTime"));
        return physicalClassSessionRepository.getUpcomingPhysicalSessionsListByClassLimit(
                physicalClass,
                LocalDateTime.now(),
                pageRequest
        ).stream().map(session -> new SessionDateTimeDTO(session.getId(), session.getDateAndTime(),
                session.getDateAndTime().plusMinutes(session.getDuration()))).collect(Collectors.toList());
    }

    @Override
    public Page<SessionDateTimeDTO> getAllUpcomingSessionsForClass(long classId, Pageable pageable) {
        PhysicalClass clazz = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        Page<PhysicalClassSession> sessionPage = physicalClassSessionRepository.getUpcomingSessionsPageByClassLimit(
                clazz,
                LocalDateTime.now(),
                pageable
        );
        return sessionPage.map(session -> new SessionDateTimeDTO(session.getId(), session.getDateAndTime(),
                session.getDateAndTime().plusMinutes(session.getDuration())));
    }

    @Override
    public Page<ClassForBusinessProfileDTO> getPhysicalClassesForBusinessProfile(long bpId, String username, Pageable pageable) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        Trainer trainer = authUser.getTrainer();
        if (trainer == null) return new PageImpl<>(new ArrayList<>(), pageable, 0);
        BusinessProfile bp = businessProfileRepository.findById(bpId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<PhysicalClass> classPage = physicalClassTrainerRepository.getPhysicalClassesByBusinessProfileAndTrainerOrderByRatingDesc(bp, trainer, pageable);
        return classPage.map(c -> new ClassForBusinessProfileDTO(
                c.getId(),
                c.getName(),
                c.getProfileImage(),
                c.getCalorieBurnOut(),
                getAvgPerWeek(c),
                c.getRating(),
                c.getRatingCount()));
    }

    @Override
    public Page<SessionDateTimeDTO> getUpcomingSessionByPhysicalClassForCoach(long classId, Pageable pageable) {
        PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        return physicalClassSessionRepository.getUpcomingPhysicalSessions(physicalClass, pageable).map(session ->
                new SessionDateTimeDTO(
                        session.getId(),
                        session.getDateAndTime(),
                        session.getDateAndTime().plusMinutes(session.getDuration())));
    }

    @Override
    public void delete(long id) {
        PhysicalClass physicalClass = physicalClassRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        physicalClassRepository.delete(physicalClass);
    }

    @Override
    public Page<SessionDetailDTO> getUpcomingSessionByBusinessProfile(long businessProfileId, LocalDate date, String token, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId)
                .orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(publicUserId)
                .orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        Page<PhysicalClassSession> classSessions = physicalClassSessionRepository.getUpcomingSessionByBusinessProfileAndDate(businessProfile, date, pageable);
        List<SessionDetailDTO> session = new ArrayList<>();

        for (PhysicalClassSession classSession : classSessions) {

            SessionDetailDTO sessionDetailDTO = modelMapper.map(classSession, SessionDetailDTO.class);

            Trainer trainer = classSession.getTrainer();
            AuthUser authUser = trainer.getAuthUser();
            PhysicalClass physicalClass = classSession.getPhysicalClass();
            String classType = physicalClass.getClassType().getTypeName();

            SessionButtonStatus buttonStatus = getButtonStatusBySession(classSession, physicalClass, publicUser, date.atStartOfDay());

            sessionDetailDTO.setEndDateAndTime(classSession.getDateAndTime().plusMinutes(classSession.getDuration()));
            sessionDetailDTO.setTrainerId(trainer.getId());
            sessionDetailDTO.setTrainerName(authUser.getFirstName() + " " + authUser.getLastName());
            sessionDetailDTO.setClassType(classType);
            sessionDetailDTO.setClassRating(physicalClass.getRating());
            sessionDetailDTO.setRatingCount(physicalClass.getRatingCount());
            sessionDetailDTO.setSessionStatus(buttonStatus);

            session.add(sessionDetailDTO);
        }
        return new PageImpl<>(session, pageable, session.size());
    }

    public void setPaymentMethod(String stripePaymentId, SessionEnrollDTO sessionEnrollDTO) {
        if (stripePaymentId.startsWith("CASH_")) sessionEnrollDTO.setPaymentMethod("CASH");
        else if (stripePaymentId.startsWith("FIRST_FREE_")) {
            sessionEnrollDTO.setPaymentMethod("FIRST_FREE");
            sessionEnrollDTO.setPaymentStatus(SessionEnrollStatus.FREE);
        } else sessionEnrollDTO.setPaymentMethod("ONLINE");
    }

}
