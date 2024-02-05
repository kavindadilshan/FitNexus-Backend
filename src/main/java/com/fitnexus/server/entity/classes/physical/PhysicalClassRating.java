package com.fitnexus.server.entity.classes.physical;

import com.fitnexus.server.entity.publicuser.PublicUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;


@Setter
@Getter
@NoArgsConstructor
@Entity
public class PhysicalClassRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private double rating;
    @Lob
    private String comment;
    @UpdateTimestamp
    private LocalDateTime dateTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "physical_class_id")
    private PhysicalClass physicalClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private PublicUser publicUser;
}
