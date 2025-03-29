package org.example.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name="name")
    private String name;
    @Column(name = "type")
    private String type;

}
