package com.forum.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import com.forum.model.university.UniversityCourse;

@Entity
@Table(name = "forums")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Forum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = true)
    private UniversityCourse course;

    @Column(name = "group_name")
    private String groupName;

    @Enumerated(EnumType.STRING)
    private ForumType type;

    @ManyToOne
    @JoinColumn(name = "professor_id")
    private User professor; 

    public enum ForumType {
        MAIN_COURSE,
        GROUP_SUBFORUM
    }

    @OneToMany(mappedBy = "forum", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Post> posts;

    @ManyToMany
    @JoinTable(
        name = "forum_allowed_users",
        joinColumns = @JoinColumn(name = "forum_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private java.util.Set<User> allowedUsers;
}
