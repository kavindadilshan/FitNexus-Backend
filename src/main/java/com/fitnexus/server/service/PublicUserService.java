package com.fitnexus.server.service;

import com.fitnexus.server.dto.admin.FrontendEventDTO;
import com.fitnexus.server.dto.classsession.SessionZoomDetails;
import com.fitnexus.server.dto.common.CardDetailsResponse;
import com.fitnexus.server.dto.common.NotificationTokenDTO;
import com.fitnexus.server.dto.common.OTPRequestDTO;
import com.fitnexus.server.dto.common.PinVerifyDTO;
import com.fitnexus.server.dto.payhere.GeneratedHashValueDetailsDTO;
import com.fitnexus.server.dto.payhere.PreApproveResponseDTO;
import com.fitnexus.server.dto.publicuser.*;
import com.fitnexus.server.dto.publicuser.*;
import com.fitnexus.server.entity.classes.ClassSession;
import com.fitnexus.server.entity.classes.ClassSessionEnroll;
import com.fitnexus.server.entity.classes.packages.PublicUserPackageSubscription;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.instructor.InstructorPackage;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.publicuser.PublicUserMembership;
import com.fitnexus.server.entity.publicuser.PublicUserNotification;
import com.fitnexus.server.enums.AuthType;
import com.fitnexus.server.enums.EventType;
import com.fitnexus.server.enums.UserDeviceTypes;
import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.model.PaymentIntent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


@Service
public interface PublicUserService {

    void requestRegisterOtp(OTPRequestDTO otpRequestDTO);

    void verifyOtp(PinVerifyDTO pinVerifyDTO);

    void checkMobileAccount(PublicUserDTO publicUserDTO);

    void createMobileAccount(PublicUserRegisterDTO userRegisterDTO);

    JsonNode verifySocialLogin(PublicUserDTO publicUserDTO);

    String updateProfilePhoto(PublicUserDTO publicUserDTO);

    void changeForgotPassword(UpdateMobileForgotPasswordDTO passwordChangeDTO);

    PublicUserDTO updateProfile(PublicUserDTO publicUserDTO);

    void updateName(PublicUserDTO publicUserDTO);

    void updateDob(PublicUserDTO publicUserDTO);

    void updateVerificationNo(PublicUserDTO publicUserDTO);

    void updateHeightAndWeight(PublicUserDTO publicUserDTO);

    void updateUserMobile(PublicUserRegisterDTO publicUserDTO);

    void updateUserEmail(PublicUserDTO publicUserDTO);

    PublicUserEmailDTO getEmailVerification(long userId);

    void updateAddresses(PublicUserDTO publicUserDTO);

    void updateGender(PublicUserDTO publicUserDTO);

    void userAlreadyExists(PublicUserDTO publicUserDTO);

    void requestLoginOtp(OTPRequestDTO otpRequestDTO);

    void verifyOtpLogin(PinVerifyDTO pinVerifyDTO);

    @Transactional
    void createMobileSocialAccount(PublicUserRegisterDTO userRegisterDTO);

    void checkBeforeSocialRegAndOtp(PublicUserRegisterDTO userRegisterDTO);

    void changePassword(PublicUserPasswordChangeDTO passwordChangeDTO);

    List<CardDetailsResponse> getCardsOfUser(long userId);

    String getSaveCardIntent(long userId);

    PreApproveResponseDTO generatePreApproveDetails(long userId,String type);

    PreApproveResponseDTO generatePreApproveDetails(long userId,String amount,String type);
    PreApproveResponseDTO generateMembershipCheckoutParameters(long userId,String amount,String type);
    PreApproveResponseDTO generatePreApproveDetailsForOnlineSessionEnroll(long userId,String amount,String type);
    PreApproveResponseDTO generateOnlineSessionCheckoutDetails(long userId,String amount,String type);
    PreApproveResponseDTO generatePreApproveDetailsForPhysicalSessionEnroll(long userId,String amount,String type);

    PreApproveResponseDTO generateCheckoutDetailsForPhysicalSessionEnroll(long userId,String amount,String type);

    String saveCardDetailsByWeb(String card_no, String order_id, String card_expiry, String payhere_amount,
                         String payhere_currency, String card_holder_name, String method, String payment_id,
                         String status_code, String md5sig, String status_message, String customer_token);

    String saveCardDetailsByMobile(String card_no, String order_id, String card_expiry, String payhere_amount,
                           String payhere_currency, String card_holder_name, String method, String payment_id,
                           String status_code, String md5sig, String status_message, String customer_token);

//    @Async
//    void handleUserStripeRegOrNot(PublicUser publicUser, PaymentIntent paymentIntent);

//    void saveCardBySetupIntent(SetupIntent setupIntent);

    void updatePaymentCard(CardDetailsResponse cardDetailsDTO, String token);

    void removePaymentCard(String paymentMethodId, String token);
    void saveOrRemovePaymentCard(String paymentMethodId,String action, String token);
    Page<PublicUserNotificationResponse> getUserNotifications(long userId, Pageable pageable);

    @Transactional
    Page<PublicUserNotificationResponse> getUserAllNotifications(long userId, Pageable pageable);

    long getUserNotificationsCount(long userId);

    @Transactional
    void saveInstructorPackageNotification(InstructorPackage instructorPackage, List<PublicUser> publicUsers, String title, String message);

    void saveSessionNotification(ClassSession classSession, List<PublicUser> publicUsers, String title, String message, boolean cancelOld);

    void savePhysicalSessionNotification(PhysicalClassSession classSession, List<PublicUser> publicUsers, String title, String message, boolean cancelOld);

    @Transactional
    void saveMembershipNotification(PublicUserMembership publicUserMembership, String title, String message, boolean cancelOld);

    @Transactional
    void savePackageNotification(PublicUserPackageSubscription subscription, String title, String message);

    void cancelScheduledNotificationList(List<PublicUserNotification> allScheduledNotifications, UserDeviceTypes userDeviceType);

    void saveUserTimeZone(PublicUser publicUser, String timeZone);

    void updateUserTimeZone();

    void addWebPushTokenForUser(NotificationTokenDTO notificationTokenDTO);

    @Transactional
    void addPushTokenForUser(NotificationTokenDTO notificationTokenDTO);

    String getUserIdWithSocial(AuthType socialMediaType, String socialId);

    void createVerificationToken(PublicUser publicUser, String token);

    @Transactional
    HttpStatus checkVerificationToken(String token);

    void resendEmailToken(String existingToken);

    PaymentIntent getPaymentIntentByPublicUser(PublicUser publicUser, BigDecimal amount, String paymentMethodId);

    GeneratedHashValueDetailsDTO getPayherePaymentDetailsByPublicUser(PublicUser publicUser, BigDecimal amount, String paymentMethodId, String type);
    GeneratedHashValueDetailsDTO getSessionEnrollPayherePaymentDetailsByPublicUser(PublicUser publicUser, BigDecimal amount, String paymentMethodId, String type);

    List<PublicUserDiscountDTO> getDiscountsForUser(long id);

    void sendCallNotification(PublicUser publicUser, SessionZoomDetails zoomDetails);

    void sendSessionStartNotification(List<ClassSessionEnroll> classSessionEnrolls, ClassSession classSession);

    String getCountryOfUserFromToken(String token);

    void sendSupportEmail(SupportEmailDTO supportEmailDTO, long userId);

    void sendVerifyEmail(String email, long publicUserId);

    void saveEventType(long publicUserId, PublicUserEventDTO publicUserEventDTO);

    FrontendEventDTO getFrontendEvents(EventType eventType, long userId);

    void deletePublicUser(String mobile);

    boolean checkUserWithZoomMeetingID(long userId, long zoomMeetingId);
}
