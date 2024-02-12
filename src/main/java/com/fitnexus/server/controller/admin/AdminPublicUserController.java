package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.admin.FrontendEventDTO;
import com.fitnexus.server.dto.classsession.SessionEnrollPublicDTO;
import com.fitnexus.server.dto.common.ChartDataDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.common.DashBoardDTO;
import com.fitnexus.server.dto.instructor.InstructorPackageEnrollPublicDTO;
import com.fitnexus.server.dto.membership.MembershipEnrollPublicDTO;
import com.fitnexus.server.dto.packages.PublicUserPackageSubscribeDTO;
import com.fitnexus.server.dto.publicuser.PublicUserAdminDTO;
import com.fitnexus.server.enums.EventType;
import com.fitnexus.server.service.AdminPublicUserService;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/public")
public class AdminPublicUserController {

    private final AdminPublicUserService adminPublicUserService;
    private final PublicUserService publicUserService;

    @GetMapping(value = "/users")
    public ResponseEntity getTotalNumberOfUsers() {
        log.info("Get total numbers of users");
        long numberOfUsers = adminPublicUserService.getNumberOfUsers();
        log.info("Response : Number of total users-{}", numberOfUsers);
        return ResponseEntity.ok(new CommonResponse<>(true, numberOfUsers));
    }

    @GetMapping(value = "/enrollments")
    public ResponseEntity getTotalNumberOfEnrollments(@RequestHeader(name = "Authorization") String token) {
        log.info("Get total numbers of enrollments");
        String username = getUsername(token);
        long totalNumberOfEnrollments = adminPublicUserService.getTotalNumberOfEnrollments(username);
        log.info("Number of total enrollments - {}", totalNumberOfEnrollments);
        return ResponseEntity.ok(new CommonResponse<>(true, totalNumberOfEnrollments));
    }

    @GetMapping(value = "/dashboard")
    public ResponseEntity getDashBoarDetails(@RequestHeader(name = "Authorization") String token) {
        log.info("Get dashboard details");
        String username = getUsername(token);
        DashBoardDTO dashBoarDetails = adminPublicUserService.getDashBoarDetails(username);
        log.info("Response : Dashboard details - {}", dashBoarDetails);
        return ResponseEntity.ok(new CommonResponse<>(true, dashBoarDetails));
    }

    @GetMapping(value = "/user")
    public ResponseEntity getAllUsers(Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        log.info("Get all users");
        String username = getUsername(token);
        Page<PublicUserAdminDTO> users = adminPublicUserService.getAllUsers(pageable, username);
        log.info("Response - users page");
        return ResponseEntity.ok(new CommonResponse<>(true, users));
    }

    @PostMapping(value = "/user/search")
    public ResponseEntity searchUsers(@RequestBody Map<String, String> data, Pageable pageable, @RequestHeader(name = "Authorization") String token) {
        log.info("Search users : \ntext: {}", data.get("data"));
        String username = getUsername(token);
        Page<PublicUserAdminDTO> users = adminPublicUserService.searchUsers(data.get("data"), pageable, username);
        log.info("Response - users page");
        return ResponseEntity.ok(new CommonResponse<>(true, users));
    }

    @GetMapping(value = "/user/{id}")
    public ResponseEntity getPublicUserById(@PathVariable("id") long id) {
        log.info("Get public user by id : \nuser id: {}", id);
        PublicUserAdminDTO user = adminPublicUserService.getPublicUserById(id);
        log.info("Response - user by id");
        return ResponseEntity.ok(new CommonResponse<>(true, user));
    }

    @GetMapping(value = "/session/physical/enrollments/{id}")
    public ResponseEntity getPhysicalClassEnrollments(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get physical session enrolls by public user : \nid: {}", id);
        Page<SessionEnrollPublicDTO> enrollments = adminPublicUserService.getPhysicalClassEnrollments(id, pageable);
        log.info("Response - Physical session enrollment page");
        return ResponseEntity.ok(new CommonResponse<>(true, enrollments));
    }

    @GetMapping(value = "/package/enrollments/{id}")
    public ResponseEntity getInstructorPackageEnrollsByPublicUserId(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get instructor package enrolls by public user id : \nid: {}", id);
        Page<InstructorPackageEnrollPublicDTO> instructorEnrollments = adminPublicUserService.getInstructorEnrollments(id, pageable);
        log.info("Response - Instructor enrollment page");
        return ResponseEntity.ok(new CommonResponse<>(true, instructorEnrollments));
    }

    @GetMapping(value = "/membership/physical-class/enrollments/{id}")
    public ResponseEntity getPhysicalCLassMembershipEnrollments(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get physical class membership enrollments by public user id : \nid: {}", id);
        Page<MembershipEnrollPublicDTO> enrollments = adminPublicUserService.getPhysicalCLassMembershipEnrollments(id, pageable);
        log.info("Response - Physical class membership enrollment page");
        return ResponseEntity.ok(new CommonResponse<>(true, enrollments));
    }

    @GetMapping(value = "/membership/gym/enrollments/{id}")
    public ResponseEntity getGymMembershipEnrollments(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get gym membership enrollments by public user id : \nid: {}", id);
        Page<MembershipEnrollPublicDTO> enrollments = adminPublicUserService.getGymMembershipEnrollments(id, pageable);
        log.info("Response - Gym membership enrollment page");
        return ResponseEntity.ok(new CommonResponse<>(true, enrollments));
    }

    @GetMapping(value = "/class-package/enrollments/{id}")
    public ResponseEntity getClassPackageEnrollments(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get class package enrollments by public user id : \nid: {}", id);
        Page<PublicUserPackageSubscribeDTO> subscriptions = adminPublicUserService.getPackageSubscriptionsForPublicUser(id, pageable);
        log.info("Response -Class package enrollments page");
        return ResponseEntity.ok(new CommonResponse<>(true, subscriptions));
    }

    @PostMapping(value = "/chart")
    public ResponseEntity chart(@RequestBody Map<String, LocalDateTime> data, @RequestHeader(name = "Authorization") String token) {
        LocalDateTime start = data.get("start");
        LocalDateTime end = data.get("end");
        log.info("Chart : \nstart: {} \nend: {}", start, end);
        String username = getUsername(token);
        ChartDataDTO chartData = adminPublicUserService.getChartData(start, end, username);
        log.info("Response - chart data");
        return ResponseEntity.ok(new CommonResponse<>(true, chartData));
    }

    @GetMapping(value = "/event/{eventType}/track/{id}")
    public ResponseEntity getEventTrackData(@PathVariable("eventType") EventType eventType, @PathVariable("id") long id) {
        log.info("Get eventType : {} event track public user : {}", eventType, id);
        FrontendEventDTO frontendEventDTO = publicUserService.getFrontendEvents(eventType, id);
        return ResponseEntity.ok(new CommonResponse<>(true, frontendEventDTO));
    }

    //This endpoint is created for permanently delete a public user from the database and this is used at a special request from client. This is not used in mobile or web.
    @DeleteMapping(value = "/delete/{mobile}")
    public ResponseEntity deletePublicUser(@PathVariable("mobile") String mobile) {
        log.info("Delete public user - {}", mobile);
        publicUserService.deletePublicUser(mobile);
        return ResponseEntity.ok(new CommonResponse<>(true, "Public User{" + mobile + "} Deleted Successfully!"));
    }

    @PostMapping(value = "/timezone")
    public ResponseEntity updateUserTimeZone() {
        log.info("Update user timezones");
        publicUserService.updateUserTimeZone();
        return ResponseEntity.ok(new CommonResponse<>(true, "Time zones updated Successfully!"));
    }

    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}
