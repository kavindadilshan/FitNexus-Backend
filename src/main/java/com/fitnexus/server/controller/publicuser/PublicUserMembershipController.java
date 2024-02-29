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

    @PostMapping(value = "/payhere/purchase")
    public ResponseEntity purchaseMembershipByPayhere(@RequestBody MembershipBookDTO membershipBookDTO,
                                             @RequestHeader("Authorization") String token,
                                             @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user purchase membership: {}\nTime zone: {}", membershipBookDTO, timeZone);
        CustomUserAuthenticator.checkPublicUserIdWithToken(membershipBookDTO.getUserId(), token);
        String response = membershipService.reserveMembershipByPayhere(membershipBookDTO, timeZone);
        log.info("Response is success.");
        return ResponseEntity.ok(new CommonResponse<>(true, response));
    }

    @PostMapping(value = "/payhere/checkout/{status}")
    public ResponseEntity purchaseMembershipCheckoutByPayhere(@RequestBody MembershipBookDTO membershipBookDTO,
                                                              @PathVariable("status") String status,
                                                      @RequestHeader("Authorization") String token,
                                                      @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Public user purchase membership: {}\nTime zone: {}", membershipBookDTO, timeZone);
        CustomUserAuthenticator.checkPublicUserIdWithToken(membershipBookDTO.getUserId(), token);

        if(status.equals("yes")){

            PreApproveResponseDTO preApproveResponseDTO = membershipService.checkoutMembershipByPayhere(membershipBookDTO, timeZone);
            log.info("Response is success.");
            return ResponseEntity.ok(new CommonResponse<>(true, preApproveResponseDTO));

        } else {

            PreApproveResponseDTO preApproveResponseDTO = membershipService.makeOneTimePaymentMembershipByPayhere(membershipBookDTO, timeZone);
            log.info("Response is success.");
            return ResponseEntity.ok(new CommonResponse<>(true, preApproveResponseDTO));

        }

    }

    @GetMapping(value = "/purchased/all/{id}")
    public ResponseEntity getMembershipsByUser(@PathVariable("id") long id,
                                               @RequestParam(value = "type", required = false) MembershipType type,
                                               @RequestHeader("Authorization") String token,
                                               Pageable pageable) {
        if (type == null) type = MembershipType.ALL;
        log.info("Get user purchased memberships: \npublic user id - {}\ntype - {}", id, type);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<PublicUserMembershipDTO> membershipsByUser = membershipService.getMembershipsByUser(id, type, pageable);
        log.info("Response: membership page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, membershipsByUser));
    }

    @GetMapping(value = "/corporate/{id}")
    public ResponseEntity getCorporateMembershipsByUser(@PathVariable("id") long id,
                                                        @RequestHeader("Authorization") String token,
                                                        Pageable pageable) {
        log.info("Get user corporate memberships: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<PublicUserMembershipDTO> membershipsByUser = membershipService.getMembershipsByUser(id, MembershipType.CORPORATE, pageable);
        log.info("Response: corporate membership page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, membershipsByUser));
    }

    @GetMapping(value = "/purchased/day-pass/{id}")
    public ResponseEntity getPurchasedDayPasses(@PathVariable("id") long id,
                                                @RequestHeader("Authorization") String token,
                                                Pageable pageable) {
        log.info("Get user purchased day passes: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<DayPassPurchasedDTO> purchasedDayPasses = membershipService.getPurchasedDayPasses(id, pageable);
        log.info("Response: day pass page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedDayPasses));
    }

    @GetMapping(value = "/purchased/day-pass/history/{id}")
    public ResponseEntity getPurchasedDayPassesHistory(@PathVariable("id") long id,
                                                       @RequestHeader("Authorization") String token,
                                                       Pageable pageable) {
        log.info("Get user purchased day passes history: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<DayPassPurchasedDTO> purchasedDayPassesHistory = membershipService.getPurchasedDayPassesHistory(id, pageable);
        log.info("Response: day pass history page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedDayPassesHistory));
    }

    @GetMapping(value = "/purchased/gym/{id}")
    public ResponseEntity getPurchasedGymMemberships(@PathVariable("id") long id,
                                                     @RequestHeader("Authorization") String token,
                                                     Pageable pageable) {
        log.info("Get user purchased gym memberships: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<PurchasedMembershipForGymDTO> purchasedGymMemberships = membershipService.getPurchasedGymMemberships(id, pageable);
        log.info("Response: gym membership page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedGymMemberships));
    }

    @GetMapping(value = "/purchased/gym/history/{id}")
    public ResponseEntity getPurchasedGymMembershipsHistory(@PathVariable("id") long id,
                                                            @RequestHeader("Authorization") String token,
                                                            Pageable pageable) {
        log.info("Get user purchased day passes history: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<PurchasedMembershipForGymDTO> purchasedGymMembershipsHistory = membershipService.getPurchasedGymMembershipsHistory(id, pageable);
        log.info("Response: gym membership history page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedGymMembershipsHistory));
    }

    @GetMapping(value = "/purchased/{id}")
    public ResponseEntity getUserMembershipByPublicUser(@PathVariable("id") long id) {
        log.info("Get user purchased membership by public user membership id : {}", id);
        PublicUserMembershipDTO membership = membershipService.getUserMembershipByPublicUser(id);
        log.info("Response: membership details");
        return ResponseEntity.ok(new CommonResponse<>(true, membership));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity getMembershipById(@PathVariable("id") long id,
                                            @RequestHeader("Authorization") String token) {
        log.info("Get membership by membership id : {}", id);
        MembershipDTO membership = membershipService.getMembershipById(id, token);
        log.info("Response: membership details");
        return ResponseEntity.ok(new CommonResponse<>(true, membership));
    }
    //------------------------------------------------------------------------------------------------------------------------------

    //physical class

    @PostMapping(value = "/session/reserve")
    public ResponseEntity reserveMembershipSession(@RequestBody MembershipSessionReserveDTO dto,
                                                   @RequestHeader(TIME_ZONE_HEADER) String timeZone,
                                                   @RequestHeader("Authorization") String token) {
        log.info("Reserve a session in membership : {}", dto);
        CustomUserAuthenticator.checkPublicUserIdWithToken(dto.getUserId(), token);
        membershipService.reserveMembershipSession(dto, timeZone);
        log.info("Response: session reserved");
        return ResponseEntity.ok(new CommonResponse<>(true, "Session reserved successfully"));
    }

    @PostMapping(value = "/session/reservation/cancel")
    public ResponseEntity cancelMembershipSessionReservation(@RequestBody MembershipSessionReserveDTO dto,
                                                             @RequestHeader("Authorization") String token) {
        log.info("Cancel session reservation in membership : {}", dto);
        CustomUserAuthenticator.checkPublicUserIdWithToken(dto.getUserId(), token);
        membershipService.cancelMembershipSessionReservation(dto);
        log.info("Response: session reservation canceled");
        return ResponseEntity.ok(new CommonResponse<>(true, "Session reservation canceled successfully"));
    }

    @GetMapping(value = "/purchased/physical-class/{id}")
    public ResponseEntity getPurchasedPhysicalClassMemberships(@PathVariable("id") long id,
                                                               @RequestHeader("Authorization") String token,
                                                               Pageable pageable) {
        log.info("Get user purchased physical class memberships: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<MembershipsForPhysicalClassDTO> purchasedPhysicalClassMemberships = membershipService.getPurchasedPhysicalClassMemberships(id, pageable);
        log.info("Response: physical class page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedPhysicalClassMemberships));
    }

    @GetMapping(value = "/purchased/physical-class/history/{id}")
    public ResponseEntity getPurchasedPhysicalClassMembershipsHistory(@PathVariable("id") long id,
                                                                      @RequestHeader("Authorization") String token,
                                                                      Pageable pageable) {
        log.info("Get user purchased physical class history: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<MembershipsForPhysicalClassDTO> purchasedPhysicalClassMembershipsHistory = membershipService.getPurchasedPhysicalClassMembershipsHistory(id, pageable);
        log.info("Response: physical class membership history page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedPhysicalClassMembershipsHistory));
    }

    //Online class

    @PostMapping(value = "/session/online/reserve")
    public ResponseEntity reserveMembershipOnlineSession(@RequestBody MembershipSessionReserveDTO dto,
                                                         @RequestHeader(TIME_ZONE_HEADER) String timeZone,
                                                         @RequestHeader("Authorization") String token) {
        log.info("Reserve a online session in membership : {}", dto);
        CustomUserAuthenticator.checkPublicUserIdWithToken(dto.getUserId(), token);
        membershipService.reserveOnlineMembershipSession(dto, timeZone);
        log.info("Response: Online session reserved");
        return ResponseEntity.ok(new CommonResponse<>(true, "Session reserved successfully"));
    }

    @PostMapping(value = "/session/online/reservation/cancel")
    public ResponseEntity cancelOnlineMembershipSessionReservation(@RequestBody MembershipSessionReserveDTO dto,
                                                                   @RequestHeader("Authorization") String token) {
        log.info("Cancel online session reservation in membership : {}", dto);
        CustomUserAuthenticator.checkPublicUserIdWithToken(dto.getUserId(), token);
        membershipService.cancelOnlineMembershipSessionReservation(dto);
        log.info("Response: Online session reservation canceled");
        return ResponseEntity.ok(new CommonResponse<>(true, "Session reservation canceled successfully"));
    }

    @GetMapping(value = "/purchased/online-class/history/{id}")
    public ResponseEntity getPurchasedOnlineClassMembershipsHistory(@PathVariable("id") long id,
                                                                    @RequestHeader("Authorization") String token,
                                                                    Pageable pageable) {
        log.info("Get user purchased online class history: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<MembershipsForOnlineClassDTO> purchasedOnlineClassMembershipsHistory = membershipService.getPurchasedOnlineClassMembershipsHistory(id, MembershipType.ONLINE_CLASS, pageable);
        log.info("Response: online class membership history page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedOnlineClassMembershipsHistory));
    }

    @GetMapping(value = "/purchased/online-class/{id}")
    public ResponseEntity getPurchasedOnlineClassMemberships(@PathVariable("id") long id,
                                                             @RequestHeader("Authorization") String token,
                                                             Pageable pageable) {
        log.info("Get user purchased online class memberships: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<MembershipsForOnlineClassDTO> purchasedOnlineClassMemberships = membershipService.getPurchasedOnlineClassMemberships(id, MembershipType.ONLINE_CLASS, pageable);
        log.info("Response: online class page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedOnlineClassMemberships));
    }

    //------------------------------------------------------------------------------------------------------------------------------

    //corporate

    @GetMapping(value = "/purchased/corporate/history/{id}")
    public ResponseEntity getPurchasedCorporateMembershipsHistory(@PathVariable("id") long id,
                                                                  @RequestHeader("Authorization") String token,
                                                                  Pageable pageable) {
        log.info("Get user purchased corporate membership history: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<MembershipsForOnlineClassDTO> purchasedOnlineClassMembershipsHistory = membershipService.getPurchasedOnlineClassMembershipsHistory(id, MembershipType.CORPORATE, pageable);
        log.info("Response: corporate membership history page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedOnlineClassMembershipsHistory));
    }

    @GetMapping(value = "/purchased/corporate/{id}")
    public ResponseEntity getPurchasedCorporateMemberships(@PathVariable("id") long id,
                                                           @RequestHeader("Authorization") String token,
                                                           Pageable pageable) {
        log.info("Get user purchased corporate memberships: {}", id);
        CustomUserAuthenticator.checkPublicUserIdWithToken(id, token);
        Page<MembershipsForOnlineClassDTO> purchasedOnlineClassMemberships = membershipService.getPurchasedOnlineClassMemberships(id, MembershipType.CORPORATE, pageable);
        log.info("Response: corporate membership page by user");
        return ResponseEntity.ok(new CommonResponse<>(true, purchasedOnlineClassMemberships));
    }

    //corporate check

    @GetMapping(value = "/corporate/state")
    public ResponseEntity getUserCorporateState(@RequestHeader("Authorization") String token) {
        long publicUserId = CustomUserAuthenticator.getPublicUserIdFromToken(token);
        log.info("Check user corporate state : {}", publicUserId);
        CorporateStateDTO userCorporateState = corporateMembershipService.getUserCorporateState(publicUserId);
        log.info("Response: user corporate state: {}", userCorporateState);
        return ResponseEntity.ok(new CommonResponse<>(true, userCorporateState));
    }


    //add user to a corporate membership (with activation code)
    @PostMapping(value = "/corporate/add-user/{userId}")
    public ResponseEntity addUserToCorporateMembership(@PathVariable("userId") long userId,
                                                       @RequestParam("activationCode") String activationCode,
                                                       @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Add user to a corporate membership (with activation code) : \nactivationCode - {} \nuserId - {}", activationCode, userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        String message = corporateMembershipService.addUserToCorporateMembership(userId, activationCode);
        log.info("Response: User is successfully added to corporate membership");
        return ResponseEntity.ok(new CommonResponse<>(true, message));
    }

    @GetMapping(value = "/corporate/tnc/{userId}")
    public ResponseEntity getCorporateMembershipTermsAndConditions(@PathVariable("userId") long userId,
                                                                   @RequestParam("activationCode") String activationCode,
                                                                   @RequestHeader("Authorization") String token) {
        log.info("Get terms and conditions of corporate membership : \nactivationCode - {} \nuserId - {}", activationCode, userId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        CorporateMembershipTermsAndConditionsDTO tc = corporateMembershipService.getTermsAndConditionsOfCorporateMembership(userId, activationCode);
        log.info("Response: Terms and Conditions of the corporate membership ({})", tc.isConditionsApplied());
        return ResponseEntity.ok(new CommonResponse<>(true, tc));
    }

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

    //day pass

    @GetMapping(value = "/gym/day-pass/all")
    public ResponseEntity getGymDayPasses(@RequestParam("longitude") double longitude,
                                          @RequestParam("latitude") double latitude,
                                          Pageable pageable,
                                          @RequestHeader("Authorization") String token) {
        log.info("Get gym day pass memberships:\nlongitude: {} \nlatitude: {} \npage request: {}", longitude, latitude, pageable);
        Page<GymMembershipDTO> gymMemberships = membershipService.getGymDayPasses(longitude, latitude, pageable, token);
        log.info("Gym day pass memberships page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymMemberships));
    }

    @GetMapping(value = "/gym/day-pass/search")
    public ResponseEntity searchGymDayPasses(@RequestParam("longitude") double longitude,
                                             @RequestParam("latitude") double latitude,
                                             @RequestParam("name") String name,
                                             Pageable pageable,
                                             @RequestHeader("Authorization") String token) {
        String country = publicUserService.getCountryOfUserFromToken(token);
        log.info("Search gym day pass memberships:\nlongitude: {} \nlatitude: {} \ncountry: {} \nname: {} \npage request: {}", longitude, latitude, country, name, pageable);
        Page<GymMembershipDTO> gymMemberships = membershipService.searchGymDayPasses(longitude, latitude, country, name, pageable, token);
        log.info("Gym day pass memberships page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymMemberships));
    }
}
