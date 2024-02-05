package com.fitnexus.server.controller.admin;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.classes.ClassDTO;
import com.fitnexus.server.dto.classes.ClassNameIdDTO;
import com.fitnexus.server.dto.classes.ClassRatingDTO;
import com.fitnexus.server.dto.classsession.SessionCreateDTO;
import com.fitnexus.server.dto.classsession.SessionDTO;
import com.fitnexus.server.dto.classsession.SessionEnrollDTO;
import com.fitnexus.server.dto.common.ClassAndSessionSearchDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.trainer.TrainerNameIdDTO;
import com.fitnexus.server.enums.SessionGetDateType;
import com.fitnexus.server.service.PhysicalClassService;
import com.fitnexus.server.service.UserSessionEnrollService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/class/physical")
public class AdminPhysicalClassController {

    private final PhysicalClassService physicalClassService;
    private final UserSessionEnrollService userSessionEnrollService;

    @GetMapping(value = "/all/profile/{id}")
    public ResponseEntity getAllPhysicalClassesForBusinessProfile(@PathVariable long id) {
        log.info("Get all physical classes by business profile : \nbusiness profile id : " + id);
        List<ClassNameIdDTO> classes = physicalClassService.getAllPhysicalClassesForBusinessProfile(id);
        log.info("Response : Class name id list");
        return ResponseEntity.ok(new CommonResponse<>(true, classes));
    }

    @PostMapping(value = "/create")
    public ResponseEntity createPhysicalClass(@RequestBody ClassDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Create physical class : \nclass dto: {} ", dto);
        String username = getUsername(token);
        physicalClassService.createPhysicalClass(dto, username);
        log.info("Response : Physical class created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class created successfully"));
    }

    @PutMapping(value = "/update")
    public ResponseEntity updatePhysicalClass(@RequestBody ClassDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Update physical class : \nclass dto: {} ", dto);
        String username = getUsername(token);
        physicalClassService.updatePhysicalClass(dto, username);
        log.info("Response : Physical class updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class updated successfully"));
    }

    @GetMapping(value = "/all/by-profile/{id}")
    public ResponseEntity getAllPhysicalClassesByProfile(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get all physical classes by business profile : \nbusiness profile id: {}", id);
        Page<ClassDTO> allClasses = physicalClassService.getAllPhysicalClassesByProfile(id, pageable);
        log.info("Response : Physical class page");
        return ResponseEntity.ok(new CommonResponse<>(true, allClasses));
    }

    @GetMapping(value = "/all")
    public ResponseEntity getAllPhysicalClasses(Pageable pageable) {
        log.info("Get all classes");
        Page<ClassDTO> allClasses = physicalClassService.getAllPhysicalClasses(pageable);
        log.info("Response : Physical class page");
        return ResponseEntity.ok(new CommonResponse<>(true, allClasses));
    }

    @PostMapping(value = "/search")
    public ResponseEntity searchPhysicalClass(@RequestBody ClassAndSessionSearchDTO dto, Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        log.info("Search physical class : \ntext: {} \ncategory: {} \nbusinessProfileId: {}", dto.getText(), dto.getCategory(), dto.getBusinessProfileId());
        String username = getUsername(token);
        Page<ClassDTO> classes = physicalClassService.searchPhysicalClass(dto.getText(), dto.getBusinessProfileId(), pageable, username);
        log.info("Response : Physical class search page");
        return ResponseEntity.ok(new CommonResponse<>(true, classes));
    }

    @PostMapping(value = "/search/name")
    public ResponseEntity searchPhysicalClassByName(@RequestBody ClassAndSessionSearchDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Search physical class by name : \nname: {}", dto.getText());
        String username = getUsername(token);
        List<ClassDTO> classes = physicalClassService.searchPhysicalClassByName(dto.getText(), username);
        log.info("Response : Physical class search page");
        return ResponseEntity.ok(new CommonResponse<>(true, classes));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity getPhysicalClassById(@PathVariable("id") long id) {
        log.info("Get physical class by id : \nphysical class id: {}", id);
        ClassDTO classById = physicalClassService.getPhysicalClassById(id);
        log.info("Response : Physical class by ID ");
        return ResponseEntity.ok(new CommonResponse<>(true, classById));
    }

    @GetMapping(value = "/ratings/{id}")
    public ResponseEntity getRatingsForPhysicalClass(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get ratings for physical class : \nphysical class id - {}", id);
        Page<ClassRatingDTO> classRatings = physicalClassService.getPhysicalClassRatings(id, pageable);
        log.info("Response : physical class rating page");
        return ResponseEntity.ok(new CommonResponse<>(true, classRatings));
    }

    //session
    @PostMapping(value = "/session")
    public ResponseEntity createPhysicalSession(@RequestBody SessionCreateDTO dto) {
        log.info("Create physical session : \nsession dto: {}", dto);
        physicalClassService.createPhysicalSession(dto);
        log.info("Response : Physical session created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Physical session created successfully"));
    }

    @PutMapping(value = "/session")
    public ResponseEntity updatePhysicalSession(@RequestBody SessionDTO dto) {
        log.info("Update physical session : \nsession dto: {}", dto);
        physicalClassService.updatePhysicalSession(dto);
        log.info("Response : Physical session updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Physical session updated successfully"));
    }

    @GetMapping(value = "/session")
    public ResponseEntity getAllPhysicalSessions(Pageable pageable) {
        log.info("Get all physical sessions");
        Page<SessionDTO> allSessions = physicalClassService.getAllPhysicalSessions(pageable);
        log.info("Response - session page");
        return ResponseEntity.ok(new CommonResponse<>(true, allSessions));
    }

    @GetMapping(value = "/session/by-class/{id}")
    public ResponseEntity getAllPhysicalSessionsByClass(@PathVariable("id") long id,
                                                        @RequestParam(value = "dateType", required = false) SessionGetDateType dateType, Pageable pageable) {
        log.info("Get physical sessions by physical class : \nphysical class id: {}", id);
        Page<SessionDTO> allSessions = physicalClassService.getAllPhysicalSessionsByClass(id, dateType, pageable);
        log.info("Response - session for class page");
        return ResponseEntity.ok(new CommonResponse<>(true, allSessions));
    }

    @PostMapping(value = "/session/search")
    public ResponseEntity searchPhysicalSession(@RequestBody ClassAndSessionSearchDTO dto, Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        log.info("Search physical session : \ntext: {} \nclassId: {} ", dto.getText(), dto.getClassId());
        String username = getUsername(token);
        Page<SessionDTO> sessions = physicalClassService.searchPhysicalSession(dto.getText(), dto.getClassId(), pageable, username);
        log.info("Response - session page");
        return ResponseEntity.ok(new CommonResponse<>(true, sessions));
    }

    @GetMapping(value = "/trainers/{id}")
    public ResponseEntity getTrainersForPhysicalClass(@PathVariable("id") long id) {
        log.info("Get trainers for physical class : \nphysical class id: {}", id);
        List<TrainerNameIdDTO> trainersForClass = physicalClassService.getTrainersForPhysicalClass(id);
        log.info("Response : Trainers list for physical class");
        return ResponseEntity.ok(new CommonResponse<>(true, trainersForClass));
    }

    @GetMapping(value = "/session/{id}")
    public ResponseEntity getPhysicalSessionById(@PathVariable("id") long id) {
        log.info("Get physical session by id : \nphysical session id: {}", id);
        SessionDTO sessionById = physicalClassService.getPhysicalSessionById(id);
        log.info("Response : Physical session by id");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionById));
    }

    @PutMapping(value = "/session/cancel")
    public ResponseEntity cancelPhysicalSession(@RequestParam long sessionId) {
        log.info("Cancel session : \nsession id: {}", sessionId);
        physicalClassService.cancelPhysicalSession(sessionId);
        log.info("Response : Session canceled successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Session canceled successfully"));
    }

    @PutMapping(value = "/session/reschedule")
    public ResponseEntity reschedulePhysicalSession(@RequestParam long sessionId, @RequestParam String dateTime) {
        log.info("Reschedule physical session : \nsession id: {} \ndate time: {}", sessionId, dateTime);
        Instant instant = Instant.parse(dateTime);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("GMT"));
        physicalClassService.reschedulePhysicalSession(sessionId, localDateTime);
        log.info("Response : Session rescheduled successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "session rescheduled successfully"));
    }

    @GetMapping(value = "/session/enrollments/{id}")
    public ResponseEntity getPhysicalSessionEnrollments(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get physical session enrollments : \nphysical session id: {}", id);
        Page<SessionEnrollDTO> sessionEnrolls = physicalClassService.getPhysicalSessionEnrollments(id, pageable);
        log.info("Response - session enroll page");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionEnrolls));
    }

    @PatchMapping(value = "/session/pay/{id}")
    public ResponseEntity cashPaymentForPhysicalSession(@PathVariable("id") long id,
                                                        @RequestHeader(AUTHORIZATION) String token) {
        log.info("Cash payment for physical session : \nphysical session enroll id: {}", id);
        long userId = CustomUserAuthenticator.getAuthUserIdFromToken(token);
        userSessionEnrollService.cashPaymentForPhysicalSession(id, userId);
        log.info("Response : Cash payment is successful");
        return ResponseEntity.ok(new CommonResponse<>(true, "Cash payment is successful"));
    }

    @PatchMapping(value = "/visible/{id}")
    public ResponseEntity updateClassVisible(@PathVariable("id") long id,
                                             @RequestParam boolean visible,
                                             @RequestHeader(AUTHORIZATION) String token) {
        log.info("Update physical class visibility : \nphysical class id: {}", id);
        physicalClassService.updateClassVisible(id, visible);
        log.info("Response : Physical class visibility saved successful");
        return ResponseEntity.ok(new CommonResponse<>(true, "Physical class visibility saved successful"));
    }


    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}
