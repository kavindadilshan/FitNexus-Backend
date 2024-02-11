package com.fitnexus.server.dto.instructor;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class InstructorDTO {

    private long instructorId;
    private String name;
    private String instructorImage;
    private List<String> instructorTypes;
    private long numberOfPackages;
    private double ratings;
    private long ratingCount;
    private long businessProfileId;
    private String businessProfileName;
    private String youtubeUrl;
}
