package com.fitnexus.server.dto.instructor;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class InstructorNameImageDTO {

    private long id;
    private String firstName;
    private String lastName;
    private String publicUsername;
    private String image;
    private String description;
    private long userId;
    private double rating;
    private long ratingCount;
    private String country;
    private List<String> packageTypes;

    public InstructorNameImageDTO(long id, String firstName, String lastName, String publicUsername, String image, String description, long ratingCount, String country, List<String> packageTypes) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.publicUsername = publicUsername;
        this.image = image;
        this.description = description;
        this.ratingCount = ratingCount;
        this.country = country;
        this.packageTypes = packageTypes;
    }
}
