package com.app.socialmedia.repositories;

import com.app.socialmedia.models.Tweet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TweetRepository extends MongoRepository<Tweet, String> {
    List<Tweet> findByUserId(String userId);
}
