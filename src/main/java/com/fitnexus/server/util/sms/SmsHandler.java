package com.fitnexus.server.util.sms;

import com.fitnexus.server.util.sms.handler.SMSHandler;
import com.twilio.Twilio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class SmsHandler {

    @Value("${twilioPhoneNumber}")
    private String twilioPhoneNumber;
    @Value("${twilioAccountSid}")
    private String twilioAccountSid;
    @Value("${twilioAuthToken}")
    private String twilioAuthToken;

    @PostConstruct
    private void initTwilio() {
        Twilio.init(twilioAccountSid, twilioAuthToken);
    }

    /*
    //twilio
    /**
     * This cau use to send SMS for the given numbers.
     *
     * @param numbers the mobile numbers list of the receivers.
     * @param smsBody the message that needs to send.
     *//*
   @Async
    public void sendMessages(List<String> numbers, String smsBody) {
        log.info("\nSending message to: {} \n message: {}", numbers, smsBody);
        try {
            numbers.forEach(number -> {
                Message message = Message.creator(
                        new PhoneNumber(number),
                        new PhoneNumber(twilioPhoneNumber),
                        smsBody).create();
                log.info("Sent message w/ sid: {}", message.getSid());
            });
        } catch (ApiException e) {
            log.error("\nTwilio error: Error code: {}\n Http status: {}\n Error Message: {}\n", e.getCode(), e.getStatusCode(), e.getMessage(), e);
        }
    }
*/


    //Lanka Bell
    private final static String AUTHORIZATION_HEADER = "LB_Key750 RklUWktZODcwX0IwUzp0Sk5rQ3czVmhzX0I5Uw==";
    private final static String API_NAME = "FITZKY870";
    private final static String PASSWORD = "tJNkCw3Vhs";
    private static String SINGLE_SMS_SEND_URL = "http://smsm.lankabell.com:4040/Sms.svc/PostSendSms";
    private static String BULK_SMS_SEND_URL = "http://smsm.lankabell.com:4040/Sms.svc/SingleSmsBulk";
    private final RestTemplate restTemplate;

    private final SMSHandler smsHandler;

    //Lanka Bell
    @Async
    public void sendMessages(List<String> numbers, String smsBody) {
        log.info("\nSending message to: {} \n message: {}", numbers, smsBody);

        for (String number : numbers) {
            try {
                //Lanka Bell
                /*HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_JSON);

                String requestBody = " { " +
                        "\"CompanyId\":\"" + API_NAME +
                        "\", \"Pword\":\"" + PASSWORD +
                        "\", \"SmsMessage\":\"" + smsBody +
                        "\", \"PhoneNumber\":\"" + number.replace("+", "") + "\" } ";
                log.info("Request body - {}", requestBody);
                HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);
                ResponseEntity<String> response = restTemplate.exchange(SINGLE_SMS_SEND_URL, HttpMethod.POST, entity, String.class);
                log.info("Response - {}", response.toString());*/

                //Dialog E-SMS
//                Gson gson = new Gson();
                JsonArray msisdnList = new JsonArray();
                msisdnList.add(number);
                smsHandler.sendSMS(msisdnList, smsBody);

            } catch (Exception exception) {
                log.error(exception.getMessage());
                exception.printStackTrace();
            }
        }
    }

    @Async
    public void sendBulkMessages(List<String> numbers, String smsBody) {
        int count = 0;
        while (count <= numbers.size()) {
            List<String> currentNumberList = new ArrayList<>();
            for (int i = count; i < count + 100; i++) {
                if (i >= numbers.size()) break;
                currentNumberList.add("\"" + numbers.get(i).replace("+", "") + "\"");
            }
            if (!currentNumberList.isEmpty()) {
                try {

                    //Lanka Bell
                    /*log.info("\nSending message to: {} \n message: {}", currentNumberList, smsBody);
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setContentType(MediaType.APPLICATION_JSON);

                    String requestBody = " { " +
                            "\"CompanyId\":\"" + API_NAME + "\", " +
                            "\"Pword\":\"" + PASSWORD + "\", " +
                            "\"SmsMessage\":\"" + smsBody + "\", " +
                            "\"PhoneNumber\":" + currentNumberList + " } ";
                    log.info("Request body - {}", requestBody);
                    HttpEntity<String> entity = new HttpEntity<>(requestBody, httpHeaders);
                    ResponseEntity<String> response = restTemplate.exchange(BULK_SMS_SEND_URL, HttpMethod.POST, entity, String.class);
                    log.info("Response - {}", response.toString());*/

                    //Dialog E-SMS
                    Gson gson = new Gson();
                    JsonArray msisdnList = gson.toJsonTree(currentNumberList).getAsJsonArray();
                    smsHandler.sendSMS(msisdnList, smsBody);

                } catch (Exception exception) {
                    log.error(exception.getMessage());
                    exception.printStackTrace();
                }
            }

            count += 100;
        }
    }
}
