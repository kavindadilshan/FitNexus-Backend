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



    private AuthUser getAuthUser(String username) {
        AuthUser authUser = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new CustomOauthException("You have entered an invalid username or password"));
        if (authUser.getStatus() == UserStatus.INACTIVE || authUser.getStatus() == UserStatus.INACTIVE_PENDING) {
            throw new CustomOauthException("This user account is inactive. Please contract FitNexus support.");
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
            throw new CustomOauthException("This user account is inactive. Please contract FitNexus support.");
        }

        return publicUser;
    }



}

