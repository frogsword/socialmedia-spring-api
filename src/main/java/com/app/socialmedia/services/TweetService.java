package com.app.socialmedia.services;

import com.app.socialmedia.dtos.tweet.TweetDto;
import com.app.socialmedia.models.Tweet;
import com.app.socialmedia.models.User;
import com.app.socialmedia.repositories.TweetRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class TweetService {
    private final TweetRepository tweetRepository;

    public TweetService(TweetRepository tweetRepository) {
        this.tweetRepository = tweetRepository;
    }

    public Tweet createTweet(TweetDto tweetDto, User user) {
        Tweet tweet = new Tweet();

        tweet.setBody(tweetDto.getBody());
        tweet.setImage(tweetDto.getImage());

        tweet.setUserName(user.getName());
        tweet.setUserId(user.getId());
        tweet.setUserPfp(user.getProfilePicture());

        tweet.setCreatedAt(new Date());

        tweet.setLikeCount(0);
        tweet.setReplyCount(0);
        tweet.setDeleted(false);
        tweet.setParentIds(new ArrayList<>());

        return tweetRepository.save(tweet);
    }

    public List<Tweet> getUserTweets(String userId) {
        return tweetRepository.findByUserId(userId);
    }

    public boolean toggleSoftDelete(String tweetId) {
        Optional<Tweet> tweetOptional = tweetRepository.findById(tweetId);
        if (tweetOptional.isPresent()) {
            Tweet tweet = tweetOptional.get();
            tweet.setDeleted(!tweet.isDeleted());
            tweetRepository.save(tweet);
            return true;
        }
        return false;
    }
}