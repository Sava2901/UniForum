package com.forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private AuthorDto author;
    private Long forumId;
    private LocalDateTime timestamp;
    private boolean isPinned;
    private int score;
    private List<CommentResponse> comments;
}
