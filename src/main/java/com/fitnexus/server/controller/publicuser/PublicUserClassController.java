package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.classes.*;
import com.fitnexus.server.dto.classsession.ClassSessionListResponse;
import com.fitnexus.server.dto.classsession.ClassSessionSingleResponse;
import com.fitnexus.server.dto.classsession.SessionZoomDetails;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.Gender;
import com.fitnexus.server.service.ClassService;
import com.fitnexus.server.service.ClassSessionService;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.GuestUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@RequestMapping(value = "/users/class")
public class PublicUserClassController {

    private final ClassSessionService classSessionService;
    private final ClassService classService;

    @GetMapping(value = "/{classId}")
    public ResponseEntity<CommonResponse<ClassListDTO>> getClassDetails(
            @RequestParam(value = "dateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @PathVariable(value = "classId") long classId, @RequestHeader("Authorization") String token) {
        log.info("\nGet class details for public user view class \tClass Id: {}\tdate", classId, dateTime);
        ClassListDTO classDetails = classService.getClassDetails(classId, dateTime, token);
        log.info("Response: Class details by id");
        return ResponseEntity.ok(new CommonResponse<>(true, classDetails));
    }

    @GetMapping(value = "/session/{sessionId}")
    public ResponseEntity<CommonResponse<ClassSessionSingleResponse>> getSessionDetails(
            @PathVariable(value = "sessionId") long sessionId, @RequestHeader("Authorization") String token,
            @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet session details for public user view session \tSession Id: {}", sessionId);
        ClassSessionSingleResponse classSession = classSessionService.getClassSession(sessionId, token,
                CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone));
        log.info("Response: class session by id");
        return ResponseEntity.ok(new CommonResponse<>(true, classSession));
    }

    @GetMapping(value = "/types")
    public ResponseEntity<CommonResponse<List<ClassTypeDTO>>> getClassTypes() {
        log.info("\nGet class types for public user ");
        List<ClassTypeDTO> classTypes = classSessionService.getClassTypes();
        log.info("Response: Class Types list");
        return ResponseEntity.ok(new CommonResponse<>(true, classTypes));
    }

    @GetMapping(value = "/sessions")
    public ResponseEntity<CommonResponse<Page<ClassSessionListResponse>>> getSessionsForHome(
            @RequestParam(value = "startDateTime", required = true) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime startDateTime,
            @RequestParam(value = "endDateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime endDateTime,
            @RequestParam(value = "gender") Gender gender,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "classTypes", required = false) List<Long> classTypeIds,
            @RequestParam(value = "category", required = false) ClassCategory category,
            @RequestParam(value = "corporateOnly", required = false) boolean corporateOnly,
            @RequestParam(value = "corporateMembershipId", required = false) String corporateMembershipId,
            @RequestParam(value = "packageOnly", required = false) boolean packageOnly,
            Pageable pageable,
            @RequestHeader("Authorization") String token, @RequestHeader(TIME_ZONE_HEADER) String timeZone) {
        if (name == null) name = "";
        if (classTypeIds == null) {
            classTypeIds = Collections.emptyList();
        } else {
            if (classTypeIds.size() == 1) {
                classTypeIds.add((long) 0);
            }
        }
        log.info("\nGet class sessions for week for public user \tstart: {}\tend: {}\tgender: {}\tname: {}" +
                        "\tclassTypes: {} \tCategory: {} \tcorporateOnly: {} \tcorporateMembershipId: {} \tpackageOnly: {}\tpage: {}\tTimeZone: {}",
                startDateTime, endDateTime, gender, name, classTypeIds, category, corporateOnly, corporateMembershipId, packageOnly, pageable, timeZone);
        long corporateMembershipIdConverted = corporateMembershipId == null ? 0 : Long.parseLong(corporateMembershipId);
        Page<ClassSessionListResponse> sessionsForHome = classSessionService.getSessionsForHome(
                gender, name, classTypeIds, startDateTime, endDateTime, category, pageable, token, corporateOnly, corporateMembershipIdConverted, packageOnly);
        log.info("Response: sessions list");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionsForHome));
    }

    @GetMapping(value = "/popular")
    public ResponseEntity<CommonResponse<Page<ClassListDTO>>> getPopularClasses(
            @RequestParam(value = "category", required = false) ClassCategory category, Pageable pageable
            , @RequestHeader("Authorization") String token) {
        log.info("\nGet popular class details pages for public: category : {}\tpage req: {}", category, pageable);
        Page<ClassListDTO> activeClasses = classService.getActiveClasses(category, pageable, token);
        log.info("Response: classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, activeClasses));
    }

    @GetMapping(value = "/trainer/{trainerId}")
    public ResponseEntity<CommonResponse<List<ClassListDTO>>> getClassesByTrainer(@PathVariable(value = "trainerId") long trainerId,
                                                                                  @RequestParam(value = "category", required = false)
                                                                                          ClassCategory category,
                                                                                  @RequestHeader("Authorization") String token) {
        log.info("\nGet class details by trainer for public \ntrainer user: {}\tcategory: {}", trainerId, category);
        List<ClassListDTO> classesByTrainer = classService.getClassesByTrainer(trainerId, category, token);
        log.info("Response: classes of trainer ");
        return ResponseEntity.ok(new CommonResponse<>(true, classesByTrainer));
    }

    @GetMapping(value = "/session/{id}/zoom")
    public ResponseEntity getZoomSessionDetails(@PathVariable("id") long id, @RequestHeader("Authorization") String token) {
        log.info("Get zoom details by session by public user: id - {}", id);
        SessionZoomDetails sessionZoomInfo = classSessionService.getSessionZoomInfoForPublicUser(id, token);
        log.info("Response : Session Zoom Info by public user");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionZoomInfo));
    }

    @PostMapping(value = "/rate")
    public ResponseEntity rateClassByUser(@RequestBody ClassRateDTO rateDTO, @RequestHeader("Authorization") String token) {
        log.info("\nPublic user rate class: " + rateDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(rateDTO.getUserId(), token);
        classService.rateClass(rateDTO, 0);
        log.info("Response : Class is rated.");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class is rated."));
    }

    @GetMapping(value = "/{classId}/user/{userId}/ratings")
    public ResponseEntity getRatingForClass(@PathVariable("userId") long userId, @PathVariable("classId") long classId,
                                            @RequestHeader("Authorization") String token) {
        log.info("\nGet class rating details by class by public user: user id - {}\t: class id - {}", userId, classId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        ClassRateDTO rateForClassByUser = classService.getRateForClassByUser(userId, classId);
        log.info("Response : Class rating details by class");
        return ResponseEntity.ok(new CommonResponse<>(true, rateForClassByUser));
    }

    @GetMapping(value = "/{classId}/ratings")
    public ResponseEntity getRatingOfTrainerByClass(@PathVariable("classId") long classId,
                                                    @RequestHeader("Authorization") String token, Pageable pageable) {

        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        log.info("\nGet class reviews by public user: class id - {}\t: pageable - {}", classId, pageable);
        Page<PublicUserReviewsResponse> publicUserReviews = classService.getClassRatingsByUser(classId, pageable);
        log.info("Response : Class reviews by class");
        return ResponseEntity.ok(new CommonResponse<>(true, publicUserReviews));
    }

    @PostMapping(value = "/rate-class-and-trainer")
    public ResponseEntity rateClassAndTrainerByUser(@RequestBody ClassAndTrainerRateDTO rateDTO,
                                                    @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        log.info("\nPublic user rate class and trainer: " + rateDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(rateDTO.getUserId(), token);
        classService.rateClassAndTrainer(rateDTO, 0);
        log.info("Response : Class and trainer are rated.");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class and trainer are rated."));
    }

    @GetMapping(value = "/{classId}/sessions")
    public ResponseEntity<CommonResponse<Page<ClassSessionListResponse>>> getSessionsByClass(
            @RequestParam(value = "dateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @PathVariable("classId") long classId,
            @RequestHeader("Authorization") String token,
            Pageable pageable) {
        log.info("\nGet sessions by class: {}\tpage: {}\tdate: {}", classId, pageable, dateTime);
        Page<ClassSessionListResponse> sessionsForHome = classSessionService
                .getClassSessionsListByClassId(classId, dateTime, token, pageable);
        log.info("Response: sessions list");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionsForHome));
    }

    @GetMapping(value = "/online-classes/visibility")
    public ResponseEntity isOnlineClassesVisible(@RequestHeader("Authorization") String token) {
        log.info("\nCheck online classes's visibility");
        boolean onlineClassesVisible = classService.isOnlineClassesVisible(token);
        log.info("Response : Online classes visible-{}", onlineClassesVisible);
        return ResponseEntity.ok(new CommonResponse<>(true, onlineClassesVisible));
    }

    @GetMapping(value = "/sessions/weekly")
    public ResponseEntity<CommonResponse<Object>> getSessionsForWeek(
            @RequestParam(value = "startDateTime", required = true) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime startDateTime,
            @RequestParam(value = "endDateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime endDateTime,
            @RequestParam(value = "gender") Gender gender,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "classTypes", required = false) List<Long> classTypeIds,
            @RequestParam(value = "category", required = false) ClassCategory category,
            @RequestParam(value = "corporateOnly", required = false) boolean corporateOnly,
            @RequestParam(value = "corporateMembershipId", required = false) String corporateMembershipId,
            @RequestParam(value = "packageOnly", required = false) boolean packageOnly,
            @RequestHeader("Authorization") String token, @RequestHeader(TIME_ZONE_HEADER) String timeZone,
            @RequestParam(value = "page", required = false) String page,
            @RequestParam(value = "size", required = false) String size) {

        if (name == null) name = "";
        if (classTypeIds == null) {
            classTypeIds = Collections.emptyList();
        } else {
            if (classTypeIds.size() == 1) {
                classTypeIds.add((long) 0);
            }
        }
        log.info("\nGet class sessions for week for public user \tstart: {}\tend: {}\tgender: {}\tname: {}" +
                        "\tclassTypes: {}\tCategory: {} corporateOnly: {}\tcorporateMembershipId: {}\tpackageOnly: {}\tTimeZone: {}\tpage: {}\tsize: {}",
                startDateTime, endDateTime, gender, name, classTypeIds, category, corporateOnly, corporateMembershipId, packageOnly, timeZone, page, size);

        long corporateMembershipIdConverted = corporateMembershipId == null ? 0 : Long.parseLong(corporateMembershipId);

        Object sessionsForWeek = null;

        if (size != null && page != null) {

            //this part is added only for the web because with the previous logic it send lot of sessions to the front end
            // and it reduce the user friendliness of the UI. By this condition only requested number of sessions are sent
            // with the pagination enabled

            log.info("Get sessions with pagination({})",timeZone);
            sessionsForWeek = classSessionService.getAllSessionsForWeekWithPagination(gender, name, classTypeIds, startDateTime,
                    endDateTime, category, token, corporateOnly, corporateMembershipIdConverted, packageOnly, PageRequest.of(Integer.parseInt(page), Integer.parseInt(size)));

            //end of the new pagination condition

        } else {
            log.info("Get sessions without pagination({})",timeZone);
            sessionsForWeek = classSessionService.getAllSessionsForWeek(
                    gender, name, classTypeIds, startDateTime, endDateTime, category, token, corporateOnly, corporateMembershipIdConverted, packageOnly);
        }

        log.info("Response: sessions list");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionsForWeek));
    }

    @GetMapping(value = "/class-name/{className}")
    public ResponseEntity<CommonResponse<ClassListDTO>> getClassDetails(
            @RequestParam(value = "dateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @PathVariable(value = "className") String className, @RequestHeader("Authorization") String token) {
        log.info("\nGet class details for public user view class \tClass Name: {}\tdate: {}", className, dateTime);
        ClassListDTO classDetails = classService.getClassDetailsByClassName(className, dateTime, token);
        log.info("Response: Class details by class name");
        return ResponseEntity.ok(new CommonResponse<>(true, classDetails));
    }

    @GetMapping(value = "/popular/upcoming/session")
    public ResponseEntity<CommonResponse<Page<ClassSessionListResponse>>> getUpComingPopularClasses(
            @RequestParam(value = "category", required = false) ClassCategory category,
            Pageable pageable
            , @RequestHeader("Authorization") String token) {
        log.info("\nGet upcoming class session details page : category : {}\tpage req: {}", category, pageable);
        Page<ClassSessionListResponse> upcomingSessionsByDate = classSessionService.getUpcomingSessionsForHome(category, pageable, token);
        log.info("Response: class session page");
        return ResponseEntity.ok(new CommonResponse<>(true, upcomingSessionsByDate));
    }

    @GetMapping(value = "/session/date/{className}/{dateTime}")
    public ResponseEntity<CommonResponse<ClassSessionSingleResponse>> getSessionDetailsByClassNameAndDate(
            @PathVariable(value = "className") String className,
            @PathVariable(value = "dateTime") @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet session details for public user view session \tclass Name: {} \tdateTime: {}", className, dateTime);
        ClassSessionSingleResponse classSession = classSessionService.getClassSessionByClassNameAndDate(className, token, dateTime,
                CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone));
        log.info("Response: class session by class name and date");
        return ResponseEntity.ok(new CommonResponse<>(true, classSession));
    }

    @GetMapping(value = "/type/count/weekly")
    public ResponseEntity<CommonResponse<List<ClassesPerWeekByType>>> getClassesPerWeekByClassType(@RequestHeader("Authorization") String token) {
        log.info("Get classes per week by class types");
        List<ClassesPerWeekByType> classesPerWeekByType = classService.getClassesPerWeekByType();
        log.info("Response: weekly session count by class type");
        return ResponseEntity.ok(new CommonResponse<>(true, classesPerWeekByType));
    }
}
