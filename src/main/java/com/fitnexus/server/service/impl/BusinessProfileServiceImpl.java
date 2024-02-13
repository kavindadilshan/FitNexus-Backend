package com.fitnexus.server.service.impl;

import com.fitnexus.server.config.email.OnRegistrationCompleteEvent;
import com.fitnexus.server.constant.FitNexusConstants;
import com.fitnexus.server.dto.businessprofile.*;
import com.fitnexus.server.dto.classes.ClassForBusinessProfileDTO;
import com.fitnexus.server.dto.classes.ClassRevenueSummaryDTO;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.membership.MembershipRevenueSummaryDTO;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.auth.UserRoleDetail;
import com.fitnexus.server.entity.businessprofile.*;
import com.fitnexus.server.entity.classes.*;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.hide.HiddenClassType;
import com.fitnexus.server.entity.classes.physical.PhysicalClassMembership;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.classes.physical.PhysicalSessionEnroll;
import com.fitnexus.server.entity.gym.Gym;
import com.fitnexus.server.entity.gym.GymMembership;
import com.fitnexus.server.entity.instructor.InstructorPackage;
import com.fitnexus.server.entity.instructor.InstructorPackageEnroll;
import com.fitnexus.server.entity.membership.Membership;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.publicuser.PublicUserDiscountsHistory;
import com.fitnexus.server.entity.publicuser.PublicUserMembership;
import com.fitnexus.server.entity.trainer.Trainer;
import com.fitnexus.server.enums.*;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.auth.UserRoleDetailRepository;
import com.fitnexus.server.repository.auth.UserRoleRepository;
import com.fitnexus.server.repository.businessprofile.*;
import com.fitnexus.server.repository.classes.*;
import com.fitnexus.server.repository.classes.hide.HiddenClassTypeRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassMembershipRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassSessionRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalSessionEnrollRepository;
import com.fitnexus.server.repository.gym.GymMembershipRepository;
import com.fitnexus.server.repository.gym.GymRepository;
import com.fitnexus.server.repository.instructor.InstructorPackageEnrollRepository;
import com.fitnexus.server.repository.instructor.InstructorPackageRepository;
import com.fitnexus.server.repository.publicuser.PublicUserMembershipRepository;
import com.fitnexus.server.repository.publicuser.PublicUserRepository;
import com.fitnexus.server.service.*;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.FileHandler;
import com.fitnexus.server.util.UsernameGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.fitnexus.server.constant.FitNexusConstants.AmountConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.PatternConstants.REGEX;
import static com.fitnexus.server.enums.BusinessProfileLocationType.BRANCH;
import static com.fitnexus.server.enums.BusinessProfileLocationType.HEAD_OFFICE;
import static com.fitnexus.server.util.FileHandler.BUSINESS_FOLDER;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BusinessProfileServiceImpl implements BusinessProfileService {

    private final AuthUserRepository authUserRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProfileImageRepository businessProfileImageRepository;
    private final BusinessProfileLocationRepository businessProfileLocationRepository;
    private final BusinessAgreementRepository businessAgreementRepository;
    private final BusinessProfilePaymentRepository businessProfilePaymentRepository;
    private final PackageDetailRepository packageDetailRepository;
    private final BusinessProfileManagerRepository businessProfileManagerRepository;
    private final BusinessProfileRevenueRepository businessProfileRevenueRepository;
    private final PaymentSettlementRepository paymentSettlementRepository;
    private final InstructorPackageEnrollRepository instructorPackageEnrollRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassRepository classRepository;
    private final ClassSessionEnrollRepository classSessionEnrollRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRoleDetailRepository userRoleDetailRepository;
    private final ClassTypeRepository classTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileHandler fileHandler;
    private final ApplicationEventPublisher eventPublisher;
    private final ModelMapper modelMapper;

    @Autowired
    private TrainerService trainerService;
    private final CommonUserService commonUserService;
    @Autowired
    private ClassSessionService classSessionService;
    private final HiddenClassTypeRepository hiddenClassTypeRepository;
    private final LocationFacilityRepository locationFacilityRepository;
    private final GymMembershipRepository gymMembershipRepository;
    private final PhysicalClassMembershipRepository physicalClassMembershipRepository;
    private final PhysicalSessionEnrollRepository physicalSessionEnrollRepository;
    private final PublicUserMembershipRepository publicUserMembershipRepository;
    private final InstructorPackageRepository instructorPackageRepository;
    private final PhysicalClassRepository physicalClassRepository;
    private final PhysicalClassSessionRepository physicalClassSessionRepository;
    private final BusinessProfileClassTypeRepository businessProfileClassTypeRepository;
    private final GymRepository gymRepository;
    private final OnlineClassMembershipRepository onlineClassMembershipRepository;
    private final PublicUserRepository publicUserRepository;
    private final UsernameGeneratorUtil usernameGeneratorUtil;

    @Autowired
    private CoachService coachService;
    private final LocationService locationService;

    @Value("${business_manager_login_page}")
    private String businessManagerLoginPage;

    @Override
    public String checkProfileExistence(BusinessProfileCreateDto dto) {
        String businessName = dto.getBusinessName();
        String registrationNumber = dto.getBusinessRegistrationNumber();
        String telephone = dto.getTelephone();
        String email = dto.getEmail();
        if (businessProfileRepository.existsByBusinessName(businessName))
            throw new CustomServiceException("We found another studio profile for given name");
        if (registrationNumber != null && !registrationNumber.isEmpty()) {
            if (businessProfileRepository.existsByRegNumber(registrationNumber))
                throw new CustomServiceException("We found another studio profile for given registration number");
        }
        if (businessProfileRepository.existsByTelephone(telephone))
            throw new CustomServiceException("We found another studio profile for given telephone number");
        if (businessProfileRepository.existsByEmail(email))
            throw new CustomServiceException("We found another studio profile for given email");
        return "Studio profile is not existing";
    }

    /**
     * this method checks if there is a business profile with same business name or register number
     * if found throws an exception
     * if not create new business profile entity and saves it
     *
     * @param dto details of business profile
     */
    @Override
    @Transactional
    public Map<String, String> createBusinessProfile(BusinessProfileCreateDto dto, String creatingUsername) {

        BusinessProfile bp = businessProfileRepository.findBusinessProfileByBusinessName(dto.getBusinessName());
        if (bp != null) throw new CustomServiceException("Business name already existing");
        if (dto.getBusinessRegistrationNumber() != null && !dto.getBusinessRegistrationNumber().isEmpty()) {
            bp = businessProfileRepository.findBusinessProfileByRegNumber(dto.getBusinessRegistrationNumber());
            if (bp != null) throw new CustomServiceException("Business registration number already existing");
        }

        bp = new BusinessProfile();
        bp.setBusinessName(dto.getBusinessName());
        bp.setRegNumber(dto.getBusinessRegistrationNumber());
        if (dto.getProfileImage() != null && !dto.getProfileImage().isEmpty()) {
            String name = dto.getBusinessName().replaceAll(REGEX, "") + "-profileImage" + UUID.randomUUID();
            bp.setProfileImage(fileHandler.saveBase64File(dto.getProfileImage(), name, BUSINESS_FOLDER));
        }
        bp.setDescription(dto.getDescription());
        bp.setTelephone(dto.getTelephone());
        bp.setEmail(dto.getEmail());
        bp.setAccountNumber(dto.getAccountNumber());
        bp.setAccountName(dto.getAccountName());
        bp.setBankName(dto.getBankName());
        bp.setBankCode(dto.getBankCode());
        bp.setBranchName(dto.getBranchName());
        bp.setBranchCode(dto.getBranchCode());
        bp.setSwiftCode(dto.getSwiftCode());
        bp.setCreatedBy(authUserRepository.findByUsername(creatingUsername).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND)));
        final BusinessProfile businessProfile = businessProfileRepository.save(bp);
        log.info("Save business profile - " + businessProfile);

        /*save unique businessName*/
        usernameGeneratorUtil.setBusinessProfileUniqueName(businessProfile);

        PackageDetail pd = new PackageDetail();
        pd.setAmount(BigDecimal.valueOf(dto.getAmount()));
        pd.setDescription(dto.getPackageDescription());
        pd.setPaymentModel(dto.getPaymentModel());
        pd = packageDetailRepository.save(pd);
        log.info("Save business profile package details - " + pd);

        BusinessProfileAgreementDTO agreementDetails = dto.getAgreementDetails();
        BusinessAgreement ba = new BusinessAgreement();
        ba.setBusinessProfile(businessProfile);
        ba.setPackageDetail(pd);
        ba.setStartDate(convertDateFromString(agreementDetails.getAgreementStartDate()));
        ba.setExpDate(convertDateFromString(agreementDetails.getAgreementExpireDate()));
        ba.setStatus(BusinessAgreementStatus.INACTIVE);
        if (agreementDetails.getFile() != null && !agreementDetails.getFile().isEmpty()) {
            String name = dto.getBusinessName().replaceAll(REGEX, "") + "-agreement" + UUID.randomUUID();
            ba.setFile(fileHandler.saveBase64File(agreementDetails.getFile(), name, BUSINESS_FOLDER));
        }
        ba = businessAgreementRepository.save(ba);
        log.info("Save business profile agreement details - " + ba);

        List<BusinessProfileLocation> locations = getLocations(dto, businessProfile);
        businessProfileLocationRepository.saveAll(locations);
        log.info("Save business profile locations - " + locations);

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            List<BusinessProfileImage> images = dto.getImages().stream().map(image ->
                    new BusinessProfileImage(businessProfile,
                            fileHandler.saveBase64File(image, businessProfile.getBusinessName().replaceAll(REGEX, "") + UUID.randomUUID(), BUSINESS_FOLDER)))
                    .collect(Collectors.toList());
            businessProfileImageRepository.saveAll(images);
            log.info("Save business profile images - " + images);
        }

        BusinessProfileManagerDTO managerDTO = dto.getManager();
        saveNewManager(managerDTO, bp);

        Map<String, String> result = new HashMap<>();
        result.put("username", managerDTO.getUsername());
        result.put("message", "Business profile created successfully - " + businessProfile.getBusinessName());
        return result;
    }

    /**
     * this method checks if there is a business profile for given register number
     * if not found throws an exception
     * if found calls updateDatabase method
     *
     * @param dto details of business profile
     */
    @Override
    @Transactional
    public void updateBusinessProfile(BusinessProfileCreateDto dto, String updatingUsername) {

        AuthUser updatingUser = authUserRepository.findByUsername(updatingUsername).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole userRole = commonUserService.getRole(updatingUser);
        BusinessProfile profile = businessProfileRepository.findById(dto.getProfileId()).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));

        if (userRole.equals(UserRole.BUSINESS_PROFILE_MANAGER)) {
            if (!updatingUser.getBusinessProfileManager().getBusinessProfile().equals(profile))
                throw new CustomServiceException("You can't update other business profiles");
        }

        //business name
        if (!profile.getBusinessName().equals(dto.getBusinessName())) {
            BusinessProfile bp = businessProfileRepository.findBusinessProfileByBusinessName(dto.getBusinessName());
            if (bp != null && bp.getId() != profile.getId())
                throw new CustomServiceException("Business name already existing");
            profile.setBusinessName(dto.getBusinessName());
        }

        //registration number
        if (dto.getBusinessRegistrationNumber() != null && !dto.getBusinessRegistrationNumber().isEmpty()) {
            BusinessProfile bp = businessProfileRepository.findBusinessProfileByRegNumber(dto.getBusinessRegistrationNumber());
            if (bp != null && bp.getId() != profile.getId())
                throw new CustomServiceException("Business registration number already existing");
        }
        profile.setRegNumber(dto.getBusinessRegistrationNumber());

        //description, telephone, email
        profile.setDescription(dto.getDescription());
        profile.setTelephone(dto.getTelephone());
        profile.setEmail(dto.getEmail());

        //profile image
        if (dto.getProfileImage().isEmpty() || dto.getProfileImage().startsWith("http://") || dto.getProfileImage().startsWith("https://")) {
            profile.setProfileImage(dto.getProfileImage());
        } else {
            String name = dto.getBusinessName().replaceAll(REGEX, "") + "-profile-image" + UUID.randomUUID();
            profile.setProfileImage(fileHandler.saveBase64File(dto.getProfileImage(), name, BUSINESS_FOLDER));
        }

        //head office and branches
        List<BusinessProfileLocation> locations = new ArrayList<>();

        BusinessProfileLocationDTO headOfficeDTO = dto.getHeadOffice();

        BusinessProfileLocation headOffice;

        if (headOfficeDTO.getId() != 0)
            headOffice = businessProfileLocationRepository.findById(headOfficeDTO.getId()).orElseThrow(() -> new CustomServiceException("Head office not found"));
        else {
            headOffice = businessProfileLocationRepository.findBusinessProfileLocationByTypeAndBusinessProfile(HEAD_OFFICE, profile);
            if (headOffice == null) headOffice = new BusinessProfileLocation();
        }
        headOffice.setName(headOfficeDTO.getName());
        headOffice.setAddressLine1(headOfficeDTO.getAddressLine1());
        headOffice.setAddressLine2(headOfficeDTO.getAddressLine2());
        headOffice.setCity(headOfficeDTO.getCity());
        headOffice.setProvince(headOfficeDTO.getProvince());
        headOffice.setCountry(headOfficeDTO.getCountry());
        headOffice.setPostalCode(headOfficeDTO.getPostalCode());
        headOffice.setLongitude(headOfficeDTO.getLongitude());
        headOffice.setLatitude(headOfficeDTO.getLatitude());
        headOffice.setTimeZone(headOfficeDTO.getTimeZone());
        locations.add(headOffice);

        List<BusinessProfileLocationDTO> branches = dto.getBranches();
        for (BusinessProfileLocationDTO branch : branches) {
            BusinessProfileLocation branchLocation;
            if (branch.getId() == 0) {
                branchLocation = new BusinessProfileLocation();
                branchLocation.setBusinessProfile(profile);
                branchLocation.setType(BRANCH);
            } else
                branchLocation = businessProfileLocationRepository.findById(branch.getId())
                        .orElseThrow(() -> new CustomServiceException("Branch not found - " + branch.getName()));
            branchLocation.setName(branch.getName());
            branchLocation.setAddressLine1(branch.getAddressLine1());
            branchLocation.setAddressLine2(branch.getAddressLine2());
            branchLocation.setCity(branch.getCity());
            branchLocation.setProvince(branch.getProvince());
            branchLocation.setCountry(branch.getCountry());
            branchLocation.setPostalCode(branch.getPostalCode());
            branchLocation.setLongitude(branch.getLongitude());
            branchLocation.setLatitude(branch.getLatitude());
            branchLocation.setTimeZone(branch.getTimeZone());
            locations.add(branchLocation);
        }

        List<BusinessProfileLocation> removingLocations = new ArrayList<>();
        List<BusinessProfileLocation> businessProfileLocations = profile.getBusinessProfileLocations();
        for (BusinessProfileLocation location : businessProfileLocations) {
            if (!locations.contains(location)) {
                Gym gym = location.getGym();
                List<PhysicalClassSession> physicalClassSessions = location.getPhysicalClassSessions();
                List<LocationFacility> locationFacilities = location.getLocationFacilities();
                if (gym != null)
                    throw new CustomServiceException(location.getName() + " has a gym. Can not remove this location");
                if (physicalClassSessions != null && physicalClassSessions.size() > 0)
                    throw new CustomServiceException(location.getName() + " has fitness class sessions. Can not remove this location");
                if (locationFacilities != null && locationFacilities.size() > 0) {
                    locationFacilityRepository.deleteAll(locationFacilities);
                }
                removingLocations.add(location);
            }
        }

        log.info("Remove locations - " + removingLocations);
        businessProfileLocationRepository.deleteAll(removingLocations);

        locations = businessProfileLocationRepository.saveAll(locations);
        log.info("Update locations - " + locations);

        //images
        List<String> allImages = businessProfileImageRepository.findAllImages(profile.getId());
        List<String> images = new ArrayList<>();
        for (String imageString : dto.getImages()) {
            if (!(imageString.startsWith("http://") || imageString.startsWith("https://"))) {
                String name = dto.getBusinessName().replaceAll(REGEX, "") + UUID.randomUUID();
                BusinessProfileImage newImage = new BusinessProfileImage(profile, fileHandler.saveBase64File(imageString, name, BUSINESS_FOLDER));
                businessProfileImageRepository.save(newImage);
                log.info("Save new business profile image - " + newImage);
            } else {
                images.add(imageString);
            }
        }
        allImages.removeAll(images);
        if (!allImages.isEmpty()) {
            for (String image : allImages) {
                businessProfileImageRepository.delete(businessProfileImageRepository.findBusinessProfileImageByImageAndBusinessProfile(image, profile));
            }
            log.info("Removed images : " + allImages);
        }

        //manager
        BusinessProfileManagerDTO manager = dto.getManager();
        BusinessProfileManager profileManager = profile.getBusinessProfileManager();
        if (profileManager != null) {
            AuthUser managerAuthUser = profileManager.getAuthUser();
            managerAuthUser.setFirstName(manager.getFirstName());
            managerAuthUser.setLastName(manager.getLastName());

            if (managerAuthUser.getEmail() != null && !managerAuthUser.getEmail().equals(manager.getEmail())) {
                managerAuthUser.setEmail(manager.getEmail());
                if ((managerAuthUser.getZoomUserId() != null && !managerAuthUser.getZoomUserId().isEmpty())) {
                    coachService.changeZoomMailAndReCreateZoom(managerAuthUser);
                }
                coachService.sendEmailUpdatedMail(manager.getEmail());
            }

            managerAuthUser.setMobile(manager.getMobile());
            if (manager.getTimeZone() != null) {
                String timeZone = manager.getTimeZone().split("[)]")[0].replace(":", "").replace("(", "").trim();
                managerAuthUser.setTimeZone(timeZone);
            }
            managerAuthUser.setTimeZoneLongName(manager.getTimeZone());
            managerAuthUser = authUserRepository.save(managerAuthUser);
            log.info("Update business profile manager - " + managerAuthUser);
        } else {
            saveNewManager(manager, profile);
        }

        //bank details
        profile.setBankName(dto.getBankName());
        profile.setBankCode(dto.getBankCode());
        profile.setAccountNumber(dto.getAccountNumber());
        profile.setAccountName(dto.getAccountName());
        profile.setBranchName(dto.getBranchName());
        profile.setBranchCode(dto.getBranchCode());
        profile.setSwiftCode(dto.getSwiftCode());

        /*save agreement pdf*/
        BusinessProfileAgreementDTO agreementDetails = dto.getAgreementDetails();
        if (agreementDetails != null) {
            BusinessAgreement businessAgreement = businessAgreementRepository.findById(agreementDetails.getAgreementId())
                    .orElseThrow(() -> new CustomServiceException(404, "No agreement found!"));

            if (agreementDetails.getFile() != null && !agreementDetails.getFile().isEmpty()) {
                String name = dto.getBusinessName().replaceAll(REGEX, "") + "-agreement" + UUID.randomUUID();
                businessAgreement.setFile(fileHandler.saveBase64File(agreementDetails.getFile(), name, BUSINESS_FOLDER));
            }
            businessAgreement = businessAgreementRepository.save(businessAgreement);
            log.info("Save business profile agreement details - " + businessAgreement);
        }

        profile = businessProfileRepository.save(profile);
        log.info("Update business profile - " + profile);
    }

    private void saveNewManager(BusinessProfileManagerDTO managerDTO, BusinessProfile businessProfile) {
        if (managerDTO.getEmail().isEmpty()) throw new CustomServiceException("Email can not be empty");
        Optional<AuthUser> byEmail = authUserRepository.findByEmail(managerDTO.getEmail());
        if (byEmail.isPresent())
            throw new CustomServiceException(FitNexusConstants.DuplicatedConstants.EMAIL_ALREADY_EXISTS + " - " + managerDTO.getEmail());
        Optional<AuthUser> byUsername = authUserRepository.findByUsername(managerDTO.getUsername());
        if (byUsername.isPresent())
            throw new CustomServiceException(FitNexusConstants.DuplicatedConstants.USERNAME_ALREADY_EXISTS + " - " + managerDTO.getUsername());
        Optional<AuthUser> byMobile = authUserRepository.findByMobile(managerDTO.getMobile());
        if (byMobile.isPresent())
            throw new CustomServiceException(FitNexusConstants.DuplicatedConstants.MOBILE_ALREADY_EXISTS + " - " + managerDTO.getMobile());
        AuthUser authUser = new AuthUser();
        authUser.setFirstName(managerDTO.getFirstName());
        authUser.setLastName(managerDTO.getLastName());
        authUser.setEmail(managerDTO.getEmail());
        authUser.setMobile(managerDTO.getMobile());
        authUser.setUsername(managerDTO.getUsername());

        String password = CustomGenerator.generatePassword();
        authUser.setPassword(passwordEncoder.encode(password));
        if (managerDTO.getTimeZone() != null) {
            String timeZone = managerDTO.getTimeZone().split("[)]")[0].replace(":", "").replace("(", "").trim();
            authUser.setTimeZone(timeZone);
        }
        authUser.setTimeZoneLongName(managerDTO.getTimeZone());
        authUser.setStatus(UserStatus.PENDING);

        AuthUser savedAuthUser = authUserRepository.save(authUser);
        log.info("Save manager auth user- " + authUser);

        UserRoleDetail userRoleDetail = new UserRoleDetail();
        userRoleDetail.setUserRole(userRoleRepository.findUserRoleByRole(UserRole.BUSINESS_PROFILE_MANAGER));
        userRoleDetail.setAuthUser(authUser);
        userRoleDetail = userRoleDetailRepository.save(userRoleDetail);
        log.info("Save manager user role- " + userRoleDetail);

        BusinessProfileManager manager = new BusinessProfileManager(ManagerStatus.PENDING, authUser, businessProfile, false);
        manager = businessProfileManagerRepository.save(manager);
        log.info("Save manager details- " + manager);

        //send email
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(authUser, password, Locale.ENGLISH, businessManagerLoginPage));
        log.info("Email sent successfully");

        /*set publicUsername*/
        usernameGeneratorUtil.setAuthUserUniqueName(savedAuthUser);
    }

    /**
     * find profiles which contains given content in name or registration number and creates page of BusinessProfileResponseDto
     *
     * @param data     searching content
     * @param pageable the pageable request
     * @return created page
     */
    @Override
    public Page<BusinessProfileResponseDto> searchBusinessProfiles(String data, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        Page<BusinessProfile> page = businessProfileRepository.searchBusinessProfile(data, pageRequest);
        List<BusinessProfile> allProfiles = page.getContent();
        List<BusinessProfileResponseDto> result = new ArrayList<>();
        for (BusinessProfile profile : allProfiles) {
            result.add(convert(profile));
        }
        return new PageImpl<>(result, pageRequest, page.getTotalElements());
    }


    /**
     * this get all profile details from the database and creates a list
     *
     * @return created lis of BusinessProfileDto
     */
    @Override
    public Page<BusinessProfileResponseDto> getAllProfiles(Pageable pageable, String username) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole userRole = commonUserService.getRole(authUser);
        if (userRole.equals(UserRole.SUPER_ADMIN)) {
            Page<BusinessProfile> page = businessProfileRepository.findAllByOrderByBusinessNameAsc(pageRequest);
            List<BusinessProfile> allProfiles = page.getContent();
            List<BusinessProfileResponseDto> result = new ArrayList<>();
            for (BusinessProfile profile : allProfiles) {
                result.add(convert(profile));
            }
            return new PageImpl<>(result, pageRequest, page.getTotalElements());
        } else if (userRole.equals(UserRole.BUSINESS_PROFILE_MANAGER)) {
            List<BusinessProfileResponseDto> result = new ArrayList<>();
            result.add(convert(authUser.getBusinessProfileManager().getBusinessProfile()));
            return new PageImpl<>(result, pageRequest, 1);
        } else {
            return null;
        }
    }

    @Override
    public List<BusinessProfileResponseDto> getAllBusinessProfiles() {
        List<BusinessProfile> businessProfiles = businessProfileRepository.findAll();
        return modelMapper.map(businessProfiles, new TypeToken<List<BusinessProfileResponseDto>>() {
        }.getType());
    }

    /**
     * get names and IDs of all business profiles create a list of map objects
     *
     * @return created list
     */
    @Override
    public List<BusinessProfileNameIdDTO> getAllProfileIDsAndNames(String username) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole userRole = commonUserService.getRole(authUser);
        if (userRole.equals(UserRole.SUPER_ADMIN)) {
            return businessAgreementRepository.findBusinessAgreementsByStatusOrderByAgreementIdAsc(BusinessAgreementStatus.ACTIVE).stream().map(
                    ba -> new BusinessProfileNameIdDTO(ba.getBusinessProfile().getId(), ba.getBusinessProfile().getBusinessName()))
                    .collect(Collectors.toList())
                    .stream().sorted(Comparator.comparing(BusinessProfileNameIdDTO::getName)).collect(Collectors.toList());
        } else return getBusinessProfileNameForManager(authUser, userRole);
    }

    /**
     * @param username the requesting user
     * @return the business profile name, id list which has instructors
     */
    @Override
    public List<BusinessProfileNameIdDTO> getBusinessNamesHasInstructors(String username) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole userRole = commonUserService.getRole(authUser);
        if (userRole.equals(UserRole.SUPER_ADMIN)) {
            return businessProfileRepository.getProfileNamesHasInstructors();
        } else {
            return getBusinessProfileNameForManager(authUser, userRole);
        }
    }

    /**
     * finds the relevant business profile entity for given ID and crates a BusinessProfileResponseDto
     *
     * @param id ID of the business profile
     * @return created DTO object
     */
    @Override
    public BusinessProfileResponseDto getBusinessProfileByID(long id, String username) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole userRole = commonUserService.getRole(authUser);
        BusinessProfile businessProfile = businessProfileRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        if (userRole.equals(UserRole.BUSINESS_PROFILE_MANAGER)) {
            if (!authUser.getBusinessProfileManager().getBusinessProfile().equals(businessProfile))
                throw new CustomServiceException("You can view only your business profile");
        }
        return convert(businessProfile);
    }

    /**
     * finds the relevant business profile for given ID and latest business agreement for the profile
     * <p>
     * if status is INACTIVE
     * checks if the profile agreement is expired
     * if it is expired changes agreement's status to INACTIVE without checking any conditions
     * if it is not checks if the profile has active personal session enrollments and pending class sessions
     * if it has set agreement status INACTIVE and set returning message a warning
     * if not set agreement status INACTIVE
     * <p>
     * if status is ACTIVE set agreement status ACTIVE
     *
     * @param id               business profile id
     * @param status           changing status
     * @param updatingUsername who changes the profile status
     * @return created string with or without warnings
     */
    @Override
    @Transactional
    public String changeProfileStatus(long id, BusinessAgreementStatus status, String updatingUsername) {
        String message = "";

        BusinessProfile profile = businessProfileRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        BusinessAgreement latestAgreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(profile);

        if (status.equals(BusinessAgreementStatus.ACTIVE)) {
            latestAgreement.setStatus(BusinessAgreementStatus.ACTIVE);
            message = profile.getBusinessName() + " is activated";
        } else if (status.equals(BusinessAgreementStatus.INACTIVE)) {

            if (latestAgreement.getExpDate().isBefore(LocalDateTime.now())) {
                message = profile.getBusinessName() + " is deactivated";
            } else {
                List<InstructorPackageEnroll> packageEnrolls = instructorPackageEnrollRepository.findInstructorPackageEnrollsByInstructorPackage_BusinessProfile(profile);
                boolean hasActivePersonalSessions = false;
                boolean hasPendingClassSessions = false;

                if (packageEnrolls != null && packageEnrolls.size() > 0) {
                    for (InstructorPackageEnroll enroll : packageEnrolls) {
                        LocalDateTime dateTime = enroll.getDateTime().plusDays(enroll.getInstructorPackage().getInstructorPackageType().getTimePeriod());
                        if (!LocalDateTime.now().isBefore(dateTime)) {
                            hasActivePersonalSessions = true;
                            break;
                        }
                    }
                }

                List<ClassSession> classSessions = classSessionRepository.findClassSessionsByClassParent_BusinessProfileAndStatus(profile, ClassSessionStatus.PENDING);
                if (classSessions != null && classSessions.size() > 0) {
                    hasPendingClassSessions = true;
                }

                if (hasActivePersonalSessions && hasPendingClassSessions) {
                    message = profile.getBusinessName() + " is deactivated.But it has active personal sessions and pending class sessions";
                } else if (hasActivePersonalSessions) {
                    message = profile.getBusinessName() + " is deactivated.But it has active personal sessions";
                } else if (hasPendingClassSessions) {
                    message = profile.getBusinessName() + " is deactivated.But it has pending class sessions";
                } else {
                    message = profile.getBusinessName() + " is deactivated";
                }
            }
            latestAgreement.setStatus(BusinessAgreementStatus.INACTIVE);
        }
        profile.setUpdatedBy(authUserRepository.findByUsername(updatingUsername).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND)));
        businessProfileRepository.save(profile);
        businessAgreementRepository.save(latestAgreement);

        return message;
    }

    /**
     * finds all payment models of business profiles and creates map object with that data
     *
     * @return created map object
     */
    @Override
    public Page<BusinessProfileListResponse> getActiveBusinessProfiles(String name, List<Long> classTypeIds,
                                                                       String type, long userId,
                                                                       double longitude, double latitude,
                                                                       boolean corporateOnly,
                                                                       Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        String country = publicUser.getCountry();
        Page<BusinessProfile> businessProfiles;


        // type - GROUP, PERSONAL, PHYSICAL, INSTRUCTOR_PACKAGE ,GYM
        if (type == null || type.isEmpty()) {
            if (classTypeIds != null && classTypeIds.size() > 0) {
                businessProfiles = businessProfileClassTypeRepository.getBusinessProfileByClassTypes(name, classTypeIds, pageable);
            } else {
                businessProfiles = businessProfileRepository.getAllActiveBusinessProfiles(name, pageable);
            }
        } else if (type.equals("GROUP")) {
            if (corporateOnly)
                businessProfiles = classRepository.getActiveCorporateBusinessProfiles(name, classTypeIds, ClassCategory.GROUP, publicUser, pageable);
            else
                businessProfiles = classRepository.getActiveBusinessProfilesByClassTypeAndCategory(name, classTypeIds, ClassCategory.GROUP, pageable);
        } else if (type.equals("PERSONAL")) {
            businessProfiles = classRepository.getActiveBusinessProfilesByClassTypeAndCategory(name, classTypeIds, ClassCategory.PERSONAL, pageable);
        } else if (type.equals("PHYSICAL")) {
            businessProfiles = physicalClassRepository.getActiveBusinessProfilesByClassType(name, classTypeIds, country, pageable);
        } else if (type.equals("INSTRUCTOR_PACKAGE")) {
            businessProfiles = instructorPackageRepository.getActiveBusinessProfiles(name, pageable);
        } else if (type.equals("GYM")) {
            businessProfiles = gymRepository.getActiveBusinessProfiles(name, country, pageable);
        } else throw new CustomServiceException("Invalid type");

        if (longitude != 0 && latitude != 0) {
            Page<BusinessProfileListResponse> page = businessProfiles.map(businessProfile -> getBusinessProfileResponse(businessProfile, longitude, latitude));

            List<BusinessProfileListResponse> content = new ArrayList<>(page.getContent());

            content.sort(Comparator.comparingDouble(BusinessProfileListResponse::getDistance));

            PageImpl<BusinessProfileListResponse> newPage = new PageImpl<>(content,
                    PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "distance")),
                    page.getTotalElements());

            return newPage;
        } else return businessProfiles.map(this::getBusinessProfileResponse);
    }


    @Override
    public BusinessProfileListResponse getBusinessProfile(long id) {

        BusinessProfile businessProfile = businessProfileRepository.findById(id)
                .orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));

        BusinessProfileSingleResponse businessProfileResponse = modelMapper.map(businessProfile, BusinessProfileSingleResponse.class);

        businessProfileResponse.setAddresses(businessProfile.getBusinessProfileLocations().stream().map(this::getAddressResponse).collect(Collectors.toList()));
        businessProfileResponse.setImages(businessProfile.getBusinessProfileImages().stream().map(BusinessProfileImage::getImage).collect(Collectors.toList()));

        List<String> classTypes = getClassTypesForBusinessProfile(businessProfile);
        businessProfileResponse.setTypes(classTypes);
        businessProfileResponse.setClassTypes(classTypes);

        HiddenClassType classType = hiddenClassTypeRepository.findHiddenClassTypeByType(ClassMethod.ONLINE);

        businessProfileResponse.setOnlineClassesVisible(!classType.isHidden());

        long gymMembershipCount = gymMembershipRepository.countDistinctByMembership_TypeAndGym_Location_BusinessProfile(MembershipType.GYM, businessProfile);
        long physicalClassMembershipCount = physicalClassMembershipRepository.countDistinctByPhysicalClass_BusinessProfileAndMembership_StatusNot(businessProfile, MembershipStatus.HIDDEN);
        long onlineClassMembershipCount = onlineClassMembershipRepository.countDistinctByClassParent_BusinessProfileAndMembership_Type(businessProfile, MembershipType.ONLINE_CLASS);

        businessProfileResponse.setGymMembershipCount(gymMembershipCount);
        businessProfileResponse.setPhysicalClassMembershipCount(physicalClassMembershipCount);
        businessProfileResponse.setOnlineClassMembershipCount(onlineClassMembershipCount);
        businessProfileResponse.setMembershipCount(gymMembershipCount + physicalClassMembershipCount + onlineClassMembershipCount);

        return getAllAvgClassesPerWeek(businessProfile, businessProfileResponse);
    }

    @Override
    public BusinessProfileListResponse getBusinessProfileByName(String businessName) {
        BusinessProfile businessProfileByPublicBusinessName = businessProfileRepository.findBusinessProfileByPublicBusinessName(businessName);
        if (businessProfileByPublicBusinessName == null) {
            throw new CustomServiceException("Can't find this business profile");
        }

        return getBusinessProfile(businessProfileByPublicBusinessName.getId());
    }

    @Override
    public BusinessProfileCoachDTO getBusinessProfileForCoach(long id, String username) {
        BusinessProfile bp = businessProfileRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        BusinessProfileListResponse bpListResponse = getAllAvgClassesPerWeek(bp, new BusinessProfileListResponse());
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        return new BusinessProfileCoachDTO(bp.getBusinessName(), bp.getRating(), bp.getRatingCount(), bp.getProfileImage(),
                bpListResponse.getAverageClassesPerWeek(), getBusinessProfileCountry(bp), bp.getDescription(), getClassTypesForBusinessProfile(bp),
                bp.getTrainerBusinessProfiles().stream().map(trainerBusinessProfile -> trainerService.getTrainerForCoachApp(trainerBusinessProfile.getTrainer())).sorted().collect(Collectors.toList()),
                getClassesForBusinessProfile(bp, authUser.getTrainer()));
    }

    @Override
    @Transactional
    public void updateBusinessProfileSessionRevenue(ClassSessionEnroll classSessionEnroll) {
        try {
            PublicUserDiscountsHistory discount = classSessionEnroll.getPublicUserDiscountsHistory();
            ClassSession classSession = classSessionEnroll.getClassSession();
            PublicUser publicUser = classSessionEnroll.getPublicUser();
            BusinessProfile businessProfile = classSession.getClassParent().getBusinessProfile();
            String description = "Payment (" + classSessionEnroll.getPaidAmount() + ") " +
                    "for session of " + classSession.getClassParent().getName() +
                    " (" + classSession.getDateAndTime() + ") by "
                    + publicUser.getFirstName() + " " + publicUser.getLastName();
            saveRevenueWithDiscount(businessProfile, description, classSessionEnroll.getId(), classSessionEnroll.getPaymentId(),
                    classSessionEnroll.getPaidAmount(), classSession.getPrice(), discount, BusinessProfileRevenueType.SESSION);
        } catch (Exception e) {
            log.error("Error saving business profile revenue by session: {}", e);
        }
    }

    @Override
    @Transactional
    public void updateBusinessProfilePhysicalSessionRevenue(PhysicalSessionEnroll physicalSessionEnroll) {
        try {
            PublicUserDiscountsHistory discount = physicalSessionEnroll.getPublicUserDiscountsHistory();
            PhysicalClassSession physicalSession = physicalSessionEnroll.getPhysicalClassSession();
            PublicUser publicUser = physicalSessionEnroll.getPublicUser();
            BusinessProfile businessProfile = physicalSession.getPhysicalClass().getBusinessProfile();
            String description = "Payment (" + physicalSessionEnroll.getPaidAmount() + ") " +
                    "for physical session of " + physicalSession.getPhysicalClass().getName() +
                    " (" + physicalSession.getDateAndTime() + ") by "
                    + publicUser.getFirstName() + " " + publicUser.getLastName();
            saveRevenueWithDiscount(businessProfile, description, physicalSessionEnroll.getId(), physicalSessionEnroll.getPaymentId(),
                    physicalSessionEnroll.getPaidAmount(), physicalSession.getPrice(), discount, BusinessProfileRevenueType.PHYSICAL_SESSION);
        } catch (Exception e) {
            log.error("Error saving business profile revenue by session: {}", e);
        }
    }

    @Override
    @Transactional
    public void updateBusinessProfileInstructorPackageRevenue(InstructorPackageEnroll enroll) {
        try {
            PublicUser publicUser = enroll.getPublicUser();
            InstructorPackage instructorPackage = enroll.getInstructorPackage();
            BusinessProfile businessProfile = instructorPackage.getBusinessProfile();
            String description = "Payment (" + enroll.getPaidAmount() + ") " +
                    "for personal coaching package (" + instructorPackage.getName() + ") by "
                    + publicUser.getFirstName() + " " + publicUser.getLastName();
            saveRevenueWithOutDiscount(businessProfile, description, enroll.getPackageEnrollId(), enroll.getStripePaymentId(),
                    enroll.getPaidAmount(), instructorPackage.getPrice(), BusinessProfileRevenueType.INSTRUCTOR_PACKAGE,IPGType.PAYHERE);
        } catch (Exception e) {
            log.error("Error saving business profile revenue by instructor package: {}", e);
        }
    }

    @Override
    @Transactional
    public void updateBusinessProfileMembershipRevenue(PublicUserMembership userMembership,IPGType ipgType) {
        try {
            PublicUser publicUser = userMembership.getPublicUser();

            Membership membership = userMembership.getMembership();

            BusinessProfile businessProfile;

            if (membership.getType().equals(MembershipType.PHYSICAL_CLASS)) {
                List<PhysicalClassMembership> physicalClassMemberships = membership.getPhysicalClassMemberships();
                PhysicalClassMembership physicalClassMembership = physicalClassMemberships.get(0);
                businessProfile = physicalClassMembership.getPhysicalClass().getBusinessProfile();
            } else if (membership.getType().equals(MembershipType.ONLINE_CLASS)) {
                List<OnlineClassMembership> onlineClassMemberships = membership.getOnlineClassMemberships();
                OnlineClassMembership onlineClassMembership = onlineClassMemberships.get(0);
                businessProfile = onlineClassMembership.getClassParent().getBusinessProfile();
            } else {
                businessProfile = membership.getGymMembership().getGym().getLocation().getBusinessProfile();
            }

            String description = "Payment (" + userMembership.getPaidAmount() + ") " +
                    "for membership " +
                    " (" + membership.getName() + "-" + membership.getType() + ") by "
                    + publicUser.getFirstName() + " " + publicUser.getLastName();

            BusinessProfileRevenueType revenueType = null;

            if (membership.getType().equals(MembershipType.PHYSICAL_CLASS))
                revenueType = BusinessProfileRevenueType.MEMBERSHIP_PHYSICAL_CLASS;

            if (membership.getType().equals(MembershipType.ONLINE_CLASS))
                revenueType = BusinessProfileRevenueType.MEMBERSHIP_ONLINE_CLASS;

            if (membership.getType().equals(MembershipType.GYM))
                revenueType = BusinessProfileRevenueType.MEMBERSHIP_GYM;

            if (membership.getType().equals(MembershipType.GYM_DAY_PASS))
                revenueType = BusinessProfileRevenueType.MEMBERSHIP_GYM_DAY_PASS;

            saveRevenueWithOutDiscount(
                    businessProfile,
                    description,
                    userMembership.getId(),
                    userMembership.getPaymentId(),
                    userMembership.getPaidAmount(),
                    membership.getPrice(),
                    revenueType,
                    ipgType
            );

        } catch (Exception e) {
            log.error("Error saving business profile revenue by session: {}", e);
        }
    }

    private void saveRevenueWithDiscount(BusinessProfile businessProfile, String description, long enrollId, String stripePaymentId,
                                         BigDecimal paidAmount, BigDecimal price, PublicUserDiscountsHistory discount, BusinessProfileRevenueType revenueType) {

        DiscountCategory discountType = discount == null ? null : discount.getCategory();

        BigDecimal fitzkyAmount;
        BigDecimal profileAmount;
        BigDecimal payhereAmount = getPayhereCostForPrice(paidAmount);

        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);

        PackageDetail packageDetail = activeAgreement.getPackageDetail();
        if (packageDetail.getPaymentModel().equals(BusinessProfilePaymentModel.COMMISSION)) {
            // COMMISSION model.
            if (discountType == DiscountCategory.FIRST_FREE) {
                // has not paid.
                profileAmount = BigDecimal.ZERO;
                fitzkyAmount = BigDecimal.ZERO;
                payhereAmount = BigDecimal.ZERO;
            } else {
                // paid to enroll.
                BigDecimal profileContribution = getProfileContribution(payhereAmount);

                profileAmount = price.multiply(ClassSessionServiceImpl.ONE_HUNDRED.subtract(packageDetail.getAmount()))
                        .divide(ClassSessionServiceImpl.ONE_HUNDRED, 2, RoundingMode.UP);

                profileAmount = profileAmount.subtract(profileContribution);
                fitzkyAmount = price.subtract(payhereAmount).subtract(profileAmount);

                if (discountType == DiscountCategory.SESSION_INVITED_FRIEND) {
                    // paid a discounted amount.
                    fitzkyAmount = fitzkyAmount.subtract(price.subtract(paidAmount));
                }
            }
        } else {
            // SUBSCRIPTION model.
            if (discountType == DiscountCategory.FIRST_FREE) {
                // has not paid.
                profileAmount = BigDecimal.ZERO;
                fitzkyAmount = BigDecimal.ZERO;
                payhereAmount = BigDecimal.ZERO;
            } else if (discountType == DiscountCategory.SESSION_INVITED_FRIEND) {
                // paid a discounted amount.
                BigDecimal fitzkyContribution = getFitzkyContribution(payhereAmount);
                BigDecimal profileContribution = getProfileContribution(payhereAmount);

                profileAmount = price.subtract(profileContribution);
                fitzkyAmount = price.subtract(paidAmount)
                        .add(fitzkyContribution).multiply(BigDecimal.valueOf(-1));
            } else {
                // paid full amount so no discounts.
                profileAmount = paidAmount.subtract(payhereAmount);
                fitzkyAmount = BigDecimal.ZERO;
            }
        }

        fitzkyAmount = fitzkyAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
        profileAmount = profileAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN);

        BusinessProfileRevenue revenue = new BusinessProfileRevenue(description, fitzkyAmount, profileAmount, payhereAmount,
                revenueType, String.valueOf(enrollId), businessProfile);
        revenue.setPaymentId(stripePaymentId);
        revenue.setPublicUserDiscountsHistory(discount);
        revenue = businessProfileRevenueRepository.save(revenue);
        log.info("Save revenue - " + revenue);
    }

    private void saveRevenueWithOutDiscount(BusinessProfile businessProfile, String description, long userMembershipId, String stripePaymentId,
                                            BigDecimal paidAmount, BigDecimal price, BusinessProfileRevenueType revenueType,IPGType ipgType) {

        BigDecimal fitzkyAmount;
        BigDecimal profileAmount;
        BigDecimal payhereAmount = getPayhereCostForPrice(paidAmount);

        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);

        PackageDetail packageDetail = activeAgreement.getPackageDetail();
        if (packageDetail.getPaymentModel().equals(BusinessProfilePaymentModel.COMMISSION)) {
            // COMMISSION model.
            BigDecimal profileContribution = getProfileContribution(payhereAmount);

            profileAmount = (paidAmount.multiply(ClassSessionServiceImpl.ONE_HUNDRED.subtract(packageDetail.getAmount()))
                    .divide(ClassSessionServiceImpl.ONE_HUNDRED, 2, RoundingMode.UP)).subtract(profileContribution);

            fitzkyAmount = paidAmount.subtract(payhereAmount).subtract(profileAmount);

        } else {
            // SUBSCRIPTION model.
            profileAmount = paidAmount.subtract(payhereAmount);
            fitzkyAmount = BigDecimal.ZERO;
        }

        fitzkyAmount = fitzkyAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
        profileAmount = profileAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN);

        BusinessProfileRevenue revenue = new BusinessProfileRevenue(description, fitzkyAmount, profileAmount, payhereAmount,
                revenueType, String.valueOf(userMembershipId), businessProfile);
        revenue.setPaymentId(stripePaymentId);
        revenue.setIpgType(ipgType);

        log.info("revenue id::::::::::::::"+ revenue.getId());
        log.info("revenue type::::::::::::::"+ ipgType);

        businessProfileRevenueRepository.save(revenue);
        log.info("Save revenue - " + revenue);


    }


    @Override
    public Page<BusinessProfileRevenueDTO> getRevenueDetails(LocalDateTime start, LocalDateTime end, String username, String name, Pageable pageable) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        UserRole userRole = commonUserService.getRole(authUser);
        if (userRole.equals(UserRole.SUPER_ADMIN)) {
            PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
            Page<BusinessProfile> profilePage;
            if (name == null) profilePage = businessProfileRepository.findAll(pageRequest);
            else profilePage = businessProfileRepository.getBusinessProfilesByName(name, pageRequest);
            List<BusinessProfileRevenueDTO> result = profilePage.getContent().stream().map(bp -> getRevenueDetailsForBusinessProfile(bp, start, end)).collect(Collectors.toList());
            return new PageImpl<>(result, pageRequest, profilePage.getTotalElements());
        } else {
            BusinessProfileManager businessProfileManager = authUser.getBusinessProfileManager();
            BusinessProfile businessProfile = businessProfileManager.getBusinessProfile();
            BusinessProfileRevenueDTO revenueDetailsForBusinessProfile = getRevenueDetailsForBusinessProfile(businessProfile, start, end);
            return new PageImpl<>(Collections.singletonList(revenueDetailsForBusinessProfile), pageable, 1);
        }
    }

    @Override
    public Page<ClassRevenueSummaryDTO> getOnlineClassesSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<BusinessProfileRevenue> revenuePage = businessProfileRevenueRepository.
                findBusinessProfileRevenuesByBusinessProfileAndTypeAndDateTimeBetweenOrderByDateTimeDesc
                        (businessProfile, BusinessProfileRevenueType.SESSION, start, end, pageable);
        List<ClassRevenueSummaryDTO> result = revenuePage.getContent().stream().map(
                revenue -> getSingleClassSummary(revenue, BusinessProfileRevenueType.SESSION)).collect(Collectors.toList());
        return new PageImpl<>(result, pageRequest, revenuePage.getTotalElements());
    }

    @Override
    public Page<ClassRevenueSummaryDTO> getPhysicalClassesSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<BusinessProfileRevenue> revenuePage = businessProfileRevenueRepository.
                findBusinessProfileRevenuesByBusinessProfileAndTypeAndDateTimeBetweenOrderByDateTimeDesc
                        (businessProfile, BusinessProfileRevenueType.PHYSICAL_SESSION, start, end, pageable);
        List<ClassRevenueSummaryDTO> result = revenuePage.getContent().stream().map(
                revenue -> getSingleClassSummary(revenue, BusinessProfileRevenueType.PHYSICAL_SESSION)).collect(Collectors.toList());
        return new PageImpl<>(result, pageRequest, revenuePage.getTotalElements());
    }

    @Override
    public Page<MembershipRevenueSummaryDTO> getPhysicalClassesMembershipSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<BusinessProfileRevenue> revenuePage = businessProfileRevenueRepository.
                findBusinessProfileRevenuesByBusinessProfileAndTypeAndDateTimeBetweenOrderByDateTimeDesc
                        (businessProfile, BusinessProfileRevenueType.MEMBERSHIP_PHYSICAL_CLASS, start, end, pageable);
        List<MembershipRevenueSummaryDTO> result = revenuePage.getContent().stream().map(
                revenue -> getSingleMembershipSummary(revenue, BusinessProfileRevenueType.MEMBERSHIP_PHYSICAL_CLASS)).collect(Collectors.toList());
        return new PageImpl<>(result, pageRequest, revenuePage.getTotalElements());
    }

    @Override
    public Page<MembershipRevenueSummaryDTO> getOnlineClassesMembershipSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<BusinessProfileRevenue> revenuePage = businessProfileRevenueRepository.
                findBusinessProfileRevenuesByBusinessProfileAndTypeAndDateTimeBetweenOrderByDateTimeDesc
                        (businessProfile, BusinessProfileRevenueType.MEMBERSHIP_ONLINE_CLASS, start, end, pageable);
        List<MembershipRevenueSummaryDTO> result = revenuePage.getContent().stream().map(
                revenue -> getSingleMembershipSummary(revenue, BusinessProfileRevenueType.MEMBERSHIP_ONLINE_CLASS)).collect(Collectors.toList());
        return new PageImpl<>(result, pageRequest, revenuePage.getTotalElements());
    }

    @Override
    public Page<MembershipRevenueSummaryDTO> getGymMembershipSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<BusinessProfileRevenue> revenuePage = businessProfileRevenueRepository.
                findBusinessProfileRevenuesForGymMemberships(businessProfile, start, end, pageable);
        List<MembershipRevenueSummaryDTO> result = revenuePage.getContent().stream().map(
                revenue -> getSingleMembershipSummary(revenue, BusinessProfileRevenueType.MEMBERSHIP_GYM)).collect(Collectors.toList());

        return new PageImpl<>(result, pageRequest, revenuePage.getTotalElements());
    }

    private BigDecimal getFitzkyContribution(BigDecimal stripeAmount) {
        return stripeAmount.multiply(FITZKY_PAYHERE_CONTRIBUTION_PERCENTAGE)
                .divide(ClassSessionServiceImpl.ONE_HUNDRED, 2, RoundingMode.UP);
    }

    private BigDecimal getProfileContribution(BigDecimal stripeAmount) {
        return stripeAmount.multiply(ClassSessionServiceImpl.ONE_HUNDRED.subtract(FITZKY_PAYHERE_CONTRIBUTION_PERCENTAGE))
                .divide(ClassSessionServiceImpl.ONE_HUNDRED, 2, RoundingMode.UP);
    }

    @Override
    public Page<SubscriptionPaymentDTO> getSubscriptionPaymentHistory(Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "dateTime"));
        Page<BusinessProfilePayment> paymentPage = businessProfilePaymentRepository.findAll(pageable);
        List<SubscriptionPaymentDTO> paymentDTOList = paymentPage.getContent().stream().map(payment -> new SubscriptionPaymentDTO(payment.getId(),
                payment.getBusinessAgreement().getBusinessProfile().getBusinessName(), payment.getBusinessAgreement().getBusinessProfile().getRegNumber(),
                payment.getBusinessAgreement().getAgreementId(), payment.getDateTime(), payment.getAmount(),
                (payment.getBusinessAgreement().getPackageDetail().getPaymentModel().equals(BusinessProfilePaymentModel.SUBSCRIPTION_MONTHLY) ?
                        payment.getDateTime().plusMonths(1) : payment.getDateTime().plusYears(1)))).collect(Collectors.toList());
        return new PageImpl<>(paymentDTOList, pageRequest, paymentPage.getTotalElements());
    }

    @Override
    public Page<PaymentSummaryDTO> getProfilePaymentSummary(String name, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "dateTime"));
        Page<PaymentSettlement> paymentSettlementPage = paymentSettlementRepository.getPaymentSettlementsFiltered(name, pageRequest);
        List<PaymentSummaryDTO> summary = paymentSettlementPage.getContent().stream().map(paymentSettlement ->
                new PaymentSummaryDTO(
                        paymentSettlement.getDateTime(),
                        paymentSettlement.getId(),
                        paymentSettlement.getBusinessProfile().getId(),
                        paymentSettlement.getBusinessProfile().getBusinessName(),
                        paymentSettlement.getAmount(),
                        paymentSettlement.getBalance(),
                        paymentSettlement.getPaymentMethod(),
                        paymentSettlement.getPaymentProof())).collect(Collectors.toList());
        return new PageImpl<>(summary, pageRequest, paymentSettlementPage.getTotalElements());
    }

    @Override
    public Page<DuePaymentDTO> getProfileDuePayments(String name, Pageable pageable) {
        PageRequest pageRequest = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        Page<BusinessProfile> profilePage = businessProfileRepository.getAllActiveBusinessProfiles(name, pageRequest);
        List<DuePaymentDTO> duePayments = profilePage.getContent().stream().map(this::getDuePaymentsForProfile).collect(Collectors.toList());
        return new PageImpl<>(duePayments, pageable, profilePage.getTotalElements());
    }

    @Override
    @Transactional
    public void settleProfilePayment(PaymentSummaryDTO payment) {

        //business profile
        BusinessProfile businessProfile = businessProfileRepository.findById(payment.getBusinessProfileId()).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));

        //calculate balance
        PaymentSettlement lastSettlement = paymentSettlementRepository.findTopByBusinessProfileOrderByDateTimeDesc(businessProfile);
        BigDecimal actualDueAmount;
        if (lastSettlement != null) {
            LocalDateTime lastPaidDateTime = lastSettlement.getDateTime();
            BigDecimal balance = lastSettlement.getBalance();
            BigDecimal dueAmountAfter = businessProfileRevenueRepository.findDueAmountAfter(lastPaidDateTime, businessProfile);
            if (dueAmountAfter != null) actualDueAmount = balance.add(dueAmountAfter);
            else actualDueAmount = balance;
        } else {
            actualDueAmount = businessProfileRevenueRepository.findDueAmount(businessProfile);
        }
        BigDecimal balance = actualDueAmount.subtract(payment.getAmount());

        //save payment proof
        String paymentProof = payment.getPaymentProof();
        if (paymentProof == null) throw new CustomServiceException("Payment proof is requested");
        String name = businessProfile.getBusinessName().replaceAll(REGEX, "") + "-PaymentProof-" + payment.getDate() + UUID.randomUUID();
        String paymentProofFile = fileHandler.saveBase64File(paymentProof, name, BUSINESS_FOLDER);

        PaymentSettlement settlement = new PaymentSettlement();
        settlement.setBusinessProfile(businessProfile);
        settlement.setBalance(balance);
        settlement.setPaymentProof(paymentProofFile);
        settlement.setDateTime(payment.getDate());
        settlement.setAmount(payment.getAmount());
        settlement.setPaymentMethod(payment.getPaymentMethod());
        settlement = paymentSettlementRepository.save(settlement);
        log.info("Save payment settlement - " + settlement);
    }

    @Override
    public Page<BusinessProfileListResponse> getBusinessProfilesByClassType(long classTypeId, Pageable pageable) {
        ClassType classType = classTypeRepository.findById(classTypeId).orElseThrow(() -> new CustomServiceException(NO_CLASS_TYPE_FOUND));
        Page<BusinessProfile> profiles = classRepository.findBusinessProfilesForClassType(classType, pageable);
        return profiles.map(this::getBusinessProfileResponse);
    }

    private DuePaymentDTO getDuePaymentsForProfile(BusinessProfile businessProfile) {

        DuePaymentDTO duePayment = new DuePaymentDTO();
        duePayment.setBusinessProfileId(businessProfile.getId());
        duePayment.setBusinessProfileName(businessProfile.getBusinessName());
        duePayment.setBusinessProfileRegNumber(businessProfile.getRegNumber());

        BusinessAgreement latestAgreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(businessProfile);
        if (latestAgreement != null) {
            duePayment.setAgreementNo(latestAgreement.getAgreementId());
            duePayment.setPaymentModel(latestAgreement.getPackageDetail().getPaymentModel());
        } else throw new CustomServiceException("Agreement not found");

        PaymentSettlement settlement = paymentSettlementRepository.findTopByBusinessProfileOrderByDateTimeDesc(businessProfile);

        BigDecimal dueAmount;

        if (settlement != null) {
            LocalDateTime lastPaidDateTime = settlement.getDateTime();
            BigDecimal balance = settlement.getBalance();
            BigDecimal dueAmountAfter = businessProfileRevenueRepository.findDueAmountAfter(lastPaidDateTime, businessProfile);
            if (dueAmountAfter != null) dueAmount = balance.add(dueAmountAfter);
            else dueAmount = balance;
            duePayment.setLastPaidDateTime(lastPaidDateTime);
        } else {
            dueAmount = businessProfileRevenueRepository.findDueAmount(businessProfile);
        }

        duePayment.setDueAmount(dueAmount);
        return duePayment;
    }

    @Override
    @Transactional
    public void updateAgreement(long businessProfileId, BusinessProfileCreateDto dto) {

        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));

        BusinessAgreement oldAgreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(businessProfile);
        if (oldAgreement != null) oldAgreement.setStatus(BusinessAgreementStatus.INACTIVE);

        PackageDetail pd = new PackageDetail();
        pd.setAmount(BigDecimal.valueOf(dto.getAmount()));
        pd.setDescription(dto.getPackageDescription());
        pd.setPaymentModel(dto.getPaymentModel());
        packageDetailRepository.save(pd);

        BusinessProfileAgreementDTO agreementDetails = dto.getAgreementDetails();
        BusinessAgreement ba = new BusinessAgreement();
        ba.setBusinessProfile(businessProfile);
        ba.setPackageDetail(pd);
        ba.setStartDate(convertDateFromString(agreementDetails.getAgreementStartDate()));
        ba.setExpDate(convertDateFromString(agreementDetails.getAgreementExpireDate()));
        ba.setStatus(BusinessAgreementStatus.ACTIVE);
        if (agreementDetails.getFile() != null && !agreementDetails.getFile().isEmpty()) {
            String name = businessProfile.getBusinessName().replaceAll(REGEX, "") + "-agreement" + UUID.randomUUID();
            ba.setFile(fileHandler.saveBase64File(agreementDetails.getFile(), name, BUSINESS_FOLDER));
        }
        List<BusinessAgreement> savingList = new ArrayList<>();
        savingList.add(oldAgreement);
        savingList.add(ba);
        businessAgreementRepository.saveAll(savingList);
    }

    @Override
    public void renewAgreement(long businessProfileId, BusinessProfileAgreementDTO dto) {

        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));

        BusinessAgreement agreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(businessProfile);
        if (agreement != null) {
            agreement.setStatus(BusinessAgreementStatus.ACTIVE);
            agreement.setStartDate(convertDateFromString(dto.getAgreementStartDate()));
            agreement.setExpDate(convertDateFromString(dto.getAgreementExpireDate()));
            if (dto.getFile() != null && !dto.getFile().isEmpty()) {
                String name = businessProfile.getBusinessName().replaceAll(REGEX, "") + "-agreement" + UUID.randomUUID();
                agreement.setFile(fileHandler.saveBase64File(dto.getFile(), name, BUSINESS_FOLDER));
            }
            businessAgreementRepository.save(agreement);
        }
    }

    @Override
    public void cancelAgreement(long businessProfileId) {

        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));

        BusinessAgreement agreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(businessProfile);
        if (agreement != null) {
            agreement.setStatus(BusinessAgreementStatus.INACTIVE);
            businessAgreementRepository.save(agreement);
        }
    }

    @Override
    public List<BusinessProfileLocationDTO> getLocationsForProfile(long profileId) {
        if (!businessProfileRepository.existsById(profileId))
            throw new CustomServiceException(NO_BUSINESS_PROFILE_FOUND);
        return businessProfileLocationRepository.findBusinessProfileLocationsByBusinessProfileIdOrderById(profileId).stream().map(this::mapLocation).collect(Collectors.toList());
    }

    @Override
    public List<BusinessProfileLocationDTO> getGymLocationsForBusinessProfile(long profileId) {
        if (!businessProfileRepository.existsById(profileId))
            throw new CustomServiceException(NO_BUSINESS_PROFILE_FOUND);
        List<BusinessProfileLocationDTO> locations = new ArrayList<>();
        List<BusinessProfileLocation> locationEntities = businessProfileLocationRepository.findBusinessProfileLocationsByBusinessProfileIdOrderById(profileId);
        for (BusinessProfileLocation locationEntity : locationEntities) {
            Gym gym = locationEntity.getGym();
            if (gym == null) {
                locations.add(mapLocation(locationEntity));
            }
        }
        return locations;
    }

    @Override
    public Page<BusinessProfileLocationDTO> getLocationsForProfile(long profileId, Pageable pageable) {
        if (!businessProfileRepository.existsById(profileId))
            throw new CustomServiceException(NO_BUSINESS_PROFILE_FOUND);
        return businessProfileLocationRepository.findBusinessProfileLocationsByBusinessProfileIdOrderById(profileId, pageable).map(this::mapLocation);
    }

    private BusinessProfileLocationDTO mapLocation(BusinessProfileLocation l) {
        return new BusinessProfileLocationDTO(l.getId(), l.getName(), l.getType(), l.getCountry(), l.getTimeZone(),
                l.getAddressLine1(), l.getAddressLine2(), l.getLongitude(), l.getLatitude(), l.getCity(), l.getProvince(), l.getPostalCode());
    }

    private ClassRevenueSummaryDTO getSingleClassSummary(BusinessProfileRevenue revenue, BusinessProfileRevenueType type) {

        long enrollId = Long.parseLong(revenue.getTypeId());
        String publicUserName = "";
        String className = "";
        ClassCategory category = null;
        LocalDateTime sessionDateTime = null;
        BigDecimal sessionFee = BigDecimal.ZERO;
        BigDecimal paidAmount = BigDecimal.ZERO;
        if (type.equals(BusinessProfileRevenueType.SESSION)) {
            Optional<ClassSessionEnroll> optionalEnroll = classSessionEnrollRepository.findById(enrollId);
            if (optionalEnroll.isPresent()) {
                ClassSessionEnroll enroll = optionalEnroll.get();
                publicUserName = enroll.getPublicUser().getFirstName() + " " + enroll.getPublicUser().getLastName();
                className = enroll.getClassSession().getClassParent().getName();
                category = enroll.getClassSession().getClassParent().getCategory();
                sessionDateTime = enroll.getClassSession().getDateAndTime();
                sessionFee = enroll.getClassSession().getPrice();
                paidAmount = enroll.getPaidAmount();
            } else {
                log.info("Session enroll not found : enrollId - " + enrollId);
            }
        } else {
            Optional<PhysicalSessionEnroll> optionalEnroll = physicalSessionEnrollRepository.findById(enrollId);
            if (optionalEnroll.isPresent()) {
                PhysicalSessionEnroll enroll = optionalEnroll.get();
                publicUserName = enroll.getPublicUser().getFirstName() + " " + enroll.getPublicUser().getLastName();
                className = enroll.getPhysicalClassSession().getPhysicalClass().getName();
                category = ClassCategory.PHYSICAL;
                sessionDateTime = enroll.getPhysicalClassSession().getDateAndTime();
                sessionFee = enroll.getPhysicalClassSession().getPrice();
                paidAmount = enroll.getPaidAmount();
            } else {
                log.info("Session enroll not found : enrollId - " + enrollId);
            }
        }

        PublicUserDiscountsHistory discounts = revenue.getPublicUserDiscountsHistory();
        DiscountCategory discountCode = discounts == null ? null : discounts.getCategory();
        BigDecimal studioRev = revenue.getProfileAmount();
        BigDecimal fitzkyRev = revenue.getFitzkyAmount();
        BigDecimal stripeFee = revenue.getIpgAmount();

        return new ClassRevenueSummaryDTO(publicUserName, className, category, sessionDateTime, sessionFee, paidAmount, discountCode, studioRev, fitzkyRev, stripeFee);
    }

    private MembershipRevenueSummaryDTO getSingleMembershipSummary(BusinessProfileRevenue revenue, BusinessProfileRevenueType type) {

        long userMembershipId = Long.parseLong(revenue.getTypeId());
        MembershipRevenueSummaryDTO membershipRevenueSummaryDTO = new MembershipRevenueSummaryDTO();
        Optional<PublicUserMembership> optionalPurchase = publicUserMembershipRepository.findById(userMembershipId);

        if (optionalPurchase.isPresent()) {
            PublicUserMembership userMembership = optionalPurchase.get();
            Membership membership = userMembership.getMembership();
            PublicUser publicUser = userMembership.getPublicUser();

            membershipRevenueSummaryDTO.setPublicUserName(publicUser.getFirstName() + " " + publicUser.getLastName());
            membershipRevenueSummaryDTO.setMembershipName(membership.getName());
            membershipRevenueSummaryDTO.setType(membership.getType());

            BigDecimal listedPrice = membership.getPrice();
            double discount = membership.getDiscount();
            BigDecimal discountedPrice = userMembership.getPaidAmount();

            if (userMembership.getListedPrice() != null && userMembership.getListedPrice().doubleValue() > 0) {
                listedPrice = userMembership.getListedPrice();
                discount = listedPrice.subtract(userMembership.getPaidAmount()).doubleValue();
            }

            membershipRevenueSummaryDTO.setListedPrice(listedPrice);
            membershipRevenueSummaryDTO.setDiscountedPrice(discountedPrice);
            membershipRevenueSummaryDTO.setDiscount(discount);
            membershipRevenueSummaryDTO.setPaidAmount(userMembership.getPaidAmount());

            if (type.equals(BusinessProfileRevenueType.MEMBERSHIP_PHYSICAL_CLASS)) {
                List<PhysicalClassMembership> physicalClassMemberships = membership.getPhysicalClassMemberships();
                membershipRevenueSummaryDTO.setClassNames(physicalClassMemberships.stream().map(physicalClassMembership -> physicalClassMembership.getPhysicalClass().getName()).collect(Collectors.toList()));
            } else if (type.equals(BusinessProfileRevenueType.MEMBERSHIP_ONLINE_CLASS)) {
                List<OnlineClassMembership> onlineClassMemberships = membership.getOnlineClassMemberships();
                membershipRevenueSummaryDTO.setClassNames(onlineClassMemberships.stream().map(onlineClassMembership -> onlineClassMembership.getClassParent().getName()).collect(Collectors.toList()));
            } else {
                GymMembership gymMembership = membership.getGymMembership();
                membershipRevenueSummaryDTO.setGymName(gymMembership.getGym().getName());
            }
        }

        membershipRevenueSummaryDTO.setFitzkyRev(revenue.getFitzkyAmount());
        membershipRevenueSummaryDTO.setStudioRev(revenue.getProfileAmount());
        membershipRevenueSummaryDTO.setStripeFee(revenue.getIpgAmount());
        return membershipRevenueSummaryDTO;
    }

    private BigDecimal getStripeCostForPrice(BigDecimal price) {
        return (price.multiply(STRIPE_TRANSACTION_COST_PERCENTAGE.add(STRIPE_CURRENCY_COST_PERCENTAGE))
                .divide(ClassSessionServiceImpl.ONE_HUNDRED, 2, RoundingMode.UP)).add(STRIPE_TRANSACTION_COST_AMOUNT).setScale(2, BigDecimal.ROUND_UP);
    }

    private BigDecimal getPayhereCostForPrice(BigDecimal price) {
        return (price.multiply(PAYHERE_TRANSACTION_COST_PERCENTAGE)
                .divide(ClassSessionServiceImpl.ONE_HUNDRED, 2, RoundingMode.UP)).setScale(2, BigDecimal.ROUND_UP);
    }

    private BusinessProfileRevenueDTO getRevenueDetailsForBusinessProfile(BusinessProfile bp, LocalDateTime start, LocalDateTime end) {

        BusinessProfileRevenueDTO revenueDTO = new BusinessProfileRevenueDTO();
        revenueDTO.setBusinessProfileId(bp.getId());
        revenueDTO.setBusinessProfileName(bp.getBusinessName());

        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileOrderByExpDateDesc(bp);
        PackageDetail packageDetail = activeAgreement == null ? null : activeAgreement.getPackageDetail();

        if (packageDetail != null) {
            BusinessProfilePaymentModel paymentModel = packageDetail.getPaymentModel();
            switch (paymentModel) {
                case COMMISSION:
                    revenueDTO.setPaymentModel("Commission");
                    revenueDTO.setCommissionPercentage(packageDetail.getAmount().doubleValue());
                    break;
                case SUBSCRIPTION_MONTHLY:
                    revenueDTO.setPaymentModel("Subscription");
                    revenueDTO.setSubscriptionType("Monthly");
                    break;
                case SUBSCRIPTION_ANNUALLY:
                    revenueDTO.setPaymentModel("Subscription");
                    revenueDTO.setSubscriptionType("Annually");
                    break;
            }
        }

        revenueDTO.setNumberOfClasses(classRepository.countClassesByBusinessProfileAndCreatedDateBetween(bp, start, end));
        revenueDTO.setNumberOfSessions(classSessionRepository.countClassSessionsByBusinessProfileAndDateAndTimeBetween(bp, start, end));
        revenueDTO.setNumberOfInstructorPackages(instructorPackageRepository.countInstructorPackagesByBusinessProfile(bp));
        revenueDTO.setNumberOfPhysicalClasses(physicalClassRepository.countPhysicalClassesByBusinessProfile(bp));
        revenueDTO.setNumberOfPhysicalSessions(physicalClassSessionRepository.countPhysicalClassSessionsByPhysicalClass_BusinessProfile(bp));
        revenueDTO.setNumberOfGymMemberships(gymMembershipRepository.countDistinctByMembership_TypeAndGym_Location_BusinessProfile(MembershipType.GYM, bp));
        revenueDTO.setNumberOfGymDayPasses(gymMembershipRepository.countDistinctByMembership_TypeAndGym_Location_BusinessProfile(MembershipType.GYM_DAY_PASS, bp));
        revenueDTO.setNumberOfPhysicalClassMemberships(physicalClassMembershipRepository.countDistinctByPhysicalClass_BusinessProfile(bp));

        long physicalClassMembershipEnrolls = physicalClassMembershipRepository.
                countPhysicalClassMembershipEnrollsByBusinessProfile(bp, start, end);
        long gymMembershipEnrolls = publicUserMembershipRepository.
                countPublicUserMembershipsByMembership_GymMembership_Gym_Location_BusinessProfileAndDateTimeBetween(bp, start, end);
        long physicalSessionEnrolls = physicalSessionEnrollRepository.
                countPhysicalSessionEnrollsByPhysicalClassSession_PhysicalClass_BusinessProfileAndDateTimeBetween(bp, start, end);
        long sessionEnrolls = classSessionEnrollRepository.
                countClassSessionEnrollsByClassSession_ClassParent_BusinessProfileAndDateTimeBetween(bp, start, end);
        long instructorPackageEnrolls = instructorPackageEnrollRepository.
                countInstructorPackageEnrollsByInstructorPackage_BusinessProfileAndDateTimeBetween(bp, start, end);
        revenueDTO.setNumberOfEnrollments(sessionEnrolls + instructorPackageEnrolls + physicalSessionEnrolls + gymMembershipEnrolls + physicalClassMembershipEnrolls);

        BigDecimal profileIncome = BigDecimal.valueOf(0);
        BigDecimal fitzkyIncome = BigDecimal.valueOf(0);
        List<BusinessProfileRevenue> revenues = businessProfileRevenueRepository.findBusinessProfileRevenuesByBusinessProfileAndDateTimeBetween(bp, start, end);
        if (revenues != null && revenues.size() > 0) {
            for (BusinessProfileRevenue revenue : revenues) {
                profileIncome = profileIncome.add(revenue.getProfileAmount());
                fitzkyIncome = fitzkyIncome.add(revenue.getFitzkyAmount());
            }
        }
        revenueDTO.setFitzkyIncome(fitzkyIncome);
        revenueDTO.setRevenue(profileIncome);
        return revenueDTO;
    }

    private String getBusinessProfileCountry(BusinessProfile bp) {
        BusinessProfileLocation headOffice = businessProfileLocationRepository.findBusinessProfileLocationByTypeAndBusinessProfile(HEAD_OFFICE, bp);
        return headOffice == null ? "" : headOffice.getCountry();
    }

    private List<String> getClassTypesForBusinessProfile(BusinessProfile bp) {
        return businessProfileClassTypeRepository.findBusinessProfileClassTypesByBusinessProfile(bp)
                .stream().map(bpct -> bpct.getClassType().getTypeName()).collect(Collectors.toList());
    }

    private List<ClassForBusinessProfileDTO> getClassesForBusinessProfile(BusinessProfile bp, Trainer trainer) {
        if (trainer == null) return new ArrayList<>();
        PageRequest pageRequest = PageRequest.of(0, 3);
//        Page<Class> classPage = classTrainerRepository.getClassesByBusinessProfileAndTrainerOrderByRatingDesc(bp, trainer, pageRequest);
        Page<Class> classPage = classRepository.getAllByBusinessProfileOrderByRatingDesc(bp, pageRequest);
        return classPage.getContent().stream().map(c -> new ClassForBusinessProfileDTO(c.getId(), c.getName(),
                c.getProfileImage(), c.getCalorieBurnOut(), classSessionService.getSessionsPerWeek(c), c.getRating(),
                c.getRatingCount())).collect(Collectors.toList());
    }

    @Override
    public BusinessProfileListResponse getBusinessProfileResponse(BusinessProfile businessProfile, double longitude, double latitude) {
        BusinessProfileListResponse businessProfileResponse = getBusinessProfileResponse(businessProfile);
        BusinessAddressListResponse headOffice = businessProfileResponse.getHeadOffice();
        if (headOffice != null) {
            businessProfileResponse.setDistance(locationService.getDistance(longitude, headOffice.getLongitude(), latitude, headOffice.getLatitude()));
        }

        return businessProfileResponse;
    }

    @Override
    public BusinessProfileListResponse getBusinessProfileResponse(BusinessProfile businessProfile) {
        BusinessProfileListResponse businessProfileResponse = modelMapper.map(businessProfile, BusinessProfileListResponse.class);
        businessProfileResponse.setClassTypes(getClassTypesForBusinessProfile(businessProfile));
        BusinessProfileLocation location = businessProfileLocationRepository.findBusinessProfileLocationByTypeAndBusinessProfile(HEAD_OFFICE, businessProfile);
        businessProfileResponse.setHeadOffice(getAddressResponse(location));
        businessProfileResponse.setCountry(location == null ? null : location.getCountry());
        businessProfileResponse.setImages(businessProfile.getBusinessProfileImages().stream().map(BusinessProfileImage::getImage).collect(Collectors.toList()));
        businessProfileResponse.setAddresses(businessProfile.getBusinessProfileLocations().stream().map(this::getAddressResponse).collect(Collectors.toList()));
        businessProfileResponse = getAllAvgClassesPerWeek(businessProfile, businessProfileResponse);
        return businessProfileResponse;
    }

    private BusinessProfileListResponse getAllAvgClassesPerWeek(BusinessProfile businessProfile, BusinessProfileListResponse businessResponse) {
        getAvgOnlineClassesPerWeek(businessProfile, businessResponse);
        return getAvgPhysicalClassesPerWeek(businessProfile, businessResponse);
    }

    private BusinessProfileListResponse getAvgOnlineClassesPerWeek(BusinessProfile businessProfile, BusinessProfileListResponse businessResponse) {
        ClassSession lastSessionOfProfile = classSessionRepository.findTopByClassParentBusinessProfileOrderByDateAndTimeDesc(businessProfile);
        if (lastSessionOfProfile == null) return businessResponse;
        long differenceInDays = Duration.between(LocalDateTime.now(), lastSessionOfProfile.getDateAndTime()).toDays();
        long classCount = classSessionRepository.countAllByBusinessProfileAndDateAndTimeAfter(businessProfile);

        businessResponse.setAverageOnlineClassesPerWeek(getSingleAverage(classCount, differenceInDays));

//        System.out.println(lastSessionOfProfile);
//        System.out.println(differenceInDays);
//        System.out.println(classCount);

        return setAllAvgPerWeek(businessResponse, differenceInDays, classCount);
    }

    private BusinessProfileListResponse getAvgPhysicalClassesPerWeek(BusinessProfile businessProfile, BusinessProfileListResponse businessResponse) {
        PhysicalClassSession lastSessionOfProfile = physicalClassSessionRepository.findTopByPhysicalClassBusinessProfileOrderByDateAndTimeDesc(businessProfile);
        if (lastSessionOfProfile == null) return businessResponse;
        long differenceInDays = Duration.between(LocalDateTime.now(), lastSessionOfProfile.getDateAndTime()).toDays();
        long classCount = physicalClassSessionRepository.countAllByBusinessProfileAndDateAndTimeAfter(businessProfile);

        businessResponse.setAveragePhysicalClassesPerWeek(getSingleAverage(classCount, differenceInDays));

//        System.out.println(lastSessionOfProfile);
//        System.out.println(differenceInDays);
//        System.out.println(classCount);

        return setAllAvgPerWeek(businessResponse, differenceInDays, classCount);
    }

    private long getSingleAverage(long classCount, long differenceInDays) {
        if (classCount <= 0) return 0;

        if (differenceInDays <= 7) {
            return classCount;
        }

        long avgClassCount = classCount / (differenceInDays / 7);
        return avgClassCount == 0 ? 1 : avgClassCount;
    }

    private BusinessProfileListResponse setAllAvgPerWeek(BusinessProfileListResponse businessResponse,
                                                         long differenceInDays, long classCount) {
        if (classCount <= 0) return businessResponse;
        if (differenceInDays <= 7) {
            businessResponse.setAverageClassesPerWeek(businessResponse.getAverageClassesPerWeek() + classCount);
            return businessResponse;
        }
        long avgClassCount = classCount / (differenceInDays / 7);
        businessResponse.setAverageClassesPerWeek(businessResponse.getAverageClassesPerWeek()
                + (avgClassCount == 0 ? 1 : avgClassCount));
        return businessResponse;
    }

//    private BusinessContactNoResponse getContactNoResponse(BusinessProfileContactNumber businessProfileContact) {
//        return BusinessContactNoResponse.builder().contactType(businessProfileContact.getContactType())
//                .number(businessProfileContact.getNumber()).build();
//    }

    private BusinessAddressListResponse getAddressResponse(BusinessProfileLocation businessProfileLocation) {
        return modelMapper.map(businessProfileLocation, BusinessAddressListResponse.class);
    }

    private BusinessProfileResponseDto convert(BusinessProfile profile) {

        BusinessProfileResponseDto profileDto = new BusinessProfileResponseDto();

        profileDto.setId(profile.getId());
        profileDto.setBusinessName(profile.getBusinessName());
        profileDto.setAccountStatus(getProfileStatus(profile));
        profileDto.setBusinessRegistrationNumber(profile.getRegNumber());
        profileDto.setProfileImage(profile.getProfileImage());
        List<BusinessProfileImage> images = profile.getBusinessProfileImages();
        if (images != null && !images.isEmpty()) {
            profileDto.setImages(images.stream().map(BusinessProfileImage::getImage).collect(Collectors.toList()));
        }
        profileDto.setTelephone(profile.getTelephone());
        profileDto.setEmail(profile.getEmail());
        profileDto.setDescription(profile.getDescription());
        profileDto.setRating(profile.getRating());
        profileDto.setRatingCount(profile.getRatingCount());

        List<BusinessProfileLocation> profileLocations = profile.getBusinessProfileLocations();
        List<BusinessProfileLocationDTO> locationList = new ArrayList<>();
        for (BusinessProfileLocation profileLocation : profileLocations) {
            if (profileLocation.getType().equals(HEAD_OFFICE)) {
                profileDto.setHeadOffice(getLocation(profileLocation));
            } else {
                locationList.add(getLocation(profileLocation));
            }
        }
        profileDto.setBranches(locationList);

        profileDto.setAccountNumber(profile.getAccountNumber());
        profileDto.setAccountName(profile.getAccountName());
        profileDto.setBankName(profile.getBankName());
        profileDto.setBankCode(profile.getBankCode());
        profileDto.setBranchName(profile.getBranchName());
        profileDto.setBranchCode(profile.getBranchCode());
        profileDto.setSwiftCode(profile.getSwiftCode());

        BusinessAgreement latestAgreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(profile);
        if (latestAgreement != null) {
            profileDto.setPaymentModel(latestAgreement.getPackageDetail().getPaymentModel());
            profileDto.setAmount(latestAgreement.getPackageDetail().getAmount() != null ? latestAgreement.getPackageDetail().getAmount().doubleValue() : 0.0);
            profileDto.setPackageDescription(latestAgreement.getPackageDetail().getDescription());
        }
        profileDto.setAgreementDetails(getAgreementDetails(profile));
        profileDto.setManager(getManagerDetail(profile));
        return profileDto;
    }

    private BusinessProfileLocationDTO getLocation(BusinessProfileLocation location) {
        return new BusinessProfileLocationDTO(
                location.getId(),
                location.getName(),
                location.getType(),
                location.getCountry(),
                location.getTimeZone(),
                location.getAddressLine1(),
                location.getAddressLine2(),
                location.getLongitude(),
                location.getLatitude(),
                location.getCity(),
                location.getProvince(),
                location.getPostalCode());
    }

    private List<BusinessProfileLocation> getLocations(BusinessProfileCreateDto dto, BusinessProfile businessProfile) {

        List<BusinessProfileLocation> locations = new ArrayList<>();
        BusinessProfileLocationDTO headOffice = dto.getHeadOffice();
        locations.add(new BusinessProfileLocation(
                businessProfile,
                headOffice.getName(),
                HEAD_OFFICE,
                headOffice.getCountry(),
                headOffice.getTimeZone(),
                headOffice.getAddressLine1(),
                headOffice.getAddressLine2(),
                headOffice.getLongitude(),
                headOffice.getLatitude(),
                headOffice.getCity(),
                headOffice.getProvince(),
                headOffice.getPostalCode()));
        locations.addAll(dto.getBranches().stream().map(location -> new BusinessProfileLocation(
                businessProfile,
                location.getName(),
                BRANCH,
                location.getCountry(),
                location.getTimeZone(),
                location.getAddressLine1(),
                location.getAddressLine2(),
                location.getLongitude(),
                location.getLatitude(),
                location.getCity(),
                location.getProvince(),
                location.getPostalCode())).collect(Collectors.toList()));
        return locations;
    }

    private String getProfileStatus(BusinessProfile profile) {
        log.info("getProfileStatus : businessProfileName - {}", profile.getBusinessName());
        BusinessAgreement agreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(profile);
        if (agreement == null) {
            return "No agreement found";
        } else {
            switch (agreement.getStatus()) {
                case ACTIVE:
                    long dif = LocalDateTime.now().until(agreement.getExpDate(), ChronoUnit.DAYS);
                    if (dif < 0) {
                        return "Expired " + (-dif) + " days ago";
                    } else if (dif > 0) {
                        if (dif <= 5) {
                            return "Expires in " + dif + " days";
                        } else {
                            return "Active";
                        }
                    } else {
                        return "Expires today";
                    }
                case INACTIVE:
                    return "Inactive";
                default:
                    return "";
            }
        }
    }

    private BusinessProfileManagerDTO getManagerDetail(BusinessProfile profile) {
        log.info("getManagerDetails : businessProfileName - {}", profile.getBusinessName());
        if (profile.getBusinessProfileManager() != null) {
            BusinessProfileManager manager = profile.getBusinessProfileManager();
            AuthUser authUser = manager.getAuthUser();
            return new BusinessProfileManagerDTO(
                    authUser.getId(),
                    manager.getId(),
                    authUser.getUsername(),
                    authUser.getEmail(),
                    authUser.getFirstName(),
                    authUser.getLastName(),
                    authUser.getMobile(),
                    authUser.getTimeZoneLongName(),
                    manager.isConditionsAccepted());
        } else return null;
    }

    private List<BusinessProfileAgreementDTO> getAgreementDetails(BusinessProfile profile) {
        log.info("getAgreementDetails : businessProfileName - {}", profile.getBusinessName());
        if (profile.getBusinessAgreements() != null && profile.getBusinessAgreements().size() > 0) {
            return profile.getBusinessAgreements().stream().map(ba ->
                    new BusinessProfileAgreementDTO(
                            ba.getAgreementId(),
                            ba.getFile(),
                            convertDate(ba.getStartDate()),
                            convertDate(ba.getExpDate()),
                            ba.getStatus()
                    )).sorted().collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private String convertDate(LocalDateTime date) {
        String dateString = date.toString().split("T")[0];
        if (dateString.equals("2800-01-01")) dateString = "";
        return dateString;
    }

    private LocalDateTime convertDateFromString(String dateString) {
        if (dateString == null || dateString.isEmpty()) dateString = "2800-01-01";
        return LocalDateTime.ofInstant(Instant.parse(dateString + "T00:00:00Z"), ZoneId.of("GMT"));
    }

    @Override
    public void deleteBusinessProfile(long id) {
        BusinessProfile businessProfile = businessProfileRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        BusinessProfileManager manager = businessProfile.getBusinessProfileManager();
        AuthUser managerAuthUser = manager.getAuthUser();
        authUserRepository.delete(managerAuthUser);
        businessProfileManagerRepository.delete(manager);
        businessProfileRepository.delete(businessProfile);
    }

    private List<BusinessProfileNameIdDTO> getBusinessProfileNameForManager(AuthUser authUser, UserRole userRole) {
        if (userRole.equals(UserRole.BUSINESS_PROFILE_MANAGER)) {
            List<BusinessProfileNameIdDTO> result = new ArrayList<>();
            BusinessProfile bp = authUser.getBusinessProfileManager().getBusinessProfile();
            result.add(new BusinessProfileNameIdDTO(bp.getId(), bp.getBusinessName()));
            return result;
        } else {
            return null;
        }
    }
}
