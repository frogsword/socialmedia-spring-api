package com.app.socialmedia.services;

import com.app.socialmedia.dtos.tweet.ThreadTweets;
import com.app.socialmedia.dtos.tweet.TweetDto;
import com.app.socialmedia.models.Tweet;
import com.app.socialmedia.models.User;
import com.app.socialmedia.repositories.TweetRepository;
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

    public List<Tweet> getTweets() {
        List<Tweet> allTweets = tweetRepository.findAll().reversed();
        List<Tweet> allParents = new ArrayList<>();

        for (Tweet tweet : allTweets) {
            if (tweet.getParentIds().isEmpty()) {
                allParents.add(tweet);
            }
        }

        return allParents;
    }

    public List<Tweet> getUserTweets(String userName) {
        List<Tweet> tweetsTemp = tweetRepository.findByUserName(userName).reversed();
        List<Tweet> tweets = new ArrayList<>(tweetsTemp);

        return tweets;
    }

    public ThreadTweets getTweetThread(String tweetId) {
        Tweet mainTweet = tweetRepository.findById(tweetId).orElse(null);

        if (mainTweet == null) {
            return null;
        }

        List<Tweet> parents = new ArrayList<>();
        for (String parentId : mainTweet.getParentIds()) {
            Tweet parentTweet = tweetRepository.findById(parentId).orElse(null);
            parents.add(parentTweet);
        }

        List<Tweet> children = new ArrayList<>();
        for (String replyId : mainTweet.getReplyIds()) {
            Tweet replyTweet = tweetRepository.findById(replyId).orElse(null);
            children.add(replyTweet);
        }

        ThreadTweets threadTweets = new ThreadTweets();
        threadTweets.setParents(parents);
        threadTweets.setMainTweet(mainTweet);
        threadTweets.setChildren(children);

        return threadTweets;
    }

    public Tweet createTweet(TweetDto tweetDto, User user) throws IOException {
        Tweet tweet = new Tweet();

        if (tweetDto.getImage() != null) {
            byte[] image = tweetDto.getImage().getBytes();
            tweet.setImage(image);
        }
        else {
            tweet.setImage(null);
        }

        tweet.setBody(tweetDto.getBody());

        tweet.setUserName(user.getName());
        tweet.setUserChangeableName(user.getChangeableName());
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
                byte[] image = tweetDto.getImage().getBytes();
                tweet.setImage(image);
            }

            tweet.setBody(tweetDto.getBody());

            tweet.setUserName(user.getName());
            tweet.setUserChangeableName(user.getChangeableName());
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

    public boolean deleteTweet(String tweetId) {
        Optional<Tweet> tweetOptional = tweetRepository.findById(tweetId);
        if (tweetOptional.isPresent()) {
            Tweet tweet = tweetOptional.get();
            tweet.setDeleted(true);
            tweet.setImage(null);
            tweet.setUserPfp(null);
            tweetRepository.save(tweet);
            return true;
        }
        return false;
    }
    
    public boolean updateTweetPfps(User user) {
        byte[] newPfp = user.getProfilePicture();
        
        List<Tweet> userTweets = tweetRepository.findByUserId(user.getId());
        
        if (userTweets.isEmpty()) {
            return false;
        }
        
        for (Tweet tweet : userTweets) {
            tweet.setUserPfp(newPfp);
            tweetRepository.save(tweet);
        }
        
        return true;
    }
}
