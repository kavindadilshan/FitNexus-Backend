package com.fitnexus.server.dto.businessprofile;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class FacilityDTO {
    private long id;
    private String name;
    @ToString.Exclude
    private String image;
}
