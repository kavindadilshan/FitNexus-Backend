package com.fitnexus.server.controller;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.MembershipService;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.service.UserSessionEnrollService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(value = "/payhere")
public class PayhereController {

    private final PublicUserService publicUserService;
    private final MembershipService membershipService;
    private final UserSessionEnrollService userSessionEnrollService;

    @Autowired
    public PayhereController(PublicUserService publicUserService, MembershipService membershipService, UserSessionEnrollService userSessionEnrollService) {
        this.publicUserService = publicUserService;
        this.membershipService = membershipService;
        this.userSessionEnrollService = userSessionEnrollService;
    }


    @PostMapping(value = "/card/preapprove/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyPreApproveDetailsMobile(@RequestParam(required = false,name = "card_no") String card_no,
                                                  @RequestParam(required = false,name = "order_id") String order_id,
                                                  @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                  @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                  @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                  @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                  @RequestParam(required = false,name = "method") String method,
                                                  @RequestParam(required = false,name = "payment_id") String payment_id,
                                                  @RequestParam(required = false,name = "status_code") String status_code,
                                                  @RequestParam(required = false,name = "md5sig") String md5sig,
                                                  @RequestParam(required = false,name = "status_message") String status_message,
                                                  @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to Preapproval Notification");

       String response = publicUserService.saveCardDetailsByMobile(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/preapprove/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyPreApproveDetailsWeb(@RequestParam(required = false,name = "card_no") String card_no,
                                                  @RequestParam(required = false,name = "order_id") String order_id,
                                                  @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                  @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                  @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                  @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                  @RequestParam(required = false,name = "method") String method,
                                                  @RequestParam(required = false,name = "payment_id") String payment_id,
                                                  @RequestParam(required = false,name = "status_code") String status_code,
                                                  @RequestParam(required = false,name = "md5sig") String md5sig,
                                                  @RequestParam(required = false,name = "status_message") String status_message,
                                                  @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to Preapproval Notification");

        String response = publicUserService.saveCardDetailsByWeb(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }


    @PostMapping(value = "/card/checkout/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCardPaymentCheckoutDetailsMobile(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to Checkout Notification");

        log.info(allRequestParams.get("order_id"));
        log.info(allRequestParams.get("payment_id"));
        log.info(allRequestParams.get("payhere_amount"));
        log.info(allRequestParams.get("payhere_currency"));
        log.info(allRequestParams.get("status_code"));
        log.info(allRequestParams.get("md5sig"));
        log.info(allRequestParams.get("status_message"));

        Boolean response = membershipService.handlePaymentSuccessPayhereMobile(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/checkout/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCardPaymentCheckoutDetailsWeb(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to Checkout Notification");

        Boolean response = membershipService.handlePaymentSuccessPayhereWeb(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/checkout/session/enroll/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifySessionEnrollCardPaymentCheckoutDetailsWeb(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to Checkout Notification");

        Boolean response = userSessionEnrollService.handleSessionEnrollPaymentSuccessPayhereWeb(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/checkout/session/enroll/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifySessionEnrollCardPaymentCheckoutDetailsMobile(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to Checkout Notification");

        Boolean response = userSessionEnrollService.handleSessionEnrollPaymentSuccessPayhereMobile(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/preapprove/checkout/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCheckoutMembershipByMobile(@RequestParam(required = false,name = "card_no") String card_no,
                                                   @RequestParam(required = false,name = "order_id") String order_id,
                                                   @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                   @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                   @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                   @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                   @RequestParam(required = false,name = "method") String method,
                                                   @RequestParam(required = false,name = "payment_id") String payment_id,
                                                   @RequestParam(required = false,name = "status_code") String status_code,
                                                   @RequestParam(required = false,name = "md5sig") String md5sig,
                                                   @RequestParam(required = false,name = "status_message") String status_message,
                                                   @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to notifyCheckoutMembershipByMobile");

        Boolean response = membershipService.verifyMembershipPaymentByMobile(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/preapprove/checkout/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCheckoutMembershipByWeb(@RequestParam(required = false,name = "card_no") String card_no,
                                                   @RequestParam(required = false,name = "order_id") String order_id,
                                                   @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                   @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                   @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                   @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                   @RequestParam(required = false,name = "method") String method,
                                                   @RequestParam(required = false,name = "payment_id") String payment_id,
                                                   @RequestParam(required = false,name = "status_code") String status_code,
                                                   @RequestParam(required = false,name = "md5sig") String md5sig,
                                                   @RequestParam(required = false,name = "status_message") String status_message,
                                                   @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to notifyCheckoutMembershipByWeb");

        Boolean response = membershipService.verifyMembershipPaymentByWeb(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/onetime/checkout/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyOnetimePaymentMembershipByMobile(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to notifyCheckoutMembershipByWeb");

        Boolean response = membershipService.verifyMembershipOneTimePaymentByMobile(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/onetime/checkout/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyOnetimePaymentMembershipByWeb(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to notifyCheckoutMembershipByWeb");

        Boolean response = membershipService.verifyMembershipOneTimePaymentByWeb(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/preapprove/checkout/physical/session/enroll/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCheckoutSessionMembershipByWeb(@RequestParam(required = false,name = "card_no") String card_no,
                                                        @RequestParam(required = false,name = "order_id") String order_id,
                                                        @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                        @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                        @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                        @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                        @RequestParam(required = false,name = "method") String method,
                                                        @RequestParam(required = false,name = "payment_id") String payment_id,
                                                        @RequestParam(required = false,name = "status_code") String status_code,
                                                        @RequestParam(required = false,name = "md5sig") String md5sig,
                                                        @RequestParam(required = false,name = "status_message") String status_message,
                                                        @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to notifyCheckoutSessionMembershipByWeb");

        Boolean response = userSessionEnrollService.verifyPhysicalSessionPaymentByWeb(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/preapprove/checkout/physical/session/enroll/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCheckoutSessionMembershipByMobile(@RequestParam(required = false,name = "card_no") String card_no,
                                                               @RequestParam(required = false,name = "order_id") String order_id,
                                                               @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                               @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                               @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                               @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                               @RequestParam(required = false,name = "method") String method,
                                                               @RequestParam(required = false,name = "payment_id") String payment_id,
                                                               @RequestParam(required = false,name = "status_code") String status_code,
                                                               @RequestParam(required = false,name = "md5sig") String md5sig,
                                                               @RequestParam(required = false,name = "status_message") String status_message,
                                                               @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to notifyCheckoutSessionMembershipByMobile");

        Boolean response = userSessionEnrollService.verifyPhysicalSessionPaymentByMobile(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/preapprove/checkout/online/session/enroll/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCheckoutOnlineSessionMembershipByWeb(@RequestParam(required = false,name = "card_no") String card_no,
                                                               @RequestParam(required = false,name = "order_id") String order_id,
                                                               @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                               @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                               @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                               @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                               @RequestParam(required = false,name = "method") String method,
                                                               @RequestParam(required = false,name = "payment_id") String payment_id,
                                                               @RequestParam(required = false,name = "status_code") String status_code,
                                                               @RequestParam(required = false,name = "md5sig") String md5sig,
                                                               @RequestParam(required = false,name = "status_message") String status_message,
                                                               @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to notifyCheckoutOnlineSessionMembershipByWeb");

        Boolean response = userSessionEnrollService.verifyOnlineSessionPaymentByWeb(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/preapprove/checkout/online/session/enroll/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyCheckoutOnlineSessionMembershipByMobile(@RequestParam(required = false,name = "card_no") String card_no,
                                                                  @RequestParam(required = false,name = "order_id") String order_id,
                                                                  @RequestParam(required = false,name = "card_expiry") String card_expiry,
                                                                  @RequestParam(required = false,name = "payhere_amount") String payhere_amount,
                                                                  @RequestParam(required = false,name = "payhere_currency") String payhere_currency,
                                                                  @RequestParam(required = false,name = "card_holder_name") String card_holder_name,
                                                                  @RequestParam(required = false,name = "method") String method,
                                                                  @RequestParam(required = false,name = "payment_id") String payment_id,
                                                                  @RequestParam(required = false,name = "status_code") String status_code,
                                                                  @RequestParam(required = false,name = "md5sig") String md5sig,
                                                                  @RequestParam(required = false,name = "status_message") String status_message,
                                                                  @RequestParam(required = false,name = "customer_token") String customer_token) {

        log.info("\n Listening to notifyCheckoutOnlineSessionMembershipByMobile");

        Boolean response = userSessionEnrollService.verifyOnlineSessionPaymentByMobile(card_no,order_id,card_expiry,payhere_amount,payhere_currency,card_holder_name,method,payment_id,status_code,md5sig,status_message,customer_token);

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/checkout/physical/session/enroll/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyOnetimePaymentPhysicalSessionEnrollByMobile(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to notifyOnetimePaymentPhysicalSessionEnrollByMobile");

        Boolean response = userSessionEnrollService.verifyPhysicalSessionOneTimePaymentByMobile(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/checkout/physical/session/enroll/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyOnetimePaymentPhysicalSessionEnrollByWeb(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to notifyOnetimePaymentPhysicalSessionEnrollByWeb");

        Boolean response = userSessionEnrollService.verifyPhysicalSessionOneTimePaymentByWeb(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/checkout/online/session/enroll/notify/mobile",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyOnetimePaymentOnlineSessionEnrollByMobile(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to notifyOnetimePaymentPhysicalSessionEnrollByMobile");

        Boolean response = userSessionEnrollService.verifyOnlineSessionOneTimePaymentByMobile(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }

    @PostMapping(value = "/card/checkout/online/session/enroll/notify/web",produces = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity notifyOnetimePaymentOnlineSessionEnrollByWeb(@RequestParam Map<String,String> allRequestParams) {

        log.info("\n Listening to notifyOnetimePaymentPhysicalSessionEnrollByWeb");

        Boolean response = userSessionEnrollService.verifyOnlineSessionOneTimePaymentByWeb(allRequestParams.get("order_id"),allRequestParams.get("payment_id"),allRequestParams.get("payhere_amount"),allRequestParams.get("payhere_currency"),allRequestParams.get("status_code"),allRequestParams.get("md5sig"),allRequestParams.get("status_message"));

        return ResponseEntity.ok(new CommonResponse<>(true, response));

    }




}
