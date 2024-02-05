package com.fitnexus.server.dto.classsession;

import com.fitnexus.server.dto.businessprofile.BusinessProfileLocationDTO;
import com.fitnexus.server.dto.businessprofile.FacilityDTO;
import com.fitnexus.server.dto.trainer.TrainerNameIdDTO;
import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.ClassSessionStatus;
import com.fitnexus.server.enums.Gender;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SessionDTO {

    private long sessionId;
    private String sessionName;
    private ClassSessionStatus status;
    private int maxJoiners;
    private long duration;
    private String description;
    private double price;
    private Gender gender;
    private LocalDateTime dateTime;
    private long classId;
    private String className;
    private String classProfileImage;
    private List<String> classImages;
    private ClassCategory classCategory;
    private long businessProfileId;
    private String businessProfileName;
    private long trainerId;
    private String trainerName;
    @ToString.Exclude
    private List<String> images;
    private String language;
    private List<TrainerNameIdDTO> trainersForClass;
    private long numberOfEnrollments;

    //physical
    private long locationId;
    private BusinessProfileLocationDTO location;
    private List<Long> facilityIdList;
    private List<FacilityDTO> facilities;
    private boolean allowCashPayment;

    public SessionDTO(long sessionId, String sessionName, ClassSessionStatus status, int maxJoiners, long duration, String description, double price, Gender gender, LocalDateTime dateTime, long classId, String className, String classProfileImage, List<String> classImages, ClassCategory classCategory, long businessProfileId, String businessProfileName, long trainerId, String trainerName, List<String> images, String language, List<TrainerNameIdDTO> trainersForClass, long numberOfEnrollments) {
        this.sessionId = sessionId;
        this.sessionName = sessionName;
        this.status = status;
        this.maxJoiners = maxJoiners;
        this.duration = duration;
        this.description = description;
        this.price = price;
        this.gender = gender;
        this.dateTime = dateTime;
        this.classId = classId;
        this.className = className;
        this.classProfileImage = classProfileImage;
        this.classImages = classImages;
        this.classCategory = classCategory;
        this.businessProfileId = businessProfileId;
        this.businessProfileName = businessProfileName;
        this.trainerId = trainerId;
        this.trainerName = trainerName;
        this.images = images;
        this.language = language;
        this.trainersForClass = trainersForClass;
        this.numberOfEnrollments = numberOfEnrollments;
    }
}
