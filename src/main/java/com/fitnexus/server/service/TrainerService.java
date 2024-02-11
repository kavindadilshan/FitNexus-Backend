package com.fitnexus.server.service;

import com.fitnexus.server.dto.classes.ClassForTrainerDTO;
import com.fitnexus.server.dto.coach.CoachDetailsResponse;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.dto.trainer.*;
import com.fitnexus.server.dto.trainer.*;
import com.fitnexus.server.entity.trainer.Trainer;
import com.fitnexus.server.enums.Gender;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface TrainerService {
    List<TrainerNameIdDTO> getTrainersForBusinessProfile(long businessProfileId);

    Page<TrainerDetailDTO> getAllActiveTrainers(Gender gender, String name, List<String> types, boolean allTrainers, Pageable pageable);

    Page<TrainerDetailDTO> getAllActivePhysicalTrainers(Gender gender, String name, String country, List<String> types, Pageable pageable);

    List<CoachDetailsResponse> getAllActiveTrainersByClass(Gender gender, long classId);

    List<CoachDetailsResponse> getAllActiveTrainersByPhysicalClass(Gender gender, long classId);

    TrainerSingleDTO getTrainer(long trainerId, LocalDateTime dateTime, String token);

    @Transactional
    void rateTrainer(TrainerRateDTO rateDTO, int count);

    @Transactional
    void ratePhysicalTrainer(TrainerRateDTO rateDTO, int count);

    TrainerRateDTO getRateForTrainerByUser(long publicUserId, long trainerId);

    TrainerRateDTO getRateForPhysicalTrainerByUser(long publicUserId, long trainerId);

    TrainerCoachDTO getTrainerForCoachApp(Trainer trainer);

    Page<PublicUserReviewsResponse> getTrainerRatingsByUser(long publicUserId, Pageable pageable);

    Page<PublicUserReviewsResponse> getPhysicalTrainerRatingsByUser(long trainerId, Pageable pageable);

    Page<TrainerNameIdDTO> getTrainersForClass(long classId, Pageable pageable);

    Page<TrainerRatingDTO> getTrainerRatings(long id, Pageable pageable);

    Page<TrainerRatingDTO> getPhysicalTrainerRatings(long id, Pageable pageable);

    Page<ClassForTrainerDTO> getClassesForTrainer(long id, Pageable pageable);

    Page<ClassForTrainerDTO> getPhysicalClassesForTrainer(long id, Pageable pageable);

    void createTrainerType(String type);
}
