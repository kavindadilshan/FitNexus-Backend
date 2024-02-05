package com.fitnexus.server.entity.classes.physical;

import com.fitnexus.server.entity.trainer.Trainer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;


@Setter
@Getter
@NoArgsConstructor
@ToString
@Entity
public class PhysicalClassTrainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "physical_class_id")
    private PhysicalClass physicalClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private Trainer trainer;

}
