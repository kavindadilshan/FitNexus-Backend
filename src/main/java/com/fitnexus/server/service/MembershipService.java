package com.fitnexus.server.service;

import com.fitnexus.server.dto.classes.ClassDetailsDTO;
import com.fitnexus.server.dto.classes.ClassMembershipAdminDTO;
import com.fitnexus.server.dto.classes.OnlineClassMembershipAdminDTO;
import com.fitnexus.server.dto.classes.OnlineClassMembershipDTO;
import com.fitnexus.server.dto.classsession.ClassSessionListResponse;
import com.fitnexus.server.dto.common.StripeCheckResponse;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.dto.gym.GymMembershipAdminDTO;
import com.fitnexus.server.dto.gym.GymMembershipDTO;
import com.fitnexus.server.dto.membership.*;
import com.fitnexus.server.dto.membership.*;
import com.fitnexus.server.dto.payhere.PreApproveResponseDTO;
import com.fitnexus.server.dto.physical_class.PhysicalCLassMembershipDTO;
import com.fitnexus.server.dto.publicuser.PublicUserMembershipDTO;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.gym.Gym;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.enums.MembershipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public interface MembershipService {
    @Transactional
    void createOnlineClassMembership(OnlineClassMembershipDTO dto);

    @Transactional
    void createPhysicalCLassMembership(PhysicalCLassMembershipDTO dto);

    @Transactional
    void updateOnlineClassMembership(OnlineClassMembershipDTO dto);

    @Transactional
    void updatePhysicalClassMembership(PhysicalCLassMembershipDTO dto);

    @Transactional
    void createGymMembership(GymMembershipDTO dto);

    @Transactional
    void updateGymMembership(GymMembershipDTO dto);

    @Transactional
    String reserveMembershipByStripe(MembershipBookDTO membershipBookDTO, String timeZone);
    @Transactional
    Boolean verifyMembershipPaymentByMobile(String card_no, String order_id, String card_expiry, String payhere_amount,
                                           String payhere_currency, String card_holder_name, String method, String payment_id,
                                           String status_code, String md5sig, String status_message, String customer_token);

    @Transactional
    Boolean verifyMembershipPaymentByWeb(String card_no, String order_id, String card_expiry, String payhere_amount,
                                            String payhere_currency, String card_holder_name, String method, String payment_id,
                                            String status_code, String md5sig, String status_message, String customer_token);

    @Transactional
    Boolean verifyMembershipOneTimePaymentByWeb(String order_id,String payment_id,String payhere_amount,String payhere_currency,String status_code,String md5sig,String status_message);

    @Transactional
    Boolean verifyMembershipOneTimePaymentByMobile(String order_id,String payment_id,String payhere_amount,String payhere_currency,String status_code,String md5sig,String status_message);

    @Transactional
    String reserveMembershipByPayhere(MembershipBookDTO membershipBookDTO, String timeZone);

//    @Transactional
//    String reserveMembershipByPayhere(MembershipBookDTO membershipBookDTO, String timeZone);

    @Transactional
    PreApproveResponseDTO checkoutMembershipByPayhere(MembershipBookDTO membershipBookDTO, String timeZone);

    @Transactional
    PreApproveResponseDTO makeOneTimePaymentMembershipByPayhere(MembershipBookDTO membershipBookDTO, String timeZone);

    StripeCheckResponse checkBookingAndIsStripe(MembershipBookDTO dto);

//    @Transactional
//    boolean handlePaymentSuccessWebhook(PaymentIntent paymentIntent);

    @Transactional
    boolean handlePaymentSuccessPayhereMobile(String order_id,String payment_id,String payhere_amount,String payhere_currency,String status_code,String md5sig,String status_message);

    @Transactional
    boolean handlePaymentSuccessPayhereWeb(String order_id,String payment_id,String payhere_amount,String payhere_currency,String status_code,String md5sig,String status_message);

//    @Transactional
//    boolean handlePaymentErrorWebhook(PaymentIntent paymentIntent);

    Page<GymMembershipDTO> getGymDayPasses(double longitude, double latitude, Pageable pageable, String token);

    Page<GymMembershipDTO> searchGymDayPasses(double longitude, double latitude, String country, String text, Pageable pageable, String token);

    Page<DayPassPurchasedDTO> getPurchasedDayPasses(long userId, Pageable pageable);

    Page<DayPassPurchasedDTO> getPurchasedDayPassesHistory(long userId, Pageable pageable);

    Page<PublicUserMembershipDTO> getMembershipsByUser(long userId, MembershipType type, Pageable pageable);

    PublicUserMembershipDTO getUserMembershipByPublicUser(long publicUserMembershipId);

    MembershipDTO getMembershipById(long id, String token);

    void showMembership(long membershipId);

    Page<MembershipEnrollDTO> getStudentsForMembership(long membershipId, Pageable pageable);

    List<MembershipDTO> getMembershipsByPhysicalSession(PhysicalClassSession session, PublicUser publicUser);

    List<MembershipDTO> getMembershipsByPhysicalClass(PhysicalClass physicalClass, PublicUser publicUser);

    void setPhysicalClassMembershipDetails(ClassSessionListResponse sessionResponse, ClassDetailsDTO classResponse, PhysicalClass physicalClass, PublicUser publicUser);

    void setOnlineClassMembershipDetails(ClassSessionListResponse sessionResponse, ClassDetailsDTO classResponse, Class classParent, PublicUser publicUser);

    Page<MembershipsForPhysicalClassDTO> getPurchasedPhysicalClassMemberships(long userId, Pageable pageable);

    Page<MembershipsForOnlineClassDTO> getPurchasedOnlineClassMemberships(long userId, MembershipType type, Pageable pageable);

    Page<MembershipsForPhysicalClassDTO> getPurchasedPhysicalClassMembershipsHistory(long userId, Pageable pageable);

    Page<MembershipsForOnlineClassDTO> getPurchasedOnlineClassMembershipsHistory(long userId, MembershipType type, Pageable pageable);

    GymDTO setMembershipDetailsForGymDTO(Gym gym, GymDTO gymDTO, PublicUser publicUser);

    GymDTO setMembershipDetailsForGymOpen(Gym gym, GymDTO gymDTO);

    List<MembershipDTO> getGymMembershipsForGym(Gym gym, PublicUser publicUser);

    Page<MembershipDTO> getGymMembershipsForGym(long gymId, long userId, Pageable pageable);

    Page<MembershipsForGymDTO> getGymMembershipsByBusinessProfile(long businessId, Pageable pageable, String token);

    Page<GymMembershipDTO> getGymMemberships(double longitude, double latitude, Pageable pageable, String token);

    Page<GymMembershipDTO> searchGymMemberships(double longitude, double latitude, String country, String text, Pageable pageable, String token);

    Page<PurchasedMembershipForGymDTO> getPurchasedGymMemberships(long userId, Pageable pageable);

    Page<PurchasedMembershipForGymDTO> getPurchasedGymMembershipsHistory(long userId, Pageable pageable);

    DayPassForGymDTO getDayPassForGym(Gym gym, PublicUser publicUser);

    @Transactional
    void createGymDayPass(GymMembershipDTO gymMembershipDTO);

    @Transactional
    void updateGymDayPass(GymMembershipDTO gymMembershipDTO);

    Page<MembershipDTO> getAllMembershipsByPhysicalClassId(long physicalClassId, Pageable pageable);

    Page<MembershipDTO> getAllMembershipsByOnlineClassId(long classId, Pageable pageable);

    Page<MembershipDTO> getAllMembershipsByGymId(long gymId, Pageable pageable);

    Page<GymMembershipDTO> getAllGymMembershipsByBusinessProfileId(long businessId, Pageable pageable);

    Page<GymMembershipDTO> searchGymMembershipsByBusinessProfileId(long businessId, String text, Pageable pageable);

    Page<MembershipDTO> searchMembershipByClassId(long physicalClassId, String text, Pageable pageable);

    Page<MembershipDTO> searchMembershipByOnlineClassId(long classId, String text, Pageable pageable);

    Page<ClassMembershipAdminDTO> getAllMembershipClassesByBusinessProfileId(long businessProfileId, Pageable pageable);

    Page<OnlineClassMembershipAdminDTO> getAllMembershipOnlineClassesByBusinessProfileId(long businessProfileId, Pageable pageable);

    Page<GymMembershipAdminDTO> getAllMembershipGymsByBusinessProfileId(long businessProfileId, Pageable pageable);

    Page<ClassMembershipAdminDTO> searchMembershipClassesByBusinessProfileId(long businessProfileId, String text, Pageable pageable);

    Page<OnlineClassMembershipAdminDTO> searchMembershipOnlineClassesByBusinessProfileId(long businessProfileId, String text, Pageable pageable);

    Page<MembershipsForPhysicalClassDTO> getPhysicalClassMembershipsByBusinessProfile(long businessId, Pageable pageable, String token);

    Page<MembershipsForOnlineClassDTO> getOnlineClassMembershipsByBusinessProfile(long businessId, Pageable pageable, String token);

    @Transactional
    void reserveMembershipSession(MembershipSessionReserveDTO dto, String timeZone);

    @Transactional
    void reserveOnlineMembershipSession(MembershipSessionReserveDTO dto, String timeZone);

    @Transactional
    void cancelMembershipSessionReservation(MembershipSessionReserveDTO dto);

    @Transactional
    void cancelOnlineMembershipSessionReservation(MembershipSessionReserveDTO dto);

    void hideMembership(long membershipId);

    void cashPaymentForMembership(long enrollId, long userId);

    Page<MembershipSummeryDTO> getAllGymMembershipSummery(LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<MembershipDTO> getAllOnlineClassMemberships();

    List<MembershipDTO> getAllPhysicalClassMemberships();
}
