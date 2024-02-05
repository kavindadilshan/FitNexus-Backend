package com.fitnexus.server.entity.classes.physical;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.classes.ClassType;
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
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
public class PhysicalClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = true)
    private String classUniqueName;

    private double rating;
    private long ratingCount;

    @Lob
    private String howToPrepare;

    @Lob
    private String description;

    private String profileImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private ClassType classType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BusinessProfile businessProfile;

    private int calorieBurnOut;
    private boolean firstSessionFree;

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
    @OneToMany(mappedBy = "physicalClass", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClassTrainer> physicalClassTrainers;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "physicalClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClassRating> physicalClassRatings;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "physicalClass", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClassImage> physicalClassImages;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "physicalClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClassSession> physicalClassSessions;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "physicalClass", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClassMembership> physicalClassMemberships;

    private boolean visible;
    private String youtubeUrl;
}
