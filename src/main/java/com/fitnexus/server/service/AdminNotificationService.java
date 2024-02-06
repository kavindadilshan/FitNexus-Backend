package com.fitnexus.server.service;

import com.fitnexus.server.dto.admin.AdminNotificationDTO;
import com.fitnexus.server.dto.admin.CustomNotificationDTO;
import com.fitnexus.server.entity.publicuser.PublicUser;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AdminNotificationService {
    void sendNotification(AdminNotificationDTO dto, String username);

    void sendCustomNotification(CustomNotificationDTO dto, String username);

    void performCustomNotificationSending(String message, List<PublicUser> publicUsers, boolean sendPush, boolean sendSms, String username);
}
