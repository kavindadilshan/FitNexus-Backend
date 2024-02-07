package com.fitnexus.server.service;

import com.fitnexus.server.dto.businessprofile.*;
import com.fitnexus.server.dto.businessprofile.*;
import com.fitnexus.server.dto.classes.ClassRevenueSummaryDTO;
import com.fitnexus.server.dto.membership.MembershipRevenueSummaryDTO;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.classes.ClassSessionEnroll;
import com.fitnexus.server.entity.classes.physical.PhysicalSessionEnroll;
import com.fitnexus.server.entity.instructor.InstructorPackageEnroll;
import com.fitnexus.server.entity.publicuser.PublicUserMembership;
import com.fitnexus.server.enums.BusinessAgreementStatus;
import com.fitnexus.server.enums.IPGType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public interface BusinessProfileService {

    String checkProfileExistence(BusinessProfileCreateDto dto);

    @Transactional
    Map<String, String> createBusinessProfile(BusinessProfileCreateDto profileDto, String updatingUsername);

    @Transactional
    void updateBusinessProfile(BusinessProfileCreateDto profileDto, String updatingUsername);

    Page<BusinessProfileResponseDto> searchBusinessProfiles(String data, Pageable pageable);

    Page<BusinessProfileResponseDto> getAllProfiles(Pageable pageable, String username);

    List<BusinessProfileResponseDto> getAllBusinessProfiles();

    List<BusinessProfileNameIdDTO> getAllProfileIDsAndNames(String username);

    List<BusinessProfileNameIdDTO> getBusinessNamesHasInstructors(String username);

    BusinessProfileResponseDto getBusinessProfileByID(long id, String username);

    @Transactional
    String changeProfileStatus(long id, BusinessAgreementStatus status, String updatingUsername);

    Page<BusinessProfileListResponse> getActiveBusinessProfiles(String name, List<Long> classTypeIds,
                                                                String type, long userId,
                                                                double longitude, double latitude,
                                                                boolean corporateOnly,
                                                                Pageable pageable);

    BusinessProfileListResponse getBusinessProfile(long id);

    BusinessProfileListResponse getBusinessProfileByName(String businessName);

    BusinessProfileCoachDTO getBusinessProfileForCoach(long id,String username);

    @Transactional
    void updateBusinessProfileSessionRevenue(ClassSessionEnroll classSessionEnroll);

    @Transactional
    void updateBusinessProfilePhysicalSessionRevenue(PhysicalSessionEnroll physicalSessionEnroll);

    @Transactional
    void updateBusinessProfileInstructorPackageRevenue(InstructorPackageEnroll enroll);

    @Transactional
    void updateBusinessProfileMembershipRevenue(PublicUserMembership userMembership, IPGType ipgType);

    Page<BusinessProfileRevenueDTO> getRevenueDetails(LocalDateTime start, LocalDateTime end, String username, String name, Pageable pageable);

    Page<ClassRevenueSummaryDTO> getOnlineClassesSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable);

    Page<ClassRevenueSummaryDTO> getPhysicalClassesSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable);

    Page<MembershipRevenueSummaryDTO> getPhysicalClassesMembershipSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable);

    Page<MembershipRevenueSummaryDTO> getOnlineClassesMembershipSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable);

    Page<MembershipRevenueSummaryDTO> getGymMembershipSummary(LocalDateTime start, LocalDateTime end, long businessProfileId, Pageable pageable);

    Page<SubscriptionPaymentDTO> getSubscriptionPaymentHistory(Pageable pageable);

    Page<BusinessProfileListResponse> getBusinessProfilesByClassType(long classTypeId, Pageable pageable);

    @Transactional
    void updateAgreement(long businessProfileId, BusinessProfileCreateDto dto);

    void renewAgreement(long businessProfileId, BusinessProfileAgreementDTO dto);

    void cancelAgreement(long businessProfileId);

    List<BusinessProfileLocationDTO> getLocationsForProfile(long profileId);

    List<BusinessProfileLocationDTO> getGymLocationsForBusinessProfile(long profileId);

    Page<BusinessProfileLocationDTO> getLocationsForProfile(long profileId, Pageable pageable);

    BusinessProfileListResponse getBusinessProfileResponse(BusinessProfile businessProfile);

    Page<PaymentSummaryDTO> getProfilePaymentSummary(String name, Pageable pageable);

    Page<DuePaymentDTO> getProfileDuePayments(String name, Pageable pageable);

    @Transactional
    void settleProfilePayment(PaymentSummaryDTO payment);

    BusinessProfileListResponse getBusinessProfileResponse(BusinessProfile businessProfile, double longitude, double latitude);

    void deleteBusinessProfile(long id);
}
