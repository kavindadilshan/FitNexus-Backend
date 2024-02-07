package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.businessprofile.*;
import com.fitnexus.server.dto.businessprofile.*;
import com.fitnexus.server.dto.classes.ClassRevenueSummaryDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.membership.MembershipRevenueSummaryDTO;
import com.fitnexus.server.enums.BusinessAgreementStatus;
import com.fitnexus.server.service.BusinessProfileManagerService;
import com.fitnexus.server.service.BusinessProfileService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin")
public class AdminBusinessProfileController {

    private final BusinessProfileService businessProfileService;
    private final BusinessProfileManagerService businessProfileManagerService;

    @PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
    @PostMapping(value = "/profile")
    public ResponseEntity createBusinessProfile(@RequestBody BusinessProfileCreateDto profileDto, @RequestHeader(name = "Authorization") String token) {
        log.info("Create business profile : \nprofile dto: {}", profileDto);
        String username = getUsername(token);
        Map<String, String> result = businessProfileService.createBusinessProfile(profileDto, username);
        log.info(result.get("message"));
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @PutMapping(value = "/profile")
    public ResponseEntity updateBusinessProfile(@RequestBody BusinessProfileCreateDto profileDto, @RequestHeader(name = "Authorization") String token) {
        log.info("Update business profile : \nprofile dto: {}", profileDto);
        String username = getUsername(token);
        businessProfileService.updateBusinessProfile(profileDto, username);
        log.info("Response : business profile updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Business profile updated successfully"));
    }

    @GetMapping(value = "/profile")
    public ResponseEntity getAllProfileDetails(Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        log.info("Get all Business profiles");
        String username = getUsername(token);
        Page<BusinessProfileResponseDto> result = businessProfileService.getAllProfiles(pageable, username);
        log.info("Response - business profiles page");
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @GetMapping(value = "/profile/names")
    public ResponseEntity getAllProfileIDsAndNames(@RequestHeader(name = "Authorization") String token) {
        log.info("Get all business profile names and IDs");
        String username = getUsername(token);
        List<BusinessProfileNameIdDTO> result = businessProfileService.getAllProfileIDsAndNames(username);
        log.info("Response -all business profile names and IDs");
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @GetMapping(value = "/profile/names/has-instructors")
    public ResponseEntity getAllProfileIDsAndNamesWhichHasInstructors(@RequestHeader(name = "Authorization") String token) {
        log.info("Get all business profile names and IDs which has-instructors");
        String username = getUsername(token);
        List<BusinessProfileNameIdDTO> result = businessProfileService.getBusinessNamesHasInstructors(username);
        log.info("Response -all business profile names and IDs which has-instructors");
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @GetMapping(value = "/profile/{id}")
    public ResponseEntity getBusinessProfileByID(@PathVariable long id, @RequestHeader(name = "Authorization") String token) {
        log.info("Get business profile by id : \nbusiness profile id: {}", id);
        String username = getUsername(token);
        BusinessProfileResponseDto result = businessProfileService.getBusinessProfileByID(id, username);
        log.info("Response - business profile by ID");
        return ResponseEntity.ok(new CommonResponse<>(true, result));
    }

    @PostMapping(value = "/profile/search")
    public ResponseEntity searchBusinessProfiles(@RequestBody Map<String, String> data, Pageable pageable) {
        log.info("Search business profiles : \ntext: {}", data.get("data"));
        Page<BusinessProfileResponseDto> businessProfiles = businessProfileService.searchBusinessProfiles(data.get("data"), pageable);
        log.info("Response - business profile search page");
        return ResponseEntity.ok(new CommonResponse<>(true, businessProfiles));
    }

    @PatchMapping(value = "/profile/status")
    public ResponseEntity changeBusinessProfileStatus(@RequestParam long profileId, @RequestParam BusinessAgreementStatus status, @RequestHeader(name = "Authorization") String token) {
        String username = getUsername(token);
        log.info("Change business profile status : \nprofile id: {}, \nstatus: {}, \nupdating username: {}", profileId, status, username);
        String message = businessProfileService.changeProfileStatus(profileId, status, username);
        log.info("Response : {}", message);
        return ResponseEntity.ok(new CommonResponse<>(true, message));
    }

    @PostMapping(value = "/profile/revenue")
    public ResponseEntity getRevenues(@RequestBody Map<String, LocalDateTime> data, Pageable pageable,
                                      @RequestHeader(name = "Authorization") String token,
                                      @RequestParam(value = "name", required = false) String name) {
        LocalDateTime start = data.get("start");
        LocalDateTime end = data.get("end");
        String username = getUsername(token);
        log.info("Get revenues : \nstart: {} \nend: {} \nusername: {}\nname: {}", start, end, username, name);
        Page<BusinessProfileRevenueDTO> revenueDetails = businessProfileService.getRevenueDetails(start, end, username, name, pageable);
        log.info("Response - Revenue page");
        return ResponseEntity.ok(new CommonResponse<>(true, revenueDetails));
    }

    @PostMapping(value = "/profile/{id}/class/summary")
    public ResponseEntity getOnlineClassSummary(@PathVariable(value = "id") long id, @RequestBody Map<String, LocalDateTime> data, Pageable pageable) {
        LocalDateTime start = data.get("start");
        LocalDateTime end = data.get("end");
        log.info("Get online class summary : \nstart: {} \nend: {} \nbusiness profile id: {} ", start, end, id);
        Page<ClassRevenueSummaryDTO> onlineClassesSummary = businessProfileService.getOnlineClassesSummary(start, end, id, pageable);
        log.info("Response - online classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, onlineClassesSummary));
    }

    @PostMapping(value = "/profile/{id}/class/physical/summary")
    public ResponseEntity getPhysicalClassSummary(@PathVariable(value = "id") long id, @RequestBody Map<String, LocalDateTime> data, Pageable pageable) {
        LocalDateTime start = data.get("start");
        LocalDateTime end = data.get("end");
        log.info("Get physical class summary : \nstart: {} \nend: {} \nbusiness profile id: {} ", start, end, id);
        Page<ClassRevenueSummaryDTO> physicalClassesSummary = businessProfileService.getPhysicalClassesSummary(start, end, id, pageable);
        log.info("Response - physical classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, physicalClassesSummary));
    }

    @PostMapping(value = "/profile/{id}/membership/gym/summary")
    public ResponseEntity getGymMembershipSummary(@PathVariable(value = "id") long id, @RequestBody Map<String, LocalDateTime> data, Pageable pageable) {
        LocalDateTime start = data.get("start");
        LocalDateTime end = data.get("end");
        log.info("Get gym membership summary : \nstart: {} \nend: {} \nbusiness profile id: {} ", start, end, id);
        Page<MembershipRevenueSummaryDTO> gymMembershipSummary = businessProfileService.getGymMembershipSummary(start, end, id, pageable);
        log.info("Response - gym memberships page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymMembershipSummary));
    }

    @PostMapping(value = "/profile/{id}/membership/physical-class/summary")
    public ResponseEntity getPhysicalClassesMembershipSummary(@PathVariable(value = "id") long id, @RequestBody Map<String, LocalDateTime> data, Pageable pageable) {
        LocalDateTime start = data.get("start");
        LocalDateTime end = data.get("end");
        log.info("Get physical class membership summary : \nstart: {} \nend: {} \nbusiness profile id: {} ", start, end, id);
        Page<MembershipRevenueSummaryDTO> physicalClassesMembershipSummary = businessProfileService.getPhysicalClassesMembershipSummary(start, end, id, pageable);
        log.info("Response - physical class memberships page");
        return ResponseEntity.ok(new CommonResponse<>(true, physicalClassesMembershipSummary));
    }

    @PostMapping(value = "/profile/{id}/membership/online-class/summary")
    public ResponseEntity getOnlineClassesMembershipSummary(@PathVariable(value = "id") long id, @RequestBody Map<String, LocalDateTime> data, Pageable pageable) {
        LocalDateTime start = data.get("start");
        LocalDateTime end = data.get("end");
        log.info("Get online class membership summary : \nstart: {} \nend: {} \nbusiness profile id: {} ", start, end, id);
        Page<MembershipRevenueSummaryDTO> onlineClassesMembershipSummary = businessProfileService.getOnlineClassesMembershipSummary(start, end, id, pageable);
        log.info("Response - online class memberships page");
        return ResponseEntity.ok(new CommonResponse<>(true, onlineClassesMembershipSummary));
    }

    @GetMapping(value = "/profile/subscription/history")
    public ResponseEntity getSubscriptionHistory(Pageable pageable) {
        log.info("get subscription summary");
        Page<SubscriptionPaymentDTO> subscriptionPaymentHistory = businessProfileService.getSubscriptionPaymentHistory(pageable);
        log.info("Response - subscription payment history page");
        return ResponseEntity.ok(new CommonResponse<>(true, subscriptionPaymentHistory));
    }

    @GetMapping(value = "/profile/payment/history")
    public ResponseEntity getProfilePaymentSummary(@RequestParam(value = "name", required = false) String name,
                                                   Pageable pageable) {
        log.info("Get profile payment summary: name: {}\tpage: {}", name, pageable);
        Page<PaymentSummaryDTO> profilePaymentSummary = businessProfileService.getProfilePaymentSummary(name, pageable);
        log.info("Response - payment summary page");
        return ResponseEntity.ok(new CommonResponse<>(true, profilePaymentSummary));
    }

    @GetMapping(value = "/profile/payment/due")
    public ResponseEntity getProfileDuePayments(@RequestParam(value = "name", required = false) String name,
                                                Pageable pageable) {
        log.info("Get profile due payments: name: {}\tpage: {}", name, pageable);
        Page<DuePaymentDTO> profileDuePayments = businessProfileService.getProfileDuePayments(name, pageable);
        log.info("Response - profile due payment page");
        return ResponseEntity.ok(new CommonResponse<>(true, profileDuePayments));
    }

    @PostMapping(value = "/profile/payment/settle")
    public ResponseEntity settleProfilePayment(@RequestBody PaymentSummaryDTO payment) {
        log.info("Settle profile payments : \npayment dto: {}", payment);
        businessProfileService.settleProfilePayment(payment);
        log.info("Response : Payment settled successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Payment settled successfully"));
    }

    @PutMapping(value = "/profile/agreement/update/{id}")
    public ResponseEntity updateAgreement(@PathVariable("id") long id, @RequestBody BusinessProfileCreateDto dto) {
        log.info("Update agreement : \nbusiness profile id: {}", id);
        businessProfileService.updateAgreement(id, dto);
        log.info("Response is success");
        return ResponseEntity.ok(new CommonResponse<>(true, "Agreement updated successfully"));
    }

    @PutMapping(value = "/profile/agreement/renew/{id}")
    public ResponseEntity renewAgreement(@PathVariable("id") long id, @RequestBody BusinessProfileAgreementDTO dto) {
        log.info("Renew agreement : \nbusiness profile id: {}", id);
        businessProfileService.renewAgreement(id, dto);
        log.info("Response : Agreement renewed successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Agreement renewed successfully"));
    }

    @PutMapping(value = "/profile/agreement/cancel/{id}")
    public ResponseEntity cancelAgreement(@PathVariable("id") long id) {
        log.info("Cancel agreement : \nbusiness profile id: {}", id);
        businessProfileService.cancelAgreement(id);
        log.info("Response : Agreement canceled successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Agreement canceled successfully"));
    }

    @GetMapping(value = "/profile/locations/{id}")
    public ResponseEntity getLocations(@PathVariable("id") long id) {
        log.info("Get business profile locations : \nprofile id: {}", id);
        List<BusinessProfileLocationDTO> locations = businessProfileService.getLocationsForProfile(id);
        log.info("Response - location list");
        return ResponseEntity.ok(new CommonResponse<>(true, locations));
    }

    @GetMapping(value = "/profile/gym/locations/{id}")
    public ResponseEntity getGymLocationsForBusinessProfile(@PathVariable("id") long id) {
        log.info("get gym locations for business profile: profile id - " + id);
        List<BusinessProfileLocationDTO> locations = businessProfileService.getGymLocationsForBusinessProfile(id);
        log.info("Response - location list");
        return ResponseEntity.ok(new CommonResponse<>(true, locations));
    }

    @GetMapping(value = "/profile/locations/page/{id}")
    public ResponseEntity getLocations(@PathVariable("id") long id, Pageable pageable) {
        log.info("get locations with pagination : profile id - " + id);
        Page<BusinessProfileLocationDTO> locations = businessProfileService.getLocationsForProfile(id, pageable);
        log.info("Response - location page");
        return ResponseEntity.ok(new CommonResponse<>(true, locations));
    }

    @PostMapping(value = "/profile/existence")
    public ResponseEntity checkProfileExistence(@RequestBody BusinessProfileCreateDto dto) {
        log.info("\nCheck businessProfile existence : \nbusiness name: {} \nregistration number: {} \ntelephone: {} \nemail: {}",
                dto.getBusinessName(), dto.getBusinessRegistrationNumber(), dto.getTelephone(), dto.getEmail());
        String response = businessProfileService.checkProfileExistence(dto);
        log.info("Response : " + response);
        return ResponseEntity.ok(new CommonResponse<>(true, response));
    }

    @PostMapping(value = "/profile/manager/email/resend/{id}")
    public ResponseEntity resendBusinessProfileManagerEmail(@PathVariable("id") long id, @RequestParam(value = "email") String email) {
        log.info("\nResend business profile manager email : \nbusiness id: {} \nemail: {}", id, email);
        businessProfileManagerService.resendBusinessProfileManagerEmail(id, email);
        log.info("Response : Business Profile Manager account activation email resent successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Business Profile Manager account activation email resent successfully"));
    }

    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}
