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

}
