package com.fitnexus.server.dto.classes;

import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.SessionButtonStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class ClassListDTO {

    private long id;
    private String name;
    private String classUniqueName;
    private double rating;
    private String howToPrepare;
    private String description;
    private List<String> images;
    private long ratingCount;
    private String profileImage;
    private int calorieBurnOut;
    private boolean firstSessionFree;
    private long averageSessionsPerWeek;
    private boolean sessionsUpcoming;
    private BigDecimal lastSessionPrice;

    private ClassTypeDTO classType;
    private ClassCategory category;
    private SessionButtonStatus buttonStatus;
    private BigDecimal discountMaxAmount;
    private double discountPercentage;
    private String discountDescription;
    private String youtubeUrl;

    public ClassListDTO(long id, String name, double rating, String howToPrepare, String description, long ratingCount, String profileImage, int calorieBurnOut, boolean firstSessionFree) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.howToPrepare = howToPrepare;
        this.description = description;
        this.ratingCount = ratingCount;
        this.profileImage = profileImage;
        this.calorieBurnOut = calorieBurnOut;
        this.firstSessionFree = firstSessionFree;
    }
}
