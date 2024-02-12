package com.fitnexus.server.dto.admin;

import com.fitnexus.server.enums.EventType;
import com.fitnexus.server.enums.PublicUserAppType;
import com.fitnexus.server.enums.SelectedEvent;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FrontendEventDTO {

    private long event_id;
    private EventType eventType;
    private SelectedEvent selectedEvent;
    private LocalDateTime dateTime;
    private PublicUserAppType appType;
    private long publicUserId;
}
