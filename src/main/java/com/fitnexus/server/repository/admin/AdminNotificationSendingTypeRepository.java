package com.fitnexus.server.repository.admin;

import com.fitnexus.server.entity.admin.AdminNotificationSendingType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminNotificationSendingTypeRepository extends JpaRepository<AdminNotificationSendingType,Long> {
}
