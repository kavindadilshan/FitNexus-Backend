package com.fitnexus.server.entity.admin;

import com.fitnexus.server.enums.AdminNotificationSubType;
import com.fitnexus.server.enums.AdminNotificationType;
import com.fitnexus.server.enums.NotificationUserGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private Admin admin;

    @Enumerated(EnumType.STRING)
    private NotificationUserGroup userGroup;

    @Enumerated(EnumType.STRING)
    private AdminNotificationType type;

    @Enumerated(EnumType.STRING)
    private AdminNotificationSubType subType;

    @Lob
    private String message;

    private String title;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @OneToMany(mappedBy = "notification", orphanRemoval = true, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<AdminNotificationSendingType> adminNotificationSendingTypes;
}
