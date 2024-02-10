package com.fitnexus.server.dto.classsession;

import com.fitnexus.server.dto.businessprofile.FacilityDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class ClassSessionSingleResponse extends ClassSessionListResponse {
    //trainer
    private double trainerRating;
    private long trainerRatingCount;
    private String trainerImage;
    private long trainerId;
    private long trainerUserId;
    private String trainerDescription;
    //class
    private String howToPrepare;
    private String classDescription;
    private long classId;
    private String classImage;

    //physical
    private List<FacilityDTO> facilities;
}
