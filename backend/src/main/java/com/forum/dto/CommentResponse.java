package com.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private AuthorDto author;
    private Long postId;
    private LocalDateTime timestamp;
    private int score;
    private Long parentId;
    private List<CommentResponse> replies;
}
