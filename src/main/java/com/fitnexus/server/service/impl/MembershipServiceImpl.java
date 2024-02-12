package com.fitnexus.server.service.impl;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.constant.FitNexusConstants;
import com.fitnexus.server.dto.businessprofile.BusinessProfileLocationDTO;
import com.fitnexus.server.dto.classes.*;
import com.fitnexus.server.dto.classsession.ClassSessionListResponse;
import com.fitnexus.server.dto.classsession.SessionForMembershipDTO;
import com.fitnexus.server.dto.common.CardDetailsResponse;
import com.fitnexus.server.dto.common.StripeCheckResponse;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.dto.gym.GymMembershipAdminDTO;
import com.fitnexus.server.dto.gym.GymMembershipDTO;
import com.fitnexus.server.dto.membership.*;
import com.fitnexus.server.dto.membership.corporate.CorporateMembershipNameIdDTO;
import com.fitnexus.server.dto.payhere.GeneratedHashValueDetailsDTO;
import com.fitnexus.server.dto.payhere.PreApproveResponseDTO;
import com.fitnexus.server.dto.physical_class.PhysicalCLassMembershipDTO;
import com.fitnexus.server.dto.physical_class.PhysicalClassMembershipSlotCountDTO;
import com.fitnexus.server.dto.publicuser.PublicUserDiscountDTO;
import com.fitnexus.server.dto.publicuser.PublicUserMembershipDTO;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.businessprofile.BusinessAgreement;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.businessprofile.BusinessProfileLocation;
import com.fitnexus.server.entity.businessprofile.FailedTransaction;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.ClassSession;
import com.fitnexus.server.entity.classes.ClassSessionEnroll;
import com.fitnexus.server.entity.classes.OnlineClassMembership;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.classes.physical.PhysicalClassMembership;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.classes.physical.PhysicalSessionEnroll;
import com.fitnexus.server.entity.gym.Gym;
import com.fitnexus.server.entity.gym.GymMembership;
import com.fitnexus.server.entity.membership.Membership;
import com.fitnexus.server.entity.membership.corporate.Corporate;
import com.fitnexus.server.entity.publicuser.*;
import com.fitnexus.server.enums.*;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.businessprofile.BusinessAgreementRepository;
import com.fitnexus.server.repository.businessprofile.BusinessProfileRepository;
import com.fitnexus.server.repository.businessprofile.FailedTransactionRepository;
import com.fitnexus.server.repository.classes.ClassRepository;
import com.fitnexus.server.repository.classes.ClassSessionEnrollRepository;
import com.fitnexus.server.repository.classes.ClassSessionRepository;
import com.fitnexus.server.repository.classes.OnlineClassMembershipRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassMembershipRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassSessionRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalSessionEnrollRepository;
import com.fitnexus.server.repository.gym.GymMembershipRepository;
import com.fitnexus.server.repository.gym.GymRepository;
import com.fitnexus.server.repository.membership.MembershipRepository;
import com.fitnexus.server.repository.publicuser.*;
import com.fitnexus.server.service.*;
import com.fitnexus.server.util.AIAUserCheck;
import com.fitnexus.server.util.EmailSender;
import com.fitnexus.server.util.PayhereUtil;
import com.fitnexus.server.util.sms.SmsHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fitnexus.server.util.CustomGenerator;
import com.stripe.model.PaymentIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.DuplicatedConstants.MEMBERSHIP_ALREADY_EXISTS;
import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.*;
import static org.apache.logging.log4j.util.Chars.SPACE;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipServiceImpl implements MembershipService {

    @Value("${support_mail}")
    private String supportMail;

    @Value("${payment_bcc_mail_list}")
    private List<String> bccMailList;

    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    private final MembershipRepository membershipRepository;

    private final PublicUserTempCardDetailRepository publicUserTempCardDetailRepository;
    private final GymMembershipRepository gymMembershipRepository;
    private final PhysicalClassMembershipRepository physicalClassMembershipRepository;
    private final PhysicalClassRepository physicalClassRepository;
    private final GymRepository gymRepository;
    private final PublicUserMembershipRepository publicUserMembershipRepository;
    private final PublicUserCardDetailRepository publicUserCardDetailRepository;
    private final PublicUserRepository publicUserRepository;
    private final ModelMapper modelMapper;

    private final SmsHandler smsHandler;
    private final EmailSender emailSender;
    private final PublicUserService publicUserService;
    private final FailedTransactionRepository failedTransactionRepository;
    private final StripeService stripeService;
    private final PayhereUtil payhereUtil;
    private final PhysicalClassSessionRepository physicalClassSessionRepository;
    private final ClassSessionRepository classSessionRepository;
    private final PhysicalSessionEnrollRepository physicalSessionEnrollRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final LocationService locationService;
    @Autowired
    private BusinessProfileService businessProfileService;

    private final CoachNotificationService coachNotificationService;
    private final PublicUserNotificationRepository publicUserNotificationRepository;

    private final BusinessAgreementRepository businessAgreementRepository;
    private final AuthUserRepository authUserRepository;
    private final ClassRepository classRepository;
    private final OnlineClassMembershipRepository onlineClassMembershipRepository;
    private final ClassSessionEnrollRepository classSessionEnrollRepository;
    @Autowired
    private ClassSessionService classSessionService;
    @Autowired
    private PhysicalClassService physicalClassService;
    private final PublicUserDiscountRepository publicUserDiscountRepository;
    private final PromoCodeManagementService promoCodeManagementService;
    private final SessionNotificationService sessionNotificationService;
    private final AIAUserCheck aiaUserCheck;

    @Value("${payhere.merchantSecret.mobile}")
    private String merchantSecretMobile;

    @Value("${payhere.merchantSecret.web}")
    private String merchantSecretWeb;

    @Value("${payhere.merahantID}")
    private String merahantID;

//===========================================================private methods - start===================================================================================

    private List<PhysicalClassMembership> getNewPhysicalClassMemberships(Membership membership, PhysicalCLassMembershipDTO dto) {
        List<PhysicalClassMembership> physicalClassMemberships = new ArrayList<>();
        for (Long classId : dto.getPhysicalClassIdList()) {
            PhysicalClass physicalClass = physicalClassRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
            physicalClassMemberships.add(new PhysicalClassMembership(physicalClass, membership, dto.isAllowCashPayment()));
        }
        return physicalClassMemberships;
    }

    private List<OnlineClassMembership> getNewOnlineClassMemberships(Membership membership, OnlineClassMembershipDTO dto) {
        List<OnlineClassMembership> onlineClassMemberships = new ArrayList<>();
        for (Long classId : dto.getOnlineClassIdList()) {
            Class aClass = classRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
            onlineClassMemberships.add(new OnlineClassMembership(aClass, membership));
        }
        return onlineClassMemberships;
    }

    private Membership getNewMembership(MembershipDTO dto, MembershipType type) {

        Membership membership = new Membership();

        if (!type.equals(MembershipType.GYM_DAY_PASS)) {
            if (dto.getName() == null || dto.getName().isEmpty())
                throw new CustomServiceException("Name can not be empty");
            if (type.equals(MembershipType.PHYSICAL_CLASS)) {
                if (membershipRepository.existsByNameAndType(dto.getName(), type))
                    throw new CustomServiceException(MEMBERSHIP_ALREADY_EXISTS);
            }
            if (type.equals(MembershipType.ONLINE_CLASS)) {
                if (membershipRepository.existsByNameAndType(dto.getName(), type))
                    throw new CustomServiceException(MEMBERSHIP_ALREADY_EXISTS);
            }
            membership.setName(dto.getName());
            membership.setDescription(dto.getDescription());
        }
        membership.setType(type);
        membership.setStatus(MembershipStatus.VISIBLE);
        membership.setSlotCount(dto.getSlotCount());
        membership.setDuration(dto.getDuration());
        membership.setPrice(dto.getPrice());
        membership.setDiscount(dto.getDiscount());
        return membership;
    }

    private Membership updateMembership(MembershipDTO dto) {
        Membership membership = membershipRepository.findById(dto.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
//        if (dto.getName() == null || dto.getName().isEmpty())
//            throw new CustomServiceException("Name can not be empty");
//        if (!dto.getName().equals(membership.getName())) {
//            if (membership.getType().equals(MembershipType.PHYSICAL_CLASS)) {
//                if (membershipRepository.existsByName(dto.getName()))
//                    throw new CustomServiceException(MEMBERSHIP_ALREADY_EXISTS);
//            }
//            if (membership.getType().equals(MembershipType.GYM)) {
//                Gym gym = membership.getGymMembership().getGym();
//                if (membershipRepository.existsByNameAndGymMembership_Gym(dto.getName(), gym))
//                    throw new CustomServiceException(MEMBERSHIP_ALREADY_EXISTS);
//            }
//            membership.setName(dto.getName());
//        }
        membership.setDescription(dto.getDescription());

//        if (membership.getSlotCount() != dto.getSlotCount()) {
//            if (publicUserMembershipRepository.existsByMembership(membership))
//                throw new CustomServiceException("This membership is purchased by users. Can not change slot count");
//            membership.setSlotCount(dto.getSlotCount());
//        }

        membership.setSlotCount(dto.getSlotCount());

        if (membership.getDuration() != dto.getDuration()) {
            if (publicUserMembershipRepository.existsByMembership(membership))
                throw new CustomServiceException("This membership is purchased by users. Can not change duration");
            membership.setDuration(dto.getDuration());
        }

        membership.setPrice(dto.getPrice());
        membership.setDiscount(dto.getDiscount());
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            membership.setName(dto.getName());
        }

        membership = membershipRepository.save(membership);
        log.info("Update membership - " + membership);
        return membership;
    }

    private MembershipStatus getMembershipStatus(PublicUserMembership userMembership) {
        if (userMembership.getStatus().equals(MembershipStatus.PENDING))
            return MembershipStatus.PENDING;
        if (userMembership.getExpireDateTime().isBefore(LocalDateTime.now())) {
            return MembershipStatus.OPEN;
        }
        if (userMembership.getMembership().getType().equals(MembershipType.PHYSICAL_CLASS)) {
            if (userMembership.getRemainingSlots() == 0)
                return MembershipStatus.FULL;
        }
        if (userMembership.getMembership().getType().equals(MembershipType.ONLINE_CLASS)) {
            if (userMembership.getRemainingSlots() == 0)
                return MembershipStatus.FULL;
        }
        return userMembership.getStatus();
    }

    //full membership details (gym or physical class)
    private MembershipDTO getMembership(Membership membership, String token) {
        MembershipDTO membershipDTO = null;
        if (membership.getType().equals(MembershipType.PHYSICAL_CLASS)) {
            membershipDTO = getPhysicalClassMembershipDTO(membership, null);
        }

        if (membership.getType().equals(MembershipType.ONLINE_CLASS) || membership.getType().equals(MembershipType.CORPORATE)) {
            membershipDTO = getOnlineClassMembershipDTO(membership, null);
        }

        if (membership.getType().equals(MembershipType.GYM) || membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            membershipDTO = getGymMembershipDTOWithoutDistance(membership, null);
        }

        if (membershipDTO != null && token != null) {
            long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            PublicUserMembership purchasedMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());
            if (purchasedMembership != null) {
                membershipDTO.setStatus(getMembershipStatus(purchasedMembership));
                membershipDTO.setPublicUserMembershipId(purchasedMembership.getId());
            } else membershipDTO.setStatus(MembershipStatus.OPEN);
        }
        return membershipDTO;
    }

    //public
    private GymMembershipDTO getGymMembershipDTO(double longitude, double latitude, PublicUser publicUser, GymMembership gymMembership) {
        GymMembershipDTO gymMembershipDTO = new GymMembershipDTO();

        Membership membership = gymMembership.getMembership();

        setMembershipDetails(gymMembershipDTO, membership, publicUser);
        Gym gym = gymMembership.getGym();
        gymMembershipDTO.setGymId(gym.getId());
        gymMembershipDTO.setGymName(gym.getName());
        gymMembershipDTO.setGymUniqueName(gym.getGymUniqueName());
        gymMembershipDTO.setGymDescription(gym.getDescription());
        gymMembershipDTO.setGymRating(gym.getRating());
        gymMembershipDTO.setGymRatingCount(gym.getRatingCount());
        gymMembershipDTO.setGymImage(gym.getProfileImage());

        BusinessProfileLocationDTO location = getLocation(gym.getLocation());
        gymMembershipDTO.setLocation(location);
        double distance = locationService.getDistance(longitude, location.getLongitude(), latitude, location.getLatitude());
        gymMembershipDTO.setDistance(distance);

        setDayPassDetails(gymMembershipDTO, membership);
        return gymMembershipDTO;
    }

    //admin or public without distance calculate
    private GymMembershipDTO getGymMembershipDTOWithoutDistance(Membership membership, PublicUser publicUser) {
        GymMembershipDTO gymMembershipDTO = new GymMembershipDTO();
        setMembershipDetails(gymMembershipDTO, membership, publicUser);
        Gym gym = membership.getGymMembership().getGym();
        gymMembershipDTO.setGymId(gym.getId());
        gymMembershipDTO.setGymName(gym.getName());
        gymMembershipDTO.setGymUniqueName(gym.getGymUniqueName());
        gymMembershipDTO.setGymDescription(gym.getDescription());
        gymMembershipDTO.setGymRating(gym.getRating());
        gymMembershipDTO.setGymRatingCount(gym.getRatingCount());
        gymMembershipDTO.setGymImage(gym.getProfileImage());
        gymMembershipDTO.setLocation(getLocation(gym.getLocation()));
        setDayPassDetails(gymMembershipDTO, membership);
        return gymMembershipDTO;
    }

    private void setDayPassDetails(GymMembershipDTO gymMembershipDTO, Membership membership) {
        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            String eligibleDays = membership.getEligibleDays();
            String[] eligibleDayDetails = eligibleDays.split("");
            DayPassDTO dayPassDTO = new DayPassDTO();
            dayPassDTO.setMonday(eligibleDayDetails[0].equals("1"));
            dayPassDTO.setTuesday(eligibleDayDetails[1].equals("1"));
            dayPassDTO.setWednesday(eligibleDayDetails[2].equals("1"));
            dayPassDTO.setThursday(eligibleDayDetails[3].equals("1"));
            dayPassDTO.setFriday(eligibleDayDetails[4].equals("1"));
            dayPassDTO.setSaturday(eligibleDayDetails[5].equals("1"));
            dayPassDTO.setSunday(eligibleDayDetails[6].equals("1"));
            gymMembershipDTO.setDayPassDTO(dayPassDTO);
        }
    }

    private PhysicalCLassMembershipDTO getPhysicalClassMembershipDTO(Membership membership, PublicUser publicUser) {
        PhysicalCLassMembershipDTO physicalCLassMembershipDTO = new PhysicalCLassMembershipDTO();
        setMembershipDetails(physicalCLassMembershipDTO, membership, publicUser);
        physicalCLassMembershipDTO.setPhysicalClassList(getClassesForMembership(membership, publicUser, physicalCLassMembershipDTO));
        return physicalCLassMembershipDTO;
    }

    private OnlineClassMembershipDTO getOnlineClassMembershipDTO(Membership membership, PublicUser publicUser) {
        OnlineClassMembershipDTO onlineClassMembershipDTO = new OnlineClassMembershipDTO();
        setMembershipDetails(onlineClassMembershipDTO, membership, publicUser);
        onlineClassMembershipDTO.setOnlineClassList(getOnlineClassesForMembership(membership, publicUser, onlineClassMembershipDTO));
        return onlineClassMembershipDTO;
    }

    private List<OnlineClassForMembershipDTO> getOnlineClassesForMembership(Membership membership, PublicUser publicUser, OnlineClassMembershipDTO onlineClassMembershipDTO) {
        List<OnlineClassForMembershipDTO> onlineClassForMembershipDTOS = new ArrayList<>();
        List<OnlineClassMembership> onlineClassMemberships = membership.getOnlineClassMemberships();
        List<Class> classes = onlineClassMemberships.stream().map(OnlineClassMembership::getClassParent).collect(Collectors.toList());
        for (Class clz : classes) {
            OnlineClassForMembershipDTO onlineClassForMembershipDTO = new OnlineClassForMembershipDTO();
            onlineClassForMembershipDTO.setClassId(clz.getId());
            onlineClassForMembershipDTO.setClassName(clz.getName());
            onlineClassForMembershipDTO.setClassRating(clz.getRating());
            onlineClassForMembershipDTO.setClassRatingCount(clz.getRatingCount());
            onlineClassForMembershipDTO.setCategory(clz.getCategory());
            onlineClassForMembershipDTO.setImage(clz.getProfileImage());
            if (publicUser != null)
                onlineClassForMembershipDTO.setSessions(getMembershipSessionForOnlineClass(clz, publicUser));
            onlineClassForMembershipDTOS.add(onlineClassForMembershipDTO);
        }
        return onlineClassForMembershipDTOS;
    }

    private List<ClassForMembershipDTO> getClassesForMembership(Membership membership, PublicUser publicUser, PhysicalCLassMembershipDTO physicalCLassMembershipDTO) {
        List<ClassForMembershipDTO> classForMembershipDTOs = new ArrayList<>();
        List<PhysicalClassMembership> physicalClassMemberships = membership.getPhysicalClassMemberships();
        List<PhysicalClass> physicalClasses = physicalClassMemberships.stream().map(PhysicalClassMembership::getPhysicalClass).collect(Collectors.toList());
        for (PhysicalClass physicalClass : physicalClasses) {
            ClassForMembershipDTO classForMembershipDTO = new ClassForMembershipDTO();
            classForMembershipDTO.setClassId(physicalClass.getId());
            classForMembershipDTO.setClassName(physicalClass.getName());
            classForMembershipDTO.setClassRating(physicalClass.getRating());
            classForMembershipDTO.setClassRatingCount(physicalClass.getRatingCount());
            classForMembershipDTO.setImage(physicalClass.getProfileImage());
            if (publicUser != null)
                classForMembershipDTO.setSessions(getMembershipSessionForClass(physicalClass, publicUser));
            classForMembershipDTOs.add(classForMembershipDTO);
        }
        if (!physicalClassMemberships.isEmpty())
            physicalCLassMembershipDTO.setAllowCashPayment(physicalClassMemberships.get(0).isAllowCashPayment());
        return classForMembershipDTOs;
    }

    private List<SessionForMembershipDTO> getMembershipSessionForClass(PhysicalClass physicalClass, PublicUser publicUser) {
        List<PhysicalClassSession> sessionList = physicalClassSessionRepository.getUpcomingPhysicalSessionsListByClass(physicalClass, LocalDateTime.now(), PageRequest.of(0, 10)).getContent();
        return sessionList.stream().map(session ->
                new SessionForMembershipDTO(session.getId(),
                        session.getDuration(),
                        session.getDateAndTime(),
                        session.getDateAndTime().plusMinutes(session.getDuration()),
                        getLocation(session.getBusinessProfileLocation()),
                        getButtonStatusBySession(session, publicUser))).collect(Collectors.toList());
    }

    private List<SessionForMembershipDTO> getMembershipSessionForOnlineClass(Class classParent, PublicUser publicUser) {
        Gender gender = publicUser.getGender() == null ? Gender.UNISEX : publicUser.getGender();
        List<ClassSession> sessionList = classSessionRepository.getUpcomingSessionsListByClass(classParent, LocalDateTime.now(), gender, PageRequest.of(0, 10)).getContent();
        return sessionList.stream().map(session ->
                new SessionForMembershipDTO(
                        session.getId(),
                        session.getDuration(),
                        session.getDateAndTime(),
                        session.getDateAndTime().plusMinutes(session.getDuration()),
                        null,
                        getButtonStatusByOnlineSession(session, publicUser))).collect(Collectors.toList());
    }

    private SessionButtonStatus getButtonStatusByOnlineSession(ClassSession classSession, PublicUser publicUser) {
        // checks public user is null to apply changes if this call from public user or not(not means an admin or coach).
        if (publicUser != null) {
            ClassSessionEnroll sessionEnrollByUser = classSessionEnrollRepository.findByClassSessionAndPublicUser(classSession, publicUser);

            int enrolledCountForSession = (int) classSessionEnrollRepository.countAllByClassSessionAndStatusAndTimeBetween(classSession, LocalDateTime.now().minusMinutes(ENROLLED_CHECK_MINUTES));
            int availableCount = classSession.getMaxJoiners() - enrolledCountForSession;
            if (sessionEnrollByUser != null) {
                if (sessionEnrollByUser.getPaymentId().startsWith("MEMBERSHIP_") || sessionEnrollByUser.getPaymentId().startsWith("CORPORATE_MEMBERSHIP_")) {
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.PENDING)
                        return SessionButtonStatus.BOOKED;
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.BOOKED)
                        return SessionButtonStatus.BOOKED;
                } else {
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.PENDING)
                        return SessionButtonStatus.PENDING_PURCHASE;
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.BOOKED)
                        return SessionButtonStatus.PURCHASED;
                }
            } else if (availableCount <= 0) {
                return SessionButtonStatus.FULL;
            }
        }
        return SessionButtonStatus.PAY;
    }

    private SessionButtonStatus getButtonStatusBySession(PhysicalClassSession physicalClassSession, PublicUser publicUser) {
        // checks public user is null to apply changes if this call from public user or not(not means an admin or coach).
        if (publicUser != null) {
            PhysicalSessionEnroll sessionEnrollByUser = physicalSessionEnrollRepository.findByPhysicalClassSessionAndPublicUser(physicalClassSession, publicUser);

            int enrolledCountForSession = (int) physicalSessionEnrollRepository.countAllByPhysicalSessionAndStatusAndTimeBetween(physicalClassSession, LocalDateTime.now().minusMinutes(ENROLLED_CHECK_MINUTES));
            int availableCount = physicalClassSession.getMaxJoiners() - enrolledCountForSession;
            if (sessionEnrollByUser != null) {
                if (sessionEnrollByUser.getPaymentId().startsWith("MEMBERSHIP_")) {
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.PENDING)
                        return SessionButtonStatus.BOOKED;
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.BOOKED)
                        return SessionButtonStatus.BOOKED;
                } else {
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.PENDING)
                        return SessionButtonStatus.PENDING_PURCHASE;
                    if (sessionEnrollByUser.getStatus() == SessionEnrollStatus.BOOKED)
                        return SessionButtonStatus.PURCHASED;
                }
            } else if (availableCount <= 0) {
                return SessionButtonStatus.FULL;
            }
        }
        return SessionButtonStatus.PAY;
    }

    private BusinessProfileLocationDTO getLocation(BusinessProfileLocation l) {
        return new BusinessProfileLocationDTO(l.getId(), l.getName(), l.getType(), l.getCountry(), l.getTimeZone(),
                l.getAddressLine1(), l.getAddressLine2(), l.getLongitude(), l.getLatitude(), l.getCity(), l.getProvince(), l.getPostalCode());
    }

    //only membership details (common)
    private MembershipDTO setMembershipDetails(MembershipDTO membershipDTO, Membership membership, PublicUser publicUser) {

        membershipDTO.setMembershipId(membership.getId());
        membershipDTO.setType(membership.getType());
        membershipDTO.setName(membership.getName());
        membershipDTO.setDescription(membership.getDescription());
        membershipDTO.setDuration(membership.getDuration());
        membershipDTO.setSlotCount(membership.getSlotCount());
        membershipDTO.setPrice(membership.getPrice());
        membershipDTO.setDiscount(membership.getDiscount());
        BigDecimal discountedPrice = membership.getPrice().subtract((membership.getPrice().multiply(BigDecimal.valueOf(membership.getDiscount()))).divide(ONE_HUNDRED, 2, RoundingMode.UP));

        BigDecimal roundDiscountedPrice = discountedPrice.setScale(0, RoundingMode.HALF_UP);
        membershipDTO.setDiscountedPrice(roundDiscountedPrice);

        membershipDTO.setNumberOfPurchase(publicUserMembershipRepository.countByMembership(membership));

        if (publicUser != null) {
            PublicUserMembership purchasedMembership;
            if (membership.getType() == MembershipType.GYM || membership.getType() == MembershipType.GYM_DAY_PASS) {
                purchasedMembership = publicUserMembershipRepository.
                        findPublicUserGymMembershipByPublicUserAndMembership
                                (publicUser.getId(), membership.getId());
            } else {
                purchasedMembership = publicUserMembershipRepository.
                        findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());
            }

            if (purchasedMembership != null) {
                if (purchasedMembership.getStatus().equals(MembershipStatus.PENDING)) {
                    membershipDTO.setStatus(MembershipStatus.OPEN);
                } else membershipDTO.setStatus(getMembershipStatus(purchasedMembership));
                membershipDTO.setPublicUserMembershipId
                        (purchasedMembership.getId());
            } else membershipDTO.setStatus(MembershipStatus.OPEN);
        } else {
            membershipDTO.setStatus(membership.getStatus());
        }

        if (membership.getType() == MembershipType.PHYSICAL_CLASS) {
            List<PhysicalClassMembership> physicalClassMemberships = membership.getPhysicalClassMemberships();
            if (!physicalClassMemberships.isEmpty()) {
                membershipDTO.setAllowCashPayment(physicalClassMemberships.get(0).isAllowCashPayment());
                for (PhysicalClassMembership physicalClassMembership : physicalClassMemberships) {
                    membershipDTO.setTrainers(physicalClassMembership.getPhysicalClass().getPhysicalClassTrainers().stream().map(
                            physicalClassTrainer -> physicalClassTrainer.getTrainer().getAuthUser().getFirstName()
                                    + SPACE + physicalClassTrainer.getTrainer().getAuthUser().getLastName()).collect(Collectors.toList()));
                }
            }
        }

        if (membership.getType() == MembershipType.ONLINE_CLASS || membership.getType() == MembershipType.CORPORATE) {
            List<OnlineClassMembership> onlineClassMemberships = membership.getOnlineClassMemberships();
            if (!onlineClassMemberships.isEmpty()) {
                for (OnlineClassMembership onlineClassMembership : onlineClassMemberships) {
                    membershipDTO.setTrainers(onlineClassMembership.getClassParent().getClassTrainers().stream().map(
                            onlineClassTrainer -> onlineClassTrainer.getTrainer().getAuthUser().getFirstName()
                                    + SPACE + onlineClassTrainer.getTrainer().getAuthUser().getLastName()).collect(Collectors.toList()));
                }
            }
        }
        return membershipDTO;
    }

    private MembershipDTO setMembershipHistoryDetails(MembershipDTO membershipDTO, PublicUserMembership userMembership) {
        Membership membership = userMembership.getMembership();
        membershipDTO.setMembershipId(membership.getId());
        membershipDTO.setType(membership.getType());
        membershipDTO.setName(membership.getName());
        membershipDTO.setDescription(membership.getDescription());
        membershipDTO.setDuration(membership.getDuration());
        membershipDTO.setSlotCount(membership.getSlotCount());
        membershipDTO.setPrice(membership.getPrice());
        membershipDTO.setDiscount(membership.getDiscount());
        BigDecimal discountedPrice = membership.getPrice().subtract((membership.getPrice().multiply(BigDecimal.valueOf(membership.getDiscount()))).divide(ONE_HUNDRED, 2, RoundingMode.UP));
        membershipDTO.setDiscountedPrice(discountedPrice);

        membershipDTO.setNumberOfPurchase(publicUserMembershipRepository.countByMembership(membership));
        membershipDTO.setStatus(MembershipStatus.EXPIRED);
        membershipDTO.setPublicUserMembershipId(userMembership.getId());
        return membershipDTO;
    }

    private ClassMembershipAdminDTO getClassMembershipForAdmin(PhysicalClass physicalClass, BusinessProfilePaymentModel paymentModel) {
        return new ClassMembershipAdminDTO(physicalClass.getId(), physicalClass.getName(), physicalClass.getClassType().getTypeName(),
                physicalClassMembershipRepository.countByPhysicalClass(physicalClass), paymentModel,
                physicalClass.getPhysicalClassTrainers().stream().map(physicalClassTrainer ->
                        physicalClassTrainer.getTrainer().getAuthUser().getFirstName() + SPACE +
                                physicalClassTrainer.getTrainer().getAuthUser().getLastName()).collect(Collectors.toList()));
    }

    private OnlineClassMembershipAdminDTO getOnlineClassMembershipForAdmin(Class clz, BusinessProfilePaymentModel paymentModel) {
        return new OnlineClassMembershipAdminDTO(
                clz.getId(),
                clz.getName(),
                clz.getClassType().getTypeName(),
                clz.getCategory(),
                onlineClassMembershipRepository.countByClassParent(clz),
                paymentModel,
                clz.getClassTrainers().stream().map(classTrainer ->
                        classTrainer.getTrainer().getAuthUser().getFirstName()
                                + SPACE +
                                classTrainer.getTrainer().getAuthUser().getLastName()
                ).collect(Collectors.toList())
        );
    }

    private GymMembershipAdminDTO getGymMembershipForAdmin(Gym gym) {
        return new GymMembershipAdminDTO(gym.getId(), gym.getName(), gymMembershipRepository.countByGym(gym), getLocation(gym.getLocation()));
    }

    private void saveFailTransaction(PublicUserMembership userMembership) {

//        PaymentIntent paymentIntent = stripeService.getPaymentIntent(userMembership.getPaymentId());


        FailedTransaction failedTransaction = new FailedTransaction();
        failedTransaction.setAmount(userMembership.getPaidAmount());
        Membership membership = userMembership.getMembership();
        BusinessProfile businessProfile;
        if (membership.getType().equals(MembershipType.PHYSICAL_CLASS)) {
            List<PhysicalClassMembership> physicalClassMemberships = membership.getPhysicalClassMemberships();
            PhysicalClassMembership physicalClassMembership = physicalClassMemberships.get(0);
            PhysicalClass physicalClass = physicalClassMembership.getPhysicalClass();
            businessProfile = physicalClass.getBusinessProfile();
        } else {
            GymMembership gymMembership = membership.getGymMembership();
            Gym gym = gymMembership.getGym();
            BusinessProfileLocation location = gym.getLocation();
            businessProfile = location.getBusinessProfile();
        }
        failedTransaction.setBusinessProfile(businessProfile);
        failedTransaction.setType(TransactionType.MEMBERSHIP_PURCHASE);
        failedTransaction.setTypeId(String.valueOf(userMembership.getId()));
        failedTransaction.setPaymentId(userMembership.getPaymentId());
//        failedTransaction.setDescription(paymentIntent == null || paymentIntent.getCharges().getData().size() <= 0 ?
//                null : paymentIntent.getCharges().getData().get(0).getFailureMessage());
        failedTransactionRepository.save(failedTransaction);

    }

    private DayPassPurchasedDTO getDayPassPurchasedDTO(PublicUserMembership userMembership) {
        PublicUser publicUser = userMembership.getPublicUser();
        Membership membership = userMembership.getMembership();
        GymMembership gymMembership = membership.getGymMembership();
        Gym gym = gymMembership.getGym();
        BusinessProfileLocation location = gym.getLocation();

        return new DayPassPurchasedDTO(gym.getId(), gym.getName(), gym.getGymUniqueName(), gym.getDescription(), gym.getRating(), gym.getRatingCount(),
                gym.getProfileImage(), getLocation(location), userMembership.getDateTime(), userMembership.getExpireDateTime(),
                setMembershipDetails(new MembershipDTO(), membership, publicUser));
    }

//===========================================================private methods - end====================================================================================
//
//
//===========================================================public methods - start===================================================================================

    //admin - physical class membership

    @Override
    @Transactional
    public void createPhysicalCLassMembership(PhysicalCLassMembershipDTO dto) {
        Membership membership = getNewMembership(dto, MembershipType.PHYSICAL_CLASS);
        List<PhysicalClassMembership> physicalClassMemberships = getNewPhysicalClassMemberships(membership, dto);
        membership = membershipRepository.save(membership);
        log.info("Save membership - " + membership);
        physicalClassMembershipRepository.saveAll(physicalClassMemberships);
    }

    @Override
    @Transactional
    public void updatePhysicalClassMembership(PhysicalCLassMembershipDTO dto) {
        Membership membership = updateMembership(dto);
        List<Long> newIdList = dto.getPhysicalClassIdList();
        List<Long> existingIdList = new ArrayList<>();

        /*save existing membership allow status first*/
        List<PhysicalClassMembership> existingPhysicalClassMemberships = new ArrayList<>();
        for (PhysicalClassMembership physicalClassMembership : membership.getPhysicalClassMemberships()) {
            existingIdList.add(physicalClassMembership.getPhysicalClass().getId());
            physicalClassMembership.setAllowCashPayment(dto.isAllowCashPayment());
            existingPhysicalClassMemberships.add(physicalClassMembership);
        }
        physicalClassMembershipRepository.saveAll(existingPhysicalClassMemberships);

        List<Long> removingIdList = new ArrayList<>(existingIdList);

        removingIdList.removeAll(newIdList);
        newIdList.removeAll(existingIdList);

        if (!removingIdList.isEmpty()) {
            List<PhysicalClassMembership> physicalClassMemberships = new ArrayList<>();
            for (Long id : removingIdList) {
                PhysicalClass physicalClass = physicalClassRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
                PhysicalClassMembership physicalClassMembership = physicalClassMembershipRepository.findPhysicalClassMembershipByMembershipAndPhysicalClass(membership, physicalClass);
                physicalClassMemberships.add(physicalClassMembership);
            }
            physicalClassMembershipRepository.deleteAll(physicalClassMemberships);
        }

        if (!newIdList.isEmpty()) {
            List<PhysicalClassMembership> physicalClassMemberships = new ArrayList<>();
            for (Long id : newIdList) {
                PhysicalClass physicalClass = physicalClassRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
                PhysicalClassMembership physicalClassMembership = new PhysicalClassMembership(physicalClass, membership, dto.isAllowCashPayment());
                physicalClassMemberships.add(physicalClassMembership);
            }
            physicalClassMembershipRepository.saveAll(physicalClassMemberships);
        }
    }

    @Override
    public Page<ClassMembershipAdminDTO> getAllMembershipClassesByBusinessProfileId(long businessProfileId, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<PhysicalClass> classPage = physicalClassRepository.getAllByBusinessProfileOrderByIdDesc(businessProfile, pageable);
        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);
        return classPage.map(physicalClass -> getClassMembershipForAdmin(physicalClass, activeAgreement != null ? activeAgreement.getPackageDetail().getPaymentModel() : null));
    }

    @Override
    public Page<OnlineClassMembershipAdminDTO> getAllMembershipOnlineClassesByBusinessProfileId(long businessProfileId, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<Class> classPage = classRepository.getAllByBusinessProfileOrderByIdDesc(businessProfile, pageable);
        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);
        return classPage.map(clz -> getOnlineClassMembershipForAdmin(clz, activeAgreement != null ? activeAgreement.getPackageDetail().getPaymentModel() : null));
    }

    @Override
    public Page<MembershipDTO> getAllMembershipsByPhysicalClassId(long physicalClassId, Pageable pageable) {
        PhysicalClass physicalClass = physicalClassRepository.findById(physicalClassId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        Page<PhysicalClassMembership> membershipPage = physicalClassMembershipRepository.findPhysicalClassMembershipsByPhysicalClassOrderById(physicalClass, pageable);
        BusinessProfile businessProfile = physicalClass.getBusinessProfile();
        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);
        BusinessProfilePaymentModel paymentModel = activeAgreement != null ? activeAgreement.getPackageDetail().getPaymentModel() : null;
        return membershipPage.map(membership -> {
            MembershipDTO membershipDTO = new MembershipDTO();
            membershipDTO.setBusinessName(businessProfile.getBusinessName());
            membershipDTO.setPaymentModel(paymentModel);
            return setMembershipDetails(membershipDTO, membership.getMembership(), null);
        });
    }

    @Override
    public Page<MembershipDTO> getAllMembershipsByOnlineClassId(long classId, Pageable pageable) {
        Class clz = classRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        Page<OnlineClassMembership> membershipPage = onlineClassMembershipRepository
                .findOnlineClassMembershipByClassParentAndMembership_TypeOrderById(clz, MembershipType.ONLINE_CLASS, pageable);
        BusinessProfile businessProfile = clz.getBusinessProfile();
        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);
        BusinessProfilePaymentModel paymentModel = activeAgreement != null ? activeAgreement.getPackageDetail().getPaymentModel() : null;
        return membershipPage.map(membership -> {
            MembershipDTO membershipDTO = new MembershipDTO();
            membershipDTO.setBusinessName(businessProfile.getBusinessName());
            membershipDTO.setPaymentModel(paymentModel);
            return setMembershipDetails(membershipDTO, membership.getMembership(), null);
        });
    }

    @Override
    public Page<ClassMembershipAdminDTO> searchMembershipClassesByBusinessProfileId(long businessProfileId, String text, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<PhysicalClass> classPage = physicalClassRepository.searchClassesByBusinessProfile(text, businessProfile, pageable);
        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);
        return classPage.map(physicalClass -> getClassMembershipForAdmin(physicalClass, activeAgreement != null ? activeAgreement.getPackageDetail().getPaymentModel() : null));
    }

    @Override
    public Page<OnlineClassMembershipAdminDTO> searchMembershipOnlineClassesByBusinessProfileId(long businessProfileId, String text, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId)
                .orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<Class> classPage = classRepository.searchClassesByBusinessProfile(text, businessProfile, pageable);
        BusinessAgreement activeAgreement = businessAgreementRepository
                .findTopByBusinessProfileAndStatusOrderByExpDateDesc(businessProfile, BusinessAgreementStatus.ACTIVE);
        return classPage.map(
                classParent -> getOnlineClassMembershipForAdmin(
                        classParent,
                        activeAgreement != null ? activeAgreement.getPackageDetail().getPaymentModel() : null)
        );
    }

    @Override
    public Page<MembershipDTO> searchMembershipByClassId(long physicalClassId, String text, Pageable pageable) {
        PhysicalClass physicalClass = physicalClassRepository.findById(physicalClassId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        Page<PhysicalClassMembership> membershipPage = physicalClassMembershipRepository.searchMembershipsByClass(physicalClass, text, pageable);
        return membershipPage.map(membership -> setMembershipDetails(new MembershipDTO(), membership.getMembership(), null));
    }

    @Override
    public Page<MembershipDTO> searchMembershipByOnlineClassId(long classId, String text, Pageable pageable) {
        Class classParent = classRepository.findById(classId).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
        Page<OnlineClassMembership> membershipPage = onlineClassMembershipRepository.searchMembershipsByClass(classParent, text, pageable);
        return membershipPage.map(
                membership -> setMembershipDetails(new MembershipDTO(), membership.getMembership(), null));
    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //admin - gym membership

    @Override
    @Transactional
    public void createGymMembership(GymMembershipDTO dto) {

        Gym gym = gymRepository.findById(dto.getGymId()).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        if (membershipRepository.existsByNameAndGymMembership_Gym(dto.getName(), gym))
            throw new CustomServiceException("We found another gym membership for the given name");

        Membership membership = getNewMembership(dto, MembershipType.GYM);

        GymMembership gymMembership = new GymMembership(gym, membership);

        membership = membershipRepository.save(membership);
        gymMembershipRepository.save(gymMembership);
        log.info("Save gym membership - " + membership);
    }

    @Override
    @Transactional
    public void updateGymMembership(GymMembershipDTO dto) {
        updateMembership(dto);
    }

    @Override
    public Page<GymMembershipAdminDTO> getAllMembershipGymsByBusinessProfileId(long businessProfileId, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessProfileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<Gym> gymPage = gymRepository.getAllByLocation_BusinessProfileOrderByIdDesc(businessProfile, pageable);
        return gymPage.map(this::getGymMembershipForAdmin);
    }

    @Override
    public Page<MembershipDTO> getAllMembershipsByGymId(long gymId, Pageable pageable) {
        Gym gym = gymRepository.findById(gymId).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        Page<GymMembership> membershipPage = gymMembershipRepository.findGymMembershipsByGymOrderById(gym, pageable);
        return membershipPage.map(membership -> setMembershipDetails(new MembershipDTO(), membership.getMembership(), null));
    }

    @Override
    public Page<GymMembershipDTO> getAllGymMembershipsByBusinessProfileId(long businessId, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<GymMembership> membershipPage = gymMembershipRepository.findGymMembershipsByGym_Location_BusinessProfileOrderByIdAsc(businessProfile, pageable);
        return membershipPage.map(membership -> getGymMembershipDTOWithoutDistance(membership.getMembership(), null));
    }

    @Override
    public Page<GymMembershipDTO> searchGymMembershipsByBusinessProfileId(long businessId, String text, Pageable pageable) {
        BusinessProfile businessProfile = businessProfileRepository.findById(businessId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<GymMembership> membershipPage = gymMembershipRepository.searchGymMembership(businessProfile, text, pageable);
        return membershipPage.map(membership -> getGymMembershipDTOWithoutDistance(membership.getMembership(), null));
    }
//
//    @Override
//    public Page<MembershipDTO> getAllMembershipPage(Pageable pageable) {
//        Page<Membership> membershipPage = membershipRepository.findAll(pageable);
//        return membershipPage.map(membership -> getMembership(membership, null));
//    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //admin - gym day pass

    @Override
    @Transactional
    public void createGymDayPass(GymMembershipDTO gymMembershipDTO) {

        Gym gym = gymRepository.findById(gymMembershipDTO.getGymId()).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));

        GymMembership existingDayPass = gymMembershipRepository.findGymMembershipByMembership_TypeAndGym(MembershipType.GYM_DAY_PASS, gym);
        if (existingDayPass != null) {
            if (existingDayPass.getMembership().getStatus().equals(MembershipStatus.HIDDEN)) {
                gymMembershipDTO.setName(gym.getName() + DAILY_PASS);
                gymMembershipDTO.setDescription("Day pass membership for " + gym.getName());
                updateGymDayPass(gymMembershipDTO);
            } else throw new CustomServiceException("Day pass is already created for this gym");
        }

        Membership membership = getNewMembership(gymMembershipDTO, MembershipType.GYM_DAY_PASS);
        membership.setDuration(1);
        membership.setName(gym.getName() + DAILY_PASS);
        membership.setDescription("Day pass membership for " + gym.getName());
        membership.setStatus(MembershipStatus.VISIBLE);
        GymMembership gymMembership = new GymMembership(gym, membership);

        DayPassDTO dayPassDTO = gymMembershipDTO.getDayPassDTO();
        String monday = dayPassDTO.isMonday() ? "1" : "0";
        String tuesday = dayPassDTO.isTuesday() ? "1" : "0";
        String wednesday = dayPassDTO.isWednesday() ? "1" : "0";
        String thursday = dayPassDTO.isThursday() ? "1" : "0";
        String friday = dayPassDTO.isFriday() ? "1" : "0";
        String saturday = dayPassDTO.isSaturday() ? "1" : "0";
        String sunday = dayPassDTO.isSunday() ? "1" : "0";
        String eligibleDays = monday + tuesday + wednesday + thursday + friday + saturday + sunday;
        membership.setEligibleDays(eligibleDays);

        membership = membershipRepository.save(membership);
        gymMembershipRepository.save(gymMembership);
        log.info("Save gym day pass - " + membership);
    }

    @Override
    @Transactional
    public void updateGymDayPass(GymMembershipDTO gymMembershipDTO) {

        Membership membership = membershipRepository.findById(gymMembershipDTO.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        membership.setName(gymMembershipDTO.getName());
        membership.setPrice(gymMembershipDTO.getPrice());
        membership.setDescription(gymMembershipDTO.getDescription());
        membership.setDiscount(gymMembershipDTO.getDiscount());
        membership.setDuration(1);
        membership.setStatus(MembershipStatus.VISIBLE);

        DayPassDTO dayPassDTO = gymMembershipDTO.getDayPassDTO();
        String monday = dayPassDTO.isMonday() ? "1" : "0";
        String tuesday = dayPassDTO.isTuesday() ? "1" : "0";
        String wednesday = dayPassDTO.isWednesday() ? "1" : "0";
        String thursday = dayPassDTO.isThursday() ? "1" : "0";
        String friday = dayPassDTO.isFriday() ? "1" : "0";
        String saturday = dayPassDTO.isSaturday() ? "1" : "0";
        String sunday = dayPassDTO.isSunday() ? "1" : "0";
        String eligibleDays = monday + tuesday + wednesday + thursday + friday + saturday + sunday;
        membership.setEligibleDays(eligibleDays);

        membership = membershipRepository.save(membership);
        log.info("Update gym day pass - " + membership);
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //admin - both

    @Override
    public void hideMembership(long membershipId) {
        Membership membership = membershipRepository.findById(membershipId).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        membership.setStatus(MembershipStatus.HIDDEN);
        membership = membershipRepository.save(membership);
        log.info("Membership hided - id-{}, name-{}", membership.getId(), membership.getName());
    }

    @Override
    public void showMembership(long membershipId) {
        Membership membership = membershipRepository.findById(membershipId).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        membership.setStatus(MembershipStatus.VISIBLE);
        membership = membershipRepository.save(membership);
        log.info("Membership un-hided - id-{}, name-{}", membership.getId(), membership.getName());
    }

    @Override
    public Page<MembershipEnrollDTO> getStudentsForMembership(long membershipId, Pageable pageable) {

        Membership membership = membershipRepository.findById(membershipId).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        Page<PublicUserMembership> userMembershipPage = publicUserMembershipRepository.findPublicUserMembershipsByMembershipAndStatus(membership,MembershipStatus.BOOKED,pageable);
        return userMembershipPage.map(userMembership -> {
            PublicUser publicUser = userMembership.getPublicUser();

            String paymentMethod = "";
            String stripePaymentId = userMembership.getPaymentId();

            if (stripePaymentId != null && stripePaymentId.startsWith("CASH_")) {
                paymentMethod = "CASH";
            } else if (stripePaymentId != null && stripePaymentId.startsWith("FIRST_FREE_")) {
                paymentMethod = "FIRST_FREE";
            } else if (stripePaymentId != null && stripePaymentId.startsWith("MANUAL_")) {
                paymentMethod = "MANUAL";
            } else if (stripePaymentId == null) {
                paymentMethod = "CORPORATE";
            } else {
                paymentMethod = "ONLINE";
            }

            int age;
            if (publicUser.getDateOfBirth() == null) {
                age = 0;
            } else {
                age = LocalDate.now().compareTo(publicUser.getDateOfBirth());
            }

            AuthUser collectedBy = userMembership.getCollectedBy();

            return new MembershipEnrollDTO(
                    userMembership.getId(),
                    publicUser.getId(),
                    publicUser.getFirstName() + " " + publicUser.getLastName(),
                    publicUser.getGender(),
                    age,
                    publicUser.getCountry(),
                    userMembership.getDateTime(),
                    userMembership.getPaidAmount(),
                    paymentMethod,
                    userMembership.getStatus(),
                    collectedBy != null ? collectedBy.getFirstName() + SPACE + collectedBy.getLastName() : null,
                    userMembership.getRemainingSlots()
            );
        });
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //public - physical class membership

    @Override
    public List<MembershipDTO> getMembershipsByPhysicalSession(PhysicalClassSession session, PublicUser publicUser) {
        PhysicalClass physicalClass = session.getPhysicalClass();
        return getMembershipsByPhysicalClass(physicalClass, publicUser);
    }

    @Override
    public List<MembershipDTO> getMembershipsByPhysicalClass(PhysicalClass physicalClass, PublicUser publicUser) {
        List<PhysicalClassMembership> memberships = physicalClassMembershipRepository.findPhysicalClassMembershipsByPhysicalClassAndMembership_Status(physicalClass, MembershipStatus.VISIBLE);
        return memberships.stream().map(pm -> setMembershipDetails(new MembershipDTO(), pm.getMembership(), publicUser)).collect(Collectors.toList());
    }

    public List<MembershipDTO> getMembershipsByOnlineClass(Class classParent, PublicUser publicUser) {
        List<OnlineClassMembership> memberships = onlineClassMembershipRepository.
                findOnlineClassMembershipByClassParentAndMembership_StatusAndMembership_Type
                        (classParent, MembershipStatus.VISIBLE, MembershipType.ONLINE_CLASS);
        return memberships.stream().map(m ->
                setMembershipDetails(
                        new MembershipDTO(),
                        m.getMembership(),
                        publicUser)
        ).collect(Collectors.toList());
    }

    @Override
    public Page<MembershipsForPhysicalClassDTO> getPhysicalClassMembershipsByBusinessProfile(long businessId, Pageable pageable, String token) {

        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<PhysicalClass> classPage = physicalClassRepository.getAllByBusinessProfileAndCountryOnlyMembershipAndUpcoming(businessProfile.getId(), publicUser.getCountry(), pageable);

        List<MembershipsForPhysicalClassDTO> membershipsForPhysicalCLassList = new ArrayList<>();

        for (PhysicalClass physicalClass : classPage) {

            List<MembershipDTO> membershipsByPhysicalClass = getMembershipsByPhysicalClass(physicalClass, publicUser);

            List<PhysicalClassSession> classSessions = physicalClassSessionRepository.getUpcomingSessionsPageByClass(physicalClass);

            ClassSessionListResponse session = null;

            if (classSessions.size() > 0) {

                PhysicalClassSession upcomingSession = classSessions.get(0);
                SessionButtonStatus buttonStatus = physicalClassService.getButtonStatusBySession(upcomingSession, physicalClass, publicUser, LocalDateTime.now());
                PublicUserDiscountDTO discountDetails = new PublicUserDiscountDTO();
                if (buttonStatus.equals(SessionButtonStatus.DISCOUNT)) {
                    discountDetails = classSessionService.getDiscountDetails(publicUser);
                }

//                session = modelMapper.map(upcomingSession, ClassSessionListResponse.class);

                session = new ClassSessionListResponse(
                        upcomingSession.getId(),
                        upcomingSession.getName(),
                        upcomingSession.getDuration(),
                        upcomingSession.getDescription(),
                        upcomingSession.getDateAndTime(),
                        upcomingSession.getDateAndTime().plusMinutes(upcomingSession.getDuration()),
                        null,
                        upcomingSession.getPrice(),
                        upcomingSession.getGender(),
                        upcomingSession.getLanguage() != null ? upcomingSession.getLanguage().getLanguageName() : null,
                        upcomingSession.getMaxJoiners(),
                        0,
                        upcomingSession.getStatus(),
                        upcomingSession.getTrainer().getAuthUser().getFirstName(),
                        upcomingSession.getTrainer().getAuthUser().getLastName(),
                        physicalClass.getId(),
                        physicalClass.getName(),
                        physicalClass.getClassUniqueName(),
                        physicalClass.getProfileImage(),
                        null,
                        physicalClass.getClassType().getTypeName(),
                        physicalClass.getRating(),
                        physicalClass.getRatingCount(),
                        0,
                        physicalClass.getCalorieBurnOut(),
                        null,
                        buttonStatus,
                        discountDetails.getMaxDiscount(),
                        discountDetails.getPercentage(),
                        discountDetails.getDescription(),
                        null,
                        null,
                        0.0
                );

                setPhysicalClassMembershipDetails(session, null, physicalClass, publicUser);
                if (session.isCorporateMembershipBooked() &&
                        (session.getButtonStatus().equals(SessionButtonStatus.PAY) ||
                                session.getButtonStatus().equals(SessionButtonStatus.DISCOUNT) ||
                                session.getButtonStatus().equals(SessionButtonStatus.FIRST_FREE)))
                    session.setButtonStatus(SessionButtonStatus.CORPORATE_RESERVE);

            }
            if ((membershipsByPhysicalClass != null && !membershipsByPhysicalClass.isEmpty()) || session != null)
                membershipsForPhysicalCLassList.add(
                        new MembershipsForPhysicalClassDTO(
                                physicalClass.getId(),
                                physicalClass.getName(),
                                physicalClass.getClassUniqueName(),
                                session,
                                membershipsByPhysicalClass
                        ));
        }
        return new PageImpl<>(membershipsForPhysicalCLassList, classPage.getPageable(), classPage.getTotalElements());
    }

    @Override
    public Page<MembershipsForOnlineClassDTO> getOnlineClassMembershipsByBusinessProfile(long businessId, Pageable pageable, String token) {

        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<Class> classPage = classRepository.getAllByBusinessProfileOrderByIdDescOnlyUpcoming(businessProfile.getId(), pageable);

        List<MembershipsForOnlineClassDTO> membershipsForOnlineClassList = new ArrayList<>();

        for (Class clz : classPage.getContent()) {

            List<MembershipDTO> membershipsByOnlineClass = getMembershipsByOnlineClass(clz, publicUser);
            List<ClassSession> classSessions = classSessionRepository.getUpcomingClassSessionsByClassParent(clz);

            ClassSessionListResponse session = null;

            if (classSessions.size() > 0) {

                ClassSession upcomingSession = classSessions.get(0);
                SessionButtonStatus buttonStatus = classSessionService.getButtonStatusBySession(upcomingSession, clz, publicUser, LocalDateTime.now());

                PublicUserDiscountDTO discountDetails = new PublicUserDiscountDTO();
                if (buttonStatus.equals(SessionButtonStatus.DISCOUNT)) {
                    discountDetails = classSessionService.getDiscountDetails(publicUser);
                }

                session = new ClassSessionListResponse(
                        upcomingSession.getId(),
                        upcomingSession.getName(),
                        upcomingSession.getDuration(),
                        upcomingSession.getDescription(),
                        upcomingSession.getDateAndTime(),
                        upcomingSession.getDateAndTime().plusMinutes(upcomingSession.getDuration()),
                        null,
                        upcomingSession.getPrice(),
                        upcomingSession.getGender(),
                        upcomingSession.getLanguage() != null ? upcomingSession.getLanguage().getLanguageName() : null,
                        upcomingSession.getMaxJoiners(),
                        0,
                        upcomingSession.getStatus(),
                        upcomingSession.getTrainer().getAuthUser().getFirstName(),
                        upcomingSession.getTrainer().getAuthUser().getLastName(),
                        clz.getId(),
                        clz.getName(),
                        clz.getClassUniqueName(),
                        clz.getProfileImage(),
                        null,
                        clz.getClassType().getTypeName(),
                        clz.getRating(),
                        clz.getRatingCount(),
                        0,
                        clz.getCalorieBurnOut(),
                        clz.getCategory(),
                        buttonStatus,
                        discountDetails.getMaxDiscount(),
                        discountDetails.getPercentage(),
                        discountDetails.getDescription(),
                        null,
                        null,
                        0.0
                );
                setOnlineClassMembershipDetails(session, null, clz, publicUser);

                if (session.isCorporateMembershipBooked() &&
                        (session.getButtonStatus().equals(SessionButtonStatus.PAY) ||
                                session.getButtonStatus().equals(SessionButtonStatus.DISCOUNT) ||
                                session.getButtonStatus().equals(SessionButtonStatus.FIRST_FREE)))
                    session.setButtonStatus(SessionButtonStatus.CORPORATE_RESERVE);

            }
            if ((membershipsByOnlineClass != null && !membershipsByOnlineClass.isEmpty()) || session != null) {
                membershipsForOnlineClassList.add(
                        new MembershipsForOnlineClassDTO(
                                clz.getId(),
                                clz.getName(),
                                clz.getClassUniqueName(),
                                session,
                                membershipsByOnlineClass
                        )
                );
            }
        }

        return new PageImpl<>(membershipsForOnlineClassList, classPage.getPageable(), classPage.getTotalElements());
    }

    @Override
    @Transactional
    public void reserveMembershipSession(MembershipSessionReserveDTO dto, String timeZone) {

        PublicUser publicUser = publicUserRepository.findById(dto.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Membership membership = membershipRepository.findById(dto.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));

        publicUserService.saveUserTimeZone(publicUser, timeZone);

        PhysicalClassSession physicalSession = physicalClassSessionRepository.findById(dto.getSessionId()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
        PublicUserMembership userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());

        if (userMembership == null) {
            throw new CustomServiceException("User Membership expired or remaining slots not available");
        }

        if (!userMembership.getStatus().equals(MembershipStatus.BOOKED)) {
            throw new CustomServiceException("This membership is not valid");
        }

        if (publicUser.getGender() != null) {
            if (!(physicalSession.getGender().equals(Gender.UNISEX) || physicalSession.getGender().equals(publicUser.getGender()))) {
                throw new CustomServiceException("You can not reserve this session!");
            }
        }
//        long classEnrollCount = physicalSessionEnrollRepository.countAllByPublicUserAndPhysicalClassSessionPhysicalClass(publicUser, physicalSession.getPhysicalClass());
//        if (!(classEnrollCount <= 0 && physicalSession.getPhysicalClass().isFirstSessionFree()))

        PhysicalSessionEnroll duplicateEnroll = physicalSessionEnrollRepository.findByPhysicalClassSessionAndPublicUser(physicalSession, publicUser);
        if (duplicateEnroll != null && (duplicateEnroll.getStatus().equals(SessionEnrollStatus.BOOKED)))
            throw new CustomServiceException("You have already reserved this session");
        if (duplicateEnroll != null && (duplicateEnroll.getStatus().equals(SessionEnrollStatus.PENDING)))
            throw new CustomServiceException("Your reservation is pending for this session");
        userMembership.setRemainingSlots(userMembership.getRemainingSlots() - 1);
        PhysicalSessionEnroll physicalSessionEnroll = new PhysicalSessionEnroll();
        physicalSessionEnroll.setPhysicalClassSession(physicalSession);
        physicalSessionEnroll.setPublicUser(publicUser);
        physicalSessionEnroll.setDateTime(LocalDateTime.now());
        physicalSessionEnroll.setStatus(SessionEnrollStatus.BOOKED);
        physicalSessionEnroll.setPaidAmount(BigDecimal.ZERO);
        physicalSessionEnroll.setPaymentId("MEMBERSHIP_" + userMembership.getId() + "_SESSION_" + physicalSession.getId());

        physicalSessionEnroll = physicalSessionEnrollRepository.save(physicalSessionEnroll);
        publicUserMembershipRepository.save(userMembership);
        log.info("Session(" + physicalSession.getId() + ") booked by membership(user membership id- " + userMembership.getId() + ")");
        sessionNotificationService.sendPhysicalSessionNotifications(physicalSession, publicUser);
    }

    @Override
    @Transactional
    public void reserveOnlineMembershipSession(MembershipSessionReserveDTO dto, String timeZone) {

        PublicUser publicUser = publicUserRepository.findById(dto.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Membership membership = membershipRepository.findById(dto.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));

        publicUserService.saveUserTimeZone(publicUser, timeZone);

        ClassSession classSession = classSessionRepository.findById(dto.getSessionId()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
        PublicUserMembership userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());

        if (publicUser.getGender() != null) {
            if (!(classSession.getGender().equals(Gender.UNISEX) || classSession.getGender().equals(publicUser.getGender()))) {
                throw new CustomServiceException("You can not reserve this session!");
            }
        }

        if (userMembership == null) {
            throw new CustomServiceException("User Membership expired or remaining slots not available");
        }

        if (!userMembership.getStatus().equals(MembershipStatus.BOOKED)) {
            throw new CustomServiceException("This membership is not valid");
        }

        ClassSessionEnroll duplicateEnroll = classSessionEnrollRepository.findByClassSessionAndPublicUser(classSession, publicUser);
        if (duplicateEnroll != null && (duplicateEnroll.getStatus().equals(SessionEnrollStatus.BOOKED)))
            throw new CustomServiceException("You have already reserved this session");
        if (duplicateEnroll != null && (duplicateEnroll.getStatus().equals(SessionEnrollStatus.PENDING)))
            throw new CustomServiceException("Your reservation is pending for this session");

        int remainingSlots = userMembership.getRemainingSlots();
        userMembership.setRemainingSlots(remainingSlots == -1 ? -1 : remainingSlots - 1);

        ClassSessionEnroll classSessionEnroll = new ClassSessionEnroll();
        classSessionEnroll.setClassSession(classSession);
        classSessionEnroll.setPublicUser(publicUser);
        classSessionEnroll.setDateTime(LocalDateTime.now());
        classSessionEnroll.setStatus(SessionEnrollStatus.BOOKED);
        classSessionEnroll.setPaidAmount(BigDecimal.ZERO);

        String stripePaymentId = "MEMBERSHIP_" + userMembership.getId() + "_SESSION_" + classSession.getId();
        if (membership.getType().equals(MembershipType.CORPORATE)) {

            Corporate corporate = membership.getCorporate();
            if (corporate.getId() == FitNexusConstants.CorporateConstants.AIA_CORPORATE_ID && !membership.getActivationCode().equals("AIAEMP")) {
                boolean userValid = aiaUserCheck.isUserValid(publicUser.getMobile());
                if (!userValid) {
                    userMembership.setExpireDateTime(LocalDateTime.now());
                    userMembership.setStatus(MembershipStatus.DEACTIVATED);
                    publicUserMembershipRepository.save(userMembership);
                    throw new CustomServiceException("Your AIA membership is not valid anymore. Please contact AIA administrator!");
                }
            }

            stripePaymentId = "CORPORATE_MEMBERSHIP_" + userMembership.getId() + "_SESSION_" + classSession.getId();
        }
        classSessionEnroll.setPaymentId(stripePaymentId);

        classSessionEnroll = classSessionEnrollRepository.save(classSessionEnroll);
        publicUserMembershipRepository.save(userMembership);
        log.info("Online Session(" + classSession.getId() + ") booked by membership(user membership id- " + userMembership.getId() + ")");
        sessionNotificationService.sendOnlineSessionNotifications(classSession, publicUser);
    }

    @Override
    @Transactional
    public void cancelMembershipSessionReservation(MembershipSessionReserveDTO dto) {
        PublicUser publicUser = publicUserRepository.findById(dto.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Membership membership = membershipRepository.findById(dto.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        PhysicalClassSession physicalSession = physicalClassSessionRepository.findById(dto.getSessionId()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));
        PublicUserMembership userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());
        PhysicalSessionEnroll physicalSessionEnroll = physicalSessionEnrollRepository.findByPhysicalClassSessionAndPublicUser(physicalSession, publicUser);
        if (physicalSessionEnroll == null) throw new CustomServiceException("You have not reserved this session");
        if (LocalDateTime.now().until(physicalSession.getDateAndTime(), ChronoUnit.HOURS) < 2)
            throw new CustomServiceException("The session will be start in 2 hours. Can not cancel reservation");
        userMembership.setRemainingSlots(userMembership.getRemainingSlots() + 1);
        physicalSessionEnrollRepository.delete(physicalSessionEnroll);
        publicUserMembershipRepository.save(userMembership);
        log.info("Session(" + physicalSession.getId() + ") booking canceled by membership(user membership id- " + userMembership.getId() + ")");
        //cancel scheduled notifications
        List<PublicUserNotification> allScheduledNotifications = publicUserNotificationRepository
                .findAllByTypeAndTypeId(NotificationType.PHYSICAL_SESSION, String.valueOf(physicalSession.getId()));
        publicUserService.cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_MOBILE);
        publicUserService.cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_WEB);
    }

    @Override
    @Transactional
    public void cancelOnlineMembershipSessionReservation(MembershipSessionReserveDTO dto) {

        PublicUser publicUser = publicUserRepository.findById(dto.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Membership membership = membershipRepository.findById(dto.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));

        ClassSession classSession = classSessionRepository.findById(dto.getSessionId()).orElseThrow(() -> new CustomServiceException(NO_SESSION_FOUND));

        PublicUserMembership userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());

        ClassSessionEnroll classSessionEnroll = classSessionEnrollRepository.findByClassSessionAndPublicUser(classSession, publicUser);

        if (classSessionEnroll == null) throw new CustomServiceException("You have not reserved this session");

        if (LocalDateTime.now().until(classSession.getDateAndTime(), ChronoUnit.HOURS) < 2)
            throw new CustomServiceException("The session will be start in 2 hours. Can not cancel reservation");

        if (membership.getSlotCount() == -1) userMembership.setRemainingSlots(-1);
        else userMembership.setRemainingSlots(userMembership.getRemainingSlots() + 1);

        classSessionEnrollRepository.delete(classSessionEnroll);
        publicUserMembershipRepository.save(userMembership);

        log.info("Session(" + classSession.getId() + ") booking canceled by membership(user membership id- " + userMembership.getId() + ")");

        //cancel scheduled notifications
        List<PublicUserNotification> allScheduledNotifications = publicUserNotificationRepository
                .findAllByTypeAndTypeId(NotificationType.SESSION, String.valueOf(classSession.getId()));

        publicUserService.cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_MOBILE);
        publicUserService.cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_WEB);
    }

    @Override
    public void setPhysicalClassMembershipDetails(ClassSessionListResponse sessionResponse, ClassDetailsDTO classResponse, PhysicalClass physicalClass, PublicUser publicUser) {
        List<PublicUserMembership> purchasedMemberships = new ArrayList<>();
        List<PhysicalClassMembership> classMemberships = physicalClassMembershipRepository.findPhysicalClassMembershipsByPhysicalClass(physicalClass);
        for (PhysicalClassMembership classMembership : classMemberships) {
            PublicUserMembership purchasedMembership = publicUserMembershipRepository.
                    findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), classMembership.getMembership().getId());
            if (purchasedMembership != null && purchasedMembership.getStatus().equals(MembershipStatus.BOOKED))
                purchasedMemberships.add(purchasedMembership);
        }
        List<MembershipDTO> memberships = getMembershipsByPhysicalClass(physicalClass, publicUser);
        List<PhysicalClassMembershipSlotCountDTO> bookedMemberships = purchasedMemberships.stream()
                .map(um -> new PhysicalClassMembershipSlotCountDTO(
                        um.getMembership().getId(),
                        um.getId(),
                        um.getMembership().getName(),
                        um.getRemainingSlots(),
                        um.getExpireDateTime())).collect(Collectors.toList());

        setMembershipDetails(sessionResponse, classResponse, memberships, bookedMemberships);
    }

    @Override
    public void setOnlineClassMembershipDetails(ClassSessionListResponse sessionResponse, ClassDetailsDTO classResponse, Class classParent, PublicUser publicUser) {

        List<PublicUserMembership> purchasedMemberships = new ArrayList<>();
        List<OnlineClassMembership> classMemberships = onlineClassMembershipRepository.findOnlineClassMembershipsByClassParentAndAndMembership_Status(classParent, MembershipStatus.VISIBLE);

        for (OnlineClassMembership classMembership : classMemberships) {
            PublicUserMembership purchasedMembership = publicUserMembershipRepository.
                    findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), classMembership.getMembership().getId());
            if (purchasedMembership != null && purchasedMembership.getStatus().equals(MembershipStatus.BOOKED))
                purchasedMemberships.add(purchasedMembership);
        }

        List<MembershipDTO> memberships = getMembershipsByOnlineClass(classParent, publicUser);

        List<PhysicalClassMembershipSlotCountDTO> bookedMemberships = new ArrayList<>();
        List<PublicUserMembership> corporateMemberships = new ArrayList<>();

        for (PublicUserMembership um : purchasedMemberships) {
            Membership membership = um.getMembership();
            if (membership.getType().equals(MembershipType.CORPORATE)) {
                corporateMemberships.add(um);
            } else {
                bookedMemberships.add(new PhysicalClassMembershipSlotCountDTO(
                        membership.getId(),
                        um.getId(),
                        membership.getName(),
                        um.getRemainingSlots(),
                        um.getExpireDateTime()));
            }
        }
        if (sessionResponse != null) {
            if (corporateMemberships.size() > 0) {
                corporateMemberships.sort(Comparator.comparing(PublicUserMembership::getExpireDateTime));
                sessionResponse.setCorporateMembershipBooked(true);
                sessionResponse.setCorporates(
                        corporateMemberships.stream().map(userMembership -> {
                            Membership membership = userMembership.getMembership();
                            Corporate corporate = membership.getCorporate();
                            boolean membershipAIA = corporate.getId() == FitNexusConstants.CorporateConstants.AIA_CORPORATE_ID;
                            return new CorporateMembershipNameIdDTO(corporate.getId(), corporate.getName(), membership.getId(), membership.getName(), membershipAIA);
                        }).collect(Collectors.toList()));

            } else {
                sessionResponse.setCorporateMembershipBooked(false);
            }
        } else {
            corporateMemberships = null;
        }

        setMembershipDetails(sessionResponse, classResponse, memberships, bookedMemberships);
    }

    private void setMembershipDetails(ClassSessionListResponse sessionResponse, ClassDetailsDTO classResponse, List<MembershipDTO> memberships, List<PhysicalClassMembershipSlotCountDTO> bookedMemberships) {
        if (bookedMemberships.size() > 0) {
            if (sessionResponse != null) {
                sessionResponse.setMembershipBooked(true);
                sessionResponse.setBookedMemberships(bookedMemberships);
            }
            if (classResponse != null) {
                classResponse.setMembershipBooked(true);
                classResponse.setBookedMemberships(bookedMemberships);
            }
        } else {
            if (sessionResponse != null) sessionResponse.setMembershipBooked(false);
            if (classResponse != null) classResponse.setMembershipBooked(false);
        }
        if (memberships != null) {
            if (sessionResponse != null) {
                sessionResponse.setMembershipCount(memberships.size());
                sessionResponse.setMemberships(memberships);
            }
            if (classResponse != null) {
                classResponse.setMembershipCount(memberships.size());
                classResponse.setMemberships(memberships);
            }
        } else {
            if (sessionResponse != null) sessionResponse.setMembershipCount(0);
            if (classResponse != null) classResponse.setMembershipCount(0);
        }
    }

    @Override
    public Page<MembershipsForPhysicalClassDTO> getPurchasedPhysicalClassMemberships(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PhysicalClass> classPage = physicalClassMembershipRepository.findPurchasedMembershipPhysicalClasses(publicUser, pageable);
        return classPage.map(physicalClass -> {
            List<PublicUserMembership> memberships = physicalClassMembershipRepository.findPurchasedMembershipsForPhysicalClass(publicUser, physicalClass);
            List<MembershipDTO> membershipDTOList = memberships.stream().map(userMembership -> setMembershipDetails(new MembershipDTO(), userMembership.getMembership(), publicUser)).collect(Collectors.toList());
            return new MembershipsForPhysicalClassDTO(physicalClass.getId(), physicalClass.getName(), physicalClass.getClassUniqueName(), null, membershipDTOList);
        });
    }

    @Override
    public Page<MembershipsForOnlineClassDTO> getPurchasedOnlineClassMemberships(long userId, MembershipType type, Pageable pageable) {

        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        Page<Class> classPage = onlineClassMembershipRepository.findPurchasedMembershipOnlineClasses(publicUser, type, pageable);

        return classPage.map(onlineClass -> {
            List<PublicUserMembership> memberships = onlineClassMembershipRepository.findPurchasedMembershipsForOnlineClass(publicUser, onlineClass);
            List<MembershipDTO> membershipDTOList = new ArrayList<>();
            for (PublicUserMembership userMembership : memberships) {
                Membership membership = userMembership.getMembership();
                if (membership.getType().equals(type)) {
                    membershipDTOList.add(setMembershipDetails(new MembershipDTO(), membership, publicUser));
                }
            }
            return new MembershipsForOnlineClassDTO(onlineClass.getId(), onlineClass.getName(), onlineClass.getClassUniqueName(), null, membershipDTOList);
        });
    }

    @Override
    public Page<MembershipsForPhysicalClassDTO> getPurchasedPhysicalClassMembershipsHistory(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PhysicalClass> classPage = physicalClassMembershipRepository.findPurchasedMembershipPhysicalClassesHistory(publicUser, pageable);
        return classPage.map(physicalClass -> {
            List<PublicUserMembership> memberships = physicalClassMembershipRepository.findPurchasedMembershipsForPhysicalClassHistory(publicUser, physicalClass);
            List<MembershipDTO> membershipDTOList = memberships.stream().map(userMembership -> setMembershipHistoryDetails(new MembershipDTO(), userMembership)).collect(Collectors.toList());
            return new MembershipsForPhysicalClassDTO(physicalClass.getId(), physicalClass.getName(), physicalClass.getClassUniqueName(), null, membershipDTOList);
        });
    }

    @Override
    public Page<MembershipsForOnlineClassDTO> getPurchasedOnlineClassMembershipsHistory(long userId, MembershipType type, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<Class> classPage = onlineClassMembershipRepository.findPurchasedMembershipOnlineClassesHistory(publicUser, pageable);
        return classPage.map(classParent -> {
            List<PublicUserMembership> memberships = onlineClassMembershipRepository.findPurchasedMembershipsForOnlineClassHistory(publicUser, classParent, type);
            List<MembershipDTO> membershipDTOList = new ArrayList<>();
            for (PublicUserMembership userMembership : memberships) {
                Membership membership = userMembership.getMembership();
                if (membership.getType().equals(type)) {
                    membershipDTOList.add(setMembershipHistoryDetails(new MembershipDTO(), userMembership));
                }
            }
            return new MembershipsForOnlineClassDTO(classParent.getId(), classParent.getName(), classParent.getClassUniqueName(), null, membershipDTOList);
        });
    }

    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //public - gym membership

    @Override
    public GymDTO setMembershipDetailsForGymDTO(Gym gym, GymDTO gymDTO, PublicUser publicUser) {

        PublicUserMembership purchasedMembership = publicUserMembershipRepository.
                findTopByPublicUserAndMembership_GymMembership_GymAndMembership_TypeOrderByExpireDateTimeDesc(publicUser, gym, MembershipType.GYM);

        if (purchasedMembership != null && purchasedMembership.getStatus().equals(MembershipStatus.BOOKED)
                && purchasedMembership.getExpireDateTime().isAfter(CustomGenerator.getDateTimeByZone(LocalDateTime.now(), publicUser.getTimeZone()))) {
            gymDTO.setMembershipBooked(true);
            gymDTO.setMembershipExpireDateTime(purchasedMembership.getExpireDateTime());
        } else {
            gymDTO.setMembershipBooked(false);
            gymDTO.setMembershipExpireDateTime(null);
        }
        DayPassForGymDTO dayPassForGym = getDayPassForGym(gym, publicUser);
        if (dayPassForGym != null) {
            gymDTO.setDayPassAllowed(true);
            gymDTO.setDayPass(dayPassForGym);
            gymDTO.setDayPassState(dayPassForGym.getStatus().equals(MembershipStatus.BOOKED) || dayPassForGym.getStatus().equals(MembershipStatus.PENDING));
        } else gymDTO.setDayPassAllowed(false);

        List<MembershipDTO> memberships = getGymMembershipsForGym(gym, publicUser);
        if (memberships != null) {
            gymDTO.setMembershipCount(memberships.size());
            gymDTO.setMemberships(memberships);
        }
        return gymDTO;
    }

    @Override
    public GymDTO setMembershipDetailsForGymOpen(Gym gym, GymDTO gymDTO) {
        gymDTO.setMembershipBooked(false);
        gymDTO.setMembershipExpireDateTime(null);

        gymDTO.setDayPassAllowed(false);

        List<MembershipDTO> memberships = getGymMembershipsForGym(gym, null);
        if (memberships != null) {
            gymDTO.setMembershipCount(memberships.size());
            gymDTO.setMemberships(memberships);
        }
        return gymDTO;
    }

    @Override
    public List<MembershipDTO> getGymMembershipsForGym(Gym gym, PublicUser publicUser) {
        List<GymMembership> memberships = gymMembershipRepository.findGymMembershipsByMembership_TypeAndMembership_StatusAndGymOrderByMembership_DiscountDescMembership_DurationAscMembership_PriceAsc(MembershipType.GYM, MembershipStatus.VISIBLE, gym);
        return memberships.stream().map(membership -> setMembershipDetails(new MembershipDTO(), membership.getMembership(), publicUser)).sorted().collect(Collectors.toList());
    }

    @Override
    public Page<MembershipDTO> getGymMembershipsForGym(long gymId, long userId, Pageable pageable) {
        Gym gym = gymRepository.findById(gymId).orElseThrow(() -> new CustomServiceException(NO_GYM_FOUND));
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<GymMembership> memberships = gymMembershipRepository.findGymMembershipsByMembership_TypeAndMembership_StatusAndGymOrderByMembership_DurationAsc(MembershipType.GYM, MembershipStatus.VISIBLE, gym, pageable);
        return memberships.map(membership -> setMembershipDetails(new MembershipDTO(), membership.getMembership(), publicUser));
    }

    @Override
    public Page<MembershipsForGymDTO> getGymMembershipsByBusinessProfile(long businessId, Pageable pageable, String token) {
        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        BusinessProfile businessProfile = businessProfileRepository.findById(businessId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        Page<Gym> gymPage = gymRepository.getAllByLocation_BusinessProfileOrderByIdDesc(businessProfile, pageable);
        List<MembershipsForGymDTO> membershipsForGymList = new ArrayList<>();
        for (Gym gym : gymPage) {
            MembershipsForGymDTO membershipDetailsForGym = getMembershipDetailsForGym(gym, publicUser);
            List<MembershipDTO> memberships = membershipDetailsForGym.getMemberships();
            if (memberships != null && memberships.size() > 0) {
                membershipsForGymList.add(membershipDetailsForGym);
            }
        }
        return new PageImpl<>(membershipsForGymList, pageable, membershipsForGymList.size());
    }

    private MembershipsForGymDTO getMembershipDetailsForGym(Gym gym, PublicUser publicUser) {

        boolean membershipBooked = false;
        LocalDateTime membershipExpireDateTime = null;

        PublicUserMembership purchasedMembership = publicUserMembershipRepository.
                findTopByPublicUserAndMembership_GymMembership_GymAndMembership_TypeOrderByExpireDateTimeDesc(publicUser, gym, MembershipType.GYM);
        if (purchasedMembership != null) {
            membershipBooked = true;
            membershipExpireDateTime = purchasedMembership.getExpireDateTime();
        }
        return new MembershipsForGymDTO(gym.getId(), gym.getName(), gym.getGymUniqueName(), membershipBooked, membershipExpireDateTime, getGymMembershipsForGym(gym, publicUser));
    }

    @Override
    public Page<GymMembershipDTO> getGymMemberships(double longitude, double latitude, Pageable pageable, String token) {
        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<Gym> gymPage = gymMembershipRepository.findMembershipGyms(pageable, longitude, latitude);
        return getGymMembershipDtoPage(longitude, latitude, publicUser, gymPage, pageable);
    }

    @Override
    public Page<GymMembershipDTO> searchGymMemberships(double longitude, double latitude, String country, String text, Pageable pageable, String token) {
        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<Gym> gymPage = gymMembershipRepository.searchMembershipGyms(text, country, longitude, latitude, pageable);
        return getGymMembershipDtoPage(longitude, latitude, publicUser, gymPage, pageable);
    }

    private Page<GymMembershipDTO> getGymMembershipDtoPage(double longitude, double latitude, PublicUser publicUser, Page<Gym> gymPage, Pageable pageable) {
        List<GymMembershipDTO> gymMembershipDTOList = new ArrayList<>();
        for (Gym gym : gymPage) {
            //following two lines were commented because of the conflicts in the UI
//            Membership membership = membershipRepository.findTopByGymMembership_GymAndDurationAndStatusOrderByPriceAsc(gym, 365, MembershipStatus.VISIBLE);
//            if (membership == null)
            Membership membership = membershipRepository.findTopByGymMembership_GymAndTypeAndStatusOrderByDiscountDescPriceDesc(gym, MembershipType.GYM, MembershipStatus.VISIBLE);
            GymMembershipDTO gymMembershipDTO = getGymMembershipDTO(longitude, latitude, publicUser, membership.getGymMembership());
            gymMembershipDTOList.add(gymMembershipDTO);
        }
        gymMembershipDTOList.sort(Comparator.comparingDouble(GymMembershipDTO::getDistance));
        return new PageImpl<>(gymMembershipDTOList,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "distance")),
                gymPage.getTotalElements());
    }

    @Override
    public Page<PurchasedMembershipForGymDTO> getPurchasedGymMemberships(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PublicUserMembership> membershipPage = publicUserMembershipRepository.
                findPublicUserMembershipsByStatusAndMembership_TypeAndPublicUserAndExpireDateTimeAfter(MembershipStatus.BOOKED, MembershipType.GYM, publicUser, LocalDateTime.now(), pageable);
        return membershipPage.map(userMembership -> {
            Membership membership = userMembership.getMembership();
            GymMembership gymMembership = membership.getGymMembership();
            Gym gym = gymMembership.getGym();
            return new PurchasedMembershipForGymDTO(gym.getId(), gym.getName(), setMembershipDetails(new MembershipDTO(), membership, publicUser));
        });
    }

    @Override
    public Page<PurchasedMembershipForGymDTO> getPurchasedGymMembershipsHistory(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PublicUserMembership> membershipPage = publicUserMembershipRepository.
                findPublicUserMembershipsByStatusAndMembership_TypeAndPublicUserAndExpireDateTimeBefore(MembershipStatus.BOOKED, MembershipType.GYM, publicUser, LocalDateTime.now(), pageable);
        return membershipPage.map(userMembership -> {
            Membership membership = userMembership.getMembership();
            GymMembership gymMembership = membership.getGymMembership();
            Gym gym = gymMembership.getGym();
            return new PurchasedMembershipForGymDTO(gym.getId(), gym.getName(), setMembershipHistoryDetails(new MembershipDTO(), userMembership));
        });
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //public - gym day pass

    @Override
    public DayPassForGymDTO getDayPassForGym(Gym gym, PublicUser publicUser) {

        GymMembership gymMembership = gymMembershipRepository.findGymMembershipByMembership_TypeAndMembership_StatusAndGym(MembershipType.GYM_DAY_PASS, MembershipStatus.VISIBLE, gym);
        PublicUserMembership purchasedDayPass = publicUserMembershipRepository.
                findTopByPublicUserAndMembership_GymMembership_GymAndMembership_TypeOrderByExpireDateTimeDesc(publicUser, gym, MembershipType.GYM_DAY_PASS);
        if (purchasedDayPass != null) {
            if (purchasedDayPass.getExpireDateTime().isAfter(LocalDateTime.now()))
                gymMembership = purchasedDayPass.getMembership().getGymMembership();
        }
        if (gymMembership != null) {
            DayPassForGymDTO dayPassForGymDTO = new DayPassForGymDTO();
            setMembershipDetails(dayPassForGymDTO, gymMembership.getMembership(), publicUser);
            String eligibleDays = gymMembership.getMembership().getEligibleDays();
            String[] eligibleDaysDetails = eligibleDays.split("");
            DayPassDTO dayPassDTO = new DayPassDTO();
            dayPassDTO.setMonday(eligibleDaysDetails[0].equals("1"));
            dayPassDTO.setTuesday(eligibleDaysDetails[1].equals("1"));
            dayPassDTO.setWednesday(eligibleDaysDetails[2].equals("1"));
            dayPassDTO.setThursday(eligibleDaysDetails[3].equals("1"));
            dayPassDTO.setFriday(eligibleDaysDetails[4].equals("1"));
            dayPassDTO.setSaturday(eligibleDaysDetails[5].equals("1"));
            dayPassDTO.setSunday(eligibleDaysDetails[6].equals("1"));
            dayPassForGymDTO.setDayPassDTO(dayPassDTO);
            return dayPassForGymDTO;
        } else return null;
    }

    @Override
    public Page<GymMembershipDTO> getGymDayPasses(double longitude, double latitude, Pageable pageable, String token) {
        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<GymMembership> membershipPage = gymMembershipRepository.findGymMembershipsByMembership_TypeAndMembership_Status(MembershipType.GYM_DAY_PASS, MembershipStatus.VISIBLE, pageable);
        List<GymMembershipDTO> gymMembershipDTOList = membershipPage.getContent().stream()
                .map(gymMembership -> getGymMembershipDTO(longitude, latitude, publicUser, gymMembership)).collect(Collectors.toList());
//        for (GymMembershipDTO dto : gymMembershipDTOList) {
//            System.out.println(dto.getDistance());
//        }
        return new PageImpl<>(gymMembershipDTOList,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "distance")),
                membershipPage.getTotalElements());
    }

    @Override
    public Page<GymMembershipDTO> searchGymDayPasses(double longitude, double latitude, String country, String text, Pageable pageable, String token) {
        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<GymMembership> membershipPage = gymMembershipRepository.searchGymMembershipByMembershipType
                (MembershipType.GYM_DAY_PASS, text, country, longitude, latitude, pageable);
        List<GymMembershipDTO> gymMembershipDTOList = membershipPage.getContent().stream()
                .map(gymMembership -> getGymMembershipDTO(longitude, latitude, publicUser, gymMembership)).collect(Collectors.toList());
        gymMembershipDTOList.sort(Comparator.comparingDouble(GymMembershipDTO::getDistance));
        return new PageImpl<>(gymMembershipDTOList,
                PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.ASC, "distance")),
                membershipPage.getTotalElements());
    }

    @Override
    public Page<DayPassPurchasedDTO> getPurchasedDayPasses(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PublicUserMembership> membershipPage = publicUserMembershipRepository.
                findPublicUserMembershipsByStatusAndMembership_TypeAndPublicUserAndExpireDateTimeAfter(MembershipStatus.BOOKED, MembershipType.GYM_DAY_PASS, publicUser, LocalDateTime.now(), pageable);
        return membershipPage.map(this::getDayPassPurchasedDTO);
    }

    @Override
    public Page<DayPassPurchasedDTO> getPurchasedDayPassesHistory(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PublicUserMembership> membershipPage = publicUserMembershipRepository.
                findPublicUserMembershipsByStatusAndMembership_TypeAndPublicUserAndExpireDateTimeBefore(MembershipStatus.BOOKED, MembershipType.GYM_DAY_PASS, publicUser, LocalDateTime.now(), pageable);
        return membershipPage.map(this::getDayPassPurchasedDTO);
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //public - both - purchased

    @Override
    public Page<PublicUserMembershipDTO> getMembershipsByUser(long userId, MembershipType type, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PublicUserMembership> membershipPage;

//        if (type.equals(MembershipType.ALL)) {
//            membershipPage = publicUserMembershipRepository.
//                    findPublicUserMembershipsByMembership_TypeNotAndPublicUserAndMembership_StatusAndExpireDateTimeGreaterThanOrderByExpireDateTimeAsc
//                            (MembershipType.GYM_DAY_PASS, publicUser, MembershipStatus.VISIBLE, LocalDateTime.now(), pageable);

        if (type.equals(MembershipType.ALL)) {
            membershipPage = publicUserMembershipRepository.
                    findPublicUserMembershipsByStatusAndMembership_TypeNotAndPublicUserAndMembership_StatusAndExpireDateTimeGreaterThanOrderByExpireDateTimeAsc
                            (MembershipStatus.BOOKED, MembershipType.GYM_DAY_PASS, publicUser, MembershipStatus.VISIBLE, LocalDateTime.now(), pageable);

        } else if (type.equals(MembershipType.CLASS)) {
            List<MembershipType> types = new ArrayList<>();
            types.add(MembershipType.ONLINE_CLASS);
            types.add(MembershipType.PHYSICAL_CLASS);

            membershipPage = publicUserMembershipRepository.
                    findPublicUserMembershipsByMembership_TypesAndMembership_StatusAndPublicUserAndExpireDateTimeAfterOrderByExpireDateTimeAsc
                            (types, MembershipStatus.VISIBLE, publicUser, LocalDateTime.now(), pageable);
        } else {
//            membershipPage = publicUserMembershipRepository.
//                    findPublicUserMembershipsByMembership_TypeAndMembership_StatusAndPublicUserAndExpireDateTimeAfterOrderByExpireDateTimeAsc
//                            (type, MembershipStatus.VISIBLE, publicUser, LocalDateTime.now(), pageable);
            membershipPage = publicUserMembershipRepository.
                    findPublicUserMembershipsByStatusAndMembership_TypeAndMembership_StatusAndPublicUserAndExpireDateTimeAfterOrderByExpireDateTimeAsc
                            (MembershipStatus.BOOKED, type, MembershipStatus.VISIBLE, publicUser, LocalDateTime.now(), pageable);
        }
//        if (isCorporate)
//            membershipPage = publicUserMembershipRepository.
//                    findPublicUserMembershipsByMembership_TypeAndMembership_StatusAndPublicUserAndExpireDateTimeAfterOrderByExpireDateTimeAsc
//                            (MembershipType.CORPORATE, MembershipStatus.VISIBLE, publicUser, LocalDateTime.now(), pageable);
//        else
//            membershipPage = publicUserMembershipRepository.
//                    findPublicUserMembershipsByMembership_TypeNotAndPublicUserAndMembership_StatusAndExpireDateTimeGreaterThanOrderByExpireDateTimeAsc
//                            (MembershipType.GYM_DAY_PASS, publicUser, MembershipStatus.VISIBLE, LocalDateTime.now(), pageable);

        return membershipPage.map(m ->
                new PublicUserMembershipDTO(
                        m.getId(),
                        m.getMembership().getName(),
                        m.getMembership().getType(),
                        getMembershipStatus(m),
                        m.getExpireDateTime(),
                        LocalDateTime.now().until(m.getExpireDateTime(), ChronoUnit.DAYS) < 0 ?
                                0 :
                                LocalDateTime.now().until(m.getExpireDateTime(), ChronoUnit.DAYS),
                        m.getRemainingSlots()
                )
        );
    }

    @Override
    public PublicUserMembershipDTO getUserMembershipByPublicUser(long publicUserMembershipId) {
        PublicUserMembership userMembership = publicUserMembershipRepository.findById(publicUserMembershipId).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));

        PublicUserMembershipDTO publicUserMembershipDTO = new PublicUserMembershipDTO(
                userMembership.getId(),
                userMembership.getMembership().getName(),
                userMembership.getMembership().getType(),
                getMembershipStatus(userMembership),
                userMembership.getExpireDateTime(),
                LocalDateTime.now().until(userMembership.getExpireDateTime(), ChronoUnit.DAYS),
                userMembership.getRemainingSlots()
        );

        Membership membership = userMembership.getMembership();

        publicUserMembershipDTO.setRemainingDays(LocalDateTime.now().until(userMembership.getExpireDateTime(), ChronoUnit.DAYS));

        if (membership.getType().equals(MembershipType.PHYSICAL_CLASS)) {
            publicUserMembershipDTO.setPhysicalCLassMembership(getPhysicalClassMembershipDTO(membership, userMembership.getPublicUser()));
        }
        if (membership.getType().equals(MembershipType.ONLINE_CLASS) || membership.getType().equals(MembershipType.CORPORATE)) {
            publicUserMembershipDTO.setOnlineClassMembership(getOnlineClassMembershipDTO(membership, userMembership.getPublicUser()));
        }
        if (membership.getType().equals(MembershipType.GYM) || membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            publicUserMembershipDTO.setGymMembership(getGymMembershipDTOWithoutDistance(membership, userMembership.getPublicUser()));
        }

        return publicUserMembershipDTO;
    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------

    //membership - purchase both

    @Override
    @Transactional
    public String reserveMembershipByPayhere(MembershipBookDTO membershipBookDTO, String timeZone) {

        Membership membership = membershipRepository.findById(membershipBookDTO.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        PublicUser publicUser = publicUserRepository.findById(membershipBookDTO.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        PublicUserMembership userMembership = new PublicUserMembership();

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            PublicUserMembership lastDayPass = publicUserMembershipRepository.findTopByPublicUserAndMembershipOrderByDateTimeDesc(publicUser, membership);
            if (lastDayPass != null && lastDayPass.getStatus() == MembershipStatus.BOOKED) {
                if (lastDayPass.getExpireDateTime().isAfter(LocalDateTime.now()))
                    throw new CustomServiceException("Your day pass is still valid");
            }

        } else {

            // check if payment method cash
            if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {
                //check if physical class
                if (!MembershipType.PHYSICAL_CLASS.equals(membership.getType()))
                    throw new CustomServiceException(408, "Cash purchase is only allowed for fitness class memberships");
                //get physical class
                PhysicalClass physicalClass = physicalClassRepository.findById(membershipBookDTO.getPhysicalClassId()).
                        orElseThrow(() -> new CustomServiceException(404, "No class found for id: " + membershipBookDTO.getPhysicalClassId()));
                //get business agreement
                BusinessAgreement activeAgreement = businessAgreementRepository
                        .findTopByBusinessProfileAndStatusOrderByExpDateDesc(physicalClass.getBusinessProfile(), BusinessAgreementStatus.ACTIVE);
                //get payment modal
                BusinessProfilePaymentModel paymentModel = activeAgreement.getPackageDetail().getPaymentModel();

                //check if payment modal COMMISSION
                if (paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_MONTHLY && paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_ANNUALLY)
                    throw new CustomServiceException(403, "Cash payment is only allowed for subscription models");

                PhysicalClassMembership physicalClassMembership = physicalClassMembershipRepository
                        .findPhysicalClassMembershipByMembershipAndPhysicalClass(membership, physicalClass);
                if (physicalClassMembership == null)
                    throw new CustomServiceException(408, "No physical class assigned for the membership");
                if (!physicalClassMembership.isAllowCashPayment())
                    throw new CustomServiceException(403, "Cash payment is not allowed for this membership");
            }

            userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());

            if (userMembership != null) {
                if (userMembership.getStatus().equals(MembershipStatus.BOOKED)) {
                    if (userMembership.getExpireDateTime().isAfter(LocalDateTime.now()) &&
                            ((membership.getType().equals(MembershipType.PHYSICAL_CLASS) ||
                                    membership.getType().equals(MembershipType.ONLINE_CLASS)) && userMembership.getRemainingSlots() > 0))
                        throw new CustomServiceException("You have already purchased this membership");
                }

//                if (userMembership.getStatus().equals(MembershipStatus.PENDING))
//                    throw new CustomServiceException("The purchase is pending for this membership");

            } else userMembership = new PublicUserMembership();

            if (membership.getType().equals(MembershipType.GYM)) {
                PublicUserMembership purchasedGymMembership = publicUserMembershipRepository.
                        findTopByPublicUserAndMembership_GymMembership_GymAndMembership_TypeOrderByExpireDateTimeDesc
                                (publicUser, membership.getGymMembership().getGym(), MembershipType.GYM);
//                if (purchasedGymMembership != null) {
//                    if (purchasedGymMembership.getExpireDateTime().isAfter(LocalDateTime.now()))
//                        if (purchasedGymMembership.getStatus().equals(MembershipStatus.PENDING))
//                            throw new CustomServiceException("You have already had a pending payment for membership in this gym");
//                }
            }
        }

        userMembership.setMembership(membership);
        userMembership.setPublicUser(publicUser);
        userMembership.setDateTime(LocalDateTime.now());
        userMembership.setStatus(MembershipStatus.PENDING);

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            String str = LocalDateTime.now().toLocalDate().plusDays(1).toString() + " 00:00:00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime expireDateTime = LocalDateTime.parse(str, formatter);
            userMembership.setExpireDateTime(expireDateTime);
        } else userMembership.setExpireDateTime(LocalDateTime.now().plusDays(membership.getDuration()));

        userMembership.setRemainingSlots(membership.getSlotCount());

        //subtract membership discount
        BigDecimal paymentAmount = membership.getPrice().subtract((membership.getPrice().multiply(BigDecimal.valueOf(membership.getDiscount()))).divide(ONE_HUNDRED, 2, RoundingMode.UP));

        //check if discount is given
        if (membershipBookDTO.getDiscountId() != 0) {

            //check if discount is exists
            Optional<PublicUserDiscounts> optionalDiscount = publicUserDiscountRepository.findById(membershipBookDTO.getDiscountId());
            if (!optionalDiscount.isPresent())
                throw new CustomServiceException("discount not found for id: " + membershipBookDTO.getDiscountId());
            PublicUserDiscounts userDiscount = optionalDiscount.get();

            /*check if discount is expired*/
            if (userDiscount.getExpDate().isBefore(LocalDateTime.now()))
                throw new CustomServiceException("Discount is expired");

            switch (membership.getType()) {
                case GYM:
                case GYM_DAY_PASS:
                    Gym gym = membership.getGymMembership().getGym();

                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.GYM,
                            gym.getId()
                    );
                    if (!isDiscountApplicable) throw new CustomServiceException("discount is not applicable");
                    break;
                case ONLINE_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable1 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.ONLINE_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable1) throw new CustomServiceException("discount is not applicable");
                    break;
                case PHYSICAL_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable2 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.PHYSICAL_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable2) throw new CustomServiceException("discount is not applicable");
                    break;
                default:
                    throw new CustomServiceException("discount is not applicable for this membership type");
            }

            //calc discount amount
            BigDecimal discountAmount = paymentAmount
                    .multiply(new BigDecimal(Double.toString(userDiscount.getPercentage()))).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
            BigDecimal applicableDiscountAmount = discountAmount.compareTo(userDiscount.getMaxDiscount()) > 0 ? userDiscount.getMaxDiscount() : discountAmount;
            paymentAmount = paymentAmount.subtract(applicableDiscountAmount);

            //update public user discount table
            if (userDiscount.getUsageLimit() <= 0 && userDiscount.getUsageLimit() != -1)
                throw new CustomServiceException("used discount has 0 limit. this cannot happen");
            if (userDiscount.getUsageLimit() != -1) {
                userDiscount.setUsageLimit(userDiscount.getUsageLimit() - 1);
                publicUserDiscountRepository.save(userDiscount);
            }

            if (userDiscount.getUsageLimit() == 0) {
                publicUserDiscountRepository.delete(userDiscount);
            }

        }

        userMembership.setListedPrice(membership.getPrice());
        userMembership.setPaidAmount(paymentAmount);
        userMembership = publicUserMembershipRepository.save(userMembership);


        String paymentSecret = null;

        GeneratedHashValueDetailsDTO hashValueDetailsDTO = new GeneratedHashValueDetailsDTO();

        if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {

            userMembership.setPaymentId("CASH_S" + membership.getId() + "_U" + publicUser.getId() + "_UUID_" + UUID.randomUUID());
            paymentSecret = CASH_PURCHASE;

        } else {

            GeneratedHashValueDetailsDTO generatedHashValueDetailsDTO = publicUserService.getPayherePaymentDetailsByPublicUser(publicUser, userMembership.getPaidAmount(),
                    membershipBookDTO.getPaymentMethodId(),membershipBookDTO.getDeviceType());

            userMembership.setPaymentId(generatedHashValueDetailsDTO.getOrderId());

            paymentSecret = generatedHashValueDetailsDTO.getOrderId();

            hashValueDetailsDTO = generatedHashValueDetailsDTO;
        }


        JsonNode jsonNode = payhereUtil.getChargeResponse(hashValueDetailsDTO);

        log.info(jsonNode.asText());

        if(!jsonNode.get("status").asText().equals("1"))
            throw new CustomServiceException("Payment has been failed");

        if(jsonNode.get("status").asText().equals("1")){
            sendUserPaymentSuccess(publicUser, userMembership,IPGType.PAYHERE);
        }

        userMembership.setStatus(MembershipStatus.BOOKED);
        publicUserMembershipRepository.save(userMembership);
        publicUserService.saveUserTimeZone(publicUser, timeZone);

        return paymentSecret;
    }

    @Override
    public PreApproveResponseDTO checkoutMembershipByPayhere(MembershipBookDTO membershipBookDTO, String timeZone) {

        Membership membership = membershipRepository.findById(membershipBookDTO.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        PublicUser publicUser = publicUserRepository.findById(membershipBookDTO.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        PublicUserMembership userMembership = new PublicUserMembership();

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            PublicUserMembership lastDayPass = publicUserMembershipRepository.findTopByPublicUserAndMembershipOrderByDateTimeDesc(publicUser, membership);
            if (lastDayPass != null && lastDayPass.getStatus() == MembershipStatus.BOOKED) {
                if (lastDayPass.getExpireDateTime().isAfter(LocalDateTime.now()))
                    throw new CustomServiceException("Your day pass is still valid");
            }
        } else {

            // check if payment method cash
            if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {
                //check if physical class
                if (!MembershipType.PHYSICAL_CLASS.equals(membership.getType()))
                    throw new CustomServiceException(408, "Cash purchase is only allowed for fitness class memberships");
                //get physical class
                PhysicalClass physicalClass = physicalClassRepository.findById(membershipBookDTO.getPhysicalClassId()).
                        orElseThrow(() -> new CustomServiceException(404, "No class found for id: " + membershipBookDTO.getPhysicalClassId()));
                //get business agreement
                BusinessAgreement activeAgreement = businessAgreementRepository
                        .findTopByBusinessProfileAndStatusOrderByExpDateDesc(physicalClass.getBusinessProfile(), BusinessAgreementStatus.ACTIVE);
                //get payment modal
                BusinessProfilePaymentModel paymentModel = activeAgreement.getPackageDetail().getPaymentModel();

                //check if payment modal COMMISSION
                if (paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_MONTHLY && paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_ANNUALLY)
                    throw new CustomServiceException(403, "Cash payment is only allowed for subscription models");

                PhysicalClassMembership physicalClassMembership = physicalClassMembershipRepository
                        .findPhysicalClassMembershipByMembershipAndPhysicalClass(membership, physicalClass);
                if (physicalClassMembership == null)
                    throw new CustomServiceException(408, "No physical class assigned for the membership");
                if (!physicalClassMembership.isAllowCashPayment())
                    throw new CustomServiceException(403, "Cash payment is not allowed for this membership");
            }

            userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());

            if (userMembership != null) {
                if (userMembership.getStatus().equals(MembershipStatus.BOOKED)) {
                    if (userMembership.getExpireDateTime().isAfter(LocalDateTime.now()) &&
                            ((membership.getType().equals(MembershipType.PHYSICAL_CLASS) ||
                                    membership.getType().equals(MembershipType.ONLINE_CLASS)) && userMembership.getRemainingSlots() > 0))
                        throw new CustomServiceException("You have already purchased this membership");
                }

//                if (userMembership.getStatus().equals(MembershipStatus.PENDING))
//                    throw new CustomServiceException("The purchase is pending for this membership");

            } else userMembership = new PublicUserMembership();

            if (membership.getType().equals(MembershipType.GYM)) {
                PublicUserMembership purchasedGymMembership = publicUserMembershipRepository.
                        findTopByPublicUserAndMembership_GymMembership_GymAndMembership_TypeOrderByExpireDateTimeDesc
                                (publicUser, membership.getGymMembership().getGym(), MembershipType.GYM);
//                if (purchasedGymMembership != null) {
//                    if (purchasedGymMembership.getExpireDateTime().isAfter(LocalDateTime.now()))
//                        if (purchasedGymMembership.getStatus().equals(MembershipStatus.PENDING))
//                            throw new CustomServiceException("You have already had a pending payment for membership in this gym");
//                }
            }
        }

        userMembership.setMembership(membership);
        userMembership.setPublicUser(publicUser);
        userMembership.setDateTime(LocalDateTime.now());
        userMembership.setStatus(MembershipStatus.PENDING);

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            String str = LocalDateTime.now().toLocalDate().plusDays(1).toString() + " 00:00:00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime expireDateTime = LocalDateTime.parse(str, formatter);
            userMembership.setExpireDateTime(expireDateTime);
        } else userMembership.setExpireDateTime(LocalDateTime.now().plusDays(membership.getDuration()));

        userMembership.setRemainingSlots(membership.getSlotCount());

        //subtract membership discount
        BigDecimal paymentAmount = membership.getPrice().subtract((membership.getPrice().multiply(BigDecimal.valueOf(membership.getDiscount()))).divide(ONE_HUNDRED, 2, RoundingMode.UP));

        //check if discount is given
        if (membershipBookDTO.getDiscountId() != 0) {

            //check if discount is exists
            Optional<PublicUserDiscounts> optionalDiscount = publicUserDiscountRepository.findById(membershipBookDTO.getDiscountId());
            if (!optionalDiscount.isPresent())
                throw new CustomServiceException("discount not found for id: " + membershipBookDTO.getDiscountId());
            PublicUserDiscounts userDiscount = optionalDiscount.get();

            /*check if discount is expired*/
            if (userDiscount.getExpDate().isBefore(LocalDateTime.now()))
                throw new CustomServiceException("Discount is expired");

            switch (membership.getType()) {
                case GYM:
                case GYM_DAY_PASS:
                    Gym gym = membership.getGymMembership().getGym();

                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.GYM,
                            gym.getId()
                    );
                    if (!isDiscountApplicable) throw new CustomServiceException("discount is not applicable");
                    break;
                case ONLINE_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable1 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.ONLINE_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable1) throw new CustomServiceException("discount is not applicable");
                    break;
                case PHYSICAL_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable2 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.PHYSICAL_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable2) throw new CustomServiceException("discount is not applicable");
                    break;
                default:
                    throw new CustomServiceException("discount is not applicable for this membership type");
            }

            //calc discount amount
            BigDecimal discountAmount = paymentAmount
                    .multiply(new BigDecimal(Double.toString(userDiscount.getPercentage()))).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
            BigDecimal applicableDiscountAmount = discountAmount.compareTo(userDiscount.getMaxDiscount()) > 0 ? userDiscount.getMaxDiscount() : discountAmount;
            paymentAmount = paymentAmount.subtract(applicableDiscountAmount);

            //update public user discount table
            if (userDiscount.getUsageLimit() <= 0 && userDiscount.getUsageLimit() != -1)
                throw new CustomServiceException("used discount has 0 limit. this cannot happen");
            if (userDiscount.getUsageLimit() != -1) {
                userDiscount.setUsageLimit(userDiscount.getUsageLimit() - 1);
                publicUserDiscountRepository.save(userDiscount);
            }

            if (userDiscount.getUsageLimit() == 0) {
                publicUserDiscountRepository.delete(userDiscount);
            }

        }

        userMembership.setListedPrice(membership.getPrice());
        userMembership.setPaidAmount(paymentAmount);
        userMembership = publicUserMembershipRepository.save(userMembership);

        PreApproveResponseDTO hashValueDetailsDTO = new PreApproveResponseDTO();

        String paymentSecret = null;
        if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {
            userMembership.setPaymentId("CASH_S" + membership.getId() + "_U" + publicUser.getId() + "_UUID_" + UUID.randomUUID());
            paymentSecret = CASH_PURCHASE;
            hashValueDetailsDTO.setHash(paymentSecret);

        } else {

            PreApproveResponseDTO generatedHashValueDetailsDTO = publicUserService.generatePreApproveDetails(publicUser.getId(), userMembership.getPaidAmount().toString(),
                    membershipBookDTO.getDeviceType());

            userMembership.setPaymentId(generatedHashValueDetailsDTO.getOrderId());
            hashValueDetailsDTO = generatedHashValueDetailsDTO;

        }

        publicUserMembershipRepository.save(userMembership);
        publicUserService.saveUserTimeZone(publicUser, timeZone);
        return hashValueDetailsDTO;
    }

    @Override
    public PreApproveResponseDTO makeOneTimePaymentMembershipByPayhere(MembershipBookDTO membershipBookDTO, String timeZone) {

        Membership membership = membershipRepository.findById(membershipBookDTO.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        PublicUser publicUser = publicUserRepository.findById(membershipBookDTO.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        PublicUserMembership userMembership = new PublicUserMembership();

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            PublicUserMembership lastDayPass = publicUserMembershipRepository.findTopByPublicUserAndMembershipOrderByDateTimeDesc(publicUser, membership);
            if (lastDayPass != null && lastDayPass.getStatus() == MembershipStatus.BOOKED) {
                if (lastDayPass.getExpireDateTime().isAfter(LocalDateTime.now()))
                    throw new CustomServiceException("Your day pass is still valid");
            }
        } else {

            // check if payment method cash
            if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {
                //check if physical class
                if (!MembershipType.PHYSICAL_CLASS.equals(membership.getType()))
                    throw new CustomServiceException(408, "Cash purchase is only allowed for fitness class memberships");
                //get physical class
                PhysicalClass physicalClass = physicalClassRepository.findById(membershipBookDTO.getPhysicalClassId()).
                        orElseThrow(() -> new CustomServiceException(404, "No class found for id: " + membershipBookDTO.getPhysicalClassId()));
                //get business agreement
                BusinessAgreement activeAgreement = businessAgreementRepository
                        .findTopByBusinessProfileAndStatusOrderByExpDateDesc(physicalClass.getBusinessProfile(), BusinessAgreementStatus.ACTIVE);
                //get payment modal
                BusinessProfilePaymentModel paymentModel = activeAgreement.getPackageDetail().getPaymentModel();

                //check if payment modal COMMISSION
                if (paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_MONTHLY && paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_ANNUALLY)
                    throw new CustomServiceException(403, "Cash payment is only allowed for subscription models");

                PhysicalClassMembership physicalClassMembership = physicalClassMembershipRepository
                        .findPhysicalClassMembershipByMembershipAndPhysicalClass(membership, physicalClass);
                if (physicalClassMembership == null)
                    throw new CustomServiceException(408, "No physical class assigned for the membership");
                if (!physicalClassMembership.isAllowCashPayment())
                    throw new CustomServiceException(403, "Cash payment is not allowed for this membership");
            }

            userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());

            if (userMembership != null) {
                if (userMembership.getStatus().equals(MembershipStatus.BOOKED)) {
                    if (userMembership.getExpireDateTime().isAfter(LocalDateTime.now()) &&
                            ((membership.getType().equals(MembershipType.PHYSICAL_CLASS) ||
                                    membership.getType().equals(MembershipType.ONLINE_CLASS)) && userMembership.getRemainingSlots() > 0))
                        throw new CustomServiceException("You have already purchased this membership");
                }

//                if (userMembership.getStatus().equals(MembershipStatus.PENDING))
//                    throw new CustomServiceException("The purchase is pending for this membership");

            } else userMembership = new PublicUserMembership();

            if (membership.getType().equals(MembershipType.GYM)) {
                PublicUserMembership purchasedGymMembership = publicUserMembershipRepository.
                        findTopByPublicUserAndMembership_GymMembership_GymAndMembership_TypeOrderByExpireDateTimeDesc
                                (publicUser, membership.getGymMembership().getGym(), MembershipType.GYM);
//                if (purchasedGymMembership != null) {
//                    if (purchasedGymMembership.getExpireDateTime().isAfter(LocalDateTime.now()))
//                        if (purchasedGymMembership.getStatus().equals(MembershipStatus.PENDING))
//                            throw new CustomServiceException("You have already had a pending payment for membership in this gym");
//                }
            }
        }

        userMembership.setMembership(membership);
        userMembership.setPublicUser(publicUser);
        userMembership.setDateTime(LocalDateTime.now());
        userMembership.setStatus(MembershipStatus.PENDING);

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            String str = LocalDateTime.now().toLocalDate().plusDays(1).toString() + " 00:00:00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime expireDateTime = LocalDateTime.parse(str, formatter);
            userMembership.setExpireDateTime(expireDateTime);
        } else userMembership.setExpireDateTime(LocalDateTime.now().plusDays(membership.getDuration()));

        userMembership.setRemainingSlots(membership.getSlotCount());

        //subtract membership discount
        BigDecimal paymentAmount = membership.getPrice().subtract((membership.getPrice().multiply(BigDecimal.valueOf(membership.getDiscount()))).divide(ONE_HUNDRED, 2, RoundingMode.UP));

        //check if discount is given
        if (membershipBookDTO.getDiscountId() != 0) {

            //check if discount is exists
            Optional<PublicUserDiscounts> optionalDiscount = publicUserDiscountRepository.findById(membershipBookDTO.getDiscountId());
            if (!optionalDiscount.isPresent())
                throw new CustomServiceException("discount not found for id: " + membershipBookDTO.getDiscountId());
            PublicUserDiscounts userDiscount = optionalDiscount.get();

            /*check if discount is expired*/
            if (userDiscount.getExpDate().isBefore(LocalDateTime.now()))
                throw new CustomServiceException("Discount is expired");

            switch (membership.getType()) {
                case GYM:
                case GYM_DAY_PASS:
                    Gym gym = membership.getGymMembership().getGym();

                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.GYM,
                            gym.getId()
                    );
                    if (!isDiscountApplicable) throw new CustomServiceException("discount is not applicable");
                    break;
                case ONLINE_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable1 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.ONLINE_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable1) throw new CustomServiceException("discount is not applicable");
                    break;
                case PHYSICAL_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable2 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.PHYSICAL_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable2) throw new CustomServiceException("discount is not applicable");
                    break;
                default:
                    throw new CustomServiceException("discount is not applicable for this membership type");
            }

            //calc discount amount
            BigDecimal discountAmount = paymentAmount
                    .multiply(new BigDecimal(Double.toString(userDiscount.getPercentage()))).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
            BigDecimal applicableDiscountAmount = discountAmount.compareTo(userDiscount.getMaxDiscount()) > 0 ? userDiscount.getMaxDiscount() : discountAmount;
            paymentAmount = paymentAmount.subtract(applicableDiscountAmount);

            //update public user discount table
            if (userDiscount.getUsageLimit() <= 0 && userDiscount.getUsageLimit() != -1)
                throw new CustomServiceException("used discount has 0 limit. this cannot happen");
            if (userDiscount.getUsageLimit() != -1) {
                userDiscount.setUsageLimit(userDiscount.getUsageLimit() - 1);
                publicUserDiscountRepository.save(userDiscount);
            }

            if (userDiscount.getUsageLimit() == 0) {
                publicUserDiscountRepository.delete(userDiscount);
            }

        }

        userMembership.setListedPrice(membership.getPrice());
        userMembership.setPaidAmount(paymentAmount);
        userMembership = publicUserMembershipRepository.save(userMembership);

        PreApproveResponseDTO hashValueDetailsDTO = new PreApproveResponseDTO();

        String paymentSecret = null;
        if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {
            userMembership.setPaymentId("CASH_S" + membership.getId() + "_U" + publicUser.getId() + "_UUID_" + UUID.randomUUID());
            paymentSecret = CASH_PURCHASE;
            hashValueDetailsDTO.setHash(paymentSecret);

        } else {

            PreApproveResponseDTO generatedHashValueDetailsDTO = publicUserService.generateMembershipCheckoutParameters(publicUser.getId(), userMembership.getPaidAmount().toString(),
                    membershipBookDTO.getDeviceType());

            userMembership.setPaymentId(generatedHashValueDetailsDTO.getOrderId());
            hashValueDetailsDTO = generatedHashValueDetailsDTO;

        }

        publicUserMembershipRepository.save(userMembership);
        publicUserService.saveUserTimeZone(publicUser, timeZone);
        return hashValueDetailsDTO;
    }

    @Override
    public String reserveMembershipByStripe(MembershipBookDTO membershipBookDTO, String timeZone) {
        Membership membership = membershipRepository.findById(membershipBookDTO.getMembershipId()).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        PublicUser publicUser = publicUserRepository.findById(membershipBookDTO.getUserId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        PublicUserMembership userMembership = new PublicUserMembership();

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            PublicUserMembership lastDayPass = publicUserMembershipRepository.findTopByPublicUserAndMembershipOrderByDateTimeDesc(publicUser, membership);
            if (lastDayPass != null && lastDayPass.getStatus() == MembershipStatus.BOOKED) {
                if (lastDayPass.getExpireDateTime().isAfter(LocalDateTime.now()))
                    throw new CustomServiceException("Your day pass is still valid");
            }
        } else {

            // check if payment method cash
            if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {
                //check if physical class
                if (!MembershipType.PHYSICAL_CLASS.equals(membership.getType()))
                    throw new CustomServiceException(408, "Cash purchase is only allowed for fitness class memberships");
                //get physical class
                PhysicalClass physicalClass = physicalClassRepository.findById(membershipBookDTO.getPhysicalClassId()).
                        orElseThrow(() -> new CustomServiceException(404, "No class found for id: " + membershipBookDTO.getPhysicalClassId()));
                //get business agreement
                BusinessAgreement activeAgreement = businessAgreementRepository
                        .findTopByBusinessProfileAndStatusOrderByExpDateDesc(physicalClass.getBusinessProfile(), BusinessAgreementStatus.ACTIVE);
                //get payment modal
                BusinessProfilePaymentModel paymentModel = activeAgreement.getPackageDetail().getPaymentModel();

                //check if payment modal COMMISSION
                if (paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_MONTHLY && paymentModel != BusinessProfilePaymentModel.SUBSCRIPTION_ANNUALLY)
                    throw new CustomServiceException(403, "Cash payment is only allowed for subscription models");

                PhysicalClassMembership physicalClassMembership = physicalClassMembershipRepository
                        .findPhysicalClassMembershipByMembershipAndPhysicalClass(membership, physicalClass);
                if (physicalClassMembership == null)
                    throw new CustomServiceException(408, "No physical class assigned for the membership");
                if (!physicalClassMembership.isAllowCashPayment())
                    throw new CustomServiceException(403, "Cash payment is not allowed for this membership");
            }

            userMembership = publicUserMembershipRepository.findPublicUserMembershipByPublicUserAndMembership(publicUser.getId(), membership.getId());

            if (userMembership != null) {
                if (userMembership.getStatus().equals(MembershipStatus.BOOKED)) {
                    if (userMembership.getExpireDateTime().isAfter(LocalDateTime.now()) &&
                            ((membership.getType().equals(MembershipType.PHYSICAL_CLASS) ||
                                    membership.getType().equals(MembershipType.ONLINE_CLASS)) && userMembership.getRemainingSlots() > 0))
                        throw new CustomServiceException("You have already purchased this membership");
                }

//                if (userMembership.getStatus().equals(MembershipStatus.PENDING))
//                    throw new CustomServiceException("The purchase is pending for this membership");

            } else userMembership = new PublicUserMembership();

            if (membership.getType().equals(MembershipType.GYM)) {
                PublicUserMembership purchasedGymMembership = publicUserMembershipRepository.
                        findTopByPublicUserAndMembership_GymMembership_GymAndMembership_TypeOrderByExpireDateTimeDesc
                                (publicUser, membership.getGymMembership().getGym(), MembershipType.GYM);
//                if (purchasedGymMembership != null) {
//                    if (purchasedGymMembership.getExpireDateTime().isAfter(LocalDateTime.now()))
//                        if (purchasedGymMembership.getStatus().equals(MembershipStatus.PENDING))
//                            throw new CustomServiceException("You have already had a pending payment for membership in this gym");
//                }
            }
        }

        userMembership.setMembership(membership);
        userMembership.setPublicUser(publicUser);
        userMembership.setDateTime(LocalDateTime.now());
        userMembership.setStatus(MembershipStatus.PENDING);

        if (membership.getType().equals(MembershipType.GYM_DAY_PASS)) {
            String str = LocalDateTime.now().toLocalDate().plusDays(1).toString() + " 00:00:00";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime expireDateTime = LocalDateTime.parse(str, formatter);
            userMembership.setExpireDateTime(expireDateTime);
        } else userMembership.setExpireDateTime(LocalDateTime.now().plusDays(membership.getDuration()));

        userMembership.setRemainingSlots(membership.getSlotCount());

        //subtract membership discount
        BigDecimal paymentAmount = membership.getPrice().subtract((membership.getPrice().multiply(BigDecimal.valueOf(membership.getDiscount()))).divide(ONE_HUNDRED, 2, RoundingMode.UP));

        //check if discount is given
        if (membershipBookDTO.getDiscountId() != 0) {

            //check if discount is exists
            Optional<PublicUserDiscounts> optionalDiscount = publicUserDiscountRepository.findById(membershipBookDTO.getDiscountId());
            if (!optionalDiscount.isPresent())
                throw new CustomServiceException("discount not found for id: " + membershipBookDTO.getDiscountId());
            PublicUserDiscounts userDiscount = optionalDiscount.get();

            /*check if discount is expired*/
            if (userDiscount.getExpDate().isBefore(LocalDateTime.now()))
                throw new CustomServiceException("Discount is expired");

            switch (membership.getType()) {
                case GYM:
                case GYM_DAY_PASS:
                    Gym gym = membership.getGymMembership().getGym();

                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.GYM,
                            gym.getId()
                    );
                    if (!isDiscountApplicable) throw new CustomServiceException("discount is not applicable");
                    break;
                case ONLINE_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable1 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.ONLINE_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable1) throw new CustomServiceException("discount is not applicable");
                    break;
                case PHYSICAL_CLASS:
                    //check if discount is applicable -> if not applicable: send error
                    boolean isDiscountApplicable2 = promoCodeManagementService.isPromoDiscountApplicable(
                            publicUser,
                            userDiscount,
                            PromoCodeServiceCategory.PHYSICAL_CLASS_MEMBERSHIP,
                            membership.getId()
                    );
                    if (!isDiscountApplicable2) throw new CustomServiceException("discount is not applicable");
                    break;
                default:
                    throw new CustomServiceException("discount is not applicable for this membership type");
            }

            //calc discount amount
            BigDecimal discountAmount = paymentAmount
                    .multiply(new BigDecimal(Double.toString(userDiscount.getPercentage()))).divide(ONE_HUNDRED, 2, RoundingMode.DOWN);
            BigDecimal applicableDiscountAmount = discountAmount.compareTo(userDiscount.getMaxDiscount()) > 0 ? userDiscount.getMaxDiscount() : discountAmount;
            paymentAmount = paymentAmount.subtract(applicableDiscountAmount);

            //update public user discount table
            if (userDiscount.getUsageLimit() <= 0 && userDiscount.getUsageLimit() != -1)
                throw new CustomServiceException("used discount has 0 limit. this cannot happen");
            if (userDiscount.getUsageLimit() != -1) {
                userDiscount.setUsageLimit(userDiscount.getUsageLimit() - 1);
                publicUserDiscountRepository.save(userDiscount);
            }

            if (userDiscount.getUsageLimit() == 0) {
                publicUserDiscountRepository.delete(userDiscount);
            }

        }

        userMembership.setListedPrice(membership.getPrice());
        userMembership.setPaidAmount(paymentAmount);
        userMembership = publicUserMembershipRepository.save(userMembership);

        String paymentSecret = null;
        if (CASH_PURCHASE.equals(membershipBookDTO.getPaymentMethodId())) {
            userMembership.setPaymentId("CASH_S" + membership.getId() + "_U" + publicUser.getId() + "_UUID_" + UUID.randomUUID());
            paymentSecret = CASH_PURCHASE;
        } else {
            PaymentIntent paymentIntent = publicUserService.getPaymentIntentByPublicUser(publicUser, userMembership.getPaidAmount(),
                    membershipBookDTO.getPaymentMethodId());
            userMembership.setPaymentId(paymentIntent.getId());
            paymentSecret = paymentIntent.getClientSecret();
        }

        publicUserMembershipRepository.save(userMembership);
        publicUserService.saveUserTimeZone(publicUser, timeZone);
        return paymentSecret;

    }

    @Override
    public Boolean verifyMembershipPaymentByMobile(String card_no, String order_id, String card_expiry, String payhere_amount, String payhere_currency, String card_holder_name, String method, String payment_id, String status_code, String md5sig, String status_message, String customer_token) {

        try {

            log.info("\nverifyMembershipPaymentByMobile (mobile) payment success of membership: order id: {}", order_id);

            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(order_id);

            log.info("order id= "+order_id);
            log.info("payment id= "+payment_id);
            log.info("payhere amount= "+payhere_amount);
            log.info("payhere currency= "+payhere_currency);
            log.info("status code= "+status_code);
            log.info("md5sig= "+md5sig);
            log.info("status message= "+status_message);

            if (userMembership == null) return false;

            String hash = CustomGenerator.getMd5(merahantID + order_id + payhere_amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretMobile));

            if (status_code.equals("2") && hash.equals(md5sig)) {

                if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;

                LocalDateTime now = LocalDateTime.now();
                PublicUser publicUser = userMembership.getPublicUser();
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setDateTime(now);
                publicUserMembershipRepository.save(userMembership);
                sendUserPaymentSuccess(publicUser, userMembership,IPGType.PAYHERE);

                PublicUserCardDetail publicUserCardDetail = new PublicUserCardDetail();
                publicUserCardDetail.setLast4(card_no);
                publicUserCardDetail.setExpMonth(Integer.parseInt(card_expiry.substring(0, 2)));
                publicUserCardDetail.setExpYear(Integer.parseInt(card_expiry.substring(card_expiry.length() - 2)));
                publicUserCardDetail.setPayHereCustomerToken(customer_token);
                publicUserCardDetail.setMd5sig(md5sig);
                publicUserCardDetail.setCardHolderName(card_holder_name);
                publicUserCardDetail.setIpgType(IPGType.PAYHERE);
                publicUserCardDetail.setBrand(method);
                publicUserCardDetail.setPayHerePaymentMethodId(payment_id);
                publicUserCardDetail.setStatus(CardStatus.ACTIVE);
                publicUserCardDetail.setPublicUser(userMembership.getPublicUser());

                publicUserCardDetailRepository.save(publicUserCardDetail);


                log.info("\nSuccess handling session book by payhere.");

                log.info("success payment");

                return true;

            } else {

                log.info("payment failed ::::::::::::::::::::::::");

                publicUserMembershipRepository.delete(userMembership);

                PublicUser publicUser = userMembership.getPublicUser();

                saveFailTransaction(userMembership);

                smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()),
                        MEMBERSHIP_FAILED_DESC.replace("{the membership}",
                                userMembership.getMembership().getName()) + userMembership.getPaymentId()
                                + " (Reference : " + userMembership.getId() + ")");
                log.info("Membership purchase failed - " + userMembership.getId());

                return true;

            }

        } catch (Exception e) {
            log.error("Error verifyMembershipPaymentByMobile: ", e);
            return false;
        }
    }

    @Override
    public Boolean verifyMembershipPaymentByWeb(String card_no, String order_id, String card_expiry, String payhere_amount, String payhere_currency, String card_holder_name, String method, String payment_id, String status_code, String md5sig, String status_message, String customer_token) {
        try {

            log.info("\nverifyMembershipPaymentByWeb (web) payment success of membership: order id: {}", order_id);

            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(order_id);

            log.info(order_id);
            log.info(payment_id);
            log.info(payhere_amount);
            log.info(payhere_currency);
            log.info(status_code);
            log.info(md5sig);
            log.info(status_message);

            if (userMembership == null) return false;

            String hash = CustomGenerator.getMd5(merahantID + order_id + payhere_amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretWeb));

            if (status_code.equals("2") && hash.equals(md5sig)) {

                if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;

                LocalDateTime now = LocalDateTime.now();
                PublicUser publicUser = userMembership.getPublicUser();
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setDateTime(now);
                publicUserMembershipRepository.save(userMembership);
                sendUserPaymentSuccess(publicUser, userMembership,IPGType.PAYHERE);

                PublicUserCardDetail publicUserCardDetail = new PublicUserCardDetail();
                publicUserCardDetail.setLast4(card_no);
                publicUserCardDetail.setExpMonth(Integer.parseInt(card_expiry.substring(0, 2)));
                publicUserCardDetail.setExpYear(Integer.parseInt(card_expiry.substring(card_expiry.length() - 2)));
                publicUserCardDetail.setPayHereCustomerToken(customer_token);
                publicUserCardDetail.setMd5sig(md5sig);
                publicUserCardDetail.setCardHolderName(card_holder_name);
                publicUserCardDetail.setIpgType(IPGType.PAYHERE);
                publicUserCardDetail.setBrand(method);
                publicUserCardDetail.setPayHerePaymentMethodId(payment_id);
                publicUserCardDetail.setStatus(CardStatus.ACTIVE);
                publicUserCardDetail.setPublicUser(userMembership.getPublicUser());

                publicUserCardDetailRepository.save(publicUserCardDetail);


                log.info("\nSuccess handling membership book by payhere.");

                log.info("success payment");

                return true;

            } else {

                log.info("payment failed ::::::::::::::::::::::::");

                publicUserMembershipRepository.delete(userMembership);

                PublicUser publicUser = userMembership.getPublicUser();

                saveFailTransaction(userMembership);

                smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()),
                        MEMBERSHIP_FAILED_DESC.replace("{the membership}",
                                userMembership.getMembership().getName()) + userMembership.getPaymentId()
                                + " (Reference : " + userMembership.getId() + ")");
                log.info("Membership purchase failed - " + userMembership.getId());

                return true;

            }

        } catch (Exception e) {
            log.error("Error verifyMembershipPaymentByWeb: ", e);
            return false;
        }
    }

    @Override
    public Boolean verifyMembershipOneTimePaymentByWeb(String order_id, String payment_id, String payhere_amount, String payhere_currency, String status_code, String md5sig, String status_message) {
        try {

            log.info("\nverifyMembershipOneTimePaymentByWeb (web) payment success of membership: order id: {}", order_id);

            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(order_id);

            log.info(order_id);
            log.info(payment_id);
            log.info(payhere_amount);
            log.info(payhere_currency);
            log.info(status_code);
            log.info(md5sig);
            log.info(status_message);

            if (userMembership == null) return false;

            String hash = CustomGenerator.getMd5(merahantID + order_id + payhere_amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretWeb));

            if (status_code.equals("2") && hash.equals(md5sig)) {

                if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;

                LocalDateTime now = LocalDateTime.now();
                PublicUser publicUser = userMembership.getPublicUser();
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setDateTime(now);
                publicUserMembershipRepository.save(userMembership);
                sendUserPaymentSuccess(publicUser, userMembership,IPGType.PAYHERE);

                log.info("\nSuccess handling membership book by payhere.");

                log.info("success payment");

                return true;

            } else {

                log.info("payment failed ::::::::::::::::::::::::");

                publicUserMembershipRepository.delete(userMembership);

                PublicUser publicUser = userMembership.getPublicUser();

                saveFailTransaction(userMembership);

                smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()),
                        MEMBERSHIP_FAILED_DESC.replace("{the membership}",
                                userMembership.getMembership().getName()) + userMembership.getPaymentId()
                                + " (Reference : " + userMembership.getId() + ")");
                log.info("Membership purchase failed - " + userMembership.getId());

                return true;

            }

        } catch (Exception e) {
            log.error("Error verifyMembershipOneTimePaymentByWeb: ", e);
            return false;
        }
    }

    @Override
    public Boolean verifyMembershipOneTimePaymentByMobile(String order_id, String payment_id, String payhere_amount, String payhere_currency, String status_code, String md5sig, String status_message) {
        try {

            log.info("\nverifyMembershipOneTimePaymentByMobile (mobile) / of membership: order id: {}", order_id);

            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(order_id);

            log.info(order_id);
            log.info(payment_id);
            log.info(payhere_amount);
            log.info(payhere_currency);
            log.info(status_code);
            log.info(md5sig);
            log.info(status_message);

            if (userMembership == null) return false;

            String hash = CustomGenerator.getMd5(merahantID + order_id + payhere_amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretMobile));

            if (status_code.equals("2") && hash.equals(md5sig)) {

                if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;

                LocalDateTime now = LocalDateTime.now();
                PublicUser publicUser = userMembership.getPublicUser();
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setDateTime(now);
                publicUserMembershipRepository.save(userMembership);
                sendUserPaymentSuccess(publicUser, userMembership,IPGType.PAYHERE);

                log.info("\nSuccess handling membership book by payhere.");

                log.info("success payment");

                return true;

            } else {

                log.info("payment failed ::::::::::::::::::::::::");

                publicUserMembershipRepository.delete(userMembership);

                PublicUser publicUser = userMembership.getPublicUser();

                saveFailTransaction(userMembership);

                smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()),
                        MEMBERSHIP_FAILED_DESC.replace("{the membership}",
                                userMembership.getMembership().getName()) + userMembership.getPaymentId()
                                + " (Reference : " + userMembership.getId() + ")");
                log.info("Membership purchase failed - " + userMembership.getId());

                return true;

            }

        } catch (Exception e) {
            log.error("Error verifyMembershipOneTimePaymentByMobile: ", e);
            return false;
        }
    }

    @Override
    public StripeCheckResponse checkBookingAndIsStripe(MembershipBookDTO dto) {
        PublicUser publicUser = publicUserRepository.findById(dto.getUserId())
                .orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        //

//        return null;

        return StripeCheckResponse.builder().registered(
                        publicUser.getStripeClientId() == null ? StripeRegister.NOT_REGISTERED : StripeRegister.REGISTERED)
                .cards(publicUser.getPublicUserCardDetails().stream().map(publicUserCardDetail ->
                        modelMapper.map(publicUserCardDetail, CardDetailsResponse.class)).collect(Collectors.toList())).build();
    }

//    @Override
//    @Transactional
//    public boolean handlePaymentSuccessWebhook(PaymentIntent paymentIntent) {
//        try {
//            log.info("\nConfirm payment success of membership: Intent id: {}", paymentIntent.getId());
//
//            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(paymentIntent.getId());
//
//            if (userMembership == null) return false;
//
//            if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;
//
//            LocalDateTime now = LocalDateTime.now();
//            PublicUser publicUser = userMembership.getPublicUser();
//            userMembership.setStatus(MembershipStatus.BOOKED);
//            userMembership.setDateTime(now);
//            publicUserMembershipRepository.save(userMembership);
//            sendUserPaymentSuccess(publicUser, userMembership,IPGType.STRIPE);
//
//            log.info("\nSuccess handling session book by web-hook.");
//
//            return true;
//
//        } catch (Exception e) {
//            log.error("Error in class session enroll payment success handler: ", e);
//            return false;
//        }
//    }

    @Override
    public boolean handlePaymentSuccessPayhereMobile(String order_id, String payment_id, String payhere_amount, String payhere_currency, String status_code, String md5sig, String status_message) {

        try {

            log.info("\nConfirm payhere (mobile) payment success of membership: order id: {}", order_id);

            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(order_id);

            log.info(order_id);
            log.info(payment_id);
            log.info(payhere_amount);
            log.info(payhere_currency);
            log.info(status_code);
            log.info(md5sig);
            log.info(status_message);

            if (userMembership == null) return false;

            String hash = CustomGenerator.getMd5(merahantID + order_id + payhere_amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretMobile));

            if (status_code.equals("2") && hash.equals(md5sig)) {

                if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;

                LocalDateTime now = LocalDateTime.now();
                PublicUser publicUser = userMembership.getPublicUser();
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setDateTime(now);
                publicUserMembershipRepository.save(userMembership);
                sendUserPaymentSuccess(publicUser, userMembership,IPGType.PAYHERE);

                log.info("\nSuccess handling session book by payhere.");

                log.info("success payment");

                return true;

            } else {

                log.info("payment failed ::::::::::::::::::::::::");

                publicUserMembershipRepository.delete(userMembership);

                PublicUser publicUser = userMembership.getPublicUser();

                saveFailTransaction(userMembership);

                smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()),
                        MEMBERSHIP_FAILED_DESC.replace("{the membership}",
                                userMembership.getMembership().getName()) + userMembership.getPaymentId()
                                + " (Reference : " + userMembership.getId() + ")");
                log.info("Membership purchase failed - " + userMembership.getId());

                return true;

            }

        } catch (Exception e) {
            log.error("Error handlePaymentSuccessPayhereMobile: ", e);
            return false;
        }

    }

    @Override
    public boolean handlePaymentSuccessPayhereWeb(String order_id, String payment_id, String payhere_amount, String payhere_currency, String status_code, String md5sig, String status_message) {

        try {

            log.info("\nConfirm payhere (web) payment success of membership: order id: {}", order_id);

            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(order_id);

            log.info(order_id);
            log.info(payment_id);
            log.info(payhere_amount);
            log.info(payhere_currency);
            log.info(status_code);
            log.info(md5sig);
            log.info(status_message);

            if (userMembership == null) return false;

            String hash = CustomGenerator.getMd5(merahantID + order_id + payhere_amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretWeb));

            if (status_code.equals("2") && hash.equals(md5sig)) {

                if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;

                LocalDateTime now = LocalDateTime.now();
                PublicUser publicUser = userMembership.getPublicUser();
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setDateTime(now);
                publicUserMembershipRepository.save(userMembership);
                sendUserPaymentSuccess(publicUser, userMembership,IPGType.PAYHERE);

                log.info("\nSuccess handling session book by payhere.");

                log.info("success payment");

                return true;

            } else {

                log.info("payment failed ::::::::::::::::::::::::");

                publicUserMembershipRepository.delete(userMembership);

                PublicUser publicUser = userMembership.getPublicUser();

                saveFailTransaction(userMembership);

                smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()),
                        MEMBERSHIP_FAILED_DESC.replace("{the membership}",
                                userMembership.getMembership().getName()) + userMembership.getPaymentId()
                                + " (Reference : " + userMembership.getId() + ")");
                log.info("Membership purchase failed - " + userMembership.getId());

                return true;

            }

        } catch (Exception e) {
            log.error("Error handlePaymentSuccessPayhereWeb: ", e);
            return false;
        }

    }

    private void sendUserPaymentSuccess(PublicUser publicUser, PublicUserMembership userMembership, IPGType ipgType) {

        if (userMembership != null) {

            Membership membership = userMembership.getMembership();
            MembershipType membershipType = membership.getType();

            String gymOrClassName = "";
            String title = MEMBERSHIP_PURCHASED;

            if (membership.getGymMembership() != null)
                gymOrClassName = membership.getGymMembership().getGym().getName();

            else if (membership.getPhysicalClassMemberships() != null && !membership.getPhysicalClassMemberships().isEmpty())
                gymOrClassName = membership.getPhysicalClassMemberships().get(0).getPhysicalClass().getName();

            else if (membership.getOnlineClassMemberships() != null && !membership.getOnlineClassMemberships().isEmpty())
                gymOrClassName = membership.getOnlineClassMemberships().get(0).getClassParent().getName();

            String bookedMessage = MEMBERSHIP_PURCHASED_DESC
                    .replace("{firstName}", publicUser.getFirstName())
                    .replace("{the membership}", membership.getName())
                    .replace("{gym/Class}", gymOrClassName)
                    + " (Reference no: " + userMembership.getId() + ")";

            if (membership.getGymMembership() != null && membershipType.equals(MembershipType.GYM_DAY_PASS)) {
                bookedMessage = DAY_PASS_DESC
                        .replace("{firstName}", publicUser.getFirstName())
                        .replace("{the dayPass}", membership.getName())
                        .replace("{gym/Class}", gymOrClassName)
                        + " Reference no: " + userMembership.getId() + ")";
                title = DAY_PASS_PURCHASED;
            }

            emailSender.sendHtmlEmail(
                    emailSender.getPublicUserEmailList(publicUser),
                    title,
                    emailSender.getReservationSuccessHtml(bookedMessage),
                    null,
                    bccMailList
            );

            smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()), bookedMessage);

            publicUserService.saveMembershipNotification(
                    userMembership,
                    title,
                    bookedMessage,
                    false
            );
            //update business profile revenue
            businessProfileService.updateBusinessProfileMembershipRevenue(userMembership, ipgType);

            log.info("Membership purchase successful - " + userMembership.getId());
        }
    }

//    @Override
//    @Transactional
//    public boolean handlePaymentErrorWebhook(PaymentIntent paymentIntent) {
//        try {
//            log.info("\nConfirm payment error of class session: Intent id: {}", paymentIntent.getId());
//
//            PublicUserMembership userMembership = publicUserMembershipRepository.findByPaymentId(paymentIntent.getId());
//
//            if (userMembership == null) return false;
//
//            if (userMembership.getStatus() == MembershipStatus.BOOKED) return true;
//
//            publicUserMembershipRepository.delete(userMembership);
//
//            PublicUser publicUser = userMembership.getPublicUser();
//
//            saveFailTransaction(userMembership);
//
//            smsHandler.sendMessages(Collections.singletonList(publicUser.getMobile()),
//                    MEMBERSHIP_FAILED_DESC.replace("{the membership}",
//                            userMembership.getMembership().getName()) + userMembership.getPaymentId()
//                            + " (Reference : " + userMembership.getId() + ")");
//            log.info("Membership purchase failed - " + userMembership.getId());
//            return true;
//
//        } catch (Exception e) {
//            log.error("Error in class session enroll payment fail handler: ", e);
//            return false;
//        }
//    }
    //---------------------------------------------------------------------------------------------------------------------------------------------------------------


    //admin and public user - physical class and gym

    @Override
    public MembershipDTO getMembershipById(long id, String token) {
        Membership membership = membershipRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_FOUND));
        return getMembership(membership, token);
    }

    @Override
    public void cashPaymentForMembership(long enrollId, long userId) {

        AuthUser authUser = authUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));

        PublicUserMembership publicUserMembership = publicUserMembershipRepository.findById(enrollId)
                .orElseThrow(() -> new CustomServiceException(NO_MEMBERSHIP_PURCHASE_FOUND));

        if (publicUserMembership.getStatus() == MembershipStatus.PENDING) {
            PublicUser publicUser = publicUserMembership.getPublicUser();
            publicUserMembership.setStatus(MembershipStatus.BOOKED);
            publicUserMembership.setCollectedBy(authUser);
            publicUserMembershipRepository.save(publicUserMembership);
            sendUserPaymentSuccess(publicUser, publicUserMembership,null);
        } else if (publicUserMembership.getStatus() == MembershipStatus.BOOKED) {
            throw new CustomServiceException("Already paid for this membership");
        } else {
            throw new CustomServiceException("Invalid membership status!");
        }
    }

    @Override
    public Page<MembershipSummeryDTO> getAllGymMembershipSummery(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return membershipRepository.getAllGymMebershipsByDateRange(startDate, endDate, pageable);
    }

    @Override
    public List<MembershipDTO> getAllOnlineClassMemberships() {
        List<Membership> membershipList = membershipRepository.findMembershipByType(MembershipType.ONLINE_CLASS);
        List<MembershipDTO> membershipDTOS = new ArrayList<>();
        for (Membership membership : membershipList) {
            membershipDTOS.add(new MembershipDTO(
                            membership.getId(),
                            membership.getName(),
                            membership.getType()
                    )
            );
        }
        return membershipDTOS;
    }

    @Override
    public List<MembershipDTO> getAllPhysicalClassMemberships() {
        List<Membership> membershipList = membershipRepository.findMembershipByType(MembershipType.PHYSICAL_CLASS);
        List<MembershipDTO> membershipDTOS = new ArrayList<>();
        for (Membership membership : membershipList) {
            membershipDTOS.add(new MembershipDTO(
                            membership.getId(),
                            membership.getName(),
                            membership.getType()
                    )
            );
        }
        return membershipDTOS;
    }

//===========================================================public methods - end===================================================================================

    //admin - Online class membership

    @Override
    @Transactional
    public void createOnlineClassMembership(OnlineClassMembershipDTO dto) {
        Membership membership = getNewMembership(dto, MembershipType.ONLINE_CLASS);
        List<OnlineClassMembership> onlineClassMemberships = getNewOnlineClassMemberships(membership, dto);
        membership = membershipRepository.save(membership);
        log.info("Save membership - " + membership);
        onlineClassMembershipRepository.saveAll(onlineClassMemberships);
    }

    @Override
    @Transactional
    public void updateOnlineClassMembership(OnlineClassMembershipDTO dto) {
        Membership membership = updateMembership(dto);
        List<Long> newIdList = dto.getOnlineClassIdList();
        List<Long> existingIdList = new ArrayList<>();

        /*save existing membership allow status first*/
        List<OnlineClassMembership> existingOnlineClassMemberships = new ArrayList<>();
        for (OnlineClassMembership onlineClassMembership : membership.getOnlineClassMemberships()) {
            existingIdList.add(onlineClassMembership.getClassParent().getId());
            existingOnlineClassMemberships.add(onlineClassMembership);
        }
        onlineClassMembershipRepository.saveAll(existingOnlineClassMemberships);

        List<Long> removingIdList = new ArrayList<>(existingIdList);

        removingIdList.removeAll(newIdList);
        newIdList.removeAll(existingIdList);

        if (!removingIdList.isEmpty()) {
            List<OnlineClassMembership> onlineClassMemberships = new ArrayList<>();
            for (Long id : removingIdList) {
                Class clz = classRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
                OnlineClassMembership onlineClassMembership = onlineClassMembershipRepository.findOnlineClassMembershipByMembershipAndClassParent(membership, clz);
                onlineClassMemberships.add(onlineClassMembership);
            }
            onlineClassMembershipRepository.deleteAll(onlineClassMemberships);
        }

        if (!newIdList.isEmpty()) {
            List<OnlineClassMembership> onlineClassMemberships = new ArrayList<>();
            for (Long id : newIdList) {
                Class clz = classRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_CLASS_FOUND));
                OnlineClassMembership onlineClassMembership = new OnlineClassMembership(clz, membership);
                onlineClassMemberships.add(onlineClassMembership);
            }
            onlineClassMembershipRepository.saveAll(onlineClassMemberships);
        }
    }

}
