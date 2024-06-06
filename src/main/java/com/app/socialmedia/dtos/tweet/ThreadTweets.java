package com.app.socialmedia.dtos.tweet;

import com.app.socialmedia.models.Tweet;
import lombok.Data;

import java.util.List;

@Data
public class ThreadTweets {
    List<Tweet> parents;
    Tweet mainTweet;
    List<Tweet> children;
}
