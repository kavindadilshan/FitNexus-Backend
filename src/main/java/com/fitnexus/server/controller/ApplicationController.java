package com.fitnexus.server.controller;

import com.fitnexus.server.constant.FitNexusConstants;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.version.AppVersionDTO;
import com.fitnexus.server.enums.AppType;
import com.fitnexus.server.service.AppVersionService;
import com.fitnexus.server.util.AIAUserCheck;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/application")
public class ApplicationController {

    private final AppVersionService appVersionService;
    private final AIAUserCheck aiaUserCheck;

    @GetMapping(value = "/version")
    public ResponseEntity<CommonResponse<String>> getVersion() {
        String version = FitNexusConstants.ApplicationVersion.VERSION;
        log.info("\nApplication version - " + version);
        return ResponseEntity.ok(new CommonResponse<>(true, version));
    }

    @GetMapping(value = "/version/mobile")
    public ResponseEntity<CommonResponse<List<AppVersionDTO>>> getAppVersion(
            @RequestParam(value = "appType", required = false) AppType appType) {
        log.info("\nGet Mobile App Version. Type - {}", appType);
        List<AppVersionDTO> appVersion = appVersionService.getAppVersion(appType);
        return ResponseEntity.ok(new CommonResponse<>(true, appVersion));
    }

    @PutMapping(value = "/version/update")
    public ResponseEntity<CommonResponse<String>> updateAppVersion(@RequestParam(value = "version") int version, @RequestParam(value = "token") String token) {
        log.info("\nUpdate app version - {}", version);
        appVersionService.updateAppVersion(version, token);
        return ResponseEntity.ok(new CommonResponse<>(true, "App version updated successfully!"));
    }

    //This endpoint is for testing purpose
    @GetMapping(value = "/aia/user-check")
    public ResponseEntity<CommonResponse<String>> checkAIAUser(
            @RequestParam(value = "mobile") String mobile,
            @RequestParam(value = "token") String token) {
        log.info("\nCheck AIA user: \tmobile - {} \ttoken - {}", mobile, token);
        String response = aiaUserCheck.isUserValidRaw(mobile, token);
        return ResponseEntity.ok(new CommonResponse<>(true, response));
    }

    //This endpoint is for update charted accountant student corporate membership id in the constant file
    @GetMapping(value = "/acca")
    public ResponseEntity<CommonResponse<String>> updateAccaMembershipId(
            @RequestParam(value = "id") long id,
            @RequestParam(value = "token") String token) {
        log.info("\nUpdate ACCA membership ID: \tid - {} \ttoken - {}", id, token);
        if (token.equals("FitzkyACCA8921")) {
            FitNexusConstants.CorporateConstants.ACCA_CORPORATE_MEMBERSHIP_ID = id;
        } else {
            throw new CustomServiceException("Invalid token");
        }
        return ResponseEntity.ok(new CommonResponse<>(true, "ACCA membership ID updated successfully!"));
    }
}
