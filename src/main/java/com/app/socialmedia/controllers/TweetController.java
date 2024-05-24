package com.app.socialmedia.controllers;

import com.app.socialmedia.dtos.tweet.TweetDto;
import com.app.socialmedia.models.Tweet;
import com.app.socialmedia.models.User;
import com.app.socialmedia.services.TweetService;
import com.app.socialmedia.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/tweets")
public class TweetController {
    private final TweetService tweetService;
    private final UserService userService;

    public TweetController(final TweetService tweetService, UserService userService) {
        this.tweetService = tweetService;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<Tweet> createTweet(@RequestParam MultipartFile image, @RequestParam String body) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(image.getContentType(), "image/jpeg")) {
            return ResponseEntity.badRequest().build();
        }

        TweetDto tweetDto = new TweetDto();
        tweetDto.setBody(body);
        tweetDto.setImage(image);

        Tweet createdTweet = tweetService.createTweet(tweetDto, currentUser);

        return ResponseEntity.ok(createdTweet);
    }

    @PostMapping("/{parentId}/reply")
    public ResponseEntity<Tweet> replyToTweet(@PathVariable String parentId, @RequestParam MultipartFile image, @RequestParam String body) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (!Objects.equals(image.getContentType(), "image/jpeg")) {
            return ResponseEntity.badRequest().build();
        }

        TweetDto tweetDto = new TweetDto();
        tweetDto.setBody(body);
        tweetDto.setImage(image);

        Tweet reply = tweetService.replyToTweet(tweetDto, currentUser, parentId);

        return ResponseEntity.ok(reply);
    }

    @PutMapping("/{tweetId}/like")
    public ResponseEntity<String> likeTweet(@PathVariable String tweetId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        List<String> likedTweets = currentUser.getLikedTweets();
        boolean successful;

        //unlike tweet
        if (likedTweets.contains(tweetId)) {
            //successful = userService.unlikeTweet(tweetId, currentUser.getId());
            successful = userService.updateLikedTweets(tweetId, currentUser.getId(), true);
            if (successful) {
                boolean successfulLike = tweetService.updateLikeCount(tweetId, -1);
                if (successfulLike) {
                    return ResponseEntity.ok(tweetId);
                }
                else {
                    return ResponseEntity.notFound().build();
                }
            }
            else {
                return ResponseEntity.notFound().build();
            }
        }

        //like tweet
        else {
            //successful = userService.likeTweet(tweetId, currentUser.getId());
            successful = userService.updateLikedTweets(tweetId, currentUser.getId(), false);
            if (successful) {
                boolean successfulLike = tweetService.updateLikeCount(tweetId, 1);
                if (successfulLike) {
                    return ResponseEntity.ok(tweetId);
                }
                else {
                    return ResponseEntity.notFound().build();
                }
            }
            else {
                return ResponseEntity.notFound().build();
            }
        }
    }

    @GetMapping("/user/{userId}")
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
