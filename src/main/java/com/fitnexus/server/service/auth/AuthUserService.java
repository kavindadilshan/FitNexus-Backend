package com.fitnexus.server.service.auth;

import org.springframework.stereotype.Service;

@Service
public interface AuthUserService {

    void deleteAuthUser(String username);

}
