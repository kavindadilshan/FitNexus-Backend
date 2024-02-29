package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.config.throttling_config.Throttling;
import com.fitnexus.server.dto.common.*;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.payhere.PreApproveResponseDTO;
import com.fitnexus.server.dto.promoCode.PromoCodeConsumeDTO;
import com.fitnexus.server.dto.publicuser.*;
import com.fitnexus.server.enums.PromoCodeServiceCategory;
import com.fitnexus.server.service.PromoCodeManagementService;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.util.APIHandler;
import com.fitnexus.server.util.GuestUserUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fitnexus.server.config.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.TIME_ZONE_HEADER;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users")
public class PublicUserController {

    private final PublicUserService publicUserService;
    private final APIHandler apiHandler;
    private final PromoCodeManagementService promoCodeManagementService;

    @Throttling(timeFrameInSeconds = 600, calls = 4)
    @PatchMapping(value = "/register/otp/request")
    public ResponseEntity registerRequest(@RequestBody OTPRequestDTO otpRequestDTO) {
        log.info("\nPublic user OTP request: " + otpRequestDTO);
        publicUserService.requestRegisterOtp(otpRequestDTO);
        log.info("Response : OTP request is successful");
        return ResponseEntity.ok(new CommonResponse<>(true, "OTP request is successful"));
    }

    @PatchMapping(value = "/register/otp/verify")
    public ResponseEntity registerVerify(@RequestBody PinVerifyDTO pinVerifyDTO) {
        log.info("\nPublic user register OTP verify: " + pinVerifyDTO);
        publicUserService.verifyOtp(pinVerifyDTO);
        log.info("Response : OTP verification is successful");
        return ResponseEntity.ok(new CommonResponse<>(true, "OTP verification is successful"));
    }

    @PostMapping(value = "/register/check")
    public ResponseEntity registerCheck(@RequestBody PublicUserDTO publicUserDTO) {
        log.info("\nPublic user check account: " + publicUserDTO);
        publicUserService.checkMobileAccount(publicUserDTO);
        log.info("Response : Account can proceed");
        return ResponseEntity.ok(new CommonResponse<>(true, "Account can proceed"));
    }

    @PostMapping(value = "/register/account")
    public ResponseEntity registerAccount(@RequestBody PublicUserRegisterDTO userRegisterDTO, @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        if (timeZone != null) userRegisterDTO.setTimeZone(timeZone);
        log.info("\nPublic user register account: {} \ntimezone: {}", userRegisterDTO, timeZone);
        publicUserService.createMobileAccount(userRegisterDTO);
        JsonNode userAuthResp = apiHandler.getAuthResponse(userRegisterDTO.getMobile(), userRegisterDTO.getPassword(), SecurityConstants.PUBLIC_CLIENT_ID);
        log.info("Response : User Auth Response.");
        return ResponseEntity.ok(new CommonResponse<>(true, userAuthResp));
    }

    @PatchMapping(value = "/authenticate/otp/request")
    public ResponseEntity otpLoginRequest(@RequestBody OTPRequestDTO otpRequestDTO) {
        log.info("\nPublic user OTP login request: " + otpRequestDTO);
        publicUserService.requestLoginOtp(otpRequestDTO);
        log.info("Response : OTP login request is successful");
        return ResponseEntity.ok(new CommonResponse<>(true, "OTP login request is successful"));
    }

    @PatchMapping(value = "/authenticate/otp/verify")
    public ResponseEntity otpLoginVerify(@RequestBody PinVerifyDTO pinVerifyDTO) {
        log.info("\nPublic user OTP login verify: " + pinVerifyDTO);
        publicUserService.verifyOtpLogin(pinVerifyDTO);
        JsonNode userAuthResp = apiHandler.getAuthResponse(pinVerifyDTO.getMobile(), pinVerifyDTO.getMobile(), SecurityConstants.PUBLIC_CLIENT_ID);
        log.info("Response : User Auth Response.");
        return ResponseEntity.ok(new CommonResponse<>(true, userAuthResp));
    }

    @PostMapping(value = "/register/social/check")
    public ResponseEntity checkSocialAccount(@RequestBody PublicUserRegisterDTO userRegisterDTO) {
        log.info("\nPublic user check account for social: " + userRegisterDTO);
        publicUserService.checkBeforeSocialRegAndOtp(userRegisterDTO);
        log.info("Response : Account can proceed.");
        return ResponseEntity.ok(new CommonResponse<>(true, "Account can proceed."));
    }

    @PostMapping(value = "/register/social/account")
    public ResponseEntity registerSocialAccount(@RequestBody PublicUserRegisterDTO userRegisterDTO, @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nPublic user register account by social: {} \ntimezone: {}", userRegisterDTO, timeZone);
        if (timeZone != null) userRegisterDTO.setTimeZone(timeZone);
        publicUserService.createMobileSocialAccount(userRegisterDTO);
        String userIdWithSocial = publicUserService.getUserIdWithSocial(userRegisterDTO.getAuthType(), userRegisterDTO.getSocialMediaId());
        JsonNode userAuthResp = apiHandler.getAuthResponse(userRegisterDTO.getMobile(), userIdWithSocial, SecurityConstants.PUBLIC_SOCIAL_CLIENT_ID);
        log.info("Response : User Auth Response.");
        return ResponseEntity.ok(new CommonResponse<>(true, userAuthResp));
    }

    @PostMapping(value = "/authenticate/social")
    public ResponseEntity socialLoginVerify(@RequestBody PublicUserDTO publicUserDTO) {
        log.info("Public user social login: " + publicUserDTO);
        JsonNode userAuthResp = publicUserService.verifySocialLogin(publicUserDTO);
        log.info("User Auth Response. ");
        return ResponseEntity.ok(new CommonResponse<>(true, userAuthResp));
    }

    @PutMapping(value = "/profile/image")
    public ResponseEntity updateImage(@RequestBody PublicUserDTO publicUserDTO,
                                      @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user image update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        String profilePhoto = publicUserService.updateProfilePhoto(publicUserDTO);
        log.info("Profile image is updated. {} ", profilePhoto);
        return ResponseEntity.ok(new CommonResponse<>(true, profilePhoto));
    }

    @PutMapping(value = "/profile/password")
    public ResponseEntity changePassword(@RequestBody PublicUserPasswordChangeDTO passwordChangeDTO,
                                         @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        log.info("Public user password change: " + passwordChangeDTO);
        CustomUserAuthenticator.checkPublicUserMobileWithToken(passwordChangeDTO.getMobile(), token);
        publicUserService.changePassword(passwordChangeDTO);
        log.info("Password is updated. {} ", passwordChangeDTO);
        return ResponseEntity.ok(new CommonResponse<>(true, "Password is changed successfully"));
    }

    @PutMapping(value = "/profile")
    public ResponseEntity changeProfileDetails(@RequestBody PublicUserDTO publicUserDTO,
                                               @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        log.info("Public user profile update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserDTO = publicUserService.updateProfile(publicUserDTO);
        log.info("Profile is updated. {} ", publicUserDTO);
        return ResponseEntity.ok(new CommonResponse<>(true, "Profile is updated successfully"));
    }

    @PutMapping(value = "/authenticate/password/forgot/update")
    public ResponseEntity resetPassword(@RequestBody UpdateMobileForgotPasswordDTO passwordChangeDTO) {
        log.info("Public user password change: " + passwordChangeDTO);
        publicUserService.changeForgotPassword(passwordChangeDTO);
        log.info("Password is updated. {} ", passwordChangeDTO);
        return ResponseEntity.ok(new CommonResponse<>(true, "Password is changed successfully"));
    }

    @PutMapping(value = "/profile/name")
    public ResponseEntity updateName(@RequestBody PublicUserDTO publicUserDTO,
                                     @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user Name update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateName(publicUserDTO);
        log.info("Name is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Name is updated."));
    }

    @PutMapping(value = "/profile/dob")
    public ResponseEntity updateDob(@RequestBody PublicUserDTO publicUserDTO,
                                    @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user DOB update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateDob(publicUserDTO);
        log.info("DOB is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "DOB is updated."));
    }

    @PutMapping(value = "/profile/verification-no")
    public ResponseEntity updateVerificationNo(@RequestBody PublicUserDTO publicUserDTO,
                                               @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user verification no update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateVerificationNo(publicUserDTO);
        log.info("Verification no is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Verification no is updated."));
    }

    @PutMapping(value = "/profile/height-and-weight")
    public ResponseEntity updateHeightAndWeight(@RequestBody PublicUserDTO publicUserDTO,
                                                @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user height and weight update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateHeightAndWeight(publicUserDTO);
        log.info("Height and weight are updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Height and weight are updated."));
    }

    @PutMapping(value = "/profile/mobile")
    public ResponseEntity updateMobile(@RequestBody PublicUserRegisterDTO publicUserDTO,
                                       @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user mobile update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateUserMobile(publicUserDTO);
        log.info("Mobile is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Mobile is updated."));
    }

    @PutMapping(value = "/profile/email")
    public ResponseEntity updateEmail(@RequestBody PublicUserRegisterDTO publicUserDTO,
                                      @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user email update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateUserEmail(publicUserDTO);
        log.info("Email is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Email is updated."));
    }

    @GetMapping(value = "/profile/email/{userId}")
    public ResponseEntity getEmail(@PathVariable("userId") long userId, @RequestHeader("Authorization") String token) {
        log.info("Get public user email verification: " + userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        PublicUserEmailDTO publicUserEmailDTO = publicUserService.getEmailVerification(userId);
        log.info("Email verification DTO : {} ", publicUserEmailDTO);
        return ResponseEntity.ok(new CommonResponse<>(true, publicUserEmailDTO));
    }

    @PutMapping(value = "/profile/gender")
    public ResponseEntity updateGender(@RequestBody PublicUserDTO publicUserDTO,
                                       @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user Gender update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateGender(publicUserDTO);
        log.info("Gender is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Gender is updated."));
    }

    @PutMapping(value = "/profile/address")
    public ResponseEntity updateAddresses(@RequestBody PublicUserDTO publicUserDTO,
                                          @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user addresses update: " + publicUserDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserDTO.getId(), token);
        publicUserService.updateAddresses(publicUserDTO);
        log.info("Addresses are updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Addresses are updated."));
    }


    @GetMapping(value = "/profile/{userId}/cards")
    public ResponseEntity getCardsOfUser(@PathVariable("userId") long userId, @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("\nPublic user get payment cards: " + userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        List<CardDetailsResponse> cardsOfUser = publicUserService.getCardsOfUser(userId);
        log.info("\nCards list. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, cardsOfUser));
    }

    @PostMapping(value = "/profile/{userId}/cards/intent")
    public ResponseEntity saveCardIntent(@PathVariable("userId") long userId, @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("\nPublic user get setup intent card secret: " + userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        String saveCardIntent = publicUserService.getSaveCardIntent(userId);
        log.info("\nIntent secret. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, saveCardIntent));
    }

    /**
     * (payhere)
     * get details for card preapprove
     *
     * @param userId
     * @param token
     * @return
     */
    @GetMapping(value = "/profile/{userId}/cards/preapprove/{type}")
    public ResponseEntity getPreApproveDetails(@PathVariable("userId") long userId, @PathVariable("type") String type, @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("\nPublic user get generated preapprove details: " + userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        PreApproveResponseDTO generatePreApproveDetails = publicUserService.generatePreApproveDetails(userId, type);
        log.info("\nIntent secret. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, generatePreApproveDetails));
    }

    @PutMapping(value = "/profile/cards")
    public ResponseEntity updatePaymentMethod(@RequestBody CardDetailsResponse cardDetailsDTO, @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("\nPublic user update card: {}", cardDetailsDTO);
        publicUserService.updatePaymentCard(cardDetailsDTO, token);
        log.info("\nIntent secret. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Card is updated"));
    }

    @DeleteMapping(value = "/profile/cards/{paymentMethodId}")
    public ResponseEntity removePaymentMethod(@PathVariable String paymentMethodId, @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("\nPublic user remove card: {}", paymentMethodId);
        publicUserService.removePaymentCard(paymentMethodId, token);
        log.info("\nIntent secret. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Card is removed"));
    }

    @PostMapping(value = "/cards/{paymentMethodId}/{action}")
    public ResponseEntity SaveOrDeleteCard(@PathVariable String paymentMethodId, @PathVariable String action, @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("\nPublic user save or remove card: {}", paymentMethodId);
        publicUserService.saveOrRemovePaymentCard(paymentMethodId, action, token);

        return ResponseEntity.ok(new CommonResponse<>(true, "Process has been completed"));
    }

    @GetMapping(value = "/profile/{userId}/notifications")
    public ResponseEntity getNotificationsOfUser(@PathVariable("userId") long userId, Pageable pageable,
                                                 @RequestHeader("Authorization") String token) {
        log.info("\nPublic user get notifications: user id: {}\t page: {}", userId, pageable);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        Page<PublicUserNotificationResponse> userNotifications = publicUserService.getUserNotifications(userId, pageable);
        log.info("\nNotifications list. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, userNotifications));
    }

    @GetMapping(value = "/profile/{userId}/notifications/all")
    public ResponseEntity getAllNotificationsOfUser(@PathVariable("userId") long userId, Pageable pageable,
                                                    @RequestHeader("Authorization") String token) {
        log.info("\nPublic user get all notifications: user id: {}\t page: {}", userId, pageable);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        Page<PublicUserNotificationResponse> userNotifications = publicUserService.getUserAllNotifications(userId, pageable);
        log.info("\nAll notifications list. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, userNotifications));
    }

    @GetMapping(value = "/profile/{userId}/notifications/count")
    public ResponseEntity getNotificationsCountOfUser(@PathVariable("userId") long userId, @RequestHeader("Authorization") String token) {
        log.info("\nPublic user get notifications count: user id: {}", userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        long userNotificationsCount = publicUserService.getUserNotificationsCount(userId);
        log.info("\nNotifications count: {} ", userNotificationsCount);
        return ResponseEntity.ok(new CommonResponse<>(true, userNotificationsCount));
    }

    @GetMapping(value = "/profile/{userId}/discounts")
    public ResponseEntity getDiscountsOfUser(@PathVariable("userId") long userId,
                                             @RequestHeader("Authorization") String token) {
        log.info("\nPublic user get discounts: user id: {}", userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        List<PublicUserDiscountDTO> discounts = publicUserService.getDiscountsForUser(userId);
        log.info("\nDiscounts list. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, discounts));
    }

    @PostMapping(value = "/profile/notification/token/mobile")
    public ResponseEntity addPushTokenMobile(@RequestBody NotificationTokenDTO notificationTokenDTO,
                                             @RequestHeader("Authorization") String token) {
        log.info("\nPublic user notification token: " + notificationTokenDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(notificationTokenDTO.getUserId(), token);
        publicUserService.addPushTokenForUser(notificationTokenDTO);
        log.info("Public user notification is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Public user notification is updated."));
    }

    @PostMapping(value = "/profile/notification/token/web")
    public ResponseEntity addPushTokenWeb(@RequestBody NotificationTokenDTO notificationTokenDTO,
                                          @RequestHeader("Authorization") String token) {
        log.info("\nPublic user web notification token: " + notificationTokenDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(notificationTokenDTO.getUserId(), token);
        publicUserService.addWebPushTokenForUser(notificationTokenDTO);
        log.info("Public user web notification is updated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Public user web notification is updated."));
    }

    @PostMapping(value = "/support/mail")
    public ResponseEntity sendSupportMail(@RequestBody SupportEmailDTO supportEmailDTO,
                                          @RequestHeader("Authorization") String token) {
        log.info("\nPublic user send support mail: {}", supportEmailDTO);
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        publicUserService.sendSupportEmail(supportEmailDTO, publicUserId);
        log.info("Public user send support mail response. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Mail is sent."));
    }

    @PostMapping(value = "/email/verify/{email}")
    public ResponseEntity sendVerifyEmail(@PathVariable(name = "email") String email, @RequestHeader("Authorization") String token) {
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        log.info("\nPublic user verify email: email- {}, public user id- {}", email, publicUserId);
        publicUserService.sendVerifyEmail(email, publicUserId);
        log.info("Public user sent verify email successfully ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Verify email was sent successfully"));
    }

    @PostMapping(value = "event/track")
    public ResponseEntity addFrontEndEvent(@RequestHeader("Authorization") String token,
                                           @RequestBody PublicUserEventDTO publicUserEventDTO) {
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        log.info("\nPublic user event track: public user id- {}", publicUserId);
        publicUserService.saveEventType(publicUserId, publicUserEventDTO);
        return ResponseEntity.ok(new CommonResponse<>(true, "Event track saved successfully"));
    }

    @PostMapping(value = "profile/promo-code")
    public ResponseEntity addPromoCode(@RequestHeader("Authorization") String token,
                                       @RequestBody PromoCodeConsumeDTO promoCodeConsumeDTO,
                                       @RequestParam(value = "category", required = false) PromoCodeServiceCategory category,
                                       @RequestParam(value = "categoryId", required = false) Long categoryId
    ) {
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        log.info("\nConsumed user promo code : public user id- {}, code - {}", publicUserId, promoCodeConsumeDTO.getCode());

        //check if end point called from purchasing page
        if (category != null && categoryId != null) {
            boolean promoCodeApplicableForCategory = promoCodeManagementService.isPromoDiscountApplicable(
                    publicUserId,
                    promoCodeConsumeDTO.getCode(),
                    category,
                    categoryId
            );
            if (!promoCodeApplicableForCategory) {
                throw new CustomServiceException("This promo code is not applicable for this purchase.");
            }
        }

        promoCodeManagementService.consumePromoCode(publicUserId, promoCodeConsumeDTO.getCode());
        return ResponseEntity.ok(new CommonResponse<>(true, "Promo code consumed successfully"));
    }

    @GetMapping(value = "/applicable-promo-codes/{category}/{categoryId}")
    public ResponseEntity getApplicablePromoCodesOfUser(
            @PathVariable("category") PromoCodeServiceCategory category,
            @PathVariable("categoryId") Long categoryId,
            @RequestHeader("Authorization") String token) {
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        log.info("\nPublic user get applicable promo-codes: user id: {}", publicUserId);
        List<PublicUserDiscountDTO> discounts = promoCodeManagementService.getPromoCodeByCategoryAndId(category, categoryId, publicUserId);
        log.info("\nDiscounts list. {} ", discounts);
        return ResponseEntity.ok(new CommonResponse<>(true, discounts));
    }

    @GetMapping(value = "/zoom-meeting/{zoomMeetingId}/check/{publicUserId}")
    public ResponseEntity checkUserWithZoomMeetingID(@PathVariable("zoomMeetingId") long zoomMeetingId, @PathVariable("publicUserId") long publicUserId) {
        log.info("\nCheck User With Zoom Meeting ID: \npublic user id -{} \nzoom meeting id- {}", publicUserId, zoomMeetingId);
        boolean validity = publicUserService.checkUserWithZoomMeetingID(publicUserId, zoomMeetingId);
        log.info("User({}) valid for zoom meeting - {}", publicUserId, validity);
        return ResponseEntity.ok(new CommonResponse<>(true, validity));
    }
}
