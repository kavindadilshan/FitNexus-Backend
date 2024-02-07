package com.fitnexus.server.entity.businessprofile;

import com.fitnexus.server.entity.auth.AuthUser;
import com.fitnexus.server.enums.ManagerStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
public class BusinessProfileManager {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ManagerStatus status;

    @OneToOne
    @JoinColumn(nullable = false, unique = true)
    private AuthUser authUser;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private BusinessProfile businessProfile;

    private boolean conditionsAccepted;

    public BusinessProfileManager(ManagerStatus status, AuthUser authUser, BusinessProfile businessProfile, boolean conditionsAccepted) {
        this.status = status;
        this.authUser = authUser;
        this.businessProfile = businessProfile;
        this.conditionsAccepted = conditionsAccepted;
    }
}
