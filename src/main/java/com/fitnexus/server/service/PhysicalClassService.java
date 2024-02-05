package com.fitnexus.server.service;

import com.fitnexus.server.dto.classes.*;
import com.fitnexus.server.dto.classsession.*;
import com.fitnexus.server.dto.classes.*;
import com.fitnexus.server.dto.classsession.*;
import com.fitnexus.server.dto.publicuser.PublicUserReviewsResponse;
import com.fitnexus.server.dto.trainer.TrainerNameIdDTO;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.enums.Gender;
import com.fitnexus.server.enums.SessionButtonStatus;
import com.fitnexus.server.enums.SessionGetDateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public interface PhysicalClassService {
    @Transactional
    void createPhysicalClass(ClassDTO dto, String creatingUsername);

    @Transactional
    void updatePhysicalClass(ClassDTO dto, String updatingUsername);

    void updateClassVisible(long id, boolean visible);

    List<ClassNameIdDTO> getAllPhysicalClassesForBusinessProfile(long businessProfileId);

    Page<ClassDTO> getAllPhysicalClassesByProfile(long businessProfileId, Pageable pageable);

    Page<ClassDTO> getAllPhysicalClasses(Pageable pageable);

    List<PhysicalClassDTO> getAllPhysicalClassesAll();

    Page<ClassDTO> searchPhysicalClass(String data, long businessProfileId, Pageable pageable, String username);

    List<ClassDTO> searchPhysicalClassByName(String name, String username);

    ClassDTO getPhysicalClassById(long id);

    Page<ClassRatingDTO> getPhysicalClassRatings(long classId, Pageable pageable);

    @Transactional
    void createPhysicalSession(SessionCreateDTO sessionDTO);

    @Transactional
    void updatePhysicalSession(SessionDTO dto);

    @Transactional
    void updatePhysicalSessionByCoach(SessionUpdateDTO dto);

    SessionCoachDTO getPhysicalSessionByIdForCoach(long id);

    @Transactional
    void cancelPhysicalSession(long sessionId);

    @Transactional
    void reschedulePhysicalSession(long classSessionId, LocalDateTime newDateTime);

    Page<SessionDTO> getAllPhysicalSessions(Pageable pageable);

    Page<SessionDTO> getSessionsByDate(LocalDate date, Pageable pageable);

    Page<SessionDTO> getAllPhysicalSessionsByClass(long classId, SessionGetDateType dateType, Pageable pageable);

    Page<SessionDTO> searchPhysicalSession(String data, long classId, Pageable pageable, String username);

    SessionDTO getPhysicalSessionById(long id);

    List<TrainerNameIdDTO> getTrainersForPhysicalClass(long id);

    Page<SessionEnrollDTO> getPhysicalSessionEnrollments(long sessionId, Pageable pageable);

    Page<ClassListDTO> getActivePhysicalClasses(Pageable pageable, String country, String token);

    Page<ClassListDTO> getActivePhysicalClassesOpen(Pageable pageable);

    Page<ClassSessionListResponse> getPhysicalSessionsForHome(Gender gender, String name, List<Long> classTypeIds,
                                                              LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                              double longitude, double latitude, String country,
                                                              Pageable pageable, String token, String timeZone);

    ClassSessionListResponse getClassSessionResponse(PhysicalClassSession c, PublicUser publicUser, LocalDateTime dateTime);

    SessionButtonStatus getButtonStatusBySession(PhysicalClassSession physicalClassSession, PhysicalClass physicalClass, PublicUser publicUser, LocalDateTime dateTime);

    SessionButtonStatus firstSessionFreeForUser(PhysicalClassSession physicalClassSession, PhysicalClass physicalClass, PublicUser publicUser, LocalDateTime dateTime);

    int getAvailableCountBySession(PhysicalClassSession physicalClassSession);

    int getEnrolledCountBySession(PhysicalClassSession physicalClassSession);

    ClassListDTO getPhysicalClassDetails(long classId, LocalDateTime dateTime, double longitude, double latitude, String token);

    ClassListDTO getPhysicalClassDetailsOpen(long classId);

    ClassListDTO getPhysicalClassDetailsOpenByClassName(String className);

    List<ClassSessionListResponse> getUpcomingPhysicalSessionsListLimitOpen(PhysicalClass physicalClass, LocalDateTime dateTime);

    Page<ClassSessionListResponse> getUpcomingPhysicalSessionsList(PhysicalClass physicalClass, LocalDateTime dateTime,
                                                                   String token, double longitude, double latitude, Pageable pageable);

    List<ClassSessionListResponse> getUpcomingPhysicalSessionsListLimit(PhysicalClass physicalClass, LocalDateTime dateTime,
                                                                        double longitude, double latitude,
                                                                        Pageable pageable, String token);

    List<ClassSessionListResponse> getPhysicalSessionsDtoList(List<Object[]> sessionObjectList, String token, LocalDateTime dateTime);

    ClassSessionSingleResponse getPhysicalSession(long sessionId, String token, LocalDateTime dateTime, double longitude, double latitude);

    ClassSessionSingleResponse getPhysicalSessionByClassNameAndDate(String className, LocalDateTime dateTime , String token, LocalDateTime timeZoneDateTime, double longitude, double latitude);

    Page<ClassSessionListResponse> getSessionsListByPhysicalClassId(long classId, LocalDateTime dateTime, String token,
                                                                    double longitude, double latitude, Pageable pageable);

    List<ClassListDTO> getPhysicalClassesByTrainer(long trainerUserId, String token);

    Page<ClassListDTO> getPhysicalClassesByBusinessProfile(long businessProfileId, String country, Pageable pageable, String token);

    @Transactional
    void ratePhysicalClass(ClassRateDTO rateDTO, int count);

    ClassRateDTO getRateForPhysicalClassByUser(long publicUserId, long physicalClassId);

    Page<PublicUserReviewsResponse> getPhysicalClassRatingsByUser(long physicalClassId, Pageable pageable);

    @Transactional
    void ratePhysicalClassAndTrainer(ClassAndTrainerRateDTO rateDTO, int count);

    BigDecimal getUserDiscountForPhysicalSession(PublicUser publicUser, PhysicalClassSession physiSession);

    BigDecimal getUserPromoDiscountForSession(PublicUser publicUser, PhysicalClassSession classSession, long discountId);

    ClassCoachDTO getPhysicalClassByIdForCoach(long id);

    Page<SessionDateTimeDTO> getAllUpcomingSessionsForClass(long classId, Pageable pageable);

    Page<ClassForBusinessProfileDTO> getPhysicalClassesForBusinessProfile(long bpId, String username, Pageable pageable);

    Page<SessionDateTimeDTO> getUpcomingSessionByPhysicalClassForCoach(long classId, Pageable pageable);

    void delete(long id);

    Page<SessionDetailDTO> getUpcomingSessionByBusinessProfile(long businessProfileId, LocalDate dateTime, String token, Pageable pageable);
}
