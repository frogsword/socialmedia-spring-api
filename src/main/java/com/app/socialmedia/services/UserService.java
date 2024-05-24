package com.app.socialmedia.services;

import com.app.socialmedia.models.User;
import com.app.socialmedia.repositories.UserRepository;
import com.app.socialmedia.utils.ImageUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public User updatePfp(String userId, MultipartFile image) throws IOException {
        User user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            byte[] imageBytes = image.getBytes();

            user.setProfilePicture(imageBytes);

            return userRepository.save(user);
        }
        return null;
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

    public boolean toggleFollow(String userId, String followingId) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<User> followedUserOptional = userRepository.findById(followingId);

        if (userOptional.isPresent() && followedUserOptional.isPresent()) {

            User user = userOptional.get();
            User followedUser = followedUserOptional.get();
            List<String> following = user.getFollowing();
            List<String> followers = followedUser.getFollowers();

            if (following.contains(followingId) && followers.contains(userId)) {
                following.remove(followingId);
                followers.remove(userId);
            }
            else if (!following.contains(followingId) && !followers.contains(userId)) {
                following.add(followingId);
                followers.add(userId);
            }
            else {
                return false;
            }

            user.setFollowing(following);
            followedUser.setFollowers(followers);

            userRepository.save(user);
            userRepository.save(followedUser);

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
