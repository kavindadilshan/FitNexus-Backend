package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.classes.ClassDTO;
import com.fitnexus.server.dto.classes.ClassNameIdDTO;
import com.fitnexus.server.dto.classes.ClassRatingDTO;
import com.fitnexus.server.dto.classes.ClassTypeDTO;
import com.fitnexus.server.dto.classsession.SessionCreateDTO;
import com.fitnexus.server.dto.classsession.SessionDTO;
import com.fitnexus.server.dto.classsession.SessionEnrollDTO;
import com.fitnexus.server.dto.common.ClassAndSessionSearchDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.instructpackage.InstructorPackageSummeryDTO;
import com.fitnexus.server.dto.membership.MembershipSummeryDTO;
import com.fitnexus.server.dto.packages.ClassForPackageDTO;
import com.fitnexus.server.dto.trainer.TrainerNameIdDTO;
import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.ClassMethod;
import com.fitnexus.server.enums.SessionGetDateType;
import com.fitnexus.server.service.ClassService;
import com.fitnexus.server.service.ClassSessionService;
import com.fitnexus.server.service.InstructorService;
import com.fitnexus.server.service.MembershipService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/class")
public class AdminClassController {

    private final ClassService classService;
    private final ClassSessionService classSessionService;
    private final MembershipService membershipService;
    private final InstructorService instructorService;

    @GetMapping(value = "/all/profile/{id}")
    public ResponseEntity getAllClassesForBusinessProfile(@PathVariable long id) {
        log.info("Get all classes for business profile : \nprofile id: {} ", id);
        List<ClassNameIdDTO> classes = classService.getAllClassesForBusinessProfile(id);
        log.info("Response : class name id list");
        return ResponseEntity.ok(new CommonResponse<>(true, classes));
    }

    @GetMapping(value = "/all/category/{category}/type/{typeId}")
    public ResponseEntity getAllClassesByClassCategoryAndClassType(@PathVariable ClassCategory category, @PathVariable long typeId) {
        log.info("Get all classes by class category and type : \ncategory: {} \ntype id: {}", category, typeId);
        List<ClassForPackageDTO> classes = classService.getAllClassesByClassCategoryAndClassType(category, typeId);
        log.info("Response : class details list");
        return ResponseEntity.ok(new CommonResponse<>(true, classes));
    }

    //class type

    @PostMapping(value = "/type/create")
    public ResponseEntity createClassType(@RequestBody ClassTypeDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Create class type : \nclass type dto: {}", dto);
        String username = getUsername(token);
        classService.createClassType(dto, username);
        log.info("Response : Class type created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class type created successfully"));
    }

    @PutMapping(value = "/type/update")
    public ResponseEntity updateClassType(@RequestBody ClassTypeDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Update class type : \nclass type dto : {}", dto);
        String username = getUsername(token);
        classService.updateClassType(dto, username);
        log.info("Response : Class type updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class type updated successfully"));
    }

    @DeleteMapping(value = "/type/remove/{id}")
    public ResponseEntity removeClassType(@PathVariable("id") long id) {
        log.info("Remove class type : \nclass type id : {}", id);
        classService.removeClassType(id);
        log.info("Response : Class type removed successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class type removed successfully"));
    }

    @GetMapping(value = "/type/all")
    public ResponseEntity getAllClassTypes(Pageable pageable) {
        log.info("Get all class Types page");
        Page<ClassTypeDTO> typePage = classService.getAllClassTypes(pageable);
        log.info("Response : class types page");
        return ResponseEntity.ok(new CommonResponse<>(true, typePage));
    }

    @GetMapping(value = "/types")
    public ResponseEntity getAllClassTypes(@RequestParam(value = "corporateId", required = false) Long corporateId,
                                           @RequestParam(value = "packageId", required = false) Long packageId) {
        log.info("get all class types list");
        if (corporateId == null) corporateId = 0L;
        if (packageId == null) packageId = 0L;
        List<ClassTypeDTO> allClassTypes = classService.getAllClassTypes(corporateId, packageId);
        log.info("Response : class types list");
        return ResponseEntity.ok(new CommonResponse<>(true, allClassTypes));
    }

    @PostMapping(value = "/type/search")
    public ResponseEntity searchClassType(@RequestBody Map<String, String> data, Pageable pageable) {
        log.info("Search class type : \ndata : {}", data.get("data"));
        Page<ClassTypeDTO> typePage = classService.searchClassType(data.get("data"), pageable);
        log.info("Response : class type search page");
        return ResponseEntity.ok(new CommonResponse<>(true, typePage));
    }


    //class

    @PostMapping(value = "/create")
    public ResponseEntity createClass(@RequestBody ClassDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Create class : \nclass dto: {}", dto);
        String username = getUsername(token);
        classService.createClass(dto, username);
        log.info("Response : Class created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class created successfully"));
    }

    @PutMapping(value = "/update")
    public ResponseEntity updateClass(@RequestBody ClassDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Update class : \nclass dto: {}", dto);
        String username = getUsername(token);
        classService.updateClass(dto, username);
        log.info("Response : Class updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class updated successfully"));
    }

    @GetMapping(value = "/all/byprofile/{id}")
    public ResponseEntity getAllClassesByProfile(@PathVariable("id") long id, @RequestParam("category") ClassCategory category, Pageable pageable) {
        log.info("Get all classes by business profile : \nbusiness profile id: {} \nclass category: {}", id, category);
        Page<ClassDTO> allClasses = classService.getAllClassesByProfileAndCategory(id, category, pageable);
        log.info("Response : classes by profile page");
        return ResponseEntity.ok(new CommonResponse<>(true, allClasses));
    }

    @GetMapping(value = "/all")
    public ResponseEntity getAllClasses(Pageable pageable) {
        log.info("Get all classes");
        Page<ClassDTO> allClasses = classService.getAllClasses(pageable);
        log.info("Response : classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, allClasses));
    }

    @GetMapping(value = "/all/names")
    public ResponseEntity getAllClasses(@RequestParam(value = "corporateId", required = false) Long corporateId,
                                        @RequestParam(value = "packageId", required = false) Long packageId) {
        log.info("Get all class names and IDs");
        if (corporateId == null) corporateId = 0L;
        if (packageId == null) packageId = 0L;
        List<ClassNameIdDTO> allClasses = classService.getAllClasses(corporateId, packageId);
        log.info("Response : class names and IDs list");
        return ResponseEntity.ok(new CommonResponse<>(true, allClasses));
    }

    @GetMapping(value = "/all/names/{typeId}")
    public ResponseEntity getAllClassesByClassType(@PathVariable("typeId") long typeId,
                                                   @RequestParam(value = "corporateId", required = false) Long corporateId,
                                                   @RequestParam(value = "packageId", required = false) Long packageId) {
        log.info("Get all class names and IDs by class type - {}", typeId);
        if (corporateId == null) corporateId = 0L;
        if (packageId == null) packageId = 0L;
        List<ClassNameIdDTO> allClasses = classService.getAllClassesByClassType(typeId, corporateId, packageId);
        log.info("Response : class names and IDs list (by class type)");
        return ResponseEntity.ok(new CommonResponse<>(true, allClasses));
    }

    @PostMapping(value = "/search")
    public ResponseEntity searchClass(@RequestBody ClassAndSessionSearchDTO dto, Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        log.info("Search class : \ntext: {} \ncategory: {} \nbusiness profile id: {} ", dto.getText(), dto.getCategory(), dto.getBusinessProfileId());
        String username = getUsername(token);
        Page<ClassDTO> classes = classService.searchClass(dto.getText(), dto.getCategory(), dto.getBusinessProfileId(), pageable, username);
        log.info("Response : class search page");
        return ResponseEntity.ok(new CommonResponse<>(true, classes));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity getClassById(@PathVariable("id") long id) {
        log.info("Get class by id : \nclass id: {}", id);
        ClassDTO classById = classService.getClassById(id);
        log.info("Response : class by ID ");
        return ResponseEntity.ok(new CommonResponse<>(true, classById));
    }

    @GetMapping(value = "/ratings/{id}")
    public ResponseEntity getRatingsForClass(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get ratings by class : \nclass id: {}", id);
        Page<ClassRatingDTO> classRatings = classService.getClassRatings(id, pageable);
        log.info("Response : class rating page");
        return ResponseEntity.ok(new CommonResponse<>(true, classRatings));
    }

    //session
    @PostMapping(value = "/session")
    public ResponseEntity createSession(@RequestBody SessionCreateDTO dto) {
        log.info("Create session : \nsession dto: {} ", dto);
        classSessionService.createSession(dto);
        log.info("Response : Class session created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class session created successfully"));
    }

    @PutMapping(value = "/session")
    public ResponseEntity updateSession(@RequestBody SessionDTO dto) {
        log.info("Update session : \nsession dto: {} " + dto);
        classSessionService.updateSession(dto);
        log.info("Response : Class session updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Class session updated successfully"));
    }

    @GetMapping(value = "/session")
    public ResponseEntity getAllSessions(Pageable pageable) {
        log.info("Get all sessions");
        Page<SessionDTO> allSessions = classSessionService.getAllSessions(pageable);
        log.info("Response : session page");
        return ResponseEntity.ok(new CommonResponse<>(true, allSessions));
    }

    @GetMapping(value = "/session/byclass/{id}")
    public ResponseEntity getAllSessionsByClass(@PathVariable("id") long id, Pageable pageable,
                                                @RequestParam(value = "dateType", required = false) SessionGetDateType dateType) {
        log.info("Get all sessions by class : \nclass id: {}", id);
        Page<SessionDTO> allSessions = classSessionService.getAllSessionsByClass(id, dateType, pageable);
        log.info("Response : session for class page");
        return ResponseEntity.ok(new CommonResponse<>(true, allSessions));
    }

    @PostMapping(value = "/session/search")
    public ResponseEntity searchSession(@RequestBody ClassAndSessionSearchDTO dto, Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        log.info("Search session : \ntext: {} \ncategory: {} \nclassId: {}", dto.getText(), dto.getCategory(), dto.getClassId());
        String username = getUsername(token);
        Page<SessionDTO> sessions = classSessionService.searchSession(dto.getText(), dto.getCategory(), dto.getClassId(), pageable, username);
        log.info("Response : session search page");
        return ResponseEntity.ok(new CommonResponse<>(true, sessions));
    }

    @GetMapping(value = "/trainers/{id}")
    public ResponseEntity getTrainersForClass(@PathVariable("id") long id) {
        log.info("Get trainers for class : \nid: {}", id);
        List<TrainerNameIdDTO> trainersForClass = classService.getTrainersForClass(id);
        log.info("Response : trainers for class list");
        return ResponseEntity.ok(new CommonResponse<>(true, trainersForClass));
    }

    @GetMapping(value = "/session/{id}")
    public ResponseEntity sessionById(@PathVariable("id") long id) {
        log.info("Get session by id : \nid: {}", id);
        SessionDTO sessionById = classSessionService.getSessionById(id);
        log.info("Response : session by id");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionById));
    }

    @PutMapping(value = "/session/cancel")
    public ResponseEntity cancelSession(@RequestParam long sessionId) {
        log.info("Cancel session : \nsession id: {}", sessionId);
        classSessionService.cancelSession(sessionId);
        log.info("Response : Session canceled successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Session canceled successfully"));
    }

    @PutMapping(value = "/session/reschedule")
    public ResponseEntity rescheduleSession(@RequestParam long sessionId, @RequestParam String dateTime) {
        log.info("Reschedule session : \nsession id: {}, date time: {}", sessionId, dateTime);
        Instant instant = Instant.parse(dateTime);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of("GMT"));
        classSessionService.rescheduleSession(sessionId, localDateTime);
        log.info("Response : Session rescheduled successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Session rescheduled successfully"));
    }

    @GetMapping(value = "/session/enrollments/{id}")
    public ResponseEntity getSessionEnrollments(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get session enrollments : \nsession id : {}", id);
        Page<SessionEnrollDTO> sessionEnrolls = classSessionService.getSessionEnrolls(id, pageable);
        log.info("Response : session enroll page");
        return ResponseEntity.ok(new CommonResponse<>(true, sessionEnrolls));
    }

    @PatchMapping(value = "/hide/{type}")
    public ResponseEntity hideClasses(@PathVariable("type") ClassMethod type) {
        log.info("Hide classes: {}", type);
        classService.hideClasses(type);
        log.info("Response : Classes hided successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Classes hided successfully"));
    }

    @PatchMapping(value = "/show/{type}")
    public ResponseEntity showClasses(@PathVariable("type") ClassMethod type) {
        log.info("Show classes: {}", type);
        classService.showClasses(type);
        log.info("Response : Classes showed successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Classes showed successfully"));
    }

    @GetMapping(value = "/online-classes/visibility")
    public ResponseEntity isOnlineClassesVisible() {
        log.info("Check online classes's visibility");
        boolean onlineClassesVisible = classService.isOnlineClassesVisible();
        log.info("Response : Online classes visible : {}", onlineClassesVisible);
        return ResponseEntity.ok(new CommonResponse<>(true, onlineClassesVisible));
    }

    @GetMapping(value = "/sessions/date/type")
    public ResponseEntity getAllSessionsByDateByAdmin(@RequestParam("date")
                                                      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                      @RequestParam(value = "category") ClassCategory category,
                                                      Pageable pageable) {
        log.info("Get class sessions by date and category: Date: {}\tCategory: {}\tPage: {}", date, category, pageable);
        Page<SessionDTO> sessions = classSessionService.getAllSessionsByDateByAdmin(date, category, pageable);
        log.info("Response : Get class sessions by date and category");
        return ResponseEntity.ok(new CommonResponse<>(true, sessions));
    }

    @GetMapping(value = "/gym-memberships/date/type")
    public ResponseEntity getAllGymMembershipsByDateRangeByAdmin(@RequestParam("startDate")
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                 @RequestParam("endDate")
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                 Pageable pageable) {
        log.info("Get gym memberships by date-range: StartDate: {}\tEndDate: {}\tPage: {}", startDate, endDate, pageable);
        Page<MembershipSummeryDTO> allGymMembershipSummery = membershipService.getAllGymMembershipSummery(startDate, endDate, pageable);
        log.info("Response : Get gym memberships by date-range");
        return ResponseEntity.ok(new CommonResponse<>(true, allGymMembershipSummery));
    }

    @GetMapping(value = "/online-coaching/date/type")
    public ResponseEntity getAllOnlineCoachingByDateRangeByAdmin(@RequestParam("startDate")
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                                 @RequestParam("endDate")
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                                 Pageable pageable) {
        log.info("Get online-coaching by date-range: StartDate: {}\tEndDate: {}\tPage: {}", startDate, endDate, pageable);
        Page<InstructorPackageSummeryDTO> instructorPackageSummery = instructorService.getAllInstructorPackageSummery(startDate, endDate, pageable);
        log.info("Response : Get online-coaching by date-range");
        return ResponseEntity.ok(new CommonResponse<>(true, instructorPackageSummery));
    }

    @PatchMapping(value = "/visible/{id}")
    public ResponseEntity updateClassVisible(@PathVariable("id") long id,
                                             @RequestParam boolean visible,
                                             @RequestHeader(AUTHORIZATION) String token) {
        log.info("Update online class visibility : \nonline class id: {}", id);
        classService.updateClassVisible(id, visible);
        log.info("Response : Online class visibility saved successful");
        return ResponseEntity.ok(new CommonResponse<>(true, "Online class visibility saved successful"));
    }

    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}
