package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.classsession.ClassSessionBookDTO;
import com.fitnexus.server.dto.classsession.ClassSessionBookPayhereDTO;
import com.fitnexus.server.dto.classsession.ClassSessionBookedResponse;
import com.fitnexus.server.dto.classsession.ClassSessionEnrolPayhereResponseDTO;
import com.fitnexus.server.dto.common.CardDetailsResponse;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.common.StripeCheckResponse;
import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.ClassMethod;
import com.fitnexus.server.enums.StripeRegister;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.service.UserSessionEnrollService;
import com.fitnexus.server.util.CustomGenerator;
import com.fitnexus.server.util.GuestUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.fitnexus.server.constant.FitNexusConstants.DetailConstants.TIME_ZONE_HEADER;



@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users/class/enroll")
public class PublicUserSessionEnrollController {

    private final UserSessionEnrollService userSessionEnrollService;
    private final PublicUserService publicUserService;

    @PostMapping(value = "/check")
    public ResponseEntity checkSession(@RequestBody ClassSessionBookDTO classSessionBookDTO,
                                       @RequestParam(value = "method", required = false) ClassMethod method,
                                       @RequestHeader("Authorization") String token) {

        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        if (method == null) method = ClassMethod.ONLINE;
        log.info("\nPublic user session enroll check: {} \nmethod: {}", classSessionBookDTO, method);
        CustomUserAuthenticator.checkPublicUserIdWithToken(classSessionBookDTO.getUserId(), token);
//        StripeCheckResponse stripeCheckResponse = userSessionEnrollService.checkBookingAndIsStripe(classSessionBookDTO, method);
        List<CardDetailsResponse> cardsOfUser = publicUserService.getCardsOfUser(classSessionBookDTO.getUserId());
        StripeCheckResponse stripeCheckResponse = new StripeCheckResponse(StripeRegister.NOT_REGISTERED,cardsOfUser);
        log.info("Session check response is success. {}", stripeCheckResponse);
        return ResponseEntity.ok(new CommonResponse<>(true, stripeCheckResponse));
    }

    @PostMapping(value = "")
    public ResponseEntity bookSession(@RequestBody ClassSessionBookDTO classSessionBookDTO,
                                      @RequestParam(value = "method", required = false) ClassMethod method,
                                      @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
                                      @RequestHeader("Authorization") String token,
                                      @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {

        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        if (method == null) method = ClassMethod.ONLINE;
        boolean isCashPayment = false;
        if (paymentMethod != null && paymentMethod.equals("CASH")) isCashPayment = true;
        log.info("\nPublic user session enroll: {}\nTime zone: {}\nmethod: {}\nisCashPayment: {}", classSessionBookDTO, timeZone, method, isCashPayment);
        CustomUserAuthenticator.checkPublicUserIdWithToken(classSessionBookDTO.getUserId(), token);
        String paymentSecret = userSessionEnrollService.reserveSession(classSessionBookDTO, timeZone, method, isCashPayment);
        log.info("Response is success. {}");
        return ResponseEntity.ok(new CommonResponse<>(true, paymentSecret));
    }

    @PostMapping(value = "/payhere")
    public ResponseEntity bookSessionByPayhere(@RequestBody ClassSessionBookPayhereDTO classSessionBookDTO,
                                      @RequestParam(value = "method", required = false) ClassMethod method,
                                      @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
                                      @RequestHeader("Authorization") String token,
                                      @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {

        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        if (method == null) method = ClassMethod.ONLINE;
        boolean isCashPayment = false;
        if (paymentMethod != null && paymentMethod.equals("CASH")) isCashPayment = true;
        log.info("\nPublic user session enroll: {}\nTime zone: {}\nmethod: {}\nisCashPayment: {}", classSessionBookDTO, timeZone, method, isCashPayment);
        CustomUserAuthenticator.checkPublicUserIdWithToken(classSessionBookDTO.getUserId(), token);
        ClassSessionEnrolPayhereResponseDTO reserveSessionByPayhere = userSessionEnrollService.reserveSessionByPayhere(classSessionBookDTO, timeZone, method, isCashPayment);
        log.info("Response is success. {}");
        log.info("Response is success:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
        log.info("Response is success"+ reserveSessionByPayhere);
        return ResponseEntity.ok(new CommonResponse<>(true, reserveSessionByPayhere));
    }



    @GetMapping(value = "/{publicUserId}/booked")
    public ResponseEntity getBookSessions(@PathVariable("publicUserId") long publicUserId,
                                          @RequestParam(value = "category", required = false) ClassCategory category,
                                          @RequestHeader("Authorization") String token, Pageable pageable,
                                          @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet public user's upcoming session enrolls: {}\tcategory: {}\tstart date: {}\tpag: {}",
                publicUserId, category, pageable);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserId, token);
        Page<ClassSessionBookedResponse> bookedSessionsOfUser = userSessionEnrollService.getBookedSessionsOfUser(
                publicUserId, category, CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone), pageable);
        log.info("Response is success of booked sessions.");
        return ResponseEntity.ok(new CommonResponse<>(true, bookedSessionsOfUser));
    }

    @GetMapping(value = "/{publicUserId}/booked/physical")
    public ResponseEntity getBookPhysicalSessions(@PathVariable("publicUserId") long publicUserId,
                                                  @RequestHeader("Authorization") String token, Pageable pageable,
                                                  @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet public user's upcoming physical session enrolls: {}\tstart date: {}\tpag: {}", publicUserId, pageable);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserId, token);
        Page<ClassSessionBookedResponse> bookedSessionsOfUser = userSessionEnrollService.getBookedPhysicalSessionsOfUser(
                publicUserId, CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone), pageable);
        log.info("Response is success of booked physical sessions.");
        return ResponseEntity.ok(new CommonResponse<>(true, bookedSessionsOfUser));
    }

    @GetMapping(value = "/{publicUserId}/booked/history")
    public ResponseEntity getBookSessionsHistory(@PathVariable("publicUserId") long publicUserId,
                                                 @RequestParam(value = "category", required = false) ClassCategory category,
                                                 @RequestHeader("Authorization") String token, Pageable pageable,
                                                 @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet public user's upcoming session enroll history: {}\tcategory: {}\tstart date: {}\tpag: {}",
                publicUserId, category, pageable);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserId, token);
        Page<ClassSessionBookedResponse> bookedSessionsOfUser = userSessionEnrollService.getBookedSessionsHistoryOfUser(
                publicUserId, category, CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone), pageable);
        log.info("Response is success of booked sessions history.");
        return ResponseEntity.ok(new CommonResponse<>(true, bookedSessionsOfUser));
    }

    @GetMapping(value = "/{publicUserId}/booked/history/physical")
    public ResponseEntity getBookPhysicalSessionsHistory(@PathVariable("publicUserId") long publicUserId,
                                                         @RequestHeader("Authorization") String token, Pageable pageable,
                                                         @RequestHeader(value = TIME_ZONE_HEADER, required = false) String timeZone) {
        log.info("\nGet public user's upcoming physical session enroll history: {}\tstart date: {}\tpag: {}",
                publicUserId, pageable);
        CustomUserAuthenticator.checkPublicUserIdWithToken(publicUserId, token);
        Page<ClassSessionBookedResponse> bookedSessionsOfUser = userSessionEnrollService.getBookedPhysicalSessionsHistoryOfUser(
                publicUserId, CustomGenerator.getDateTimeByZone(LocalDateTime.now(), timeZone), pageable);
        log.info("Response is success of booked physical sessions history.");
        return ResponseEntity.ok(new CommonResponse<>(true, bookedSessionsOfUser));
    }

}
