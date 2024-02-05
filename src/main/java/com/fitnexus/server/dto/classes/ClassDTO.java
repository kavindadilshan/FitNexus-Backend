package com.fitnexus.server.dto.classes;

import com.fitnexus.server.dto.trainer.TrainerNameIdDTO;
import com.fitnexus.server.enums.BusinessProfilePaymentModel;
import com.fitnexus.server.enums.ClassCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ClassDTO {

    private long classId;
    private String name;
    private String classUniqueName;
    private ClassCategory category;
    private int calorieBurnOut;
    private boolean firstSessionFree;
    private long classTypeId;
    private String classTypeName;
    private String howToPrepare;
    private String description;
    @ToString.Exclude
    private String profileImage;
    @ToString.Exclude
    private List<String> images;
    private double rating;
    private long ratingCount;
    private long businessProfileId;
    private String businessProfileName;
    private BusinessProfilePaymentModel paymentModel;
    private String createdBy;
    private LocalDateTime createdDate;
    private String modifiedBy;
    private LocalDateTime modifiedDate;
    private int sessionCount;
    private List<Long> trainerIdList;
    private List<Map<String, Object>> trainerDetails;
    private List<TrainerNameIdDTO> trainersForBusinessProfile;
    private boolean visible;
    private String youtubeUrl;
}
