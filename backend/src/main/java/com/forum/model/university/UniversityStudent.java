package com.forum.model.university;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "university_students")
public class UniversityStudent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private int year;
    private int semester;
    private String groupName;
}
