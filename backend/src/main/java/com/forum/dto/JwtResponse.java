package com.forum.dto;

import com.forum.model.Role;
import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String nickname;
    private String groupName;
    private Integer studyYear;
    private Integer semester;

    public JwtResponse(String token, Long id, String email, String firstName, String lastName, Role role,
                       String nickname, String groupName, Integer studyYear, Integer semester) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.nickname = nickname;
        this.groupName = groupName;
        this.studyYear = studyYear;
        this.semester = semester;
    }
}