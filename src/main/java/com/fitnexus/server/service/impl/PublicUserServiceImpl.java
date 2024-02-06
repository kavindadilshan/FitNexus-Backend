package com.fitnexus.server.service.impl;

import com.fitnexus.server.config.email.OnRegistrationCompleteEvent;
import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.admin.FrontendEventDTO;
import com.fitnexus.server.dto.classsession.ClassSessionBookedResponse;
import com.fitnexus.server.dto.classsession.SessionZoomDetails;
import com.fitnexus.server.dto.common.CardDetailsResponse;
import com.fitnexus.server.dto.common.NotificationTokenDTO;
import com.fitnexus.server.dto.common.OTPRequestDTO;
import com.fitnexus.server.dto.common.PinVerifyDTO;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.payhere.GeneratedHashValueDetailsDTO;
import com.fitnexus.server.dto.payhere.PreApproveResponseDTO;
import com.fitnexus.server.dto.publicuser.*;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.call.OnlineCoachingCall;
import com.fitnexus.server.entity.classes.*;
import com.fitnexus.server.entity.classes.hide.HiddenClassType;
import com.fitnexus.server.entity.classes.packages.PublicUserPackageSubscription;
import com.fitnexus.server.entity.classes.physical.*;
import com.fitnexus.server.entity.instructor.Instructor;
import com.fitnexus.server.entity.instructor.InstructorPackage;
import com.fitnexus.server.entity.membership.Membership;
import com.fitnexus.server.entity.membership.corporate.AccaCode;
import com.fitnexus.server.entity.membership.corporate.Corporate;
import com.fitnexus.server.entity.membership.corporate.CorporateMembershipTempPublicUser;
import com.fitnexus.server.entity.membership.corporate.CorporatePublicUser;
import com.fitnexus.server.entity.publicuser.*;
import com.fitnexus.server.enums.*;
import com.fitnexus.server.notification.PushNotificationManager;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.call.OnlineCoachingCallRepository;
import com.fitnexus.server.repository.classes.ClassRatingRepository;
import com.fitnexus.server.repository.classes.ClassSessionEnrollRepository;
import com.fitnexus.server.repository.classes.ClassSessionRepository;
import com.fitnexus.server.repository.classes.hide.HiddenClassTypeRepository;
import com.fitnexus.server.repository.classes.packages.PublicUserPackageSubscriptionRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassRatingRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalClassSessionRepository;
import com.fitnexus.server.repository.classes.physical.PhysicalSessionEnrollRepository;
import com.fitnexus.server.repository.membership.corporate.AccaCodeRepository;
import com.fitnexus.server.repository.membership.corporate.CorporateMembershipTempPublicUserRepository;
import com.fitnexus.server.repository.membership.corporate.CorporatePublicUserRepository;
import com.fitnexus.server.repository.payhere.TempPreApproveRepository;
import com.fitnexus.server.repository.publicuser.*;
import com.fitnexus.server.service.OTPService;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.service.StripeService;
import com.fitnexus.server.util.*;
import com.fitnexus.server.util.social.AuthTokenValidator;
import com.fitnexus.server.util.zoom.ZoomTokenGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.fitnexus.server.config.security.SecurityConstants.PUBLIC_SOCIAL_CLIENT_ID;
import static com.fitnexus.server.constant.FitNexusConstants.AmountConstants.INVITE_A_FRIEND_DISCOUNT;
import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.DuplicatedConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.ErrorConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.NotPresentedConstants.MOBILE_REQUIRED;
import static com.fitnexus.server.constant.FitNexusConstants.PatternConstants.REGEX;
import static com.fitnexus.server.constant.FitNexusConstants.ZoomConstants.ROLE_JOINEE;
import static com.fitnexus.server.util.FileHandler.PUBLIC_USER_FOLDER;
import static org.apache.logging.log4j.util.Chars.SPACE;


@Slf4j
@RequiredArgsConstructor
@Service
public class PublicUserServiceImpl implements PublicUserService {

    private final OTPService otpService;
    private final PublicUserRepository publicUserRepository;
    private final PublicUserEmailVerificationTokenRepository publicUserEmailVerificationTokenRepository;
    private final PublicUserCardDetailRepository publicUserCardDetailRepository;
    private final PublicUserNotificationRepository publicUserNotificationRepository;
    private final ClassSessionRepository classSessionRepository;
    private final PublicUserDiscountRepository publicUserDiscountRepository;
    private final PublicUserDiscountHistoryRepository publicUserDiscountHistoryRepository;
    private final PublicUserPushTokenRepository publicUserPushTokenRepository;
    private final PublicUserPushTokenIdRepository publicUserPushTokenIdRepository;
    private final StripeService stripeService;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenValidator authTokenValidator;
    private final FileHandler fileHandler;
    private final APIHandler apiHandler;
    private final ApplicationEventPublisher eventPublisher;
    private final EmailSender emailSender;
    private final CustomGenerator customGenerator;
    private final PushNotificationManager pushNotificationManager;
    private final PhysicalClassSessionRepository physicalClassSessionRepository;
    private final HiddenClassTypeRepository hiddenClassTypeRepository;
    private final ZoomTokenGenerator zoomTokenGenerator;

    private final CorporateMembershipTempPublicUserRepository corporateMembershipTempPublicUserRepository;
    private final PublicUserTempCardDetailRepository publicUserTempCardDetailRepository;
    private final PublicUserMembershipRepository publicUserMembershipRepository;
    private final CorporatePublicUserRepository corporatePublicUserRepository;
    private final FrontendEventRepository frontendEventRepository;
    private final AuthUserRepository authUserRepository;
    private final TempPromoCodePublicUserRepository tempPromoCodePublicUserRepository;
    private final PromoCodePublicUserRepository promoCodePublicUserRepository;
    private final PublicUserPackageSubscriptionRepository publicUserPackageSubscriptionRepository;
    private final ClassRatingRepository classRatingRepository;
    private final PhysicalClassRatingRepository physicalClassRatingRepository;
    private final ClassSessionEnrollRepository classSessionEnrollRepository;
    private final PhysicalSessionEnrollRepository physicalSessionEnrollRepository;
    private final OnlineCoachingCallRepository onlineCoachingCallRepository;
    private final AccaCodeRepository accaCodeRepository;

    private final TempPreApproveRepository tempPreApproveRepository;

    private final PayhereAuthTokenGenerator authTokenGenerator;

    @Value("${public_user_reg_verify_api}")
    private String publicUserRegVerifyPage;

    @Value("${support_mail}")
    private String supportMail;

    @Value("${payhere.merchantSecret.mobile}")
    private String merchantSecretMobile;

    @Value("${payhere.merchantSecret.web}")
    private String merchantSecretWeb;

    @Value("${payhere.merahantID}")
    private String merahantID;

    @Value("${payhere.preapprove.amount}")
    private String amount;

    @Value("${payhere.preapprove.notify.URL}")
    private String notifyUrl;

    @Value("${payhere.preapprove.checkout.notify.URL}")
    private String preApproveCheckoutNotifyUrl;

    @Value("${payhere.onetime.checkout.notify.URL}")
    private String onetimeCheckoutNotifyUrl;

    @Value("${payhere.checkout.notify.URL}")
    private String checkoutNotifyUrl;

    @Value("${payhere.checkout.session.notify.URL}")
    private String checkoutSessionNotifyUrl;

    @Value("${payhere.preapprove.checkout.physical.session.notify.URL}")
    private String preApproveCheckoutPhysicalSessionNotifyUrl;

    @Value("${payhere.checkout.physical.session.notify.URL}")
    private String checkoutPhysicalSessionNotifyUrl;

    @Value("${payhere.preapprove.checkout.online.session.notify.URL}")
    private String preApproveCheckoutOnlineSessionNotifyUrl;

    @Value("${payhere.checkout.online.session.notify.URL}")
    private String checkoutOnlineSessionNotifyUrl;

    /**
     * This function can use to request an OTP for the given number
     *
     * @param otpRequestDTO the dto which contains OTP requested mobile.
     */
    @Override
    public void requestRegisterOtp(OTPRequestDTO otpRequestDTO) {
        if (!SMS_PASSCODE.equals(otpRequestDTO.getSmsSecret())) throw new CustomServiceException(SMS_CODE_NOT_ALLOWED);
        PublicUser publicUser = publicUserRepository.findByMobile(otpRequestDTO.getMobile());
        if (publicUser != null) throw new CustomServiceException(MOBILE_ALREADY_EXISTS);
        otpService.sendOtpAndSaveIt(otpRequestDTO.getMobile());
    }

    /**
     * This function validates the OTP in DB with the given one.
     *
     * @param pinVerifyDTO the OTP verification details dto.
     * @throws CustomServiceException if something went wrong.
     */
    @Override
    public void verifyOtp(PinVerifyDTO pinVerifyDTO) {
        otpService.verifyOtp(pinVerifyDTO);
    }

    /**
     * This will check to create a new publicUser account in the system.
     *
     * @param publicUserDTO the publicUser details for registration.
     * @throws CustomServiceException if publicUser already exists or if something went wrong.
     */
    @Override
    public void checkMobileAccount(PublicUserDTO publicUserDTO) {
        userAlreadyExists(publicUserDTO);
        otpService.sendOtpAndSaveIt(publicUserDTO.getMobile());
    }


    private void checkPreRegisterPromoCode(PublicUser publicUser) {
        List<TempPromoCodePublicUser> tempPromoCodePublicUsers = tempPromoCodePublicUserRepository.findAllByPublicUserMobileNumber(publicUser.getMobile());
        for (TempPromoCodePublicUser tempPromoCodePublicUser : tempPromoCodePublicUsers) {
            PromoCodePublicUser promoCodePublicUser = new PromoCodePublicUser();
            promoCodePublicUser.setPublicUser(publicUser);
            promoCodePublicUser.setPromoCode(tempPromoCodePublicUser.getPromoCode());
            promoCodePublicUser.setStatus(PublicUserPromoCodeStatus.NOT_CUNSUMED);

            Optional<PromoCodePublicUser> opcpu = promoCodePublicUserRepository.findByPromoCodeAndPublicUser(tempPromoCodePublicUser.getPromoCode(), publicUser);
            if (!opcpu.isPresent()) {
                promoCodePublicUserRepository.save(promoCodePublicUser);
            }
        }
    }

    /**
     * This will create a new publicUser account in the system.
     *
     * @param publicUserRegisterDTO the publicUser details for registration.
     * @throws CustomServiceException if publicUser already exists or if something went wrong.
     */
    @Override
    @Transactional
    public void createMobileAccount(PublicUserRegisterDTO publicUserRegisterDTO) {
        publicUserRegisterDTO.getOtpDetails().setMobile(publicUserRegisterDTO.getMobile());
        verifyOtp(publicUserRegisterDTO.getOtpDetails());
        userAlreadyExists(publicUserRegisterDTO);
        PublicUser publicUser = getPublicUserForRegister(publicUserRegisterDTO, AuthType.MOBILE);

        publicUser = publicUserRepository.save(publicUser);
        saveInviteAFriendDiscounts(publicUserRegisterDTO, publicUser);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(publicUser, Locale.ENGLISH, publicUserRegVerifyPage));

        //check pre-register promo code
        checkPreRegisterPromoCode(publicUser);

        /*-------------------------------------------------------------------------------------------------------------*/
        //corporate
        List<CorporateMembershipTempPublicUser> corporateMembershipTempPublicUsers =
                corporateMembershipTempPublicUserRepository.findCorporateMembershipTempPublicUsersByMobile(publicUserRegisterDTO.getMobile());

        if (corporateMembershipTempPublicUsers != null && corporateMembershipTempPublicUsers.size() > 0) {
            List<PublicUserMembership> userMemberships = new ArrayList<>();

            Set<Corporate> corporates = new HashSet<>();

            for (CorporateMembershipTempPublicUser tempPublicUser : corporateMembershipTempPublicUsers) {

                Membership membership = tempPublicUser.getMembership();
                Corporate corporate = membership.getCorporate();

                PublicUserMembership userMembership = new PublicUserMembership();
                userMembership.setMembership(membership);
                userMembership.setPublicUser(publicUser);
                userMembership.setDateTime(LocalDateTime.now());
                userMembership.setExpireDateTime(membership.getStartDateTime().plusDays(membership.getDuration()));
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setListedPrice(membership.getPrice());
                userMembership.setPaidAmount(BigDecimal.ZERO);
                userMembership.setRemainingSlots(membership.getSlotCount());
                userMemberships.add(userMembership);

                corporates.add(corporate);
            }

            PublicUser finalPublicUser = publicUser;
            List<CorporatePublicUser> corporatePublicUsers = corporates.stream().map(corporate -> new CorporatePublicUser(corporate, finalPublicUser)).collect(Collectors.toList());

            if (userMemberships.size() > 0) publicUserMembershipRepository.saveAll(userMemberships);
            if (corporatePublicUsers.size() > 0) corporatePublicUserRepository.saveAll(corporatePublicUsers);
            corporateMembershipTempPublicUserRepository.deleteAll(corporateMembershipTempPublicUsers);
        }
    }

    /**
     * This function can use to request an OTP for the given number for login.
     *
     * @param otpRequestDTO the dto which contains OTP requested mobile.
     */
    @Override
    public void requestLoginOtp(OTPRequestDTO otpRequestDTO) {
        PublicUser publicUser = publicUserRepository.findByMobile(otpRequestDTO.getMobile());
        if (publicUser == null) throw new CustomServiceException(NO_USER_FOUND);
        otpService.sendOtpAndSaveIt(otpRequestDTO.getMobile());
    }

    /**
     * This will validate the publicUser mobile OTP dor login.
     *
     * @param pinVerifyDTO the publicUser mobile and its OTP.
     * @throws CustomServiceException if publicUser does mot exist or OTP does not match.
     */
    @Override
    public void verifyOtpLogin(PinVerifyDTO pinVerifyDTO) {
        otpService.verifyOtp(pinVerifyDTO);
        PublicUser publicUser = publicUserRepository.findByMobile(pinVerifyDTO.getMobile());
        if (publicUser == null) throw new CustomServiceException(NO_USER_FOUND);
    }

    /**
     * This will create a new publicUser account with social auth in the system.
     *
     * @param publicUserRegisterDTO the publicUser details for registration.
     * @throws CustomServiceException if publicUser already exists or if something went wrong.
     */
    @Override
    @Transactional
    public void createMobileSocialAccount(PublicUserRegisterDTO publicUserRegisterDTO) {
        publicUserRegisterDTO.setPassword(String.valueOf(UUID.randomUUID()));
        checkUserSocialDetails(publicUserRegisterDTO);

        String publicUserIdWithSocial = getUserIdWithSocial(publicUserRegisterDTO.getAuthType(), publicUserRegisterDTO.getSocialMediaId());
        PublicUser publicUser = getPublicUserForRegister(publicUserRegisterDTO, publicUserRegisterDTO.getAuthType());
        publicUser.setSocialMediaId(publicUserIdWithSocial);
        publicUser.setEmailVerified(true);
        publicUser.setPassword(passwordEncoder.encode(publicUserRegisterDTO.getPassword()));

        publicUser = publicUserRepository.save(publicUser);
        saveInviteAFriendDiscounts(publicUserRegisterDTO, publicUser);

        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(publicUser, Locale.ENGLISH, publicUserRegVerifyPage));

        //check pre-register promo code
        checkPreRegisterPromoCode(publicUser);

        /*-------------------------------------------------------------------------------------------------------------*/
        //corporate
        List<CorporateMembershipTempPublicUser> corporateMembershipTempPublicUsers =
                corporateMembershipTempPublicUserRepository.findCorporateMembershipTempPublicUsersByMobile(publicUserRegisterDTO.getMobile());

        if (corporateMembershipTempPublicUsers != null && corporateMembershipTempPublicUsers.size() > 0) {
            List<PublicUserMembership> userMemberships = new ArrayList<>();

            Set<Corporate> corporates = new HashSet<>();

            for (CorporateMembershipTempPublicUser tempPublicUser : corporateMembershipTempPublicUsers) {

                Membership membership = tempPublicUser.getMembership();
                Corporate corporate = membership.getCorporate();

                PublicUserMembership userMembership = new PublicUserMembership();
                userMembership.setMembership(membership);
                userMembership.setPublicUser(publicUser);
                userMembership.setDateTime(LocalDateTime.now());
                userMembership.setExpireDateTime(membership.getStartDateTime().plusDays(membership.getDuration()));
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setListedPrice(membership.getPrice());
                userMembership.setPaidAmount(BigDecimal.ZERO);
                userMembership.setListedPrice(membership.getPrice());
                userMembership.setRemainingSlots(membership.getSlotCount());
                userMemberships.add(userMembership);

                corporates.add(corporate);
            }

            PublicUser finalPublicUser = publicUser;
            List<CorporatePublicUser> corporatePublicUsers = corporates.stream().map(corporate -> new CorporatePublicUser(corporate, finalPublicUser)).collect(Collectors.toList());

            if (userMemberships.size() > 0) publicUserMembershipRepository.saveAll(userMemberships);
            if (corporatePublicUsers.size() > 0) corporatePublicUserRepository.saveAll(corporatePublicUsers);
            corporateMembershipTempPublicUserRepository.deleteAll(corporateMembershipTempPublicUsers);
        }
    }

    /**
     * this can use to check publicUser details before register and validate social token.
     * if the data is valid, then sends an OTP for the given number and saves it.
     *
     * @param publicUserRegisterDTO the publicUser details before registration.
     */
    @Override
    public void checkBeforeSocialRegAndOtp(PublicUserRegisterDTO publicUserRegisterDTO) {
        publicUserRegisterDTO.setPassword(String.valueOf(UUID.randomUUID()));
        checkUserSocialDetails(publicUserRegisterDTO);
    }

    /**
     * This will validate the publicUser social auth for login.
     *
     * @param publicUserDTO the publicUser social token and publicUser social id with provider.
     * @throws CustomServiceException if publicUser does mot exist or social auth is failed.
     */
    @Override
    public JsonNode verifySocialLogin(PublicUserDTO publicUserDTO) {
        if (publicUserDTO.getAuthType() == null || publicUserDTO.getAuthType() == AuthType.MOBILE)
            throw new CustomServiceException(INVALID_AUTH_PROVIDER);
        String appleId = authTokenValidator.validate(publicUserDTO.getAuthType(), publicUserDTO.getSocialMediaId(), publicUserDTO.getSocialMediaToken());

        if (publicUserDTO.getAuthType() == AuthType.APPLE) publicUserDTO.setSocialMediaId(appleId);
        PublicUser publicUser = publicUserRepository.
                findBySocialMediaId(getUserIdWithSocial(publicUserDTO.getAuthType(), publicUserDTO.getSocialMediaId()));
        if (publicUser == null) throw new CustomServiceException(NO_USER_FOUND);

        return apiHandler.getAuthResponse(publicUser.getMobile(), publicUser.getSocialMediaId(), PUBLIC_SOCIAL_CLIENT_ID);
    }

    /**
     * @param publicUserDTO the public publicUser id and file data with name.
     * @return the saved file host url.
     * @throws CustomServiceException if failed to save file.
     */
    @Override
    public String updateProfilePhoto(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        String imageUrl = fileHandler.saveBase64File(publicUserDTO.getImageBase64(), publicUser.getFirstName().replaceAll(REGEX, "") + UUID.randomUUID(), PUBLIC_USER_FOLDER);
        publicUser.setImage(imageUrl);
        publicUserRepository.save(publicUser);
        return imageUrl;
    }

    /**
     * This can use to update a publicUser's password
     *
     * @param passwordChangeDTO the object should contain the publicUser's mobile no with old and new passwords.
     * @throws CustomServiceException if failed to proceed.
     */
    @Override
    public void changePassword(PublicUserPasswordChangeDTO passwordChangeDTO) {
        PublicUser publicUser = publicUserRepository.findByMobile(passwordChangeDTO.getMobile());
        if (publicUser == null) throw new CustomServiceException(NO_PUBLIC_USER_FOUND);
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), publicUser.getPassword()))
            throw new CustomServiceException(INVALID_OLD_PASS);
        publicUser.setPassword(passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
        publicUser = publicUserRepository.save(publicUser);
    }

    /**
     * This can use to update a publicUser's forgot password after pin verify
     *
     * @param passwordChangeDTO the object should contain the publicUser's mobile no with old and new passwords otp pin details.
     * @throws CustomServiceException if failed to proceed.
     */
    @Override
    public void changeForgotPassword(UpdateMobileForgotPasswordDTO passwordChangeDTO) {
        passwordChangeDTO.getOtpDetails().setMobile(passwordChangeDTO.getMobile());
        verifyOtp(passwordChangeDTO.getOtpDetails());
        PublicUser publicUser = publicUserRepository.findByMobile(passwordChangeDTO.getMobile());
        if (publicUser == null) throw new CustomServiceException(NO_PUBLIC_USER_FOUND);
        PasswordHandler.checkPasswordValidity(passwordChangeDTO.getPassword());
        publicUser.setPassword(passwordEncoder.encode(passwordChangeDTO.getPassword()));
        publicUser = publicUserRepository.save(publicUser);
    }

    /**
     * This will update the publicUser profile details with given.
     *
     * @param publicUserDTO the public publicUser details.
     * @return the updated details.
     * @throws CustomServiceException if failed to save details.
     */
    @Override
    @Transactional
    public PublicUserDTO updateProfile(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        if (publicUserDTO.getMobile() != null) throw new CustomServiceException("Cannot update mobile");
        if (publicUserDTO.getSocialMediaId() != null) throw new CustomServiceException("Cannot update social id");

        if (publicUserDTO.getVerificationNo() != null && !publicUserDTO.getVerificationNo().trim().equals("")) {
            PublicUser userByVerification = publicUserRepository
                    .findByVerificationNoAndVerificationType(publicUserDTO.getVerificationNo(), publicUserDTO.getVerificationType());
            if (userByVerification != null && userByVerification.getId() != publicUserDTO.getId())
                throw new CustomServiceException(VERIFICATION_ALREADY_EXISTS);
        }

        //validations
        if (publicUserDTO.getHeight() <= 0) throw new CustomServiceException("Invalid height value!");
        if (publicUserDTO.getWeight() <= 0) throw new CustomServiceException("Invalid weight value!");
        if (publicUserDTO.getDateOfBirth() != null && publicUserDTO.getDateOfBirth().compareTo(LocalDate.now()) >= 0)
            throw new CustomServiceException("Invalid Date of Birth!");

        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.typeMap(PublicUserDTO.class, PublicUser.class).addMappings(mapper -> {
            mapper.skip(PublicUser::setEmailVerified);
            mapper.skip(PublicUser::setEmail);
            mapper.skip(PublicUser::setImage);
        });
        modelMapper.map(publicUserDTO, publicUser);
        if (publicUserDTO.getImage() != null && !publicUserDTO.getImage().isEmpty()) {
            if (publicUserDTO.getImage().startsWith("http://") || publicUserDTO.getImage().startsWith("https://")) {
                publicUser.setImage(publicUserDTO.getImage());
            } else {
                String imageUrl = fileHandler.saveBase64File(publicUserDTO.getImage(),
                        publicUser.getFirstName().replaceAll(REGEX, "") + UUID.randomUUID(), PUBLIC_USER_FOLDER);
                publicUser.setImage(imageUrl);
            }
        }
        modelMapper = null;
        publicUserRepository.save(publicUser);
        return publicUserDTO;
    }

    /**
     * This function can use to update a user's name.
     *
     * @param publicUserDTO the user id and name object.
     */
    @Override
    public void updateName(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        if (publicUserDTO.getFirstName() == null) throw new CustomServiceException("First name is required!");
        if (publicUserDTO.getLastName() == null) throw new CustomServiceException("Last name is required!");
        publicUser.setFirstName(publicUserDTO.getFirstName());
        publicUser.setLastName(publicUserDTO.getLastName());
        publicUserRepository.save(publicUser);
    }

    /**
     * This function can use to update a user's date of birth.
     *
     * @param publicUserDTO the user id and date of birth object.
     */
    @Override
    public void updateDob(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        if (publicUserDTO.getDateOfBirth() == null) throw new CustomServiceException("Date of birth is required!");
        if (publicUserDTO.getDateOfBirth() != null && publicUserDTO.getDateOfBirth().compareTo(LocalDate.now()) >= 0)
            throw new CustomServiceException("Invalid Date of Birth!");
        publicUser.setDateOfBirth(publicUserDTO.getDateOfBirth());
        publicUserRepository.save(publicUser);
    }

    /**
     * This function can use to update a user's verification number.
     *
     * @param publicUserDTO the user id and verification number object.
     */
    @Override
    public void updateVerificationNo(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        if (publicUserDTO.getVerificationNo() == null)
            throw new CustomServiceException("Verification number is required!");
        PublicUser userByVerification = publicUserRepository
                .findByVerificationNoAndVerificationType(publicUserDTO.getVerificationNo(), publicUserDTO.getVerificationType());
        if (userByVerification != null && userByVerification.getId() != publicUserDTO.getId())
            throw new CustomServiceException(VERIFICATION_ALREADY_EXISTS);
        publicUser.setVerificationNo(publicUserDTO.getVerificationNo());
        if (publicUserDTO.getVerificationType() != null)
            publicUser.setVerificationType(publicUserDTO.getVerificationType());
        publicUserRepository.save(publicUser);
    }

    /**
     * This function can use to update a user's weight and height.
     *
     * @param publicUserDTO the user id and weight and height object.
     */
    @Override
    public void updateHeightAndWeight(PublicUserDTO publicUserDTO) {

        //validations
        if (publicUserDTO.getHeight() <= 0) throw new CustomServiceException("Invalid height value!");
        if (publicUserDTO.getWeight() <= 0) throw new CustomServiceException("Invalid weight value!");

        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        publicUser.setHeight(publicUserDTO.getHeight());
        publicUser.setWeight(publicUserDTO.getWeight());
        publicUserRepository.save(publicUser);
    }

    /**
     * This function can use to update a user's mobile if OTP matches.
     *
     * @param publicUserDTO the user id and mobile no object.
     */
    @Override
    public void updateUserMobile(PublicUserRegisterDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        publicUserDTO.getOtpDetails().setMobile(publicUserDTO.getMobile());
        verifyOtp(publicUserDTO.getOtpDetails());
        if (publicUserDTO.getMobile() == null) throw new CustomServiceException(MOBILE_REQUIRED);

        PublicUser userByMobile = publicUserRepository.findByMobile(publicUserDTO.getMobile());
        if (userByMobile != null && userByMobile.getId() != publicUserDTO.getId())
            throw new CustomServiceException(MOBILE_ALREADY_EXISTS);

        //check pre-register promo code
        checkPreRegisterPromoCode(publicUser);

//        check corporate
        List<CorporateMembershipTempPublicUser> corporateMembershipTempPublicUsers =
                corporateMembershipTempPublicUserRepository.findCorporateMembershipTempPublicUsersByMobile(publicUserDTO.getMobile());

        if (corporateMembershipTempPublicUsers != null && corporateMembershipTempPublicUsers.size() > 0) {
            List<PublicUserMembership> userMemberships = new ArrayList<>();

            Set<Corporate> corporates = new HashSet<>();

            for (CorporateMembershipTempPublicUser tempPublicUser : corporateMembershipTempPublicUsers) {

                Membership membership = tempPublicUser.getMembership();
                Corporate corporate = membership.getCorporate();

                PublicUserMembership userMembership = new PublicUserMembership();
                userMembership.setMembership(membership);
                userMembership.setPublicUser(publicUser);
                userMembership.setDateTime(LocalDateTime.now());
                userMembership.setExpireDateTime(membership.getStartDateTime().plusDays(membership.getDuration()));
                userMembership.setListedPrice(membership.getPrice());
                userMembership.setStatus(MembershipStatus.BOOKED);
                userMembership.setPaidAmount(BigDecimal.ZERO);
                userMembership.setListedPrice(membership.getPrice());
                userMembership.setRemainingSlots(membership.getSlotCount());
                userMemberships.add(userMembership);

                corporates.add(corporate);
            }

            List<CorporatePublicUser> corporatePublicUsers = corporates.stream().map(
                    corporate -> new CorporatePublicUser(corporate, publicUser)).collect(Collectors.toList());

            if (userMemberships.size() > 0) publicUserMembershipRepository.saveAll(userMemberships);
            if (corporatePublicUsers.size() > 0) corporatePublicUserRepository.saveAll(corporatePublicUsers);
            corporateMembershipTempPublicUserRepository.deleteAll(corporateMembershipTempPublicUsers);
        }

        publicUser.setMobile(publicUserDTO.getMobile());
        publicUserRepository.save(publicUser);
    }

    /**
     * This function can use to update a user's email matches.
     *
     * @param publicUserDTO the user id and email object.
     */
    @Override
    public void updateUserEmail(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        if (publicUserDTO.getEmail() == null) throw new CustomServiceException("Email is required!");
        PublicUser userByEmail = publicUserRepository.findByEmail(publicUserDTO.getEmail());
        if (userByEmail != null && userByEmail.getId() != publicUserDTO.getId())
            throw new CustomServiceException(EMAIL_ALREADY_EXISTS);
        boolean updateEmail = !publicUser.getEmail().equalsIgnoreCase(publicUserDTO.getEmail());
        if (updateEmail) publicUser.setEmailVerified(false);
        publicUser.setEmail(publicUserDTO.getEmail());
        publicUserRepository.save(publicUser);
        if (updateEmail)
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(publicUser, Locale.ENGLISH, publicUserRegVerifyPage));
    }

    @Override
    public PublicUserEmailDTO getEmailVerification(long userId) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        PublicUserEmailDTO publicUserEmailDTO = new PublicUserEmailDTO();
        publicUserEmailDTO.setEmail(publicUser.getEmail());
        publicUserEmailDTO.setEmailVerified(publicUser.isEmailVerified());

        return publicUserEmailDTO;
    }

    /**
     * This function can use to update a user's address details.
     *
     * @param publicUserDTO the user id and adderss details.
     */
    @Override
    public void updateAddresses(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        publicUser.setCity(publicUserDTO.getCity());
        publicUser.setAddressLine1(publicUserDTO.getAddressLine1());
        publicUser.setAddressLine2(publicUserDTO.getAddressLine2());
        publicUser.setProvince(publicUserDTO.getProvince());
        publicUser.setCountry(publicUserDTO.getCountry());
        publicUser.setLatitude(publicUserDTO.getLatitude());
        publicUser.setLongitude(publicUserDTO.getLongitude());
        publicUser.setPostalCode(publicUserDTO.getPostalCode());
        publicUser.setTimeZone(publicUserDTO.getTimeZone());
        publicUserRepository.save(publicUser);
    }

    /**
     * This function can use to update a user's gender.
     *
     * @param publicUserDTO the user id and gender object.
     */
    @Override
    public void updateGender(PublicUserDTO publicUserDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserDTO.getId()).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        if (publicUserDTO.getGender() == null) throw new CustomServiceException("Gender is required!");
        publicUser.setGender(publicUserDTO.getGender());
        publicUserRepository.save(publicUser);
    }

    /**
     * This can use to validate the given publicUser data with the DB.
     *
     * @param publicUserDTO the public publicUser details DTO.
     * @throws CustomServiceException if data already exists.
     */
    @Override
    public void userAlreadyExists(PublicUserDTO publicUserDTO) {
        if (publicUserDTO.getMobile() == null) throw new CustomServiceException(MOBILE_REQUIRED);
        if (publicUserRepository.findByMobile(publicUserDTO.getMobile()) != null)
            throw new CustomServiceException(MOBILE_ALREADY_EXISTS);
        if (publicUserRepository.findByEmail(publicUserDTO.getEmail()) != null)
            throw new CustomServiceException(EMAIL_ALREADY_EXISTS);
        if (publicUserDTO.getSocialMediaId() != null && !publicUserDTO.getSocialMediaId().isEmpty() && publicUserRepository.findBySocialMediaId(
                getUserIdWithSocial(publicUserDTO.getAuthType(), publicUserDTO.getSocialMediaId())) != null)
            throw new CustomServiceException(SOCIAL_ALREADY_EXISTS);
        if (publicUserDTO.getReferralFrom() != null && !publicUserDTO.getReferralFrom().isEmpty() && !publicUserRepository.findByReferralCode(publicUserDTO.getReferralFrom()).isPresent())
            throw new CustomServiceException(INVALID_REFERRAL_CODE);
    }

    /**
     * This can use to get cards of the given customer.
     *
     * @param userId the user id
     * @return the stripe cards list
     */
    @Override
    public List<CardDetailsResponse> getCardsOfUser(long userId) {

        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
//        if ((publicUser.getStripeClientId() == null || "".equals(publicUser.getStripeClientId()))
//            throw new CustomServiceException("No cards for you");
        List<PublicUserCardDetail> userCardDetail = publicUserCardDetailRepository.findAllByStatusNotAndPublicUser(CardStatus.PENDING, publicUser);

        List<CardDetailsResponse> publicUserCardDetails = new ArrayList<>();

        for (PublicUserCardDetail publicUserCardDetail : userCardDetail) {

            if(publicUserCardDetail.getStatus() != CardStatus.DELETE) {

                CardDetailsResponse cardDetailsResponse = new CardDetailsResponse();
                cardDetailsResponse.setId(publicUserCardDetail.getId());
                cardDetailsResponse.setBrand(publicUserCardDetail.getBrand());
                cardDetailsResponse.setIpgType(publicUserCardDetail.getIpgType());
                cardDetailsResponse.setCountry(publicUserCardDetail.getCountry());
                cardDetailsResponse.setLast4(publicUserCardDetail.getLast4());
                cardDetailsResponse.setExpMonth(publicUserCardDetail.getExpMonth());
                cardDetailsResponse.setExpYear(publicUserCardDetail.getExpYear());
                cardDetailsResponse.setPayHerePaymentMethodId(publicUserCardDetail.getPayHerePaymentMethodId());
                cardDetailsResponse.setStripePaymentMethodId(publicUserCardDetail.getStripePaymentMethodId());
                cardDetailsResponse.setStatus(publicUserCardDetail.getStatus());

                publicUserCardDetails.add(cardDetailsResponse);

            }

        }

        return publicUserCardDetails;

    }



    /**
     * This can use to save cards of the given user.
     *
     * @param userId the user id.
     * @return the stripe card intent secret
     */
    @Override
    @Transactional
    public String getSaveCardIntent(long userId) {

        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        SetupIntent setupIntent = null;
        if (publicUser.getStripeClientId() == null)
            setupIntent = stripeService.createCardAndNewCustomer(
                    publicUser.getFirstName() + SPACE + publicUser.getLastName(), publicUser.getEmail());
        else setupIntent = stripeService.createCardForExistingCustomer(publicUser.getStripeClientId());

        if (publicUser.getStripeClientId() == null) {
            publicUser.setStripeClientId(setupIntent.getCustomer());
            publicUserRepository.save(publicUser);
        }

        return setupIntent.getClientSecret();
    }

    /**
     * (payhere)
     * generate preapproval details for save card
     *
     * @param userId
     * @return
     */
    @Override
    public PreApproveResponseDTO generatePreApproveDetails(long userId, String type) {

        try {

            log.info("\n call function :  generatePreApproveDetails");

            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + userId + "-" + new Date().getTime();
            PreApproveResponseDTO preApproveResponseDTO = new PreApproveResponseDTO();
            TempPreApproveDetails tempPreApproveDetails = new TempPreApproveDetails();

            if (type.equals("web")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(notifyUrl + "/web");

            } else if (type.equals("mobile")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(notifyUrl + "/mobile");
            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            preApproveResponseDTO.setOrderId(orderId);
            preApproveResponseDTO.setAmount(amount);
            preApproveResponseDTO.setCurrency("LKR");

            tempPreApproveDetails.setOrderId(orderId);
            tempPreApproveDetails.setCurrency("LKR");
            tempPreApproveDetails.setAmount(amount);
            tempPreApproveDetails.setPublicUser(publicUser);
            tempPreApproveRepository.save(tempPreApproveDetails);

            return preApproveResponseDTO;


        } catch (Exception e) {
            log.error("Error generate preapprove details: {}", e);
            throw e;

        }


    }

    @Override
    public PreApproveResponseDTO generatePreApproveDetails(long userId, String amount, String type) {
        try {

            log.info("\n call function :  generatePreApproveDetails for checkout membership");

            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + userId + "-" + new Date().getTime();
            PreApproveResponseDTO preApproveResponseDTO = new PreApproveResponseDTO();
            TempPreApproveDetails tempPreApproveDetails = new TempPreApproveDetails();

            if (type.equals("web")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(preApproveCheckoutNotifyUrl + "/web");

            } else if (type.equals("mobile")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));

                log.info("generate preapprove details for save and pay");
                log.info("merahantID = "+merahantID);
                log.info("orderId = "+orderId);
                log.info("amount = "+amount);
                log.info("currency = "+"LKR");

                log.info(hash);


                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(preApproveCheckoutNotifyUrl + "/mobile");
            } else {
                throw new CustomServiceException("Invalid Device Type");
            }



            preApproveResponseDTO.setOrderId(orderId);
            preApproveResponseDTO.setAmount(amount);
            preApproveResponseDTO.setCurrency("LKR");

            tempPreApproveDetails.setOrderId(orderId);
            tempPreApproveDetails.setCurrency("LKR");
            tempPreApproveDetails.setAmount(amount);
            tempPreApproveDetails.setPublicUser(publicUser);
            tempPreApproveRepository.save(tempPreApproveDetails);

            return preApproveResponseDTO;


        } catch (Exception e) {
            log.error("Error generate preapprove details for checkout membership: {}", e);
            throw e;

        }
    }

    @Override
    public PreApproveResponseDTO generateMembershipCheckoutParameters(long userId, String amount, String type) {
        try {

            log.info("\n call function :  generateMembershipCheckoutParameters for checkout membership");

            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + userId + "-" + new Date().getTime();
            PreApproveResponseDTO preApproveResponseDTO = new PreApproveResponseDTO();
            TempPreApproveDetails tempPreApproveDetails = new TempPreApproveDetails();

            if (type.equals("web")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(onetimeCheckoutNotifyUrl + "/web");

            } else if (type.equals("mobile")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(onetimeCheckoutNotifyUrl + "/mobile");
            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            preApproveResponseDTO.setOrderId(orderId);
            preApproveResponseDTO.setAmount(amount);
            preApproveResponseDTO.setCurrency("LKR");

            tempPreApproveDetails.setOrderId(orderId);
            tempPreApproveDetails.setCurrency("LKR");
            tempPreApproveDetails.setAmount(amount);
            tempPreApproveDetails.setPublicUser(publicUser);
            tempPreApproveRepository.save(tempPreApproveDetails);

            return preApproveResponseDTO;


        } catch (Exception e) {
            log.error("Error generateMembershipCheckoutParameters: {}", e);
            throw e;

        }
    }

    @Override
    public PreApproveResponseDTO generatePreApproveDetailsForOnlineSessionEnroll(long userId, String amount, String type) {
        try {

            log.info("\n call function :  generatePreApproveDetails for checkout session");

            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + userId + "-" + new Date().getTime();
            PreApproveResponseDTO preApproveResponseDTO = new PreApproveResponseDTO();
            TempPreApproveDetails tempPreApproveDetails = new TempPreApproveDetails();

            if (type.equals("web")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(preApproveCheckoutOnlineSessionNotifyUrl + "/web");

            } else if (type.equals("mobile")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(preApproveCheckoutOnlineSessionNotifyUrl + "/mobile");
            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            preApproveResponseDTO.setOrderId(orderId);
            preApproveResponseDTO.setAmount(amount);
            preApproveResponseDTO.setCurrency("LKR");

            tempPreApproveDetails.setOrderId(orderId);
            tempPreApproveDetails.setCurrency("LKR");
            tempPreApproveDetails.setAmount(amount);
            tempPreApproveDetails.setPublicUser(publicUser);
            tempPreApproveRepository.save(tempPreApproveDetails);

            return preApproveResponseDTO;


        } catch (Exception e) {
            log.error("Error generatePreApproveDetailsForOnlineSessionEnroll: {}", e);
            throw e;

        }
    }

    @Override
    public PreApproveResponseDTO generateOnlineSessionCheckoutDetails(long userId, String amount, String type) {
        try {

            log.info("\n call function :  generateOnlineSessionCheckoutDetails for checkout session");

            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + userId + "-" + new Date().getTime();
            PreApproveResponseDTO preApproveResponseDTO = new PreApproveResponseDTO();

            if (type.equals("web")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));
                preApproveResponseDTO.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(checkoutOnlineSessionNotifyUrl + "/web");

            } else if (type.equals("mobile")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                preApproveResponseDTO.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(checkoutOnlineSessionNotifyUrl + "/mobile");
            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            preApproveResponseDTO.setOrderId(orderId);
            preApproveResponseDTO.setAmount(amount);
            preApproveResponseDTO.setCurrency("LKR");


            return preApproveResponseDTO;


        } catch (Exception e) {
            log.error("Error generateOnlineSessionCheckoutDetails: {}", e);
            throw e;

        }
    }

    @Override
    public PreApproveResponseDTO generatePreApproveDetailsForPhysicalSessionEnroll(long userId, String amount, String type) {
        try {

            log.info("\n call function :  generatePreApproveDetails for checkout session");

            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + userId + "-" + new Date().getTime();
            PreApproveResponseDTO preApproveResponseDTO = new PreApproveResponseDTO();
            TempPreApproveDetails tempPreApproveDetails = new TempPreApproveDetails();

            if (type.equals("web")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(preApproveCheckoutPhysicalSessionNotifyUrl + "/web");

            } else if (type.equals("mobile")) {
                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(preApproveCheckoutPhysicalSessionNotifyUrl + "/mobile");
            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            preApproveResponseDTO.setOrderId(orderId);
            preApproveResponseDTO.setAmount(amount);
            preApproveResponseDTO.setCurrency("LKR");

            tempPreApproveDetails.setOrderId(orderId);
            tempPreApproveDetails.setCurrency("LKR");
            tempPreApproveDetails.setAmount(amount);
            tempPreApproveDetails.setPublicUser(publicUser);
            tempPreApproveRepository.save(tempPreApproveDetails);

            return preApproveResponseDTO;


        } catch (Exception e) {
            log.error("Error generate preapprove details for checkout session: {}", e);
            throw e;

        }
    }

    @Override
    public PreApproveResponseDTO generateCheckoutDetailsForPhysicalSessionEnroll(long userId, String amount, String type) {
        try {

            log.info("\n call function :  generateCheckoutDetailsForPhysicalSessionEnroll for checkout session");

            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + userId + "-" + new Date().getTime();
            PreApproveResponseDTO preApproveResponseDTO = new PreApproveResponseDTO();
            TempPreApproveDetails tempPreApproveDetails = new TempPreApproveDetails();

            if (type.equals("web")) {

                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(checkoutPhysicalSessionNotifyUrl + "/web");

            } else if (type.equals("mobile")) {

                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                preApproveResponseDTO.setHash(hash);
                tempPreApproveDetails.setHash(hash);
                preApproveResponseDTO.setNotifyUrl(checkoutPhysicalSessionNotifyUrl + "/mobile");

            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            preApproveResponseDTO.setOrderId(orderId);
            preApproveResponseDTO.setAmount(amount);
            preApproveResponseDTO.setCurrency("LKR");

            tempPreApproveDetails.setOrderId(orderId);
            tempPreApproveDetails.setCurrency("LKR");
            tempPreApproveDetails.setAmount(amount);
            tempPreApproveDetails.setPublicUser(publicUser);
            tempPreApproveRepository.save(tempPreApproveDetails);

            return preApproveResponseDTO;


        } catch (Exception e) {
            log.error("Error generateCheckoutDetailsForPhysicalSessionEnroll: {}", e);
            throw e;

        }
    }

    @Override
    public String saveCardDetailsByWeb(String card_no, String order_id, String card_expiry, String payhere_amount, String payhere_currency, String card_holder_name, String method, String payment_id, String status_code, String md5sig, String status_message, String customer_token) {

        try {

            log.info("\n call function :  saveCardDetails");

            TempPreApproveDetails tempPreApproveDetails = tempPreApproveRepository.findByOrderId(order_id);

            if (tempPreApproveDetails == null)
                throw new CustomServiceException("Invalid Order ID");

            String hash = CustomGenerator.getMd5(merahantID + order_id + amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretWeb));

            if ((hash.equals(md5sig)) && (status_code.equals("2"))) {

                log.info("hash value::::::::::::::::::::::::::::");
                log.info(md5sig);
                log.info(tempPreApproveDetails.getHash());
                log.info("hash value::::::::::::::::::::::::::::");

                log.info(card_no);
                log.info(order_id);
                log.info(card_expiry);
                log.info(payhere_amount);
                log.info(payhere_currency);
                log.info(payhere_currency);
                log.info(card_holder_name);
                log.info(method);
                log.info(payment_id);
                log.info(status_message);
                log.info(status_code);

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
                publicUserCardDetail.setPublicUser(tempPreApproveDetails.getPublicUser());

                publicUserCardDetailRepository.save(publicUserCardDetail);
                tempPreApproveRepository.delete(tempPreApproveDetails);

                return status_message;

            } else {

                log.info("hash value::::::::::::2::::::::::::::::");
                log.info(md5sig);
                log.info(tempPreApproveDetails.getHash());
                log.info("hash value::::::::::::::2::::::::::::::");

                log.info(card_no);
                log.info(order_id);
                log.info(card_expiry);
                log.info(payhere_amount);
                log.info(payhere_currency);
                log.info(payhere_currency);
                log.info(card_holder_name);
                log.info(method);
                log.info(payment_id);
                log.info(status_message);
                log.info(status_code);
                log.info(status_message);
                log.info(status_code);
                return status_message;

            }


        } catch (Exception e) {
            log.error("Error save card details: {}", e);
            throw e;
        }


    }

    @Override
    public String saveCardDetailsByMobile(String card_no, String order_id, String card_expiry, String payhere_amount, String payhere_currency, String card_holder_name, String method, String payment_id, String status_code, String md5sig, String status_message, String customer_token) {

        try {

            log.info("\n call function :  saveCardDetails");

            TempPreApproveDetails tempPreApproveDetails = tempPreApproveRepository.findByOrderId(order_id);

            if (tempPreApproveDetails == null)
                throw new CustomServiceException("Invalid Order ID");

            String hash = CustomGenerator.getMd5(merahantID + order_id + amount + "LKR" + status_code + CustomGenerator.getMd5(merchantSecretMobile));

            if ((hash.equals(md5sig)) && (status_code.equals("2"))) {


                log.info("hash value::::::::::::::::::::::::::::");
                log.info(md5sig);
                log.info(tempPreApproveDetails.getHash());
                log.info("hash value::::::::::::::::::::::::::::");

                log.info(card_no);
                log.info(order_id);
                log.info(card_expiry);
                log.info(payhere_amount);
                log.info(payhere_currency);
                log.info(payhere_currency);
                log.info(card_holder_name);
                log.info(method);
                log.info(payment_id);
                log.info(status_message);
                log.info(status_code);

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
                publicUserCardDetail.setPublicUser(tempPreApproveDetails.getPublicUser());

                publicUserCardDetailRepository.save(publicUserCardDetail);
                tempPreApproveRepository.delete(tempPreApproveDetails);

                return status_message;

            } else {

                log.info("hash value::::::::::::2::::::::::::::::");
                log.info(md5sig);
                log.info(tempPreApproveDetails.getHash());
                log.info("hash value::::::::::::::2::::::::::::::");

                log.info(card_no);
                log.info(order_id);
                log.info(card_expiry);
                log.info(payhere_amount);
                log.info(payhere_currency);
                log.info(payhere_currency);
                log.info(card_holder_name);
                log.info(method);
                log.info(payment_id);
                log.info(status_message);
                log.info(status_code);
                log.info(status_message);
                log.info(status_code);

                return status_message;

            }


        } catch (Exception e) {
            log.error("Error save card details: {}", e);
            throw e;
        }


    }

//    /**
//     *  This can use to attach a card to local customer by checking if not presented already.
//     * @param publicUser the public user entity
//     * @param paymentIntent the payment intent from stripe
//     */
//    @Async
//    @Override
//    public void handleUserStripeRegOrNot(PublicUser publicUser, PaymentIntent paymentIntent) {
//        if (!paymentIntent.getStatus().equalsIgnoreCase(PAYMENT_SUCCESS)) return;
//        PaymentMethod paymentMethod = stripeService.getPaymentMethodById(paymentIntent.getPaymentMethod());
//        saveCardByPaymentMethod(publicUser, paymentMethod);
//    }

    /**
     * @param setupIntent the setup intent from stripe
     */
//    @Override
//    public void saveCardBySetupIntent(SetupIntent setupIntent) {
//        try {
//            if (!setupIntent.getStatus().equalsIgnoreCase(SETUP_SUCCESS)) return;
//            PaymentMethod paymentMethod = stripeService.getPaymentMethodById(setupIntent.getPaymentMethod());
//
//            PublicUser publicUser = publicUserRepository.findByStripeClientId(setupIntent.getCustomer());
//
//            saveCardByPaymentMethod(publicUser, paymentMethod);
//        } catch (Exception e) {
//            log.error("Error saving card by web-hook: {}", e);
//        }
//    }

    /**
     * @param cardDetailsDTO the card details to update
     * @param token          the user's access token to validate user.
     */
    @Override
    public void updatePaymentCard(CardDetailsResponse cardDetailsDTO, String token) {
        PublicUserCardDetail publicUserCardDetail = publicUserCardDetailRepository
                .findByStripePaymentMethodId(cardDetailsDTO.getStripePaymentMethodId());
        if (publicUserCardDetail == null) throw new CustomServiceException("No card found!");
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserCardDetail.getPublicUser().getId(), token);

        stripeService.updatePaymentMethod(cardDetailsDTO);

        publicUserCardDetail.setExpMonth(cardDetailsDTO.getExpMonth());
        publicUserCardDetail.setExpYear(cardDetailsDTO.getExpYear());
        publicUserCardDetailRepository.save(publicUserCardDetail);
    }

    /**
     * @param paymentMethodId the card method id.
     * @param token           the user's access token to validate user.
     */
    @Override
    public void removePaymentCard(String paymentMethodId, String token) {

        PublicUserCardDetail publicUserCardDetail = publicUserCardDetailRepository.findByStripePaymentMethodIdOrPayHerePaymentMethodId(paymentMethodId, paymentMethodId);

        if (publicUserCardDetail == null) throw new CustomServiceException("No card found!");
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserCardDetail.getPublicUser().getId(), token);

        boolean subscriptionsExist = publicUserPackageSubscriptionRepository.existsPublicUserPackageSubscriptionsByStripePaymentMethodIdAndStatusNot
                (paymentMethodId, ClassPackagePublicUserSubscriptionStatus.INACTIVE);

        if (subscriptionsExist)
            throw new CustomServiceException("You can not delete this card. There are monthly package subscriptions under this card!");


        if (publicUserCardDetail.getIpgType() != IPGType.PAYHERE) {
            stripeService.removePaymentMethod(paymentMethodId);
        }

        publicUserCardDetailRepository.delete(publicUserCardDetail);

    }

    @Override
    public void saveOrRemovePaymentCard(String paymentMethodId, String action, String token) {

        try {
            log.info("\n call function :  saveOrRemovePaymentCard");

            PublicUser publicUser = publicUserRepository.findById(CustomUserAuthenticator.getAuthUserIdFromToken(token)).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

            PublicUserTempCardDetail publicUserTempCardDetail = publicUserTempCardDetailRepository.findByPayHerePaymentMethodId(paymentMethodId);

            if (publicUserTempCardDetail == null)
                throw new CustomServiceException("Payment method not found..!");

            if (action.equals("yes")) {

                PublicUserCardDetail publicUserCardDetail = new PublicUserCardDetail();
                publicUserCardDetail.setLast4(publicUserTempCardDetail.getLast4());
                publicUserCardDetail.setExpMonth(publicUserTempCardDetail.getExpMonth());
                publicUserCardDetail.setExpYear(publicUserTempCardDetail.getExpYear());
                publicUserCardDetail.setPayHereCustomerToken(publicUserTempCardDetail.getPayHereCustomerToken());
                publicUserCardDetail.setMd5sig(publicUserTempCardDetail.getMd5sig());
                publicUserCardDetail.setCardHolderName(publicUserTempCardDetail.getCardHolderName());
                publicUserCardDetail.setIpgType(IPGType.PAYHERE);
                publicUserCardDetail.setBrand(publicUserTempCardDetail.getBrand());
                publicUserCardDetail.setPayHerePaymentMethodId(publicUserTempCardDetail.getPayHerePaymentMethodId());
                publicUserCardDetail.setStatus(CardStatus.ACTIVE);
                publicUserCardDetail.setPublicUser(publicUser);

                publicUserCardDetailRepository.save(publicUserCardDetail);
                publicUserTempCardDetailRepository.delete(publicUserTempCardDetail);

            } else if (action.equals("no")) {

                publicUserTempCardDetailRepository.delete(publicUserTempCardDetail);

            } else {

                throw new CustomServiceException("Invalid action type..!");
            }


        } catch (Exception e) {
            log.error("Error saveOrRemovePaymentCard: {}", e);
            throw e;
        }


    }

    /**
     * @param userId   the public user id
     * @param pageable the pageable request
     * @return the public user notifications page.
     */
    @Override
    @Transactional
    public Page<PublicUserNotificationResponse> getUserNotifications(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PublicUserNotificationResponse> userNotifications;
        HiddenClassType classType = hiddenClassTypeRepository.findHiddenClassTypeByType(ClassMethod.ONLINE);
        if (!classType.isHidden())
            userNotifications = publicUserNotificationRepository.getAllUserNotifications(publicUser, pageable);
        else
            userNotifications = publicUserNotificationRepository.getUserOfflineNotifications(publicUser, pageable);
        mapNotifications(userNotifications);
        publicUser.setLastSeenDateTime(LocalDateTime.now());
        publicUser = publicUserRepository.save(publicUser);
        return userNotifications;
    }

    @Override
    @Transactional
    public Page<PublicUserNotificationResponse> getUserAllNotifications(long userId, Pageable pageable) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        Page<PublicUserNotificationResponse> userNotifications = publicUserNotificationRepository.getAllUserNotifications(publicUser, pageable);
        mapNotifications(userNotifications);
        publicUser.setLastSeenDateTime(LocalDateTime.now());
        publicUser = publicUserRepository.save(publicUser);
        return userNotifications;
    }

    private void mapNotifications(Page<PublicUserNotificationResponse> userNotifications) {
        for (PublicUserNotificationResponse notificationResponse : userNotifications) {
            if (notificationResponse.getType() == NotificationType.SESSION)
                notificationResponse.setClassSession(getSessionDetailsBySessionId(Long.parseLong(notificationResponse.getTypeId())));
            if (notificationResponse.getType() == NotificationType.PHYSICAL_SESSION)
                notificationResponse.setClassSession(getPhysicalSessionDetailsBySessionId(Long.parseLong(notificationResponse.getTypeId())));
            if (notificationResponse.getType() == NotificationType.INSTRUCTOR_PACKAGE) {
                notificationResponse.setInstructorName(getInstructorByPackageId(Long.parseLong(notificationResponse.getTypeId())));
                notificationResponse.setPublicUsername(getInstructorPublicUsernameByPackageId(Long.parseLong(notificationResponse.getTypeId())));
            }
        }
    }

    @Override
    public long getUserNotificationsCount(long userId) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        return publicUserNotificationRepository.getUserNotificationsCount(publicUser);
    }

    @Override
    @Transactional
    public void saveInstructorPackageNotification(InstructorPackage instructorPackage, List<PublicUser> publicUsers, String title, String message) {
        List<String> userTokensMobile = new ArrayList<>();
        List<String> userTokensWeb = new ArrayList<>();
        Instructor instructor = instructorPackage.getInstructor();
        List<PublicUserNotification> notificationList = setMobileAndWebTokensAndGet(NotificationType.INSTRUCTOR_PACKAGE, instructor.getAuthUser().getId(),
                instructor.getAuthUser().getImage(), publicUsers, title, message, userTokensMobile, userTokensWeb);
        try {
            pushNotificationManager.sendPushNotification(null, title, message, userTokensMobile, UserDeviceTypes.PUBLIC_USER_MOBILE);
            pushNotificationManager.sendPushNotification(null, title, message, userTokensWeb, UserDeviceTypes.PUBLIC_USER_WEB);
            notificationList = publicUserNotificationRepository.saveAll(notificationList);
        } catch (Exception e) {
            log.error("Error while sending the notifications");
            e.printStackTrace();
        }
    }

    /**
     * This can use to save a notification for public user for class session related things.
     *
     * @param classSession the class session
     * @param publicUsers  the user entity set.
     * @param title        the notification title.
     * @param message      the notification message.
     */
    @Override
    @Transactional
    public void saveSessionNotification(ClassSession classSession, List<PublicUser> publicUsers, String title, String message, boolean cancelOld) {

        List<PublicUserPushTokenId> publicUserPushTokenIdsMobile = new ArrayList<>();
        List<PublicUserPushTokenId> publicUserPushTokenIdsWeb = new ArrayList<>();
        List<String> userTokensMobile = new ArrayList<>();
        List<String> userTokensWeb = new ArrayList<>();

        List<PublicUserNotification> notificationList = setMobileAndWebTokensAndGet(NotificationType.SESSION, classSession.getId(),
                classSession.getClassParent().getProfileImage(), publicUsers, title, message, userTokensMobile, userTokensWeb);

        List<PublicUserNotification> allScheduledNotifications = publicUserNotificationRepository
                .findAllByTypeAndTypeId(NotificationType.SESSION, String.valueOf(classSession.getId()));

        try {
            String notifyIdMobile = null;
            if (userTokensMobile.size() > 0) {
                notifyIdMobile = sendNotificationScheduled(classSession.getClassParent().getName(), classSession.getDateAndTime(), UserDeviceTypes.PUBLIC_USER_MOBILE,
                        userTokensMobile, allScheduledNotifications, title, message, cancelOld, ClassCategory.ONLINE);
            }
            String notifyIdWeb = null;
            if (userTokensWeb.size() > 0) {
                notifyIdWeb = sendNotificationScheduled(classSession.getClassParent().getName(), classSession.getDateAndTime(), UserDeviceTypes.PUBLIC_USER_WEB,
                        userTokensWeb, allScheduledNotifications, title, message, cancelOld, ClassCategory.ONLINE);
            }
            addPushTokens(publicUserPushTokenIdsMobile, publicUserPushTokenIdsWeb, notificationList, notifyIdMobile, notifyIdWeb);

            notificationList = publicUserNotificationRepository.saveAll(notificationList);
            publicUserPushTokenIdsMobile = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsMobile);
            publicUserPushTokenIdsWeb = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsWeb);
        } catch (Exception e) {
            log.error("Error while sending the notifications");
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void savePhysicalSessionNotification(PhysicalClassSession classSession, List<PublicUser> publicUsers,
                                                String title, String message, boolean cancelOld) {

        List<PublicUserPushTokenId> publicUserPushTokenIdsMobile = new ArrayList<>();
        List<PublicUserPushTokenId> publicUserPushTokenIdsWeb = new ArrayList<>();
        List<String> userTokensMobile = new ArrayList<>();
        List<String> userTokensWeb = new ArrayList<>();

        List<PublicUserNotification> notificationList = setMobileAndWebTokensAndGet(NotificationType.PHYSICAL_SESSION, classSession.getId(),
                classSession.getPhysicalClass().getProfileImage(), publicUsers, title, message, userTokensMobile, userTokensWeb);

        List<PublicUserNotification> allScheduledNotifications = publicUserNotificationRepository
                .findAllByTypeAndTypeId(NotificationType.PHYSICAL_SESSION, String.valueOf(classSession.getId()));

        try {
            String notifyIdMobile = null;
            if (userTokensMobile.size() > 0) {
                notifyIdMobile = sendNotificationScheduled(classSession.getPhysicalClass().getName(), classSession.getDateAndTime(), UserDeviceTypes.PUBLIC_USER_MOBILE,
                        userTokensMobile, allScheduledNotifications, title, message, cancelOld, ClassCategory.PHYSICAL);
            }
            String notifyIdWeb = null;
            if (userTokensWeb.size() > 0) {
                notifyIdWeb = sendNotificationScheduled(classSession.getPhysicalClass().getName(), classSession.getDateAndTime(), UserDeviceTypes.PUBLIC_USER_WEB,
                        userTokensWeb, allScheduledNotifications, title, message, cancelOld, ClassCategory.PHYSICAL);
            }
            addPushTokens(publicUserPushTokenIdsMobile, publicUserPushTokenIdsWeb, notificationList, notifyIdMobile, notifyIdWeb);

            notificationList = publicUserNotificationRepository.saveAll(notificationList);
            publicUserPushTokenIdsMobile = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsMobile);
            publicUserPushTokenIdsWeb = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsWeb);
        } catch (Exception e) {
            log.error("Error while sending the notifications");
            e.printStackTrace();
        }
    }

    @Override
    @Transactional
    public void saveMembershipNotification(PublicUserMembership publicUserMembership, String title, String message, boolean cancelOld) {

        List<PublicUserPushTokenId> publicUserPushTokenIdsMobile = new ArrayList<>();
        List<PublicUserPushTokenId> publicUserPushTokenIdsWeb = new ArrayList<>();
        List<String> userTokensMobile = new ArrayList<>();
        List<String> userTokensWeb = new ArrayList<>();

        String image = null;
        Membership membership = publicUserMembership.getMembership();
        MembershipType membershipType = membership.getType();

        if (membershipType.equals(MembershipType.GYM) || membershipType.equals(MembershipType.GYM_DAY_PASS))
            image = membership.getGymMembership().getGym().getProfileImage();
        else if (membershipType.equals(MembershipType.ONLINE_CLASS)) {
            OnlineClassMembership onlineClassMembership = membership.getOnlineClassMemberships().get(0);
            image = onlineClassMembership.getClassParent().getBusinessProfile().getProfileImage();
        } else if (membershipType.equals(MembershipType.PHYSICAL_CLASS)) {
            PhysicalClassMembership physicalClassMembership = membership.getPhysicalClassMemberships().get(0);
            image = physicalClassMembership.getPhysicalClass().getBusinessProfile().getProfileImage();
        }

        NotificationType notificationType = NotificationType.GYM_MEMBERSHIP;

        if (membershipType.equals(MembershipType.PHYSICAL_CLASS))
            notificationType = NotificationType.PHYSICAL_CLASS_MEMBERSHIP;
        if (membershipType.equals(MembershipType.ONLINE_CLASS))
            notificationType = NotificationType.ONLINE_CLASS_MEMBERSHIP;

        List<PublicUserNotification> notificationList = setMobileAndWebTokensAndGet(
                notificationType,
                publicUserMembership.getId(),
                image,
                Collections.singletonList(publicUserMembership.getPublicUser()),
                title,
                message,
                userTokensMobile,
                userTokensWeb
        );

        List<PublicUserNotification> allScheduledNotifications = publicUserNotificationRepository
                .findAllByTypeAndTypeId(notificationType, String.valueOf(publicUserMembership.getId()));

        try {
            String notifyIdMobile = null;
            if (userTokensMobile.size() > 0) {
                notifyIdMobile = sendMembershipNotification(
                        publicUserMembership,
                        UserDeviceTypes.PUBLIC_USER_MOBILE,
                        userTokensMobile,
                        allScheduledNotifications,
                        title,
                        message,
                        cancelOld
                );
            }
            String notifyIdWeb = null;
            if (userTokensWeb.size() > 0) {
                notifyIdWeb = sendMembershipNotification(
                        publicUserMembership,
                        UserDeviceTypes.PUBLIC_USER_WEB,
                        userTokensWeb,
                        allScheduledNotifications,
                        title,
                        message,
                        cancelOld
                );
            }
            addPushTokens(publicUserPushTokenIdsMobile, publicUserPushTokenIdsWeb, notificationList, notifyIdMobile, notifyIdWeb);

            notificationList = publicUserNotificationRepository.saveAll(notificationList);
            publicUserPushTokenIdsMobile = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsMobile);
            publicUserPushTokenIdsWeb = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsWeb);

        } catch (Exception e) {
            log.error("Error while sending the notifications");
            e.printStackTrace();
        }
    }

    private String sendMembershipNotification(PublicUserMembership userMembership, UserDeviceTypes userDeviceType, List<String> userTokens,
                                              List<PublicUserNotification> allScheduledNotifications,
                                              String title, String message, boolean cancelOld) {
        //schedules push notifications mobile.
        String notifyId = null;

        Membership membership = userMembership.getMembership();

        if (membership.getType().equals(MembershipType.GYM)) {
            if (membership.getDuration() == 1) {
                notifyId = pushNotificationManager.sendPushNotificationSchedled(null, MEMBERSHIP_EXPIRE_TITLE,
                        MEMBERSHIP_EXPIRE_MESSAGE.replace("{the membership}", membership.getName())
                                .replace("{time}", " 1 hour"),
                        userTokens, userDeviceType, userMembership.getExpireDateTime().minusHours(1));
            } else {
                notifyId = pushNotificationManager.sendPushNotificationSchedled(null, MEMBERSHIP_EXPIRE_TITLE,
                        MEMBERSHIP_EXPIRE_MESSAGE.replace("{the membership}", membership.getName())
                                .replace("{time}", " 1 day"),
                        userTokens, userDeviceType, userMembership.getExpireDateTime().minusDays(1));
            }
        }
        //cancels all previous schedules and sends new scheduled alert mobile.
        if (cancelOld) cancelScheduledNotificationList(allScheduledNotifications, userDeviceType);
        pushNotificationManager.sendPushNotification(null, title, message, userTokens, userDeviceType);
        return notifyId;
    }

    @Override
    @Transactional
    public void savePackageNotification(PublicUserPackageSubscription subscription, String title, String message) {

        List<PublicUserPushTokenId> publicUserPushTokenIdsMobile = new ArrayList<>();
        List<PublicUserPushTokenId> publicUserPushTokenIdsWeb = new ArrayList<>();
        List<String> userTokensMobile = new ArrayList<>();
        List<String> userTokensWeb = new ArrayList<>();

        NotificationType notificationType = NotificationType.SUBSCRIPTION_PACKAGE;

        List<PublicUserNotification> notificationList = setMobileAndWebTokensAndGet(
                notificationType,
                subscription.getId(),
                null,
                Collections.singletonList(subscription.getPublicUser()),
                title,
                message,
                userTokensMobile,
                userTokensWeb
        );

        try {
            String notifyIdMobile = null;
            if (userTokensMobile.size() > 0) {
                pushNotificationManager.sendPushNotification(null, title, message, userTokensMobile, UserDeviceTypes.PUBLIC_USER_MOBILE);
            }
            String notifyIdWeb = null;
            if (userTokensWeb.size() > 0) {
                pushNotificationManager.sendPushNotification(null, title, message, userTokensWeb, UserDeviceTypes.PUBLIC_USER_WEB);
            }
            addPushTokens(publicUserPushTokenIdsMobile, publicUserPushTokenIdsWeb, notificationList, notifyIdMobile, notifyIdWeb);

            notificationList = publicUserNotificationRepository.saveAll(notificationList);
            publicUserPushTokenIdsMobile = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsMobile);
            publicUserPushTokenIdsWeb = publicUserPushTokenIdRepository.saveAll(publicUserPushTokenIdsWeb);

        } catch (Exception e) {
            log.error("Error while sending the notifications");
            e.printStackTrace();
        }
    }

    private void addPushTokens(List<PublicUserPushTokenId> publicUserPushTokenIdsMobile,
                               List<PublicUserPushTokenId> publicUserPushTokenIdsWeb,
                               List<PublicUserNotification> notificationList,
                               String notifyIdMobile,
                               String notifyIdWeb) {
        for (PublicUserNotification publicUserNotification : notificationList) {
            if (notifyIdMobile != null && !notifyIdMobile.isEmpty())
                publicUserPushTokenIdsMobile.add(new PublicUserPushTokenId(0, notifyIdMobile, UserDeviceTypes.PUBLIC_USER_MOBILE, publicUserNotification));
            if (notifyIdWeb != null && !notifyIdWeb.isEmpty())
                publicUserPushTokenIdsWeb.add(new PublicUserPushTokenId(0, notifyIdWeb, UserDeviceTypes.PUBLIC_USER_WEB, publicUserNotification));
        }

    }

    @Async
    @Override
    public void cancelScheduledNotificationList(List<PublicUserNotification> allScheduledNotifications, UserDeviceTypes userDeviceType) {
        if (allScheduledNotifications != null) {
            for (PublicUserNotification publicUserNotification : allScheduledNotifications) {
                List<PublicUserPushTokenId> publicUserPushTokenIds = publicUserNotification.getPublicUserPushTokenIds();
                if (publicUserPushTokenIds != null) {
                    for (PublicUserPushTokenId publicUserPushTokenId : publicUserPushTokenIds) {
                        if (publicUserPushTokenId.getDeviceType() == userDeviceType) {
                            pushNotificationManager.cancelScheduledNotification(
                                    publicUserPushTokenId.getNotificationPushId(), userDeviceType);
                        }
                    }
                }
            }
        }
    }

    /**
     * @param publicUser the public user entity
     * @param timeZone   the time zone of the user currently.
     */
    @Override
    public void saveUserTimeZone(PublicUser publicUser, String timeZone) {
        if (timeZone != null) {
            // any validations for time zone...
            log.info("Save user time zone id-{} mobile-{} timezone-{}", publicUser.getId(), publicUser.getMobile(), timeZone);
            publicUser.setTimeZone(timeZone);
            publicUserRepository.save(publicUser);
        }
    }

    @Override
    public void updateUserTimeZone() {
        List<PublicUser> users = publicUserRepository.findPublicUsersByTimeZone(null);
        List<PublicUser> updatedUsers = new ArrayList<>();
        for (PublicUser user : users) {
            if (user.getTimeZone() == null) {
                if (user.getCountry() != null) {
                    if (user.getCountry().equals("Sri Lanka")) {
                        user.setTimeZone("GMT+0530");
                        updatedUsers.add(user);
                    }
                }
            }
        }
        publicUserRepository.saveAll(updatedUsers);
    }

    /**
     * @param notificationTokenDTO the public user device's notification token details dto.
     * @throws CustomServiceException if an error occurred.
     */
    @Override
    @Transactional
    public void addWebPushTokenForUser(NotificationTokenDTO notificationTokenDTO) {
        notificationTokenDTO.setDeviceMac(DeviceType.WEB + "_" + notificationTokenDTO.getUserId());
        PublicUserPushToken publicUserPushToken = publicUserPushTokenRepository.findByToken(notificationTokenDTO.getToken());
        if (publicUserPushToken != null && notificationTokenDTO.getUserId() != publicUserPushToken.getPublicUser().getId())
            publicUserPushTokenRepository.delete(publicUserPushToken);
        addPushTokenForUser(notificationTokenDTO);
    }

    /**
     * @param notificationTokenDTO the public user device's notification token details dto.
     * @throws CustomServiceException if an error occurred.
     */
    @Override
    @Transactional
    public void addPushTokenForUser(NotificationTokenDTO notificationTokenDTO) {
        if (notificationTokenDTO.getToken() != null && !notificationTokenDTO.getToken().isEmpty()) {

            PublicUser publicUser = publicUserRepository.findById(notificationTokenDTO.getUserId())
                    .orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

            PublicUserPushToken publicUserPushToken =
                    publicUserPushTokenRepository.findByDeviceMacAndPublicUser(notificationTokenDTO.getDeviceMac(), publicUser);
            String previousToken = null;

            if (publicUserPushToken != null) {
                previousToken = publicUserPushToken.getToken();
                publicUserPushToken.setToken(notificationTokenDTO.getToken());
            } else {
                publicUserPushToken = publicUserPushTokenRepository.findByToken(notificationTokenDTO.getToken());
                if (publicUserPushToken != null)
                    publicUserPushToken.setDeviceMac(notificationTokenDTO.getDeviceMac());
                else
                    publicUserPushToken = modelMapper.map(notificationTokenDTO, PublicUserPushToken.class);
            }
            publicUserPushToken.setPublicUser(publicUser);

            publicUserPushToken = publicUserPushTokenRepository.save(publicUserPushToken);

            publicUser.setLastLoginSuccessful(true);
            publicUserRepository.save(publicUser);
            log.info("Public user(" + publicUser.getId() + ") Login is Successful");

            if (previousToken != null && !previousToken.equals(notificationTokenDTO.getToken())) {
                // delete previous token from one signal...
            }
        }
    }


    private List<PublicUserNotification> setMobileAndWebTokensAndGet(NotificationType type, long id, String image, List<PublicUser> publicUsers,
                                                                     String title, String message, List<String> userTokensMobile, List<String> userTokensWeb) {

        List<PublicUserNotification> notificationList = new ArrayList<>();
        for (PublicUser publicUser : publicUsers) {
            PublicUserNotification notification = new PublicUserNotification();
            notification.setPublicUser(publicUser);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setTypeId(String.valueOf(id));
            notification.setImage(image);
            notificationList.add(notification);

            for (PublicUserPushToken publicUserPushToken : publicUser.getPublicUserPushTokens()) {
                if (publicUserPushToken.getDeviceType() == DeviceType.WEB)
                    userTokensWeb.add(publicUserPushToken.getToken());
                else userTokensMobile.add(publicUserPushToken.getToken());
            }
        }
        return notificationList;
    }

    private String sendNotificationScheduled(String className, LocalDateTime dateTime, UserDeviceTypes userDeviceType, List<String> userTokens,
                                             List<PublicUserNotification> allScheduledNotifications,
                                             String title, String message, boolean cancelOld, ClassCategory category) {
        //schedules push notifications mobile.
        String startBeforeMessage = OFFLINE_SESSION_START_BEFORE_MESSAGE_PUBLIC;
        if (category.equals(ClassCategory.ONLINE)) {
            startBeforeMessage = ONLINE_SESSION_START_BEFORE_MESSAGE_PUBLIC;
        }
        String notifyId = pushNotificationManager.sendPushNotificationSchedled(null, SESSION_START_BEFORE_TITLE,
                startBeforeMessage.replace("{className}", className)
                        .replace("{minutes}", String.valueOf(SESSION_START_BEFORE_MINUTES)),
                userTokens, userDeviceType, dateTime.minusMinutes(SESSION_START_BEFORE_MINUTES));
        //cancels all previous schedules and sends new scheduled alert mobile.
        if (cancelOld) cancelScheduledNotificationList(allScheduledNotifications, userDeviceType);
        pushNotificationManager.sendPushNotification(null, title, message, userTokens, userDeviceType);
        return notifyId;
    }

    private void saveInviteAFriendDiscounts(PublicUserRegisterDTO publicUserRegisterDTO, PublicUser publicUser) {
        if (publicUserRegisterDTO.getReferralFrom() != null && !publicUserRegisterDTO.getReferralFrom().isEmpty()) {
            Optional<PublicUser> optionalReferralFrom = publicUserRepository.findByReferralCode(publicUserRegisterDTO.getReferralFrom());
            if (optionalReferralFrom.isPresent()) {
                PublicUserDiscounts discounts = new PublicUserDiscounts();
                discounts.setCategory(DiscountCategory.SESSION_INVITED_FRIEND);
                discounts.setDescription("SignUp Discount");
                discounts.setExpDate(LocalDateTime.now().plusMonths(3));
                discounts.setMaxDiscount(INVITE_A_FRIEND_DISCOUNT);
                discounts.setRefNo(publicUserRegisterDTO.getReferralFrom());
                discounts.setPercentage(100);
                discounts.setPublicUser(publicUser);

                PublicUserDiscountsHistory discountsHistory = modelMapper.map(discounts, PublicUserDiscountsHistory.class);

                PublicUserDiscounts referralFromDiscount = new PublicUserDiscounts();
                referralFromDiscount.setCategory(DiscountCategory.SESSION_INVITED_FRIEND);
                referralFromDiscount.setDescription("Referral Discount");
                referralFromDiscount.setExpDate(LocalDateTime.now().plusMonths(3));
                referralFromDiscount.setMaxDiscount(INVITE_A_FRIEND_DISCOUNT);
                referralFromDiscount.setRefNo(publicUserRegisterDTO.getReferralFrom());
                referralFromDiscount.setPercentage(50);
                referralFromDiscount.setPublicUser(optionalReferralFrom.get());
//                referralFromDiscount.setRefNo(publicUser.getReferralCode());

                PublicUserDiscountsHistory referralDiscountsHistory = modelMapper.map(referralFromDiscount, PublicUserDiscountsHistory.class);

                List<PublicUserDiscounts> userDiscounts = new ArrayList<>();
                userDiscounts.add(discounts);
                userDiscounts.add(referralFromDiscount);
                List<PublicUserDiscountsHistory> discountsHistories = new ArrayList<>();
                discountsHistories.add(discountsHistory);
                discountsHistories.add(referralDiscountsHistory);

                userDiscounts = publicUserDiscountRepository.saveAll(userDiscounts);
                discountsHistories = publicUserDiscountHistoryRepository.saveAll(discountsHistories);
            }
        }
    }

    private ClassSessionBookedResponse getSessionDetailsBySessionId(long sessionId) {
        Optional<ClassSession> optionalSession = classSessionRepository.findById(sessionId);
        if (!optionalSession.isPresent()) return null;
        ClassSession classSession = optionalSession.get();
        ClassSessionBookedResponse sessionBookedResponse = modelMapper
                .map(classSession, ClassSessionBookedResponse.class);
        sessionBookedResponse.setEndDateAndTime(classSession.getDateAndTime().plusMinutes(classSession.getDuration()));
        sessionBookedResponse.setImages(classSession.getClassSessionImages().stream().map(ClassSessionImage::getUrl).collect(Collectors.toList()));
        return sessionBookedResponse;
    }

    private ClassSessionBookedResponse getPhysicalSessionDetailsBySessionId(long sessionId) {
        Optional<PhysicalClassSession> optionalSession = physicalClassSessionRepository.findById(sessionId);
        if (!optionalSession.isPresent()) return null;
        PhysicalClassSession classSession = optionalSession.get();
        ClassSessionBookedResponse sessionBookedResponse = modelMapper.map(classSession, ClassSessionBookedResponse.class);
        sessionBookedResponse.setEndDateAndTime(classSession.getDateAndTime().plusMinutes(classSession.getDuration()));
        sessionBookedResponse.setImages(classSession.getPhysicalClass().getPhysicalClassImages().stream().map(PhysicalClassImage::getUrl).collect(Collectors.toList()));
        return sessionBookedResponse;
    }

    private String getInstructorByPackageId(long packageId) {
        Optional<AuthUser> authUserOptional = authUserRepository.findById(packageId);
        if (!authUserOptional.isPresent()) return null;
        AuthUser authUser = authUserOptional.get();
        return authUser.getFirstName() + " " + authUser.getLastName();
    }

    private String getInstructorPublicUsernameByPackageId(long packageId) {
        Optional<AuthUser> authUserOptional = authUserRepository.findById(packageId);
        if (!authUserOptional.isPresent()) return null;
        AuthUser authUser = authUserOptional.get();
        return authUser.getPublicUsername();
    }

    /**
     * @param publicUser    the public user entity.
     * @param paymentMethod the setup intent object from stripe.
     */
    private void saveCardByPaymentMethod(PublicUser publicUser, PaymentMethod paymentMethod) {
        log.info("\nSave user payment method: {}", paymentMethod.getId());
        PaymentMethod.Card card = paymentMethod.getCard();
        PublicUserCardDetail saveCardObject = modelMapper.map(card, PublicUserCardDetail.class);
        saveCardObject.setPublicUser(publicUser);
        saveCardObject.setStripePaymentMethodId(paymentMethod.getId());
        saveCardObject.setStatus(CardStatus.ACTIVE);
        try {
            publicUserCardDetailRepository.save(saveCardObject);
            if (publicUser.getStripeClientId() == null) {
                publicUser.setStripeClientId(paymentMethod.getCustomer());
                publicUserRepository.save(publicUser);
            }
        } catch (ConstraintViolationException | DataIntegrityViolationException e) {
//            stripeService.removePaymentMethod(paymentMethod.getId());
            throw new CustomServiceException("Card is already added!");
        }

    }

    /**
     * This can use to get a mapped PublicUser entity for creating a public publicUser account.
     *
     * @param publicUserRegisterDTO the publicUser details for registration.
     * @param authType              the auth type to register.
     * @return PublicUser entity
     */
    private PublicUser getPublicUserForRegister(PublicUserRegisterDTO publicUserRegisterDTO, AuthType authType) {
        PublicUser publicUser = modelMapper.map(publicUserRegisterDTO, PublicUser.class);
        publicUser.setAuthType(authType);
        publicUser.setStatus(UserStatus.ACTIVE);
        publicUser.setPassword(passwordEncoder.encode(publicUserRegisterDTO.getPassword()));
        publicUser.setReferralCode(customGenerator.generateInviteCode(publicUserRegisterDTO.getFirstName(), publicUserRepository));
        publicUser.setReferralFrom(publicUserRegisterDTO.getReferralFrom());
        return publicUser;
    }

    /**
     * This can use to check publicUser details and social ids for conflicts or invalid data.
     *
     * @param publicUserRegisterDTO the publicUser details to check.
     * @throws CustomServiceException if some data is not acceptable.
     */
    private void checkUserSocialDetails(PublicUserRegisterDTO publicUserRegisterDTO) {
        if (publicUserRegisterDTO.getAuthType() == null || publicUserRegisterDTO.getAuthType() == AuthType.MOBILE)
            throw new CustomServiceException(INVALID_AUTH_PROVIDER);
        String appleId = authTokenValidator.validate(publicUserRegisterDTO.getAuthType(), publicUserRegisterDTO.getSocialMediaId(),
                publicUserRegisterDTO.getSocialMediaToken());
        if (publicUserRegisterDTO.getAuthType() == AuthType.APPLE) publicUserRegisterDTO.setSocialMediaId(appleId);
        userAlreadyExists(publicUserRegisterDTO);
    }

    /**
     * This can use to generate a unique publicUser id among all the social media auth providers.
     *
     * @param socialMediaType the social media provider.
     * @param socialId        the social media publicUser id.
     * @return the publicUser id with type prefix.
     */
    @Override
    public String getUserIdWithSocial(AuthType socialMediaType, String socialId) {
        return (socialMediaType + "_" + socialId);
    }


    /**
     * This can use to save a mail confirmation token for a publicUser.
     *
     * @param publicUser the auth publicUser
     * @param token      the token to save.
     */
    @Override
    public void createVerificationToken(PublicUser publicUser, String token) {
        PublicUserEmailVerificationToken verificationToken = new PublicUserEmailVerificationToken();
        verificationToken.setToken(token);
        verificationToken.setPublicUser(publicUser);
        verificationToken.setExpireDateTime(LocalDateTime.now().plusDays(1));
        publicUserEmailVerificationTokenRepository.save(verificationToken);
    }

    /**
     * This can use to validate a mail verification token of public user.
     *
     * @param token the token to verify.
     * @return the HttpResponse code for validation.
     */
    @Override
    @Transactional
    public HttpStatus checkVerificationToken(String token) {

        PublicUserEmailVerificationToken verificationToken = publicUserEmailVerificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return HttpStatus.NOT_FOUND;
        }

        PublicUser publicUser = verificationToken.getPublicUser();
        if (verificationToken.getExpireDateTime().isBefore(LocalDateTime.now())) {
            return HttpStatus.GONE;
        }

        publicUser.setEmailVerified(true);
        publicUserRepository.save(publicUser);
        return HttpStatus.OK;
    }

    /**
     * @param existingToken the previously sent token.
     */
    @Override
    public void resendEmailToken(String existingToken) {
        PublicUserEmailVerificationToken verificationToken = publicUserEmailVerificationTokenRepository.findByToken(existingToken);
        if (verificationToken == null) throw new CustomServiceException("No token found!");

        PublicUser publicUser = verificationToken.getPublicUser();
        if (publicUser.isEmailVerified()) throw new CustomServiceException("You have already verified your email");
        String token = UUID.randomUUID().toString();

        verificationToken.setToken(token);
        verificationToken.setExpireDateTime(LocalDateTime.now().plusDays(1));
        publicUserEmailVerificationTokenRepository.save(verificationToken);

        String recipientAddress = publicUser.getEmail();
        String subject = "Re-send email verification token";
        String confirmUrl = customGenerator.getPageUrlWithToken(publicUserRegVerifyPage, token);

        emailSender.sendHtmlEmail(Collections.singletonList(recipientAddress), subject,
                emailSender.getVerifyTokenMailBody(confirmUrl), null, null);
    }

    /**
     * This can use to get payment intent from user and, if user is not a stripe user, then he will be one after this.
     *
     * @param publicUser      the public user to get and (if true) set payment intent.
     * @param amount          the amount to pay
     * @param paymentMethodId the token from card details.
     * @return the payment intent.
     */
    @Override
    public PaymentIntent getPaymentIntentByPublicUser(PublicUser publicUser, BigDecimal amount, String paymentMethodId) {
        if (paymentMethodId == null) {
            return stripeService.chargeAmount(amount);
        } else {
            return stripeService.chargeExistingCustomer(publicUser.getStripeClientId(), paymentMethodId, amount);
        }
    }

    @Override
    public GeneratedHashValueDetailsDTO getPayherePaymentDetailsByPublicUser(PublicUser publicUser, BigDecimal amount, String paymentMethodId, String type) {

//        if (paymentMethodId == null) {
//            return stripeService.chargeAmount(amount);
//        } else {
//            return stripeService.chargeExistingCustomer(publicUser.getStripeClientId(), paymentMethodId, amount);
//        }

        try {

            log.info("\n call function :  getPayherePaymentDetailsByPublicUser");

            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + publicUser.getId() + "-" + new Date().getTime();

            GeneratedHashValueDetailsDTO generatedHashValueDetailsDTO = new GeneratedHashValueDetailsDTO();

            JsonNode jsonNode = authTokenGenerator.getAuthResponse();
            generatedHashValueDetailsDTO.setAccessToken(jsonNode.get("access_token").asText());

            PublicUserCardDetail publicUserCardDetail = publicUserCardDetailRepository.findByPayHerePaymentMethodId(paymentMethodId);

            if (publicUserCardDetail == null)
                throw new CustomServiceException("Invalid Card Details !");

            if (type.equals("web")) {

                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));

                generatedHashValueDetailsDTO.setHash(hash);
                generatedHashValueDetailsDTO.setNotifyUrl(checkoutNotifyUrl + "/web");

            } else if (type.equals("mobile")) {

                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                generatedHashValueDetailsDTO.setHash(hash);
                generatedHashValueDetailsDTO.setNotifyUrl(checkoutNotifyUrl + "/mobile");

            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            generatedHashValueDetailsDTO.setAmount(amount.toString());
            generatedHashValueDetailsDTO.setOrderId(orderId);
            generatedHashValueDetailsDTO.setCustomerToken(publicUserCardDetail.getPayHereCustomerToken());
            generatedHashValueDetailsDTO.setCurrency("LKR");

            return generatedHashValueDetailsDTO;


        } catch (Exception e) {
            log.error("Error getPayherePaymentDetailsByPublicUser : {}", e);
            throw e;

        }


    }

    @Override
    public GeneratedHashValueDetailsDTO getSessionEnrollPayherePaymentDetailsByPublicUser(PublicUser publicUser, BigDecimal amount, String paymentMethodId, String type) {
        try {

            log.info("\n call function :  getSessionEnrollPayherePaymentDetailsByPublicUser");

            UUID uuid = UUID.randomUUID();
            String orderId = uuid + "-" + publicUser.getId() + "-" + new Date().getTime();

            GeneratedHashValueDetailsDTO generatedHashValueDetailsDTO = new GeneratedHashValueDetailsDTO();

            JsonNode jsonNode = authTokenGenerator.getAuthResponse();
            generatedHashValueDetailsDTO.setAccessToken(jsonNode.get("access_token").asText());

            PublicUserCardDetail publicUserCardDetail = publicUserCardDetailRepository.findByPayHerePaymentMethodId(paymentMethodId);

            if (publicUserCardDetail == null)
                throw new CustomServiceException("Invalid Card Details !");

            if (type.equals("web")) {

                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretWeb));

                generatedHashValueDetailsDTO.setHash(hash);
                generatedHashValueDetailsDTO.setNotifyUrl(checkoutSessionNotifyUrl + "/web");

            } else if (type.equals("mobile")) {

                String hash = CustomGenerator.getMd5(merahantID + orderId + amount + "LKR" + CustomGenerator.getMd5(merchantSecretMobile));
                generatedHashValueDetailsDTO.setHash(hash);
                generatedHashValueDetailsDTO.setNotifyUrl(checkoutSessionNotifyUrl + "/mobile");

            } else {
                throw new CustomServiceException("Invalid Device Type");
            }

            generatedHashValueDetailsDTO.setAmount(amount.toString());
            generatedHashValueDetailsDTO.setOrderId(orderId);
            generatedHashValueDetailsDTO.setCustomerToken(publicUserCardDetail.getPayHereCustomerToken());
            generatedHashValueDetailsDTO.setCurrency("LKR");

            return generatedHashValueDetailsDTO;


        } catch (Exception e) {
            log.error("Error getSessionEnrollPayherePaymentDetailsByPublicUser : {}", e);
            throw e;

        }
    }

    @Override
    public List<PublicUserDiscountDTO> getDiscountsForUser(long id) {
        PublicUser publicUser = publicUserRepository.findById(id).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        List<PublicUserDiscounts> discounts = publicUserDiscountRepository.findAllByPublicUserAndExpDateAfterOrderByExpDateAsc(publicUser, LocalDateTime.now());
        return discounts.stream().map(this::getSingleDiscountDTO).collect(Collectors.toList());
    }

    private PublicUserDiscountDTO getSingleDiscountDTO(PublicUserDiscounts discount) {
        return new PublicUserDiscountDTO(discount.getId(), discount.getDescription(), discount.getMaxDiscount(), discount.getPercentage(), discount.getExpDate(), discount.getCategory(), discount.getRefNo(), discount.getUsageLimit());
    }

    @Override
    public void sendCallNotification(PublicUser publicUser, SessionZoomDetails zoomDetails) {

        List<String> userTokensMobile = new ArrayList<>();
        List<String> userTokensWeb = new ArrayList<>();

        for (PublicUserPushToken userPushToken : publicUser.getPublicUserPushTokens()) {
            if (userPushToken.getDeviceType() == DeviceType.WEB) userTokensWeb.add(userPushToken.getToken());
            else userTokensMobile.add(userPushToken.getToken());
        }

        zoomDetails.setZoomSignatureJwt(zoomTokenGenerator.generateSignature(String.valueOf(zoomDetails.getZoomMeetingId()), ROLE_JOINEE));

        String message = zoomDetails.getCoachName() + " is calling you...";
        String title = "Call from " + zoomDetails.getCoachName();
        if (userTokensMobile.size() > 0) {
            pushNotificationManager.sendPushNotification(zoomDetails, title, message, userTokensMobile, UserDeviceTypes.PUBLIC_USER_MOBILE);
        }

        if (userTokensMobile.size() > 0) {
            pushNotificationManager.sendPushNotification(zoomDetails, title, message, userTokensWeb, UserDeviceTypes.PUBLIC_USER_WEB);
        }
    }

    @Override
    public void sendSessionStartNotification(List<ClassSessionEnroll> classSessionEnrolls, ClassSession classSession) {

        for (ClassSessionEnroll classSessionEnroll : classSessionEnrolls) {

            PublicUser publicUser = classSessionEnroll.getPublicUser();

            List<String> userTokensMobile = new ArrayList<>();
            List<String> userTokensWeb = new ArrayList<>();

            for (PublicUserPushToken userPushToken : publicUser.getPublicUserPushTokens()) {
                if (userPushToken.getDeviceType() == DeviceType.WEB) {
                    userTokensWeb.add(userPushToken.getToken());
                } else {
                    userTokensMobile.add(userPushToken.getToken());
                }
            }

            AuthUser authUser = classSession.getTrainer().getAuthUser();

            String title = "Class start now";
            String message = String.format("%s of %s now", classSession.getName(), authUser.getFirstName() + " " + authUser.getLastName());

            if (userTokensMobile.size() > 0) {
                pushNotificationManager.sendPushNotification(
                        null,
                        title,
                        message,
                        userTokensMobile,
                        UserDeviceTypes.PUBLIC_USER_MOBILE
                );
            }

            if (userTokensMobile.size() > 0) {
                pushNotificationManager.sendPushNotification(
                        null,
                        title,
                        message,
                        userTokensWeb,
                        UserDeviceTypes.PUBLIC_USER_WEB
                );
            }

            publicUserNotificationRepository.save(new PublicUserNotification(
                    title,
                    message,
                    NotificationType.SESSION,
                    String.valueOf(classSession.getId()),
                    classSession.getClassParent().getProfileImage(),
                    publicUser
            ));
        }

    }

    @Override
    public String getCountryOfUserFromToken(String token) {
        try {
            long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
            PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
            log.info("Getting country from user id: {}\tcountry: {}", userId, publicUser.getCountry());
            return publicUser.getCountry();
        } catch (Exception e) {
            log.error("Error getting country from user id: {} ", e);
            return null;
        }
    }

    @Override
    public void sendSupportEmail(SupportEmailDTO supportEmailDTO, long userId) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        if (supportEmailDTO.getTitle() == null || supportEmailDTO.getTitle().isEmpty())
            supportEmailDTO.setTitle(SUPPORT_MAIL_TITLE);
        /*Updates the message with user details*/
        supportEmailDTO.setMessage(appendUserToMessage(supportEmailDTO.getMessage(), publicUser));

        emailSender.sendEmail(Collections.singletonList(supportMail), supportEmailDTO.getTitle(), supportEmailDTO.getMessage());
    }

    /**
     * @param message    the message from user
     * @param publicUser the user details
     * @return the message updated with user details
     */
    private String appendUserToMessage(String message, PublicUser publicUser) {
        return "".concat(message).concat(String.format("\n\nUser: %s %s\nMobile: %s\nEmail: %s",
                publicUser.getFirstName(), publicUser.getLastName(), publicUser.getMobile(), publicUser.getEmail()));
    }

    @Override
    public void sendVerifyEmail(String email, long publicUserId) {
        PublicUser publicUser = publicUserRepository.findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        publicUser.setEmail(email);
        publicUserRepository.save(publicUser);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(publicUser, Locale.ENGLISH, publicUserRegVerifyPage));
    }

    @Override
    public void saveEventType(long publicUserId, PublicUserEventDTO publicUserEventDTO) {
        PublicUser publicUser = publicUserRepository.findById(publicUserId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        boolean existsByPublicUserAndEventType = frontendEventRepository.existsByPublicUserAndEventType(publicUser, publicUserEventDTO.getEventType());

        if (existsByPublicUserAndEventType) {
            return;
        }

        FrontendEvent frontendEvent = new FrontendEvent();
        frontendEvent.setAppType(publicUserEventDTO.getAppType());
        frontendEvent.setDateTime(LocalDateTime.now());
        frontendEvent.setEventType(publicUserEventDTO.getEventType());
        frontendEvent.setSelectedEvent(publicUserEventDTO.getSelectedEvent());
        frontendEvent.setPublicUser(publicUser);
        frontendEventRepository.save(frontendEvent);
    }

    @Override
    public FrontendEventDTO getFrontendEvents(EventType eventType, long userId) {
        PublicUser publicUser = publicUserRepository.findById(userId)
                .orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));
        FrontendEvent frontendEvent = frontendEventRepository.findTopByEventTypeAndPublicUser(eventType, publicUser);
        if (frontendEvent == null) return new FrontendEventDTO(
                0,
                null,
                null,
                null,
                null,
                userId
        );
        return modelMapper.map(frontendEvent, FrontendEventDTO.class);
    }

    @Override
    public void deletePublicUser(String mobile) {
        PublicUser publicUser = publicUserRepository.findByMobile(mobile);

        if (publicUser == null) throw new CustomServiceException(NO_PUBLIC_USER_FOUND);

        //remove subscriptions
        List<PublicUserPackageSubscription> subscriptions = publicUserPackageSubscriptionRepository.findPublicUserPackageSubscriptionsByPublicUser(publicUser);
        if (subscriptions.size() > 0) {
            for (PublicUserPackageSubscription subscription : subscriptions) {
                if (!subscription.getStatus().equals(ClassPackagePublicUserSubscriptionStatus.INACTIVE))
                    stripeService.cancelSubscription(subscription.getOrderId());
            }
        }
        publicUserPackageSubscriptionRepository.deleteInBatch(subscriptions);

        //remove corporate details
        List<CorporatePublicUser> corporatePublicUsers = corporatePublicUserRepository.getCorporatePublicUsersByPublicUser(publicUser);
        corporatePublicUserRepository.deleteInBatch(corporatePublicUsers);

        List<ClassRating> classRatings = classRatingRepository.findClassRatingsByPublicUser(publicUser);
        classRatingRepository.deleteInBatch(classRatings);

        List<PhysicalClassRating> physicalClassRatings = physicalClassRatingRepository.findPhysicalClassRatingsByPublicUser(publicUser);
        physicalClassRatingRepository.deleteInBatch(physicalClassRatings);

        List<ClassSessionEnroll> sessionEnrolls = classSessionEnrollRepository.findUpcomingSessionsForPublicUser(publicUser);
        for (ClassSessionEnroll enroll : sessionEnrolls) {
            //cancel scheduled notifications
            List<PublicUserNotification> allScheduledNotifications = publicUserNotificationRepository
                    .findAllByTypeAndTypeId(NotificationType.SESSION, String.valueOf(enroll.getClassSession().getId()));

            cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_MOBILE);
            cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_WEB);
        }

        List<PhysicalSessionEnroll> physicalSessionEnrolls = physicalSessionEnrollRepository.findUpcomingSessionsForPublicUser(publicUser);
        for (PhysicalSessionEnroll enroll : physicalSessionEnrolls) {
            //cancel scheduled notifications
            List<PublicUserNotification> allScheduledNotifications = publicUserNotificationRepository
                    .findAllByTypeAndTypeId(NotificationType.PHYSICAL_SESSION, String.valueOf(enroll.getPhysicalClassSession().getId()));
            cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_MOBILE);
            cancelScheduledNotificationList(allScheduledNotifications, UserDeviceTypes.PUBLIC_USER_WEB);
        }

        List<AccaCode> accaCodes = accaCodeRepository.findAccaCodesByPublicUser(publicUser);
        accaCodeRepository.deleteInBatch(accaCodes);

        publicUserRepository.delete(publicUser);
    }

    @Override
    public boolean checkUserWithZoomMeetingID(long userId, long zoomMeetingId) {
        PublicUser publicUser = publicUserRepository.findById(userId).orElseThrow(() -> new CustomServiceException(NO_PUBLIC_USER_FOUND));

        ClassSessionEnroll sessionEnroll = classSessionEnrollRepository.findClassSessionEnrollByPublicUserAndClassSession_ZoomMeetingId(publicUser, zoomMeetingId);
        if (sessionEnroll != null) {
            return true;
        } else {
            try {
                OnlineCoachingCall call = onlineCoachingCallRepository.findOnlineCoachingCallByReceiverIdAndMeetingId(publicUser.getId(), zoomMeetingId);
                return call != null;
            } catch (Exception e) {
                throw new CustomServiceException("Invalid Zoom Meeting ID!");
            }
        }

    }
}
