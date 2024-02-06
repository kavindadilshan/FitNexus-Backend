package com.fitnexus.server.controller.admin;

import com.fitnexus.server.dto.admin.AdminNotificationDTO;
import com.fitnexus.server.dto.common.CommonResponse;
import com.fitnexus.server.service.AdminNotificationService;
import com.fitnexus.server.util.CustomGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/admin/notification")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @PostMapping(value = "")
    public ResponseEntity sendNotification(@RequestBody AdminNotificationDTO dto, @RequestHeader(name = "Authorization") String token) {
        log.info("Send admin notification : \nnotification dto: {}", dto);
        String username = getUsername(token);
        adminNotificationService.sendNotification(dto, username);
        log.info("Response : Notification sent successfully");
        return ResponseEntity.ok(new CommonResponse<>(true, "Notification sent successfully"));
    }

    private String getUsername(String token) {
        return CustomGenerator.getJsonObjectFromJwt(token).getString("user_name");
    }
}
