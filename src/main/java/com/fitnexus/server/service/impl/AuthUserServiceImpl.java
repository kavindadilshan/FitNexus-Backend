package com.fitnexus.server.service.impl;

import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.repository.auth.AuthUserRepository;
import com.fitnexus.server.service.auth.AuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.fitnexus.server.constant.FitNexusConstants.NotFoundConstants.NO_USER_FOUND;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthUserServiceImpl implements AuthUserService {

    private final AuthUserRepository authUserRepository;

    @Override
    public void deleteAuthUser(String username) {
        AuthUser authUser = authUserRepository.findByUsername(username).orElseThrow(() -> new CustomServiceException(NO_USER_FOUND));
        authUserRepository.delete(authUser);
    }

}
