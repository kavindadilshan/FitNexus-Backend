package com.fitnexus.server.dto.gym;

import com.fitnexus.server.dto.businessprofile.FacilityDTO;
import com.fitnexus.server.dto.membership.DayPassForGymDTO;
import com.fitnexus.server.dto.membership.MembershipDTO;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class GymDTO {

    private long businessProfileId;
    private String businessProfileName;
    private String publicBusinessName;
    private String businessProfileImage;
    private double businessProfileRating;
    private double businessProfileRatingCount;
    private long businessProfileLocationId;
    private String locationName;
    private String country;
    private String timeZone;
    private String addressLine1;
    private String addressLine2;
    private double longitude;
    private double latitude;
    private String city;
    private String province;
    private String postalCode;

    private long gymId;
    private String gymName;
    private String gymUniqueName;
    private String description;
    private boolean openInWeekDays;
    private boolean openInWeekEnd;
    private LocalTime weekDaysOpeningHour;
    private LocalTime weekDaysClosingHour;
    private LocalTime weekendOpeningHour;
    private LocalTime weekendClosingHour;
    private LocalTime saturdayOpeningHour;
    private LocalTime saturdayClosingHour;
    private LocalTime sundayOpeningHour;
    private LocalTime sundayClosingHour;
    private boolean closedOnSpecificDay;
    private String closedSpecificDay;
    private double rating;
    private long ratingCount;
    @ToString.Exclude
    private String profileImage;
    @ToString.Exclude
    private List<String> gymImages;
    private List<Long> equipmentIdList;
    private List<EquipmentDTO> equipmentList;
    private List<Long> facilityIdList;
    private List<FacilityDTO> facilities;
    private double distance;

    private boolean membershipBooked;
    private LocalDateTime membershipExpireDateTime;
    private boolean dayPassAllowed;
    private boolean dayPassState;
    private DayPassForGymDTO dayPass;
    private int membershipCount;
    private List<MembershipDTO> memberships;

    private String youtubeUrl;

}
