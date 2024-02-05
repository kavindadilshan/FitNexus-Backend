package com.fitnexus.server.entity.classes.physical;

import com.fitnexus.server.entity.auth.Language;
import com.fitnexus.server.entity.businessprofile.BusinessProfileLocation;
import com.fitnexus.server.entity.trainer.Trainer;
import com.fitnexus.server.enums.ClassSessionStatus;
import com.fitnexus.server.enums.Gender;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
public class PhysicalClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    private int maxJoiners;
    private long duration;

    @Lob
    private String description;

    @Enumerated(EnumType.STRING)
    private ClassSessionStatus status;

    @Column(nullable = false)
    @Digits(integer = 9, fraction = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDateTime dateAndTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "physical_class_id")
    private PhysicalClass physicalClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private BusinessProfileLocation businessProfileLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Trainer trainer;

    private boolean allowCashPayment;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "physicalClassSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<PhysicalSessionEnroll> physicalSessionEnrolls;

    public PhysicalClassSession(long id) {
        this.id = id;
    }
}
