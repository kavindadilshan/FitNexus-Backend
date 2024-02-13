package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.businessprofile.BusinessProfileListResponse;
import com.fitnexus.server.dto.classes.ClassListDTO;
import com.fitnexus.server.dto.classsession.SessionDetailDTO;
import com.fitnexus.server.dto.coach.CoachDetailsResponse;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.membership.MembershipsForGymDTO;
import com.fitnexus.server.dto.membership.MembershipsForOnlineClassDTO;
import com.fitnexus.server.dto.membership.MembershipsForPhysicalClassDTO;
import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.Gender;
import com.fitnexus.server.service.*;
import com.fitnexus.server.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users/business-profile")
public class PublicUserBusinessProfileController {

    private final BusinessProfileService businessProfileService;
    private final ClassService classService;
    private final InstructorService instructorService;
    private final PhysicalClassService physicalClassService;
    private final MembershipService membershipService;
    private final PublicUserService publicUserService;
    private final ClassSessionService classSessionService;

    @GetMapping(value = "")
    public ResponseEntity<CommonResponse<Page<BusinessProfileListResponse>>> getActiveBusinessProfiles(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "classTypes", required = false) List<Long> classTypeIds,
            @RequestParam(value = "longitude", required = false) String longitude,
            @RequestParam(value = "latitude", required = false) String latitude,
            @RequestParam(value = "corporateOnly", defaultValue = "false", required = false) boolean corporateOnly,
            @RequestHeader("Authorization") String token,
            Pageable pageable) {

        // type - GROUP, PERSONAL, PHYSICAL, INSTRUCTOR_PACKAGE ,GYM
        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        log.info("\nGet business profiles pages for public \nname:{}, \ntype: {} \nclassTypeIds: {},\nuserId: {}," +
                        "\nlongitude: {},\nlatitude: {},\ncorporateOnly: {},\npage req: {}",
                name, type, classTypeIds, userId, longitude, latitude, corporateOnly, pageable);

        double lon = 0;
        double lat = 0;

        try {
            lon = Double.parseDouble(longitude);
        } catch (Exception ignored) {
        }
        try {
            lat = Double.parseDouble(latitude);
        } catch (Exception ignored) {
        }

        Page<BusinessProfileListResponse> activeBusinessProfiles =
                businessProfileService.getActiveBusinessProfiles(
                        name,
                        classTypeIds,
                        type,
                        userId,
                        lon,
                        lat,
                        corporateOnly,
                        pageable
                );
        log.info("Response : business profiles page");
        return ResponseEntity.ok(new CommonResponse<>(true, activeBusinessProfiles));
    }

    @GetMapping(value = "/{businessId}")
    // response object name is list as it is the super, but actual response is single object
    public ResponseEntity<CommonResponse<BusinessProfileListResponse>> getBusinessProfile(
            @PathVariable(value = "businessId") long businessId) {
        log.info("\nGet single business profile for public \n id: {}", businessId);
        BusinessProfileListResponse businessProfile = businessProfileService.getBusinessProfile(businessId);
        log.info("Response: business profile single response: {}", businessProfile);
        return ResponseEntity.ok(new CommonResponse<>(true, businessProfile));
    }

    @GetMapping(value = "/{businessId}/classes")
    public ResponseEntity<CommonResponse<Page<ClassListDTO>>> getSessionsByProfile(
            @RequestParam(value = "category", required = false) ClassCategory category,
            @PathVariable(value = "businessId") long businessId, Pageable pageable, @RequestHeader("Authorization") String token) {
        log.info("\nGet classes for for public user by business profile \tbusinessId: {}\tcategory: {}\tpage: {}",
                businessId, category, pageable);
        Page<ClassListDTO> classesByBusinessProfile = classService.getClassesByBusinessProfile(businessId, category, pageable, token);
        log.info("Response: classes by profiles list");
        return ResponseEntity.ok(new CommonResponse<>(true, classesByBusinessProfile));
    }

    @GetMapping(value = "/{businessId}/classes/physical")
    public ResponseEntity<CommonResponse<Page<ClassListDTO>>> getPhysicalSessionsByProfile(
            @PathVariable(value = "businessId") long businessId,
            Pageable pageable,
            @RequestHeader("Authorization") String token) {
        String country = publicUserService.getCountryOfUserFromToken(token);
        log.info("\nGet physical classes for for public user by business profile \nbusinessId: {}\ncountry: {}\npage: {}",
                businessId, country, pageable);
        Page<ClassListDTO> classesByBusinessProfile = physicalClassService.getPhysicalClassesByBusinessProfile(businessId, country, pageable, token);
        log.info("Response: physical classes by profiles list");
        return ResponseEntity.ok(new CommonResponse<>(true, classesByBusinessProfile));
    }

    @GetMapping(value = "/{businessId}/coaches")
    public ResponseEntity<CommonResponse<Page<CoachDetailsResponse>>> getCoachesByProfile(
            @PathVariable(value = "businessId") long businessId, @RequestParam(value = "gender", required = false) Gender gender, Pageable pageable) {
        log.info("\nGet coaches for for public user by business profile \nbusinessId: {}\ngender: {}", businessId, gender);
        Page<CoachDetailsResponse> coaches = instructorService.getCoachesByBusinessProfileAndGender(businessId, gender, pageable);
        log.info("Response: coaches by profile list");
        return ResponseEntity.ok(new CommonResponse<>(true, coaches));
    }

    @GetMapping(value = "/by-class-type/{classTypeId}")
    public ResponseEntity<CommonResponse<Page<BusinessProfileListResponse>>> getProfilesForClassType(
            @PathVariable("classTypeId") long classTypeId, Pageable pageable) {
        log.info("\nGet business profiles pages for public by class type\npage req: {} \nclassTypeId: {}", pageable, classTypeId);
        Page<BusinessProfileListResponse> businessProfilesByClassType = businessProfileService.getBusinessProfilesByClassType(classTypeId, pageable);
        log.info("Response: business profiles page");
        return ResponseEntity.ok(new CommonResponse<>(true, businessProfilesByClassType));
    }

    @GetMapping(value = "/{businessId}/memberships/gym")
    public ResponseEntity getGymMemberships(@PathVariable("businessId") long businessId, Pageable pageable, @RequestHeader("Authorization") String token) {
        log.info("\nGet all gym memberships for business profile: \nbusiness id: {}, \npage request: {}", pageable);
        Page<MembershipsForGymDTO> allMembershipsByEachGym = membershipService.getGymMembershipsByBusinessProfile(businessId, pageable, token);
        log.info("Response : All memberships for each gym");
        return ResponseEntity.ok(new CommonResponse<>(true, allMembershipsByEachGym));
    }

    @GetMapping(value = "/{businessId}/memberships/physical-class")
    public ResponseEntity getAllMembershipsByEachPhysicalClass(@PathVariable("businessId") long businessId, Pageable pageable, @RequestHeader("Authorization") String token) {
        log.info("\nGet all physical class memberships for business profile: \nbusiness id: {}, \npage request: {}", businessId, pageable);
        Page<MembershipsForPhysicalClassDTO> allMembershipsByEachPhysicalClass = membershipService.getPhysicalClassMembershipsByBusinessProfile(businessId, pageable, token);

//        SessionDTO upcomingSessionByBusinessProfile = physicalClassService.getUpcomingSessionByBusinessProfile(businessId, token);
//        HashMap<String, Object> response = new HashMap<>();
//        response.put("memberships", allMembershipsByEachPhysicalClass);
//        response.put("classSession", upcomingSessionByBusinessProfile);

        log.info("Response : All memberships for each physical class");
        return ResponseEntity.ok(new CommonResponse<>(true, allMembershipsByEachPhysicalClass));
    }

    @GetMapping(value = "/{businessId}/memberships/online-class")
    public ResponseEntity getAllMembershipsByEachOnlineClass(@PathVariable("businessId") long businessId, Pageable pageable, @RequestHeader("Authorization") String token) {
        log.info("\nGet all online class memberships for business profile: \nbusiness id: {}, \npage request: {}", businessId, pageable);

        Page<MembershipsForOnlineClassDTO> allMembershipsByEachOnlineClass = membershipService.getOnlineClassMembershipsByBusinessProfile(businessId, pageable, token);

//        SessionDTO upcomingSessionByBusinessProfile = classSessionService.getUpcomingSessionByBusinessProfile(businessId, token);
//        HashMap<String, Object> response = new HashMap<>();
//        response.put("memberships", allMembershipsByEachOnlineClass);
//        response.put("classSession", upcomingSessionByBusinessProfile);

        log.info("Response : All memberships for each online class");
        return ResponseEntity.ok(new CommonResponse<>(true, allMembershipsByEachOnlineClass));
    }

    @GetMapping(value = "scheduled/{businessId}")
    public ResponseEntity getAllScheduledClassSessionsByBusinessProfileAndDate(@PathVariable("businessId") long businessId,
                                                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                               @RequestParam(value = "category") ClassCategory category,
                                                                               Pageable pageable,
                                                                               @RequestHeader("Authorization") String token) {
        log.info("\nGet all scheduled online class sessions for business profile: " +
                "\nbusiness id: {}, " +
                "\ndate: {}, " +
                "\ncategory: {}, " +
                "\npage request: {}", businessId, date, category, pageable);
        Page<SessionDetailDTO> upcomingSessionByBusinessProfile = classSessionService.getUpcomingSessionByBusinessProfile(businessId, category, date, token, pageable);
        log.info("Response : All scheduled online class sessions for business profile by date and category");
        return ResponseEntity.ok(new CommonResponse<>(true, upcomingSessionByBusinessProfile));
    }

    @GetMapping(value = "scheduled/{businessId}/physical")
    public ResponseEntity getAllScheduledPhysicalClassSessionsByBusinessProfileAndDate(@PathVariable("businessId") long businessId,
                                                                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                                                       Pageable pageable,
                                                                                       @RequestHeader("Authorization") String token) {
        log.info("\nGet all scheduled physical class sessions for business profile: " +
                "\nbusiness id: {}, " +
                "\ndate: {}, " +
                "\npage request: {}", businessId, date, pageable);
        Page<SessionDetailDTO> upcomingSessionByBusinessProfile = physicalClassService.getUpcomingSessionByBusinessProfile(businessId, date, token, pageable);
        log.info("Response : All scheduled physical class sessions for business profile by date");
        return ResponseEntity.ok(new CommonResponse<>(true, upcomingSessionByBusinessProfile));
    }

    @GetMapping(value = "businessName/{businessName}")
    public ResponseEntity<CommonResponse<BusinessProfileListResponse>> getBusinessProfileByBusinessName(
            @PathVariable(value = "businessName") String businessName) {
        log.info("\nGet single business profile for public \n businessName: {}", businessName);
        BusinessProfileListResponse businessProfile = businessProfileService.getBusinessProfileByName(businessName);
        log.info("Response: business profile single response: {}", businessProfile);
        return ResponseEntity.ok(new CommonResponse<>(true, businessProfile));
    }
}
