package com.fitnexus.server.util;

import com.fitnexus.server.dto.exception.CustomServiceException;
import com.fitnexus.server.dto.payhere.GeneratedHashValueDetailsDTO;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@Component
public class PayhereUtil {

    private final RestTemplate restTemplate;

    @Value("${payhere.app.charge.url}")
    private String chargeUri;

    public JsonNode getChargeResponse(GeneratedHashValueDetailsDTO generatedHashValueDetailsDTO) {

        try {



            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.set("Authorization", "Bearer "+generatedHashValueDetailsDTO.getAccessToken());

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("type", "PAYMENT");
            requestBody.put("order_id", generatedHashValueDetailsDTO.getOrderId());
            requestBody.put("items", "membership");
            requestBody.put("currency", generatedHashValueDetailsDTO.getCurrency());
            requestBody.put("amount", generatedHashValueDetailsDTO.getAmount());
            requestBody.put("customer_token", generatedHashValueDetailsDTO.getCustomerToken());
            requestBody.put("notify_url", generatedHashValueDetailsDTO.getNotifyUrl());


            ResponseEntity<JsonNode> authResponseResponseEntity = restTemplate.postForEntity(chargeUri, new HttpEntity<>(requestBody,httpHeaders), JsonNode.class);

            log.info("payhere charge API request: " + requestBody);

            JsonNode json = authResponseResponseEntity.getBody();
            log.info("payhere charging API response: "  +json);

            return authResponseResponseEntity.getBody();

        } catch (HttpStatusCodeException e) {

            log.error("Error auth req: {}", e.getMessage());
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                    throw new AccessDeniedException("Transaction has been failed.");
            }
            throw new CustomServiceException("Payhere service error");
        }
    }


}
