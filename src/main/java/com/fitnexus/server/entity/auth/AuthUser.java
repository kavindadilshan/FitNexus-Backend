package com.fitnexus.server.entity.auth;

import com.fitnexus.server.entity.admin.Admin;
import com.fitnexus.server.entity.advertisement.Advertisement;
import com.fitnexus.server.entity.businessprofile.BusinessProfile;
import com.fitnexus.server.entity.businessprofile.BusinessProfileManager;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.ClassType;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.classes.physical.PhysicalClassSession;
import com.fitnexus.server.entity.classes.physical.PhysicalSessionEnroll;
import com.fitnexus.server.entity.gym.Gym;
import com.fitnexus.server.entity.instructor.Instructor;
import com.fitnexus.server.entity.instructor.InstructorPackage;
import com.fitnexus.server.entity.instructor.InstructorPackageType;
import com.fitnexus.server.entity.publicuser.PublicUserMembership;
import com.fitnexus.server.entity.trainer.Trainer;
import com.fitnexus.server.enums.Gender;
import com.fitnexus.server.enums.UserStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = true)
    private String publicUsername;

    @Column(unique = true, nullable = false)
    private String username;
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String mobile;
    private String firstName;
    private String lastName;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @CreationTimestamp
    private LocalDateTime notificationLastSeenDateTime;
    private String zoomUserId;

    private String country;
    private String timeZone;
    private String timeZoneLongName;
    private String addressLine1;
    private String addressLine2;
    private double longitude;
    private double latitude;
    private String city;
    private String province;
    private String postalCode;
    private boolean emailVerified;

    private String image;

    @Lob
    private String description;

    private LocalDateTime lastLoginDateTime;
    private int loginAttempts;

    @OneToOne(mappedBy = "authUser", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Admin admin;

    @OneToOne(mappedBy = "authUser", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Trainer trainer;

    @OneToOne(mappedBy = "authUser", cascade = CascadeType.ALL)
    @ToString.Exclude
    private Instructor instructor;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "authUser", fetch = FetchType.EAGER, orphanRemoval = true)
    @ToString.Exclude
    private List<UserRoleDetail> userRoleDetails;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "authUser", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<UserLanguage> userLanguages;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "authUser", orphanRemoval = true)
    @ToString.Exclude
    private List<UserNotification> userNotifications;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "authUser", orphanRemoval = true)
    @ToString.Exclude
    private List<UserPushToken> userPushTokens;

    @OneToOne(mappedBy = "authUser", cascade = CascadeType.ALL)
    @ToString.Exclude
    private BusinessProfileManager businessProfileManager;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "language", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClassSession> physicalClassSessions;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Advertisement> advertisementsCreated;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Advertisement> advertisementsUpdated;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<BusinessProfile> createdBusinessProfiles;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<BusinessProfile> updatedBusinessProfiles;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClass> createdPhysicalClasses;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClass> updatedPhysicalClasses;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Class> createdClasses;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Class> updatedClasses;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ClassType> createdClassTypes;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<ClassType> updatedClassTypes;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Gym> createdGyms;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Gym> updatedGyms;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Instructor> createdInstructors;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Instructor> updatedInstructors;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<InstructorPackage> createdInstructorPackages;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<InstructorPackage> updatedInstructorPackages;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<InstructorPackageType> createdInstructorPackageTypes;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<InstructorPackageType> updatedInstructorPackageTypes;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Trainer> createdTrainers;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "updatedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Trainer> updatedTrainers;

    @OneToMany(mappedBy = "collectedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PublicUserMembership> publicUserMemberships;

    @OneToMany(mappedBy = "collectedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalSessionEnroll> physicalSessionEnrolls;

}
