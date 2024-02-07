package com.fitnexus.server.entity.businessprofile;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.entity.classes.Class;
import com.fitnexus.server.entity.classes.physical.PhysicalClass;
import com.fitnexus.server.entity.instructor.InstructorBusinessProfile;
import com.fitnexus.server.entity.instructor.InstructorPackage;
import com.fitnexus.server.entity.trainer.TrainerBusinessProfile;
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
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String businessName;

    @Column(unique = true, nullable = true)
    private String publicBusinessName;

    private String regNumber;

    private String profileImage;

    @Lob
    private String description;

    private String telephone;
    private String email;

    private String accountNumber;
    private String accountName;
    private String bankName;
    private String bankCode;
    private String branchName;
    private String branchCode;
    private String swiftCode;

    private double rating;
    private long ratingCount;

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
    @OneToMany(mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<TrainerBusinessProfile> trainerBusinessProfiles;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<InstructorBusinessProfile> instructorBusinessProfiles;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<InstructorPackage> instructorPackages;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<BusinessAgreement> businessAgreements;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<BusinessProfileLocation> businessProfileLocations;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<BusinessProfileImage> businessProfileImages;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<BusinessProfileRevenue> businessProfileRevenues;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(cascade = {CascadeType.ALL}, mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private List<BusinessProfileClassType> businessProfileClassTypes;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "businessProfile", fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    private BusinessProfileManager businessProfileManager;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Class> classes;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalClass> physicalClasses;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PaymentSettlement> paymentSettlements;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<FailedTransaction> failedTransactions;
}
