package com.fitnexus.server.entity.auth;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
public class MobileOtp {

    @Id
    private String mobile;
    private String otp;

    @UpdateTimestamp
    private LocalDateTime dateTime;

}
