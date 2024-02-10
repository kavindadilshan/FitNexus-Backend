package com.fitnexus.server.controller.admin.delete;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.*;
import com.fitnexus.server.service.*;
import com.fitnexus.server.service.auth.AuthUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/delete")
public class AdminDeleteController {

    private final BusinessProfileService businessProfileService;
    private final ClassService classService;
    private final AdminPublicUserService adminPublicUserService;
    private final GymService gymService;
    private final PhysicalClassService physicalClassService;
    private final AuthUserService authUserService;

    @DeleteMapping(value = "/profile/{id}")
    public ResponseEntity deleteProfile(@PathVariable("id") long id) {
        businessProfileService.deleteBusinessProfile(id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Business Profile deleted successfully"));
    }

    @DeleteMapping(value = "/class/online/{classId}")
    public ResponseEntity deleteOnlineClass(@PathVariable(value = "classId") long classId) {
        log.info("\nDelete online class: {}", classId);
        classService.deleteClass(classId);
        return ResponseEntity.ok(new CommonResponse<>(true, "Online class is deleted!"));
    }

    @DeleteMapping(value = "/public/{mobile}")
    public ResponseEntity deletePublicUser(@PathVariable(value = "mobile") String mobile) {
        log.info("\nDelete public user: {}", mobile);
        adminPublicUserService.deletePublicUser(mobile);
        log.info("\nDelete public user success: {}", mobile);
        return ResponseEntity.ok(new CommonResponse<>(true, "Public user is deleted!"));
    }

    @DeleteMapping(value = "/gym/{id}")
    public ResponseEntity deleteGym(@PathVariable(value = "id") long id) {
        log.info("\nDelete gym: {}", id);
        gymService.delete(id);
        log.info("\nDelete gym success: {}", id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Gym is deleted!"));
    }

    @DeleteMapping(value = "/class/physical/{id}")
    public ResponseEntity deletePhysicalClass(@PathVariable(value = "id") long id) {
        log.info("\nDelete Physical Class: {}", id);
        physicalClassService.delete(id);
        log.info("\nDelete Physical Class success: {}", id);
        return ResponseEntity.ok(new CommonResponse<>(true, "Physical Class is deleted!"));
    }

    @DeleteMapping(value = "/coach/{username}")
    public ResponseEntity deleteAuthUser(@PathVariable(value = "username") String username) {
        log.info("\nDelete auth user: {}", username);
        authUserService.deleteAuthUser(username);
        return ResponseEntity.ok(new CommonResponse<>(true, "Auth User is deleted!"));
    }

}
