package com.fitnexus.server.entity.gym;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.businessprofile.BusinessProfileLocation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Gym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private String name;
    private double rating;
    private long ratingCount;
    private boolean openInWeekDays;
    private boolean openInWeekEnd;
    private LocalTime weekDaysOpeningHour;
    private LocalTime weekDaysClosingHour;
    private LocalTime saturdayOpeningHour;
    private LocalTime saturdayClosingHour;
    private LocalTime sundayOpeningHour;
    private LocalTime sundayClosingHour;
    private boolean closedOnSpecificDay;
    private String closedSpecificDay;
    private String profileImage;

    @Column(unique = true, nullable = true)
    private String gymUniqueName;

    @Lob
    private String description;

    @CreationTimestamp
    private LocalDateTime createdDate;
    @UpdateTimestamp
    private LocalDateTime updatedDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private AuthUser createdBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private AuthUser updatedBy;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private BusinessProfileLocation location;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "gym", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<GymImage> gymImages;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "gym", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<GymEquipment> gymEquipments;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<GymRating> gymRatings;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "gym", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<GymMembership> gymMemberships;

    private String youtubeUrl;
}
