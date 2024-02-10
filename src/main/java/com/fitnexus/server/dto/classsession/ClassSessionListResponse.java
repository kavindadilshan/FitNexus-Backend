package com.fitnexus.server.dto.classsession;

import com.fitnexus.server.dto.businessprofile.BusinessProfileLocationDTO;
import com.fitnexus.server.dto.membership.MembershipDTO;
import com.fitnexus.server.dto.membership.corporate.CorporateMembershipNameIdDTO;
import com.fitnexus.server.dto.packages.PackageNameIdDTO;
import com.fitnexus.server.dto.physical_class.PhysicalClassMembershipSlotCountDTO;
import com.fitnexus.server.enums.ClassCategory;
import com.fitnexus.server.enums.ClassSessionStatus;
import com.fitnexus.server.enums.Gender;
import com.fitnexus.server.enums.SessionButtonStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@SuperBuilder
public class ClassSessionListResponse implements Comparable<ClassSessionListResponse> {

    //session
    private long id;
    private String name;
    private long duration;
    private String description;
    private LocalDateTime dateAndTime;
    private LocalDateTime endDateAndTime;
    private List<String> images;
    private BigDecimal price;
    private Gender gender;
    private String language;
    private int maxJoiners;
    private int availableCount;
    private ClassSessionStatus sessionStatus;

    //trainer
    private String trainerFirstName;
    private String trainerLastName;

    //class
    private long classId;
    private String className;
    private String classUniqueName;
    private String classProfileImage;
    private List<String> classImages;
    private String classTypeName;
    private double classRating;
    private long ratingCount;
    private long averageSessionsPerWeek;
    private int calorieBurnOut;
    private ClassCategory category;
    private String youtubeUrl;

    // additional
    private SessionButtonStatus buttonStatus;
    private BigDecimal discountMaxAmount;
    private double discountPercentage;
    private String discountDescription;

    private String country;

    private BusinessProfileLocationDTO location;
    private double distance;
    private boolean allowCashPayment;

    //membership details
    private int membershipCount;
    private List<MembershipDTO> memberships;
    private boolean membershipBooked;
    private List<PhysicalClassMembershipSlotCountDTO> bookedMemberships;

    //corporate details
    private boolean corporateMembershipBooked;
    private List<CorporateMembershipNameIdDTO> corporates;

    //subscription package details
    private boolean packageSubscribed;
    private List<PackageNameIdDTO> subscribedPackages;
    private List<PackageNameIdDTO> packagesForClass;

    public ClassSessionListResponse(long id, String name, long duration, String description, LocalDateTime dateAndTime,
                                    LocalDateTime endDateAndTime, List<String> images, BigDecimal price, Gender gender,
                                    String language, int maxJoiners, int availableCount, ClassSessionStatus sessionStatus,
                                    String trainerFirstName, String trainerLastName, long classId, String className, String classUniqueName,
                                    String classProfileImage, List<String> classImages, String classTypeName, double classRating,
                                    long ratingCount, long averageSessionsPerWeek, int calorieBurnOut, ClassCategory category,
                                    SessionButtonStatus buttonStatus, BigDecimal discountMaxAmount, double discountPercentage,
                                    String discountDescription, String country, BusinessProfileLocationDTO location, double distance) {
        this.id = id;
        this.name = name;
        this.duration = duration;
        this.description = description;
        this.dateAndTime = dateAndTime;
        this.endDateAndTime = endDateAndTime;
        this.images = images;
        this.price = price;
        this.gender = gender;
        this.language = language;
        this.maxJoiners = maxJoiners;
        this.availableCount = availableCount;
        this.sessionStatus = sessionStatus;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.classId = classId;
        this.className = className;
        this.classUniqueName = classUniqueName;
        this.classProfileImage = classProfileImage;
        this.classImages = classImages;
        this.classTypeName = classTypeName;
        this.classRating = classRating;
        this.ratingCount = ratingCount;
        this.averageSessionsPerWeek = averageSessionsPerWeek;
        this.calorieBurnOut = calorieBurnOut;
        this.category = category;
        this.buttonStatus = buttonStatus;
        this.discountMaxAmount = discountMaxAmount;
        this.discountPercentage = discountPercentage;
        this.discountDescription = discountDescription;
        this.country = country;
        this.location = location;
        this.distance = distance;
    }

    @Override
    public int compareTo(ClassSessionListResponse o) {
        return this.dateAndTime.compareTo(o.dateAndTime);
    }
}
