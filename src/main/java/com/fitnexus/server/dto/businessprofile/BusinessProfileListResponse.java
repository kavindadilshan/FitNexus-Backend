package com.fitnexus.server.dto.businessprofile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;


@NoArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class BusinessProfileListResponse {

    private long id;
    private String businessName;
    private String publicBusinessName;
    private BusinessAddressListResponse headOffice;
    private String branchName;
    private String description;
    private String profileImage;
    private List<String> images;
    private double rating;
    private long ratingCount;
    private String telephone;
    private String email;
    private List<BusinessAddressListResponse> addresses;
    private long averageClassesPerWeek;
    private long averageOnlineClassesPerWeek;
    private long averagePhysicalClassesPerWeek;
    private List<String> classTypes;
    private String country;
    private boolean isOnlineClassesVisible;
    private double distance;
}
