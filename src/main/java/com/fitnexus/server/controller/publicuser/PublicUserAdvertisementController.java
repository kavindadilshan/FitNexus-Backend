package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/users/advertisement")
public class PublicUserAdvertisementController {

    private final AdvertisementService advertisementService;

    @GetMapping(value = "/images")
    public ResponseEntity getAdvertisementImages() {
        log.info("Get all advertisement images");
        List<String> advertisementImages = advertisementService.getAdvertisementImages();
        log.info("Response : All advertisement images");
        return ResponseEntity.ok(new CommonResponse<>(true, advertisementImages));
    }
}
