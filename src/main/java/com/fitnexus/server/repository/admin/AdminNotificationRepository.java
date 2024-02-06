package com.fitnexus.server.repository.admin;

import com.fitnexus.server.entity.admin.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification,Long> {
}
