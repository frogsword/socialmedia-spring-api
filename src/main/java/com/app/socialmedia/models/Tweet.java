package com.app.socialmedia.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document
public class Tweet {
    @Id
    private String id;
    private String body;
    private byte[] image;
    private String userId;
    private String userName;
    private byte[] userPfp;
    private Integer likeCount;
    private Integer replyCount;
    private boolean isDeleted;
    private List<String> parentIds;
    private LocalDateTime createdAt;
}
