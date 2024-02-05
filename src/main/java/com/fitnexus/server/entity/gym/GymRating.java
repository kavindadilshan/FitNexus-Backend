package com.fitnexus.server.entity.gym;

import com.fitnexus.server.entity.publicuser.PublicUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class GymRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gym_id")
    private Gym gym;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "public_user_id")
    private PublicUser publicUser;

    private double rating;
    @UpdateTimestamp
    private LocalDateTime dateTime;
    @Lob
    private String comment;
}
