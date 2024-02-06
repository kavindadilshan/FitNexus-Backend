package com.fitnexus.server.dto.admin;

import com.fitnexus.server.enums.AdminNotificationSendingType;
import com.fitnexus.server.enums.AdminNotificationSubType;
import com.fitnexus.server.enums.AdminNotificationType;
import com.fitnexus.server.enums.NotificationUserGroup;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdminNotificationDTO {

    private long id;
    private NotificationUserGroup userGroup;
    private AdminNotificationType type;
    private AdminNotificationSubType subType;
    private List<AdminNotificationSendingType> sendingTypes;
    private String title;
    private String message;
    private List<Long> classIdList;
    private List<Long> gymIdList;
}
