package com.fitnexus.server.service.impl;

import com.fitnexus.server.config.email.OnRegistrationCompleteEvent;
import com.fitnexus.server.constant.FitNexusConstants;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.businessprofile.BusinessAgreement;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.businessprofile.BusinessProfileManager;
import com.fitnexus.server.enums.BusinessAgreementStatus;
import com.fitnexus.server.enums.ManagerStatus;
import com.fitnexus.server.enums.UserStatus;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.repository.businessprofile.BusinessAgreementRepository;
import com.fitnexus.server.repository.businessprofile.BusinessProfileManagerRepository;
import com.fitnexus.server.repository.businessprofile.BusinessProfileRepository;
import com.fitnexus.server.service.BusinessProfileManagerService;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.PasswordHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.*;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class BusinessProfileManagerServiceImpl implements BusinessProfileManagerService {

    private final BusinessProfileManagerRepository businessProfileManagerRepository;
    private final BusinessAgreementRepository businessAgreementRepository;
    private final AuthUserRepository authUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final BusinessProfileRepository businessProfileRepository;

    @Value("${business_manager_login_page}")
    private String businessManagerLoginPage;

    @Override
    @Transactional
    public void changeAdminPassword(String username, String password) {

        PasswordHandler.checkPasswordValidity(password);
        Optional<AuthUser> userOptional = authUserRepository.findByUsername(username);
        if (!userOptional.isPresent()) throw new CustomServiceException(NO_USER_FOUND);

        AuthUser authUser = userOptional.get();
        authUser.setEmailVerified(true);
        authUser.setPassword(passwordEncoder.encode(password));
        authUser.setStatus(UserStatus.ACTIVE);
        authUserRepository.save(authUser);

        BusinessProfileManager manager = authUser.getBusinessProfileManager();
        manager.setStatus(ManagerStatus.PENDING);
        businessProfileManagerRepository.save(manager);
    }

    @Override
    @Transactional
    public void acceptConditions(String username) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        BusinessProfileManager manager = authUser.getBusinessProfileManager();
        if (manager == null) throw new CustomServiceException(NO_MANAGER_FOUND);
        manager.setConditionsAccepted(true);
        manager.setStatus(ManagerStatus.ACTIVE);
        BusinessProfile businessProfile = manager.getBusinessProfile();
        BusinessAgreement agreement = businessAgreementRepository.findTopByBusinessProfileOrderByExpDateDesc(businessProfile);
        agreement.setStatus(BusinessAgreementStatus.ACTIVE);
        businessProfileManagerRepository.save(manager);
        businessAgreementRepository.save(agreement);
    }

    @Override
    public void resendBusinessProfileManagerEmail(long profileId, String email) {
        String password = CustomGenerator.generatePassword();
        BusinessProfile businessProfile = businessProfileRepository.findById(profileId).orElseThrow(() -> new CustomServiceException(NO_BUSINESS_PROFILE_FOUND));
        BusinessProfileManager manager = businessProfile.getBusinessProfileManager();
        AuthUser managerAuthUser = manager.getAuthUser();

        if (manager.isConditionsAccepted()) {
            throw new CustomServiceException("Business profile manager account already activated");
        } else {
            Optional<AuthUser> optionalUserByEmail = authUserRepository.findByEmail(email);
            if (optionalUserByEmail.isPresent()) {
                AuthUser userByEmail = optionalUserByEmail.get();
                if (managerAuthUser.getId() != userByEmail.getId())
                    throw new CustomServiceException(FitNexusConstants.DuplicatedConstants.EMAIL_ALREADY_EXISTS);
            }
            managerAuthUser.setEmail(email);
            managerAuthUser.setPassword(passwordEncoder.encode(password));
            managerAuthUser.setEmailVerified(false);
            managerAuthUser.setStatus(UserStatus.PENDING);
            authUserRepository.save(managerAuthUser);
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(managerAuthUser, password, Locale.ENGLISH, businessManagerLoginPage));
        }
    }
}
