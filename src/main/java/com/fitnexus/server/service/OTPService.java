package com.fitnexus.server.service;

import com.fitnexus.server.dto.common.PinVerifyDTO;
import com.fitnexus.server.dto.exception.CustomServiceException;
import org.springframework.stereotype.Service;


@Service
public interface OTPService {
    void sendOtpAndSaveIt(String mobile);

}
