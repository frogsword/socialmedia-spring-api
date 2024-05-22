package com.app.socialmedia.services;

import com.app.socialmedia.models.Tweet;
import com.app.socialmedia.models.User;
import com.app.socialmedia.repositories.UserRepository;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        return new ArrayList<>(userRepository.findAll());
    }

    public boolean likeTweet(String tweetId, String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<String> likedTweets = user.getLikedTweets();
            likedTweets.add(tweetId);
            user.setLikedTweets(likedTweets);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean unlikeTweet(String tweetId, String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<String> likedTweets = user.getLikedTweets();
            likedTweets.remove(tweetId);
            user.setLikedTweets(likedTweets);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public boolean updateLikedTweets(String tweetId, String userId, boolean toRemove) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<String> likedTweets = user.getLikedTweets();

            if (toRemove) {
                likedTweets.remove(tweetId);
            }
            else {
                likedTweets.add(tweetId);
            }

            user.setLikedTweets(likedTweets);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public User updateUsername(String userId, String newUsername) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setName(newUsername);
            userRepository.save(user);
            return user;
        }
        return null;
    }
}
