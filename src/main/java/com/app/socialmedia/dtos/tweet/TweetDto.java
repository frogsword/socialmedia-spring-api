package com.app.socialmedia.dtos.tweet;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class TweetDto {
    private String body;
    private MultipartFile image;
}
