package com.fitnexus.server.dto.classes;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ClassRatingDTO {

    private long classRatingId;
    private long publicUserId;
    private String publicUserName;
    private double rating;
    private String comment;
    private LocalDateTime dateTime;
}
