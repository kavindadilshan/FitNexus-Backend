package com.fitnexus.server.service;

import com.fitnexus.server.dto.classes.ClassNameIdDTO;
import com.fitnexus.server.dto.gym.GymDTO;
import com.fitnexus.server.dto.gym.GymRateDTO;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public interface GymService {
    @Transactional
    void createGym(GymDTO dto, String username);

    @Transactional
    void updateGym(GymDTO dto, String username);

    Page<GymDTO> getAllGyms(Pageable pageable);

    List<GymDTO> getAllGymsAll();

    GymDTO getByIdAndPublicUserToken(long id, double longitude, double latitude, String token);

    GymDTO getByUniqueNameAndPublicUserToken(String gymName, double longitude, double latitude, String token);

    GymDTO getByIdOpen(long id);

    GymDTO getById(long id);

    Page<GymDTO> getPopularGyms(Pageable pageable, String country, double longitude, double latitude);

    Page<GymDTO> getPopularGymsOpen(Pageable pageable);

    Page<GymDTO> searchActiveGyms(String name, double longitude, double latitude, Pageable pageable);

    Page<GymDTO> searchGyms(String text, Pageable pageable);

    Page<GymDTO> getGymsByBusinessProfile(long profileId, Pageable pageable);

    Page<GymDTO> getGymsByBusinessProfileWithDistance(long profileId, double longitude, double latitude, String country, Pageable pageable);

    Page<GymDTO> searchGymsByBusinessProfile(long profileId, String text, Pageable pageable);

    @Transactional
    void rateGym(GymRateDTO rateDTO, int count);

    GymRateDTO getRateForGymByUser(long publicUserId, long gymId);

    Page<PublicUserReviewsResponse> getRateForGym(long gymId, Pageable pageable);

    List<ClassNameIdDTO> getAllGymsForBusinessProfile(long businessProfileId);

    void delete(long id);
}
