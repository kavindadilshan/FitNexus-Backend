package com.fitnexus.server.controller.admin;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.classes.ClassMembershipAdminDTO;
import com.fitnexus.server.dto.classes.OnlineClassMembershipAdminDTO;
import com.fitnexus.server.dto.classes.OnlineClassMembershipDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.gym.GymMembershipAdminDTO;
import com.fitnexus.server.dto.gym.GymMembershipDTO;
import com.fitnexus.server.dto.membership.MembershipDTO;
import com.fitnexus.server.dto.membership.MembershipEnrollDTO;
import com.fitnexus.server.dto.membership.MembershipSearchDTO;
import com.fitnexus.server.dto.physical_class.PhysicalCLassMembershipDTO;
import com.fitnexus.server.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.AUTHORIZATION;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/membership")
public class AdminMembershipController {

    private final MembershipService membershipService;




//----------------------------------------------------------------------------------------------------------------------------

    //gym

    @PostMapping(value = "/gym")
    public ResponseEntity createGymMembership(@RequestBody GymMembershipDTO dto) {
        log.info("Create gym membership : \nmembership dto: {}", dto);
        membershipService.createGymMembership(dto);
        log.info("Response : Gym membership created successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Gym membership created successfully"));
    }

    @PutMapping(value = "/gym")
    public ResponseEntity updateGymMembership(@RequestBody GymMembershipDTO dto) {
        log.info("Create gym membership : \nmembership dto: {}", dto);
        membershipService.updateGymMembership(dto);
        log.info("Response : Gym membership updated successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Gym membership updated successfully"));
    }

    @GetMapping(value = "/by-profile/gym/{id}")
    public ResponseEntity getAllMembershipGymsByBusinessProfile(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get all membership gyms by business profile : \nbusiness profile id : {} \npage request: {}", id, pageable);
        Page<GymMembershipAdminDTO> gyms = membershipService.getAllMembershipGymsByBusinessProfileId(id, pageable);
        log.info("Response : Membership gyms page");
        return ResponseEntity.ok(new CommonResponse<>(true, gyms));
    }

    @GetMapping(value = "/by-gym/{id}")
    public ResponseEntity getAllMembershipsByGymId(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get all memberships by gym: \ngym id: {} \npage request:  {}", id, pageable);
        Page<MembershipDTO> allMembershipPage = membershipService.getAllMembershipsByGymId(id, pageable);
        log.info("Response : Membership page");
        return ResponseEntity.ok(new CommonResponse<>(true, allMembershipPage));
    }

    @GetMapping(value = "/by-profile/{id}")
    public ResponseEntity getAllGymMembershipByBusinessProfile(@PathVariable("id") long id, Pageable pageable) {
        log.info("Get all gym memberships by business profile: \nprofile id: {} \npage request:  {}", id, pageable);
        Page<GymMembershipDTO> allMembershipPage = membershipService.getAllGymMembershipsByBusinessProfileId(id, pageable);
        log.info("Response : Membership page");
        return ResponseEntity.ok(new CommonResponse<>(true, allMembershipPage));
    }

    @PostMapping(value = "/search/gym/by-profile/{id}")
    public ResponseEntity searchGymMembershipsByBusinessProfileId(@PathVariable("id") long id, @RequestBody MembershipSearchDTO dto, Pageable pageable) {
        log.info("Search gym memberships by business profile: \nprofile id : {},\ntext : {},\npage request :  {}", id, dto.getText(), pageable);
        Page<GymMembershipDTO> allMembershipPage = membershipService.searchGymMembershipsByBusinessProfileId(id, dto.getText(), pageable);
        log.info("response : Membership page");
        return ResponseEntity.ok(new CommonResponse<>(true, allMembershipPage));
    }

//----------------------------------------------------------------------------------------------------------------------------



//----------------------------------------------------------------------------------------------------------------------------


}
