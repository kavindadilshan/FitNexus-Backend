package com.fitnexus.server.controller.publicuser;

import com.fitnexus.server.dto.admin.WeeklyTimeTableDTO;
import com.fitnexus.server.dto.classes.ClassListDTO;
import com.fitnexus.server.dto.coach.CoachDetailsResponse;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.service.*;
import com.fitnexus.server.util.APIHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fitnexus.server.config.security.SecurityConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.fitnexus.server.constant.FitNexusConstants.PatternConstants.DATE_TIME_RESPONSE_PATTERN;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/public/users")
public class PublicUserOpenAPIController {

    private final PhysicalClassService physicalClassService;
    private final GymService gymService;
    private final AdvertisementService advertisementService;
    private final TrainerService trainerService;
    private final WeeklyTimeTableService weeklyTimeTableService;
    private final APIHandler apiHandler;

    @GetMapping(value = "/class/physical/{classId}")
    public ResponseEntity<CommonResponse<ClassListDTO>> getClassDetails(
            @RequestParam(value = "dateTime", required = false) @DateTimeFormat(pattern = DATE_TIME_RESPONSE_PATTERN) LocalDateTime dateTime,
            @PathVariable(value = "classId") long classId) {
        log.info("\nOpen: Get physical class details for public user view class \tClass Id: {}\tdate:{}", classId, dateTime);
        ClassListDTO classDetails = physicalClassService.getPhysicalClassDetailsOpen(classId);
        log.info("Open: Response: {}", classDetails);
        return ResponseEntity.ok(new CommonResponse<>(true, classDetails));
    }

    @GetMapping(value = "/class/physical/popular")
    public ResponseEntity<CommonResponse<Page<ClassListDTO>>> getPopularClasses(Pageable pageable) {
        log.info("\nOpen: Get popular physical class details pages for public: \npage req: {}", pageable);
        Page<ClassListDTO> activeClasses = physicalClassService.getActivePhysicalClassesOpen(pageable);
        log.info("\nOpen: Response: classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, activeClasses));
    }

    @GetMapping(value = "/gym/popular")
    public ResponseEntity getMostPopularGyms(Pageable pageable) {
        log.info("\nOpen: Get most popular gyms: \tpageRequest - {}", pageable);
        Page<GymDTO> popularGyms = gymService.getPopularGymsOpen(pageable);
        log.info("Open: Response: popular gym page");
        return ResponseEntity.ok(new CommonResponse<>(true, popularGyms));
    }

}
