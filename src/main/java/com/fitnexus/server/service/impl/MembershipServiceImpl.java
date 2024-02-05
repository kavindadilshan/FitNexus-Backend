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


    private final ClassRepository classRepository;


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

    //full membership details (gym )
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


}
