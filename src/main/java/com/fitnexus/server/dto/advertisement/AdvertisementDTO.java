package com.fitnexus.server.dto.advertisement;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AdvertisementDTO {
    private long id;
    private LocalDateTime expireDateTime;
    private String image;
    private boolean visible;
}
