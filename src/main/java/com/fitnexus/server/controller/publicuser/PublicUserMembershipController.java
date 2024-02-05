package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.common.CardDetailsResponse;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.common.StripeCheckResponse;
import com.fitnexus.server.dto.gym.GymMembershipDTO;
import com.fitnexus.server.dto.membership.*;
import com.fitnexus.server.dto.membership.corporate.CorporateMembershipTermsAndConditionsDTO;
import com.fitnexus.server.dto.membership.corporate.CorporateStateDTO;
import com.fitnexus.server.dto.payhere.PreApproveResponseDTO;
import com.fitnexus.server.dto.publicuser.PublicUserMembershipDTO;
import com.fitnexus.server.enums.MembershipType;
import com.fitnexus.server.enums.StripeRegister;
import com.fitnexus.server.service.CorporateMembershipService;
import com.fitnexus.server.service.MembershipService;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.util.GuestUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.TIME_ZONE_HEADER;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users/membership")
public class PublicUserMembershipController {

    private final MembershipService membershipService;
    private final PublicUserService publicUserService;
    private final CorporateMembershipService corporateMembershipService;

    // common
    @PostMapping(value = "/check")
    public ResponseEntity checkMembership(@RequestBody MembershipBookDTO membershipBookDTO,
                                          @RequestHeader("Authorization") String token) {

        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        log.info("Public user membership check: {}", membershipBookDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(membershipBookDTO.getUserId(), token);
//        StripeCheckResponse stripeCheckResponse = membershipService.checkBookingAndIsStripe(membershipBookDTO);
        List<CardDetailsResponse> cardsOfUser = publicUserService.getCardsOfUser(membershipBookDTO.getUserId());
        StripeCheckResponse stripeCheckResponse = new StripeCheckResponse(StripeRegister.NOT_REGISTERED,cardsOfUser);
        log.info("Membership check response is success. {}", stripeCheckResponse);
        return ResponseEntity.ok(new CommonResponse<>(true, stripeCheckResponse));
    }

//    @PostMapping(value = "/purchase")
//    public ResponseEntity purchaseMembershipByStrip(@RequestBody MembershipBookDTO membershipBookDTO,
//                                             @RequestHeader("Authorization") String token,
//                                             @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
//        //start - guest user check
//        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
//        if (gu != null) return gu;
//        //end - guest user check
//        log.info("Public user purchase membership: {}\nTime zone: {}", membershipBookDTO, timeZone);
//        CustomUserAuthenticator.checkPublicUserIdWithToken(membershipBookDTO.getUserId(), token);
//        String response = membershipService.reserveMembershipByStripe(membershipBookDTO, timeZone);
//        log.info("Response is success.");
//        return ResponseEntity.ok(new CommonResponse<>(true, response));
//    }



    //------------------------------------------------------------------------------------------------------------------------------

    //gym

    @GetMapping(value = "/gym/membership/all")
    public ResponseEntity getGymMemberships(@RequestParam("longitude") double longitude,
                                            @RequestParam("latitude") double latitude,
                                            Pageable pageable,
                                            @RequestHeader("Authorization") String token) {
        log.info("Get gym memberships:\nlongitude: {} \nlatitude: {} \npage request: {}", longitude, latitude, pageable);
        Page<GymMembershipDTO> gymMemberships = membershipService.getGymMemberships(longitude, latitude, pageable, token);
        log.info("Gym memberships page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymMemberships));
    }

    @GetMapping(value = "/gym/membership/search")
    public ResponseEntity searchGymMemberships(@RequestParam("longitude") double longitude,
                                               @RequestParam("latitude") double latitude,
                                               @RequestParam("name") String name,
                                               Pageable pageable,
                                               @RequestHeader("Authorization") String token) {
        String country = publicUserService.getCountryOfUserFromToken(token);
        log.info("Search gym memberships:\nlongitude: {} \nlatitude: {} \ncountry: {} \nname: {} \npage request: {}", longitude, latitude, country, name, pageable);
        Page<GymMembershipDTO> gymMemberships = membershipService.searchGymMemberships(longitude, latitude, country, name, pageable, token);
        log.info("Gym memberships page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymMemberships));
    }

    @GetMapping(value = "/gym/{gymId}")
    public ResponseEntity getMembershipsForGym(@PathVariable("gymId") long gymId,
                                               Pageable pageable,
                                               @RequestHeader("Authorization") String token) {
        log.info("Get memberships for gym : \ngym id: {} \npage request: {}", gymId, pageable);
        long userId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        Page<MembershipDTO> memberships = membershipService.getGymMembershipsForGym(gymId, userId, pageable);
        log.info("Memberships for gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, memberships));
    }



}
