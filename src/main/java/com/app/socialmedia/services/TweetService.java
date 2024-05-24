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

    public Tweet replyToTweet(TweetDto tweetDto, User user, String parentId) {
        Tweet parentTweet = tweetRepository.findById(parentId).orElse(null);

        if (parentTweet == null) {
            return null;
        }
        else {
            List<String> parentsParentIds = parentTweet.getParentIds();
            List<String> parentIds = new ArrayList<>(parentsParentIds);
            parentIds.add(parentId);


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
            tweet.setParentIds(parentIds);

            parentTweet.setReplyCount(parentTweet.getReplyCount() + 1);
            tweetRepository.save(parentTweet);

            return tweetRepository.save(tweet);
        }
    }

    public List<Tweet> getUserTweets(String userId) {
        return tweetRepository.findByUserId(userId);
    }

    public boolean updateLikeCount(String tweetId, int count) {
        Optional<Tweet> tweetOptional = tweetRepository.findById(tweetId);
        if (tweetOptional.isPresent()) {
            Tweet tweet = tweetOptional.get();
            tweet.setLikeCount(tweet.getLikeCount() + count);
            tweetRepository.save(tweet);
            return true;
        }
        return false;
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
