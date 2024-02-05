package com.fitnexus.server.entity.classes.physical;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PhysicalClassImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private PhysicalClass physicalClass;

    public PhysicalClassImage(String url, PhysicalClass physicalClass) {
        this.url = url;
        this.physicalClass = physicalClass;
    }
}
