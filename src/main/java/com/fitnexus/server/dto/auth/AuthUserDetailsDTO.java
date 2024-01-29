package com.fitnexus.server.dto.auth;

import com.fitnexus.server.enums.CoachVerificationType;
import com.fitnexus.server.enums.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
@NoArgsConstructor
public class AuthUserDetailsDTO implements CommonUserAuth {

    private String username;
    private String firstName;
    private String lastName;
    private String mobile;
    private String email;
    private Gender gender;

    private String description;
    @ToString.Exclude
    private String image;

    private String country;
    private String timeZone;
    private String addressLine1;
    private String addressLine2;
    private double longitude;
    private double latitude;
    private String city;
    private String province;
    private String postalCode;

    private boolean conditionsAccepted;
    private String businessAgreement;
    private long studioId;
    private String studioName;
    private CoachVerificationType verificationType;
    private String verificationNo;
}
