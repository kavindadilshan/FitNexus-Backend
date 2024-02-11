package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.classes.ClassForTrainerDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.dto.trainer.TrainerNameIdDTO;
import com.fitnexus.server.dto.trainer.TrainerRatingDTO;
import com.fitnexus.server.service.TrainerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/trainer")
public class AdminTrainerController {

    private final TrainerService trainerService;

    @GetMapping(value = "/all/profile/{id}")
    public ResponseEntity getTrainersForBusinessProfile(@PathVariable long id) {
        log.info("Get trainers for business profile : \nbusiness profile id : " + id);
        List<TrainerNameIdDTO> trainersForBusinessProfile = trainerService.getTrainersForBusinessProfile(id);
        log.info("Response : Trainer name id list");
        return ResponseEntity.ok(new CommonResponse<>(true, trainersForBusinessProfile));
    }

    @GetMapping(value = "/all/byClass/{id}")
    public ResponseEntity getTrainersForClass(@PathVariable long id, Pageable pageable) {
        log.info("Get trainers for class : \nclass id : {}", id);
        Page<TrainerNameIdDTO> trainersForClass = trainerService.getTrainersForClass(id, pageable);
        log.info("Response : Trainer page");
        return ResponseEntity.ok(new CommonResponse<>(true, trainersForClass));
    }



    @GetMapping(value = "/classes/{id}")
    public ResponseEntity getClassesForTrainer(@PathVariable long id, Pageable pageable) {
        log.info("Get classes by trainer : \ntrainer id : {}", id);
        Page<ClassForTrainerDTO> classesForTrainer = trainerService.getClassesForTrainer(id, pageable);
        log.info("Response : classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, classesForTrainer));
    }



    @GetMapping(value = "/classes/physical/{id}")
    public ResponseEntity getPhysicalClassesForTrainer(@PathVariable long id, Pageable pageable) {
        log.info("Get physical classes by trainer : \ntrainer id : {}", id);
        Page<ClassForTrainerDTO> classesForTrainer = trainerService.getPhysicalClassesForTrainer(id, pageable);
        log.info("Response : classes page");
        return ResponseEntity.ok(new CommonResponse<>(true, classesForTrainer));
    }
}
