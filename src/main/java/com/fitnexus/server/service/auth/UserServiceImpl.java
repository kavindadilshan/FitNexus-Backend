package com.fitnexus.server.service.auth;

import com.fitnexus.server.config.security.custom.CustomOauthException;
import com.fitnexus.server.constant.FitNexusConstants;
import com.fitnexus.server.dto.auth.AuthUserDetailsDTO;
import com.fitnexus.server.dto.auth.PublicAuthUserDetailsDTO;
import com.fitnexus.server.dto.auth.UserAuthDTO;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.auth.EmailVerificationToken;
import com.fitnexus.server.entity.auth.ForgotPasswordVerificationToken;
import com.fitnexus.server.entity.auth.UserRoleDetail;
import com.fitnexus.server.entity.businessprofile.BusinessAgreement;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.businessprofile.BusinessProfileManager;
import com.fitnexus.server.entity.instructor.Instructor;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.entity.trainer.Trainer;
import com.fitnexus.server.enums.BusinessAgreementStatus;
import com.fitnexus.server.enums.ManagerStatus;
import com.fitnexus.server.enums.UserRole;
import com.fitnexus.server.enums.UserStatus;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.auth.EmailVerificationTokenRepository;
import com.fitnexus.server.repository.auth.ForgotPasswordVerificationTokenRepository;
import com.fitnexus.server.repository.businessprofile.BusinessAgreementRepository;
import com.fitnexus.server.repository.businessprofile.BusinessProfileRepository;
import com.fitnexus.server.repository.publicuser.PublicUserRepository;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.fitnexus.server.config.security.SecurityConstants.*;
import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.MAX_USER_LOGIN_ATTEMPTS;
import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.USER_LOCK_PERIOD;
import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.NO_BUSINESS_PROFILE_FOUND;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final AuthUserRepository authUserRepository;
    private final PublicUserRepository publicUserRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final ForgotPasswordVerificationTokenRepository forgotPasswordVerificationTokenRepository;
    private final BusinessAgreementRepository businessAgreementRepository;
    private final BusinessProfileRepository businessProfileRepository;

    private final EmailSender emailSender;
    private final CustomGenerator customGenerator;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Value("${user_reg_verify_api}")
    private String userRegVerifyPage;

    @Value("${support_mail}")
    private String supportMail;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("User login: " + username);

        // gets current authentication principal
        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        String clientId = user.getUsername();

        switch (clientId) {

            case PUBLIC_SOCIAL_CLIENT_ID:
                PublicUser publicUser = getPublicUser(username);
                return new UserAuthDTO(publicUser.getId(), username, passwordEncoder.encode(publicUser.getSocialMediaId()), getRole(UserRole.PUBLIC_USER),
                        publicUser.getStatus(), modelMapper.map(publicUser, PublicAuthUserDetailsDTO.class));

            case PUBLIC_CLIENT_ID:
                publicUser = getPublicUser(username);
                return new UserAuthDTO(publicUser.getId(), username, publicUser.getPassword(), getRole(UserRole.PUBLIC_USER),
                        publicUser.getStatus(), modelMapper.map(publicUser, PublicAuthUserDetailsDTO.class));

            default:
                AuthUser authUser = getAuthUser(username);
                AuthUserDetailsDTO userDetailsDTO = modelMapper.map(authUser, AuthUserDetailsDTO.class);
                setVerification(authUser, userDetailsDTO);
                if (authUser.getImage() == null || authUser.getImage().isEmpty())
                    userDetailsDTO.setImage(FitNexusConstants.Avatar.AVATAR);
                setConditionAccepted(authUser, userDetailsDTO, clientId);
                return new UserAuthDTO(authUser.getId(), username, authUser.getPassword(), getRoles(authUser.getUserRoleDetails()),
                        authUser.getStatus(), userDetailsDTO);

        }

    }

    private void setConditionAccepted(AuthUser authUser, AuthUserDetailsDTO userDetailsDTO, String clientId) {
        if (clientId.equals(ADMIN_CLIENT_ID)) {
            if (authUser.getUserRoleDetails().stream().map(userRoleDetail -> userRoleDetail.getUserRole().getRole())
                    .collect(Collectors.toList()).contains(UserRole.SUPER_ADMIN))
                userDetailsDTO.setConditionsAccepted(true);
            else {
                BusinessProfileManager manager = authUser.getBusinessProfileManager();
                if (manager != null) {
                    userDetailsDTO.setConditionsAccepted(manager.isConditionsAccepted());
                    BusinessProfile businessProfile = businessProfileRepository.findById(manager.getBusinessProfile().getId()).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
                    BusinessAgreement agreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(businessProfile);
                    if (agreement.getStatus().equals(BusinessAgreementStatus.INACTIVE) && manager.getStatus().equals(ManagerStatus.ACTIVE))
                        throw new CustomServiceException("Your studio profile is inactive. Please contract Fitzky support.");
                    userDetailsDTO.setBusinessAgreement(agreement.getFile());
                    userDetailsDTO.setStudioId(businessProfile.getId());
                    userDetailsDTO.setStudioName(businessProfile.getBusinessName());
                    userDetailsDTO.setImage(businessProfile.getProfileImage());
                } else {
                    userDetailsDTO.setConditionsAccepted(false);
                }
            }
        }
    }

    private void setVerification(AuthUser authUser, AuthUserDetailsDTO userDetailsDTO) {

        Trainer trainer = authUser.getTrainer();
        Instructor instructor = authUser.getInstructor();
        if (trainer != null) {
            userDetailsDTO.setVerificationType(trainer.getVerificationType());
            userDetailsDTO.setVerificationNo(trainer.getVerificationNo());
        } else if (instructor != null) {
            userDetailsDTO.setVerificationType(instructor.getVerificationType());
            userDetailsDTO.setVerificationNo(instructor.getVerificationNo());
        }
    }

    private void checkLoginAttempts(AuthUser authUser, PublicUser publicUser) {

        //this is not applicable for a guest user
        if (authUser != null || (publicUser != null && !publicUser.getMobile().equals("+94700000000"))) {
            LocalDateTime lastLoginDateTime = authUser != null ? authUser.getLastLoginDateTime() : publicUser.getLastLoginDateTime();
            int loginAttempts = authUser != null ? authUser.getLoginAttempts() : publicUser.getLoginAttempts();

            boolean lastLoginSuccessful = publicUser != null && publicUser.isLastLoginSuccessful();

            if (lastLoginDateTime != null) {
                if (ChronoUnit.MINUTES.between(lastLoginDateTime, LocalDateTime.now()) > USER_LOCK_PERIOD) {
                    if (loginAttempts != 0) {
                        loginAttempts = 0;
                    }
                } else {
                    if (loginAttempts >= MAX_USER_LOGIN_ATTEMPTS) {
                        throw new CustomOauthException("You have reached the maximum number of sign-in attempts, please try again 1 hour later.");
                    }
                    if (!lastLoginSuccessful) loginAttempts++;
                }
            } else {
                loginAttempts++;
            }

            if (authUser != null) {
                authUser.setLastLoginDateTime(LocalDateTime.now());
                authUser.setLoginAttempts(loginAttempts);
                authUserRepository.save(authUser);
            }
            if (publicUser != null) {
                publicUser.setLastLoginDateTime(LocalDateTime.now());
                publicUser.setLoginAttempts(loginAttempts);
                publicUser.setLastLoginSuccessful(false);
                publicUserRepository.save(publicUser);
            }
        }
    }

    private AuthUser getAuthUser(String username) {
        AuthUser authUser = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new CustomOauthException("You have entered an invalid username or password"));
        if (authUser.getStatus() == UserStatus.INACTIVE || authUser.getStatus() == UserStatus.INACTIVE_PENDING) {
            throw new CustomOauthException("This user account is inactive. Please contract Fitzky support.");
        }
        checkLoginAttempts(authUser, null);
        return authUser;
    }

    private PublicUser getPublicUser(String mobile) {
        PublicUser publicUser = publicUserRepository.findByMobile(mobile);
        if (publicUser == null) {
            log.error("Invalid mobile");
            throw new CustomOauthException("You have entered an invalid username or password");
        }
        if (publicUser.getStatus() == UserStatus.INACTIVE) {
            throw new CustomOauthException("This user account is inactive. Please contract Fitzky support.");
        }
        checkLoginAttempts(null, publicUser);
        return publicUser;
    }

    /**
     * @param userRole the user role of a searched user
     * @return the user role as authority
     */
    private List<SimpleGrantedAuthority> getRole(UserRole userRole) {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole));
    }

    private List<SimpleGrantedAuthority> getRoles(List<UserRoleDetail> userRoleDetails) {

        List<SimpleGrantedAuthority> result = new ArrayList<>();
        for (UserRoleDetail role : userRoleDetails) {
            UserRole userRole = role.getUserRole().getRole();
            result.add(new SimpleGrantedAuthority("ROLE_" + userRole));
        }
        return result;
    }


    /**
     * This can use to save a mail confirmation token for a user.
     *
     * @param user the auth user
     */
    @Override
    public int createVerificationToken(AuthUser user) {
        int token = ThreadLocalRandom.current().nextInt(100000, 1000000);
        Optional<ForgotPasswordVerificationToken> tokenOptional = forgotPasswordVerificationTokenRepository.findForgotPasswordVerificationTokenByAuthUser(user);
        if (tokenOptional.isPresent()) {
            ForgotPasswordVerificationToken verificationToken = tokenOptional.get();
            verificationToken.setToken(token);
            verificationToken.setVerified(false);
            forgotPasswordVerificationTokenRepository.save(verificationToken);
        } else {
            forgotPasswordVerificationTokenRepository.save(new ForgotPasswordVerificationToken(token, false, user));
        }
        return token;
    }

    /**
     * This can use to validate a mail verification token
     *
     * @param token the token to verify.
     * @return the HttpResponse code for validation.
     */
    @Override
    @Transactional
    public HttpStatus checkVerificationToken(String token) {

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token);
        if (verificationToken == null) {
            return HttpStatus.NOT_FOUND;
        }

        AuthUser authUser = verificationToken.getAuthUser();
        if (verificationToken.getExpireDateTime().isBefore(LocalDateTime.now())) {
            return HttpStatus.GONE;
        }

        authUser.setEmailVerified(true);
        authUserRepository.save(authUser);
        return HttpStatus.OK;
    }

    /**
     * @param existingToken the previously sent token.
     */
    @Override
    public void resendEmailToken(String existingToken) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(existingToken);
        if (verificationToken == null) throw new CustomServiceException("No token found!");

        AuthUser authUser = verificationToken.getAuthUser();
        if (authUser.isEmailVerified()) throw new CustomServiceException("You have already verified your email");
        String token = UUID.randomUUID().toString();

        verificationToken.setToken(token);
        verificationToken.setExpireDateTime(LocalDateTime.now().plusDays(1));
        emailVerificationTokenRepository.save(verificationToken);

        String recipientAddress = authUser.getEmail();
        String subject = "Re-send email verification token";
        String confirmUrl = customGenerator.getPageUrlWithToken(userRegVerifyPage, token);

        emailSender.sendHtmlEmail(Collections.singletonList(recipientAddress), subject,
                emailSender.getVerifyTokenMailBody(confirmUrl), null, Collections.singletonList(supportMail));
    }

}

