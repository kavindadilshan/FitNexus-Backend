package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.config.security.custom.CustomUserAuthenticator;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.dto.gym.GymRateDTO;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.service.GymService;
import com.fitnexus.server.service.PublicUserService;
import com.fitnexus.server.util.GuestUserUtil;
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
@RequestMapping(value = "/users/gym")
public class PublicUserGymController {

    private final GymService gymService;
    private final PublicUserService publicUserService;

    @GetMapping(value = "/popular")
    public ResponseEntity getMostPopularGyms(
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            @RequestHeader(value = AUTHORIZATION) String token,
            Pageable pageable) {
        String country = publicUserService.getCountryOfUserFromToken(token);
        log.info("\nGet most popular gyms: \nlongitude: {} \nlatitude: {} \ncountry: {} \npageRequest - {}", longitude, latitude, country, pageable);
        Page<GymDTO> popularGyms = gymService.getPopularGyms(pageable, country, longitude, latitude);
        log.info("Response: popular gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, popularGyms));
    }

    @GetMapping(value = "/active")
    public ResponseEntity searchActiveGyms(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            Pageable pageable) {
        log.info("Search active gyms: name - {}, page request - ", name, pageable);
        if (name == null) name = "";
        Page<GymDTO> activeGyms = gymService.searchActiveGyms(name, longitude, latitude, pageable);
        log.info("Response: active gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, activeGyms));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity getGymById(@PathVariable("id") long id,
                                     @RequestParam(value = "longitude") double longitude,
                                     @RequestParam(value = "latitude") double latitude,
                                     @RequestHeader("Authorization") String token) {
        log.info("Get gym by id: id - {}", id);
        GymDTO gymById = gymService.getByIdAndPublicUserToken(id, longitude, latitude, token);
        log.info("Response: gymDTO - {}", gymById);
        return ResponseEntity.ok(new CommonResponse<>(true, gymById));
    }

    @GetMapping(value = "/by-profile/{id}")
    public ResponseEntity getGymsByBusinessProfile(
            @PathVariable("id") long id,
            @RequestParam(value = "longitude") double longitude,
            @RequestParam(value = "latitude") double latitude,
            @RequestHeader(value = AUTHORIZATION) String token,
            Pageable pageable) {
        String country = publicUserService.getCountryOfUserFromToken(token);
        log.info("\nGet gym by business profile: \nprofileId - {} \nlongitude: {} \nlatitude: {} \ncountry: {} \npageRequest - {}", id, longitude, latitude, country, pageable);
        Page<GymDTO> gymsByBusinessProfile = gymService.getGymsByBusinessProfileWithDistance(id, longitude, latitude, country, pageable);
        log.info("Response: gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, gymsByBusinessProfile));
    }

    @PostMapping(value = "/rate")
    public ResponseEntity rateGymByUser(@RequestBody GymRateDTO rateDTO, @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Rate gym: " + rateDTO);
        CustomUserAuthenticator.checkPublicUserIdWithToken(rateDTO.getUserId(), token);
        gymService.rateGym(rateDTO, 0);
        log.info("Response: Gym is rated.");
        return ResponseEntity.ok(new CommonResponse<>(true, "Gym is rated."));
    }

    @GetMapping(value = "/{gymId}/user/{userId}/ratings")
    public ResponseEntity getRatingForGymByUser(@PathVariable("userId") long userId, @PathVariable("gymId") long gymId,
                                                @RequestHeader("Authorization") String token) {
        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check
        log.info("Get Gym rating details by gym by public user: user id - {}, gym id - {}", userId, gymId);
        CustomUserAuthenticator.checkPublicUserIdWithToken(userId, token);
        GymRateDTO rateForGymByUser = gymService.getRateForGymByUser(userId, gymId);
        log.info("Class rating details by gym- {}", rateForGymByUser);
        return ResponseEntity.ok(new CommonResponse<>(true, rateForGymByUser));
    }

    @GetMapping(value = "/{gymId}/ratings")
    public ResponseEntity getRatingForGym(@PathVariable("gymId") long gymId,
                                          @RequestHeader("Authorization") String token,
                                          Pageable pageable) {

        //start - guest user check
        ResponseEntity gu = GuestUserUtil.isGuestUser(token);
        if (gu != null) return gu;
        //end - guest user check

        log.info("Get Gym rating details by gym: gym id - {}", gymId);
        Page<PublicUserReviewsResponse> rateForGym = gymService.getRateForGym(gymId, pageable);
        log.info("Gym rating details list by gym}");
        return ResponseEntity.ok(new CommonResponse<>(true, rateForGym));
    }

    @GetMapping(value = "gym-name/{gymName}")
    public ResponseEntity getGymByName(@PathVariable("gymName") String gymName,
                                     @RequestParam(value = "longitude") double longitude,
                                     @RequestParam(value = "latitude") double latitude,
                                     @RequestHeader("Authorization") String token) {
        log.info("Get gym by gym unique name: gymUniqueName - {}", gymName);
        GymDTO gymByName = gymService.getByUniqueNameAndPublicUserToken(gymName, longitude, latitude, token);
        log.info("Response: gymDTO - {}", gymByName);
        return ResponseEntity.ok(new CommonResponse<>(true, gymByName));
    }
}
