package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.classes.ClassAndTrainerRateDTO;
import com.fitnexus.server.dto.classes.ClassListDTO;
import com.fitnexus.server.dto.classes.ClassRateDTO;
import com.fitnexus.server.dto.classsession.ClassSessionListResponse;
import com.fitnexus.server.dto.classsession.ClassSessionSingleResponse;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.enums.Gender;
import com.fitnexus.server.service.PhysicalClassService;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.GuestUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.TIME_ZONE_HEADER;
import static com.fitnexus.server.constant.FitNexusConstants.PatternConstants.DATE_TIME_RESPONSE_PATTERN;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users/class/physical")
public class PublicUserPhysicalClassController {

    private final PhysicalClassService physicalClassService;
    private final PublicUserService publicUserService;

    @GetMapping(value = "/{classId}")
    public ResponseEntity<CommonResponse<ClassListDTO>> getClassDetails(
            @RequestParam(value = "dateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @PathVariable(value = "classId") long classId,
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            @RequestHeader("Authorization") String token) {
        log.info("\nGet physical class details for public user view class \tClass Id: {}\tdate:{},\tlongitude: {}\nlatitude: {}", classId, dateTime, longitude, latitude);
        ClassListDTO classDetails = physicalClassService.getPhysicalClassDetails(classId, dateTime, longitude, latitude, token);
        log.info("Response: {}", classDetails);
        return ResponseEntity.ok(new CommonResponse<>(true, classDetails));
    }

    @GetMapping(value = "/session/{sessionId}")
    public ResponseEntity<CommonResponse<ClassSessionSingleResponse>> getSessionDetails(
            @PathVariable(value = "sessionId") long sessionId,
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet physical session details for public user view session \tSession Id: {}", sessionId);
        ClassSessionSingleResponse classSession = physicalClassService.getPhysicalSession(sessionId, token,
                CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone), longitude, latitude);
        log.info("Response: {}", classSession);
        return ResponseEntity.ok(new CommonResponse<>(true, classSession));
    }

    @GetMapping(value = "/sessions")
    public ResponseEntity<CommonResponse<Page<ClassSessionListResponse>>> getSessionsForHome(
            @RequestParam(value = "startDateTime", required = true) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime startDateTime,
            @RequestParam(value = "endDateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime endDateTime,
            @RequestParam(value = "gender") Gender gender,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "classTypes", required = false) List<Long> classTypeIds,
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            Pageable pageable,
            @RequestHeader("Authorization") String token, @RequestHeader(TIME_ZONE_HEADER) String timeZone) {
        String country = publicUserService.getCountryOfUserFromToken(token);
        log.info("\nGet physical class sessions for home for public user \nstart: {}\nend: {}\ngender: {}\nname:{}" +
                        "\nclassTypes: {}\nPage: {}\tTimeZone: {}", startDateTime, endDateTime, gender, name, classTypeIds, pageable, timeZone);
        if (name == null) name = "";
        if (classTypeIds == null) {
            classTypeIds = Collections.emptyList();
        } else {
            if (classTypeIds.size() == 1) {
                classTypeIds.add((long) 0);
            }
        }
        Page<ClassSessionListResponse> sessionsForHome = physicalClassService.getPhysicalSessionsForHome(gender, name,
                classTypeIds, startDateTime, endDateTime, longitude, latitude, country,pageable, token, timeZone);
        log.info("Response: sessions list");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionsForHome));
    }

    @GetMapping(value = "/popular")
    public ResponseEntity<CommonResponse<Page<ClassListDTO>>> getPopularClasses(
            Pageable pageable,
            @RequestHeader("Authorization") String token) {
        String country = publicUserService.getCountryOfUserFromToken(token);
        log.info("\nGet popular physical class details pages for public: \ncountry: {} \npage req: {}", country, pageable);
        Page<ClassListDTO> activeClasses = physicalClassService.getActivePhysicalClasses(pageable, country, token);
        log.info("\nResponse: classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, activeClasses));
    }

    @GetMapping(value = "/trainer/{trainerId}")
    public ResponseEntity<CommonResponse<List<ClassListDTO>>> getClassesByTrainer(@PathVariable(value = "trainerId") long trainerId,
                                                                                  @RequestHeader("Authorization") String token) {
        log.info("\nGet class details by trainer for public \ntrainer user: {}", trainerId);
        List<ClassListDTO> classesByTrainer = physicalClassService.getPhysicalClassesByTrainer(trainerId, token);
        log.info("Response: physical classes of trainer ");
        return ResponseEntity.ok(new CommonResponse<>(true, classesByTrainer));
    }

    @PostMapping(value = "/rate")
    public ResponseEntity ratePhysicalClassByUser(@RequestBody ClassRateDTO rateDTO, @RequestHeader("Authorization") String token) {
        log.info("\nPublic user rate physical class: " + rateDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(rateDTO.getUserId(), token);
        physicalClassService.ratePhysicalClass(rateDTO, 0);
        log.info("Physical Class is rated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Physical class is rated."));
    }

    @GetMapping(value = "/{classId}/user/{userId}/ratings")
    public ResponseEntity getRatingForPhysicalClass(@PathVariable("userId") long userId, @PathVariable("classId") long classId,
                                                    @RequestHeader("Authorization") String token) {
        log.info("Get rating details by physical class by public user: user id - {}\t: class id - {}", userId, classId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        ClassRateDTO rateForClassByUser = physicalClassService.getRateForPhysicalClassByUser(userId, classId);
        log.info("Rating details by physical class- {]", rateForClassByUser);
        return ResponseEntity.ok(new CommonResponse<>(true, rateForClassByUser));
    }

    @GetMapping(value = "/{classId}/ratings")
    public ResponseEntity getRatingOfTrainerByClass(@PathVariable("classId") long classId,
                                                    @RequestHeader("Authorization") String token, Pageable pageable) {

        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        log.info("\nGet physical class reviews by public user: physical class id - {}\t: pageable - {}", classId, pageable);
        Page<PublicUserReviewsResponse> publicUserReviews = physicalClassService.getPhysicalClassRatingsByUser(classId, pageable);
        log.info("\nPhysical class reviews by class");
        return ResponseEntity.ok(new CommonResponse<>(true, publicUserReviews));
    }

    @PostMapping(value = "/rate-class-and-trainer")
    public ResponseEntity ratePhysicalClassAndTrainerByUser(@RequestBody ClassAndTrainerRateDTO rateDTO,
                                                            @RequestHeader("Authorization") String token) {
        log.info("\nPublic user rate physical class and trainer: " + rateDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(rateDTO.getUserId(), token);
        physicalClassService.ratePhysicalClassAndTrainer(rateDTO, 0);
        log.info("Physical class and trainer are rated. {} ");
        return ResponseEntity.ok(new CommonResponse<>(true, "Physical class and trainer are rated."));
    }

    @GetMapping(value = "/{classId}/sessions")
    public ResponseEntity<CommonResponse<Page<ClassSessionListResponse>>> getSessionsByClass(
            @RequestParam(value = "dateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @PathVariable("classId") long classId,
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            @RequestHeader("Authorization") String token, Pageable pageable) {
        log.info("\nGet sessions by class: {}\tpage: {}\tdate: {}", classId, pageable, dateTime);
        Page<ClassSessionListResponse> sessionsForHome = physicalClassService
                .getSessionsListByPhysicalClassId(classId, dateTime, token, longitude, latitude, pageable);
        log.info("Response: sessions list");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionsForHome));
    }

    @GetMapping(value = "/session/date/{className}/{dateTime}")
    public ResponseEntity<CommonResponse<ClassSessionSingleResponse>> getSessionDetailsByClassNameAndDate(
            @PathVariable(value = "className") String className,
            @PathVariable(value = "dateTime") @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet physical session details for public user view session by class name and date \tclass name: {} \tdate: {}", className, dateTime);
        ClassSessionSingleResponse classSession = physicalClassService.getPhysicalSessionByClassNameAndDate(className, dateTime, token,
                CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone), longitude, latitude);
        log.info("Response: {}", classSession);
        return ResponseEntity.ok(new CommonResponse<>(true, classSession));
    }

}
