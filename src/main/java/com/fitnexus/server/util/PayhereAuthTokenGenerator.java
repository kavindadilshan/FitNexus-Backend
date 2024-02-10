package com.fitnexus.server.util;

import com.fitnexus.server.dto.exception.CustomServiceException;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Log4j2
@RequiredArgsConstructor
@Component
public class PayhereAuthTokenGenerator {

    private final RestTemplate restTemplate;

    @Value("${payhere.app.retrive.accesstoken.url}")
    private String tokenUri;

    @Value("${payhere.app.basic.token}")
    private String token;

    public JsonNode getAuthResponse() {

        try {

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            httpHeaders.set("Authorization", "Basic "+token);


            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("grant_type", "client_credentials");

            ResponseEntity<JsonNode> authResponseResponseEntity = restTemplate.postForEntity(tokenUri, new HttpEntity<>(requestBody, httpHeaders), JsonNode.class);

            JsonNode json = authResponseResponseEntity.getBody();
            log.info("payhere auth response: "  +json);

            return authResponseResponseEntity.getBody();

        } catch (HttpStatusCodeException e) {

            log.error("Error auth req: {}", e.getMessage());
            switch (e.getStatusCode()) {
                case UNAUTHORIZED:
                case FORBIDDEN:
                    throw new AccessDeniedException("Unauthorized credentials.");
            }
            throw new CustomServiceException("Payhere auth servcie error");
        }
    }

}
