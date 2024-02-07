package com.fitnexus.server.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface BusinessProfileManagerService {
    @Transactional
    void changeAdminPassword(String username, String password);

    @Transactional
    void acceptConditions(String username);

    void resendBusinessProfileManagerEmail(long profileId, String email);
}
