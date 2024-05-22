package com.app.socialmedia.controllers;

import com.app.socialmedia.dtos.tweet.TweetDto;
import com.app.socialmedia.models.Tweet;
import com.app.socialmedia.models.User;
import com.app.socialmedia.services.TweetService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/tweets")
public class TweetController {
    private final TweetService tweetService;

    public TweetController(final TweetService tweetService) {
        this.tweetService = tweetService;
    }

    @PostMapping("/create")
    public ResponseEntity<Tweet> createTweet(@RequestBody TweetDto tweetDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Tweet createdTweet = tweetService.createTweet(tweetDto, currentUser);

        return ResponseEntity.ok(createdTweet);
    }

    @GetMapping("/user/{userId}/all")
    public ResponseEntity<List<Tweet>> getAllTweets(@PathVariable String userId) {
        List<Tweet> tweets = tweetService.getUserTweets(userId);
        return ResponseEntity.ok(tweets);
    }

    //client will take returned tweetId and modify isDeleted field
    @DeleteMapping("/{tweetId}/delete")
    public ResponseEntity<String> deleteTweet(@PathVariable String tweetId) {
        boolean successful = tweetService.toggleSoftDelete(tweetId);
        if (successful) {
            return ResponseEntity.ok(tweetId);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
