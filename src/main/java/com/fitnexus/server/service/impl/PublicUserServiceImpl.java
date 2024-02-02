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


    @Value("${public_user_reg_verify_api}")
    private String publicUserRegVerifyPage;



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

}
