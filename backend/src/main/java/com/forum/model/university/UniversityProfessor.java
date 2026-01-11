package com.forum.model.university;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "university_professors")
public class UniversityProfessor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    private String firstName;
    private String lastName;
    private String department;
}