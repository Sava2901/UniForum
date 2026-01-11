package com.forum.model.university;

import jakarta.persistence.*;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.forum.model.User;
import java.util.List;

@Entity
@Data
@Table(name = "university_courses")
@lombok.EqualsAndHashCode(exclude = "enrolledUsers")
public class UniversityCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int year;
    private int semester;
    
    private String description;

    @ManyToMany
    @JoinTable(
        name = "course_enrollments",
        joinColumns = @JoinColumn(name = "course_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore
    private List<User> enrolledUsers;
}
