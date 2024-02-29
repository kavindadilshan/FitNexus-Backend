package com.fitnexus.server.repository.publicuser;

import com.fitnexus.server.entity.publicuser.FrontendEvent;
import com.fitnexus.server.entity.publicuser.PublicUser;
import com.fitnexus.server.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrontendEventRepository extends JpaRepository<FrontendEvent, Long> {

    boolean existsByPublicUserAndEventType(PublicUser publicUser, EventType eventType);

    FrontendEvent findTopByEventTypeAndPublicUser(EventType eventType, PublicUser publicUser);

}
