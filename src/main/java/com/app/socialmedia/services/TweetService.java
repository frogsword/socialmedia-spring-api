package com.app.socialmedia.services;

import com.app.socialmedia.dtos.tweet.TweetDto;
import com.app.socialmedia.models.Tweet;
import com.app.socialmedia.models.User;
import com.app.socialmedia.repositories.TweetRepository;
import com.app.socialmedia.utils.ImageUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public List<Tweet> getUserTweets(String userId) {
        List<Tweet> tweetsTemp = tweetRepository.findByUserId(userId);
        List<Tweet> tweets = new ArrayList<>(tweetsTemp);

        for (Tweet tweet : tweets) {
            if (tweet.getImage() != null && tweet.getImage().length != 0) {
                tweet.setImage(ImageUtil.decompressImage(tweet.getImage()));
            }
        }

        return tweets;
    }

    public List<Tweet> getTweetThread(String tweetId) {
        Tweet mainTweet = tweetRepository.findById(tweetId).orElse(null);

        if (mainTweet == null) {
            return null;
        }

        List<Tweet> tweets = new ArrayList<>();

        for (String parentId : mainTweet.getParentIds()) {
            Tweet parentTweet = tweetRepository.findById(parentId).orElse(null);
            tweets.add(parentTweet);
        }

        tweets.add(mainTweet);

        for (String replyId : mainTweet.getReplyIds()) {
            Tweet replyTweet = tweetRepository.findById(replyId).orElse(null);
            tweets.add(replyTweet);
        }

        for (Tweet tweet : tweets) {
            if (tweet.getImage() != null && tweet.getImage().length != 0) {
                tweet.setImage(ImageUtil.decompressImage(tweet.getImage()));
            }
        }

        return tweets;
    }

    public Tweet createTweet(TweetDto tweetDto, User user) throws IOException {
        Tweet tweet = new Tweet();

        if (tweetDto.getImage() != null) {
            byte[] imageRaw = tweetDto.getImage().getBytes();
            byte[] image = ImageUtil.compressImage(imageRaw);
            tweet.setImage(image);
        }

        tweet.setBody(tweetDto.getBody());

        tweet.setUserName(user.getName());
        tweet.setUserId(user.getId());
        tweet.setUserPfp(user.getProfilePicture());

        tweet.setCreatedAt(new Date());

        tweet.setLikeCount(0);
        tweet.setReplyCount(0);
        tweet.setDeleted(false);
        tweet.setParentIds(new ArrayList<>());
        tweet.setReplyIds(new ArrayList<>());

        return tweetRepository.save(tweet);
    }

    public Tweet replyToTweet(TweetDto tweetDto, User user, String parentId) throws IOException {
        Tweet parentTweet = tweetRepository.findById(parentId).orElse(null);

        if (parentTweet == null) {
            return null;
        }
        else {
            List<String> parentsParentIds = parentTweet.getParentIds();
            List<String> parentsReplyIds = parentTweet.getReplyIds();

            List<String> parentIds = new ArrayList<>(parentsParentIds);
            List<String> replyIds = new ArrayList<>(parentsReplyIds);

            parentIds.add(parentId);

            Tweet tweet = new Tweet();

            if (tweetDto.getImage() != null) {
                byte[] imageRaw = tweetDto.getImage().getBytes();
                byte[] image = ImageUtil.compressImage(imageRaw);
                tweet.setImage(image);
            }

            tweet.setBody(tweetDto.getBody());

            tweet.setUserName(user.getName());
            tweet.setUserId(user.getId());
            tweet.setUserPfp(user.getProfilePicture());

            tweet.setCreatedAt(new Date());

            tweet.setLikeCount(0);
            tweet.setReplyCount(0);
            tweet.setDeleted(false);
            tweet.setParentIds(parentIds);
            tweet.setReplyIds(new ArrayList<>());

            Tweet newReply = tweetRepository.save(tweet);

            parentTweet.setReplyCount(parentTweet.getReplyCount() + 1);
            replyIds.add(newReply.getId());
            parentTweet.setReplyIds(replyIds);
            tweetRepository.save(parentTweet);

            return newReply;
        }
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
